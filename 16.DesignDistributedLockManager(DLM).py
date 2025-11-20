# Design a Distributed Lock Manager (DLM) that allows multiple clients to 
# safely acquire and release locks on shared resources. Ensure 
# that only one client can hold a lock at a time, and locks automatically expire 
# after a timeout if not released. 
# Simulate concurrency using threads in Python.

import threading, time

class DistributedLockManager:
    def __init__(self, timeout = 5):
        self.lock_table = {}
        self.global_lock = threading.Lock()
        self.timeout = timeout 
        threading.Thread(target = self.cleanup_expired_locks, daemon = True).start()

    def acquire_lock(self, resource, client_id):
        with self.global_lock:
            if resource not in self.lock_table:
                self.lock_table[resource] = (client_id, time.time())
                print(f"{client_id} acquired lock on {resource}")
                return True
            else:
                owner, t = self.lock_table[resource]
                if time.time() - t > self.timeout:
                    print(f"{client_id} took expired lock on {resource}")
                    self.lock_table[resource] = (client_id, time.time())
                    return True
                
                else:
                    print(f"{client_id} waiting... {resource} locked by {owner}")
                    return False
                
    def release_lock(self, resource, client_id):
        with self.global_lock:
            if resource in self.lock_table and self.lock_table[resource][0] == client_id:
                del self.lock_table[resource]
                print(f"{client_id} released lock on {resource}")

    def cleanup_expired_locks(self):
        while True:
            with self.global_lock:
                now = time.time()
                expired = [r for r, (o, t) in self.lock_table.items() if now - t > self.timeout]
                for r in expired:
                    print(f"Lock on {r} expired and removed.")
                    del self.lock_table[r]
            time.sleep(1)

def client_task(dlm, cid, resource):
    for _ in range(3):
        if dlm.acquire_lock(resource, cid):
            time.sleep(2)
            dlm.release_lock(resource, cid)
        else:
            time.sleep(1)

if __name__ == "__main__":
    dlm = DistributedLockManager(timeout=4)
    threads = []
    for i in range(3):
        t = threading.Thread(target=client_task, args=(dlm, f"Client-{i+1}", "resA"))
        threads.append(t)
        t.start()

    for t in threads:
        t.join()
                
            
        

        

