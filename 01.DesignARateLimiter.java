/* ----------------------------------------------------------------------------  */
/*   ( The Authentic JS/JAVA CodeBuff )
 ___ _                      _              _ 
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                        |__/ 
 */
/* --------------------------------------------------------------------------   */
/*    Youtube: https://youtube.com/@code-with-Bharadwaj                        */
/*    Github : https://github.com/Manu577228                                  */
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio        */
/* -----------------------------------------------------------------------  */

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class RateLimiter {

    private final int defaultMaxRequests;
    private final double defaultWindowSeconds;
    private final Map<String, Deque<Long>> store;
    private final Map<String, ReentrantLock> locks;
    private final Map<String, Limit> limits;

    private static class Limit {
        int maxRequests;
        double windowSeconds;

        Limit(int maxRequests, double windowSeconds) {
            this.maxRequests = maxRequests;
            this.windowSeconds = windowSeconds;
        }
    }

    public RateLimiter(int defaultMaxRequests, double defaultWindowSeconds) {
        this.defaultMaxRequests = defaultMaxRequests;
        this.defaultWindowSeconds = defaultWindowSeconds;
        this.store = new ConcurrentHashMap<>();
        this.locks = new ConcurrentHashMap<>();
        this.limits = new ConcurrentHashMap<>();
    }

    public void setLimit(String key, int maxRequests, double windowSeconds) {
        limits.put(key, new Limit(maxRequests, windowSeconds));
    }

    private Limit getLimit(String key) {
        return limits.getOrDefault(key, new Limit(defaultMaxRequests, defaultWindowSeconds));
    }

    public boolean allowRequest(String key) {
        long now = System.nanoTime();
        Limit limit = getLimit(key);
        double windowNanos = limit.windowSeconds * 1e9;

        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            Deque<Long> q = store.computeIfAbsent(key, k -> new ArrayDeque<>());
            long boundary = now - (long) windowNanos;

            while (!q.isEmpty() && q.peekFirst() <= boundary) {
                q.pollFirst();
            }

            if (q.size() < limit.maxRequests) {
                q.addLast(now);
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public synchronized Usage getUsage(String key) {
        long now = System.nanoTime();
        Limit limit = getLimit(key);
        double windowNanos = limit.windowSeconds * 1e9;
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            Deque<Long> q = store.computeIfAbsent(key, k -> new ArrayDeque<>());
            long boundary = now - (long) windowNanos;
            while (!q.isEmpty() && q.peekFirst() <= boundary) {
                q.pollFirst();
            }
            int count = q.size();
            double ttl = 0.0;
            if (!q.isEmpty()) {
                long oldest = q.peekFirst();
                ttl = ((oldest + windowNanos) - now) / 1e9;
                if (ttl < 0) ttl = 0;
            }
            return new Usage(count, ttl);
        } finally {
            lock.unlock();
        }
    }

    static class Usage {
        int count;
        double ttl;

        Usage(int count, double ttl) {
            this.count = count;
            this.ttl = ttl;
        }
    }

    // Worker to simulate requests (same as Python demo)
    static class Worker implements Runnable {
        private final RateLimiter limiter;
        private final String key;
        private final int attempts;
        private final double pause;
        private final List<String> results;

        Worker(RateLimiter limiter, String key, int attempts, double pause, List<String> results) {
            this.limiter = limiter;
            this.key = key;
            this.attempts = attempts;
            this.pause = pause;
            this.results = results;
        }

        @Override
        public void run() {
            for (int i = 0; i < attempts; i++) {
                boolean allowed = limiter.allowRequest(key);
                Usage usage = limiter.getUsage(key);
                String res = Thread.currentThread().getId() + 
                             " attempt " + i + ": " + 
                             (allowed ? "ALLOWED" : "REJECTED") + 
                             " | in-window=" + usage.count + 
                             " ttl=" + String.format("%.3f", usage.ttl) + "s";
                synchronized (results) {
                    results.add(res);
                }
                try {
                    Thread.sleep((long) (pause * 1000));
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public static void main(String[] args) throws Exception {
        RateLimiter limiter = new RateLimiter(5, 2.0); // 5 requests per 2 seconds
        String key = "user:alice";

        List<String> results = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int t = 0; t < 3; t++) {
            executor.submit(new Worker(limiter, key, 4, 0.2, results));
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        for (String s : results) {
            System.out.println(s);
        }
    }
}
