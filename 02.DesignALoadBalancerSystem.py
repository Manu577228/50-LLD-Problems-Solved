# Design a Load Balancer System that distributes client requests among multiple backend servers 
# based on different algorithms such as Round-Robin, Weighted Round-Robin, 
# and Least Connections, while supporting dynamic server management and simulated health checks.

import threading
import time
import random
from collections import deque
from typing import Dict, List

class Server:
    def __init__(self, id: str, addr: str, weight: int = 1):
        self.id = id
        self.addr = addr
        self.weight = weight
        self.active_conn = 0
        self.total_requests = 0
        self.healthy = True
        self.lock = threading.Lock()

    def acquire(self):
        with self.lock:
            self.active_conn += 1
            self.total_requests += 1

    def release(self):
        with self.lock:
            if self.active_conn > 0:
                self.active_conn -= 1

    def mark_unhealthy(self):
        with self.lock:
            self.healthy = False

    def mark_healthy(self):
        with self.lock:
            self.healthy = True

    def snapshot(self):
        with self.lock:
            return {
                "id": self.id,
                "addr": self.addr,
                "weight": self.weight,
                "active_conn": self.active_conn,
                "total_requests": self.total_requests,
                "healthy": self.healthy
            }
        
class LoadBalancer:
    def __init__(self, algorithm = 'round_robin'):
        self.servers: Dict[str, Server] = {}
        self.lock = threading.Lock()
        self.algorithm = algorithm
        self.rr_index = 0
        self.weighted_queue = deque()

    def add_server(self, server: Server):
        with self.lock:
            self.servers[server.id] = server
            self._rebuild_weighted_queue()

    def remove_server(self, server_id: str):
        with self.lock:
            if server_id in self.servers:
                del self.servers[server_id]
                self._rebuild_weighted_queue()

    def _rebuild_weighted_queue(self):
        self.weighted_queue.clear()
        for s in self.servers.values():
            if s.healthy:
                cap = min(s.weight, 10)
                for _ in range(cap):
                    self.weighted_queue.append(s.id)

    def set_algorithm(self, algo: str):
        with self.lock:
            self.algorithm = algo
            self.rr_index = 0

    def _get_healthy_servers_list(self) -> List[Server]:
        with self.lock:
            return [s for s in self.servers.values() if s.healthy]
        
    def select_server(self) -> Server:
        healthy = self._get_healthy_servers_list()
        if not healthy:
            return None
        
        if self.algorithm == 'round_robin':
            with self.lock:
                idx = self.rr_index % len(healthy)
                self.rr_index += 1
            return healthy[idx]
        
        elif self.algorithm == 'weighted_round_robin':
            with self.lock:
                if not self.weighted_queue:
                    self._rebuild_weighted_queue()
                if not self.weighted_queue:
                    return None
                sid = self.weighted_queue[0]
                self.weighted_queue.rotate(-1)
                return self.servers.get(sid)
            
        elif self.algorithm == 'least_connections':
            chosen = min(healthy, key = lambda s : s.active_conn)
            return chosen
        
        else:
            return random.choice(healthy)
        
    def route_request(self, request_id: int, duration: float = None):
        server = self.select_server()
        if not server:
            print(f"[LB] No healthy servers for request {request_id}")
            return False
        
        server.acquire()
        print(f"[LB] Routed req {request_id} -> server {server.id} (active = {server.active_conn})")
        t = threading.Thread(target = self.handle_request, args = (server, request_id, duration))
        t.daemon = True
        t.start()
        return True
    
    def handle_request(self, server: Server, request_id: int, duration: float):
        if duration is None:
            duration = random.uniform(0.1, 0.6)
        time.sleep(duration)
        server.release()
        print(f"[Server {server.id}] Completed req {request_id} (active = {server.active_conn})")

    def health_check_cycle(self):
        while True:
            with self.lock:
                servers = list(self.servers.values())
            for s in servers:
                if random.random() < 0.2:
                    s.mark_unhealthy()
                    print(f"[Health] Server {s.id} marked UNHEALTHY")
                elif not s.healthy and random.random() < 0.3:
                    s.mark_healthy()
                    print(f"[Health] Server {s.id} marked HEALTHY")
            with self.lock:
                self._rebuild_weighted_queue()
            time.sleep(1.0)

    def snapshot(self):
        with self.lock:
            return {sid: s.snapshot() for sid, s in self.servers.items()}
        
def demo():
    lb = LoadBalancer(algorithm='round_robin')
    # Add three servers with different weights
    lb.add_server(Server('s1', '10.0.0.1', weight=1))
    lb.add_server(Server('s2', '10.0.0.2', weight=3))
    lb.add_server(Server('s3', '10.0.0.3', weight=2))

    # start health-check thread
    hc = threading.Thread(target=lb.health_check_cycle, daemon=True)
    hc.start()

    # simulate client requests
    req_id = 0
    def clients_sim():
        nonlocal req_id
        while req_id < 25:
            # set different algorithms mid-demo to showcase behavior
            if req_id == 10:
                lb.set_algorithm('weighted_round_robin')
                print("\n--- Switched to Weighted Round Robin ---\n")
            if req_id == 18:
                lb.set_algorithm('least_connections')
                print("\n--- Switched to Least Connections ---\n")

            lb.route_request(req_id)
            req_id += 1
            time.sleep(0.08)  # new request every 80ms

    client_thread = threading.Thread(target=clients_sim)
    client_thread.start()
    client_thread.join()

    # wait briefly for outstanding requests to finish
    time.sleep(2)
    print("\nFinal snapshot:")
    snap = lb.snapshot()
    for sid, info in snap.items():
        print(sid, info)

if __name__ == "__main__":
    demo()

            

        
        

