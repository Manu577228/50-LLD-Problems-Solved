# Design and implement a thread-safe Rate Limiter that restricts the number of incoming r
# equests per user (or key) within a given time window.

import threading
import time
from collections import defaultdict, deque

class RateLimiter:
    def __init__(self, default_max_requests: int, default_window_seconds):
        self.default_max = int(default_max_requests)
        self.default_window = float(default_window_seconds)
        self.store = defaultdict(deque)
        self.locks = defaultdict(threading.Lock)
        self.limits = {}

    def set_limit(self, key, max_requests: int, window_seconds: float):
        self.limits[key] = (int(max_requests), float(window_seconds))

    def _get_limit(self, key):
        return self.limits.get(key, (self.default_max, self.default_window))
    
    def allow_request(self, key) -> bool:
        now = time.monotonic()
        max_requests, window_seconds = self._get_limit(key)

        lock = self.locks[key]
        with lock:
            q = self.store[key]
            boundary = now - window_seconds
            while q and q[0] <= boundary:
                q.popleft()

            if len(q) < max_requests:
                q.append(now)
                return True
            else:
                return False
            
    def get_usage(self, key):
        now = time.monotonic()
        max_requests, window_seconds = self._get_limit(key)
        lock = self.locks[key]
        with lock:
            q = self.store[key]
            boundary = now - window_seconds
            while q and q[0] <= boundary:
                q.popleft()
            count = len(q)

            if not q:
                return(0, 0.0)
            oldest = q[0]
            ttl = max(0.0, (oldest + window_seconds) - now)
            return (count, ttl)
        
# ==== Demo: concurrent simulation ====
def worker(limiter: RateLimiter, key: str, attempts: int, pause: float, results: list, idx_start: int):
    """
    Each worker makes `attempts` requests for `key` with `pause` seconds between attempts.
    Appends tuple (thread_idx, attempt_idx, allowed) to results.
    """
    for i in range(attempts):
        allowed = limiter.allow_request(key)
        results.append((threading.get_ident(), i, allowed, limiter.get_usage(key)))
        time.sleep(pause)

if __name__ == "__main__":
    # Example: allow 5 requests per 2 seconds per key
    limiter = RateLimiter(default_max_requests=5, default_window_seconds=2.0)

    key = "user:bharadwaj"
    results = []

    # Create 3 threads simulating concurrent clients for the same key.
    threads = []
    for t in range(3):
        # each thread will attempt 4 requests spaced at 0.2s
        thr = threading.Thread(target=worker, args=(limiter, key, 4, 0.2, results, t*4))
        thr.start()
        threads.append(thr)

    for thr in threads:
        thr.join()

    # Print results in order of recording
    for r in results:
        tid, attempt, allowed, usage = r
        count, ttl = usage
        print(f"Thread {tid} attempt {attempt}: {'ALLOWED' if allowed else 'REJECTED'} | in-window={count} ttl={ttl:.3f}s")
        