import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.io.*;

/**
 * Demonstration of Load Balancer supporting:
 * - Round Robin
 * - Weighted Round Robin
 * - Least Connections
 * along with simulated health checks and concurrency-safe operations.
 */
public class LoadBalancerDemo {

    /* ---------------------------- Server Class ---------------------------- */
    static class Server {
        String id, addr;
        int weight;
        AtomicInteger activeConn = new AtomicInteger(0);
        AtomicInteger totalReq = new AtomicInteger(0);
        volatile boolean healthy = true;

        Server(String id, String addr, int weight) {
            this.id = id;
            this.addr = addr;
            this.weight = Math.max(1, weight);
        }

        void acquire() {
            activeConn.incrementAndGet();
            totalReq.incrementAndGet();
        }

        void release() {
            if (activeConn.get() > 0)
                activeConn.decrementAndGet();
        }

        void markHealthy(boolean val) {
            healthy = val;
        }

        Map<String, Object> snapshot() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", id);
            m.put("addr", addr);
            m.put("weight", weight);
            m.put("activeConn", activeConn.get());
            m.put("totalReq", totalReq.get());
            m.put("healthy", healthy);
            return m;
        }
    }

    /* ------------------------ Load Balancer Class ------------------------ */
    static class LoadBalancer {
        ConcurrentHashMap<String, Server> servers = new ConcurrentHashMap<>();
        String algo;
        AtomicInteger rrIndex = new AtomicInteger(0);
        List<String> weightedQueue = Collections.synchronizedList(new ArrayList<>());
        Random rand = new Random();

        LoadBalancer(String algo) {
            this.algo = algo;
        }

        void addServer(Server s) {
            servers.put(s.id, s);
            rebuildWeightedQueue();
        }

        void removeServer(String id) {
            servers.remove(id);
            rebuildWeightedQueue();
        }

        void setAlgorithm(String algo) {
            this.algo = algo;
            rrIndex.set(0);
        }

        List<Server> healthyServers() {
            List<Server> list = new ArrayList<>();
            for (Server s : servers.values())
                if (s.healthy) list.add(s);
            return list;
        }

        void rebuildWeightedQueue() {
            weightedQueue.clear();
            for (Server s : servers.values()) {
                if (s.healthy) {
                    int count = Math.min(s.weight, 10); // capped for demo
                    for (int i = 0; i < count; i++)
                        weightedQueue.add(s.id);
                }
            }
        }

        Server selectServer() {
            List<Server> healthy = healthyServers();
            if (healthy.isEmpty()) return null;

            switch (algo) {
                case "round_robin":
                    int idx = rrIndex.getAndIncrement() % healthy.size();
                    return healthy.get(idx);

                case "weighted_round_robin":
                    if (weightedQueue.isEmpty()) rebuildWeightedQueue();
                    if (weightedQueue.isEmpty()) return null;
                    String sid = weightedQueue.remove(0);
                    weightedQueue.add(sid);
                    return servers.get(sid);

                case "least_connections":
                    return Collections.min(healthy, Comparator.comparingInt(s -> s.activeConn.get()));

                default:
                    return healthy.get(rand.nextInt(healthy.size()));
            }
        }

        void routeRequest(int reqId, ExecutorService pool) {
            Server s = selectServer();
            if (s == null) {
                System.out.println("[LB] No healthy servers for req " + reqId);
                return;
            }
            s.acquire();
            System.out.println("[LB] Routed req " + reqId + " -> " + s.id + " (active=" + s.activeConn.get() + ")");
            pool.submit(() -> {
                try {
                    Thread.sleep(100 + rand.nextInt(400));
                } catch (InterruptedException ignored) {}
                s.release();
                System.out.println("[Server " + s.id + "] Completed req " + reqId + " (active=" + s.activeConn.get() + ")");
            });
        }

        void healthCheckCycle() {
            new Thread(() -> {
                while (true) {
                    for (Server s : servers.values()) {
                        double p = Math.random();
                        if (p < 0.02) {
                            s.markHealthy(false);
                            System.out.println("[Health] " + s.id + " marked UNHEALTHY");
                        } else if (!s.healthy && p < 0.3) {
                            s.markHealthy(true);
                            System.out.println("[Health] " + s.id + " marked HEALTHY");
                        }
                    }
                    rebuildWeightedQueue();
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            }).start();
        }

        void snapshot() {
            for (Server s : servers.values())
                System.out.println(s.snapshot());
        }
    }

    /* ----------------------------- Main Demo ----------------------------- */
    public static void main(String[] args) {
        LoadBalancer lb = new LoadBalancer("round_robin");

        lb.addServer(new Server("s1", "10.0.0.1", 1));
        lb.addServer(new Server("s2", "10.0.0.2", 3));
        lb.addServer(new Server("s3", "10.0.0.3", 2));

        lb.healthCheckCycle();

        ExecutorService pool = Executors.newCachedThreadPool();

        for (int i = 0; i < 25; i++) {
            if (i == 10) {
                lb.setAlgorithm("weighted_round_robin");
                System.out.println("\n--- Switched to Weighted Round Robin ---\n");
            }
            if (i == 18) {
                lb.setAlgorithm("least_connections");
                System.out.println("\n--- Switched to Least Connections ---\n");
            }
            lb.routeRequest(i, pool);
            try { Thread.sleep(80); } catch (InterruptedException ignored) {}
        }

        pool.shutdown();
        try { pool.awaitTermination(3, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}

        System.out.println("\nFinal Snapshot:");
        lb.snapshot();
    }
}
