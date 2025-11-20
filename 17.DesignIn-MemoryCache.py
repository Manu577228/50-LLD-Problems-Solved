# Design a simple In-Memory Cache System that stores key-value pairs, retrieves values 
# in O(1) time, and automatically evicts the least recently used (LRU) item when the cache 
# reaches its maximum capacity — all in-memory, without using any external database.

from collections import OrderedDict
import threading

class InMemoryCache:
    def __init__(self, capacity):
        self.capacity = capacity
        self.cache = OrderedDict()
        self.lock = threading.Lock()

    def get(self, key):
        with self.lock:
            if key not in self.cache:
                print(f"key '{key}' not found!")
                return None
            self.cache.move_to_end(key)
            print(f"GET: {key} -> {self.cache[key]}")
            return self.cache[key]
        
    def put(self, key, value):
        with self.lock:
            if key in self.cache:
                self.cache.move_to_end(key)
            self.cache[key] = value
            if len(self.cache) > self.capacity:
                removed = self.cache.popitem(last = False)
                print(f"Evicted (LRU): {removed}")
            print(f"PUT: {key} -> {value}")

    def display(self):
        with self.lock:
            print("Current cache State:", dict(self.cache))

if __name__ == "__main__":
    cache = InMemoryCache(capacity = 3)  # Create cache with capacity 3

    # Insert three key-value pairs
    cache.put('A', 1)   # Cache: {'A':1}
    cache.put('B', 2)   # Cache: {'A':1, 'B':2}
    cache.put('C', 3)   # Cache: {'A':1, 'B':2, 'C':3}
    cache.display()     # Display current cache

    # Accessing 'A' marks it as recently used
    cache.get('A')      # Moves 'A' to end → {'B':2, 'C':3, 'A':1}

    # Adding new key causes eviction of least recently used ('B')
    cache.put('D', 4)   # Cache now: {'C':3, 'A':1, 'D':4}
    cache.display()     # Display new state

    # Accessing non-existent key
    cache.get('B')      # Key 'B' not found

        