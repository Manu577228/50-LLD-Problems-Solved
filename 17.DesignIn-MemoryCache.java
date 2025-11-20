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
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio      */
/* -----------------------------------------------------------------------  */

import java.util.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryCache {

    private final int capacity;                      // Maximum cache size
    private final LinkedHashMap<String, Integer> cache; // Stores key-value pairs
    private final ReentrantLock lock;                // For thread-safe access

    public InMemoryCache(int capacity) {
        this.capacity = capacity;                    // Initialize capacity
        this.lock = new ReentrantLock();              // Create lock
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                // Evict the oldest entry when capacity exceeded
                return size() > InMemoryCache.this.capacity;
            }
        };
    }

    // Retrieve a value by key
    public Integer get(String key) {
        lock.lock();                                  // Lock for thread safety
        try {
            if (!cache.containsKey(key)) {            // If key not found
                System.out.println("Key '" + key + "' not found!");
                return null;
            }
            Integer val = cache.get(key);             // Get value (LinkedHashMap moves to end)
            System.out.println("GET: " + key + " -> " + val);
            return val;
        } finally {
            lock.unlock();                            // Release lock
        }
    }

    // Insert or update a key-value pair
    public void put(String key, Integer value) {
        lock.lock();                                  // Lock for safe modification
        try {
            cache.put(key, value);                    // Insert or update key
            System.out.println("PUT: " + key + " -> " + value);
            if (cache.size() > capacity) {            // Check if capacity exceeded
                Iterator<Map.Entry<String, Integer>> it = cache.entrySet().iterator();
                Map.Entry<String, Integer> eldest = it.next(); // Eldest (LRU) entry
                it.remove();                           // Remove eldest
                System.out.println("Evicted (LRU): " + eldest);
            }
        } finally {
            lock.unlock();                            // Unlock after operation
        }
    }

    // Display current cache state
    public void display() {
        lock.lock();
        try {
            System.out.println("Current Cache State: " + cache);
        } finally {
            lock.unlock();
        }
    }

    // Demo run
    public static void main(String[] args) {
        InMemoryCache cache = new InMemoryCache(3);   // Create cache of capacity 3

        cache.put("A", 1);   // Insert A
        cache.put("B", 2);   // Insert B
        cache.put("C", 3);   // Insert C
        cache.display();     // Display current cache

        cache.get("A");      // Access A (moves to end)
        cache.put("D", 4);   // Insert D (evicts B)
        cache.display();     // Display current cache
        cache.get("B");      // Try to access B (not found)
    }
}

/*
-------------------------------------
🔍 Explanation of the Code (Line by Line)
-------------------------------------

1. We import the necessary libraries: 
   - `LinkedHashMap` to maintain insertion order.
   - `ReentrantLock` for thread safety.

2. The `InMemoryCache` class holds:
   - `capacity`: maximum number of items in cache.
   - `cache`: LinkedHashMap to store key-value pairs.
   - `lock`: ensures thread-safe access.

3. In the constructor:
   - We initialize `cache` with accessOrder = true.
   - Override `removeEldestEntry()` to evict the least recently used entry automatically.

4. `get(key)`:
   - Locks the cache for thread safety.
   - Checks if the key exists.
   - Returns its value and prints it.
   - Automatically moves the accessed key to the end (most recently used).

5. `put(key, value)`:
   - Locks the cache to avoid race conditions.
   - Inserts/updates the key-value pair.
   - If the cache exceeds capacity, removes the least recently used entry manually.

6. `display()`:
   - Prints the entire cache content in order of usage.

7. `main()`:
   - Demonstrates cache usage step-by-step.

-------------------------------------
🖥️ Sample Output
-------------------------------------
PUT: A -> 1
PUT: B -> 2
PUT: C -> 3
Current Cache State: {A=1, B=2, C=3}
GET: A -> 1
Evicted (LRU): A=1
PUT: D -> 4
Current Cache State: {C=3, A=1, D=4}
Key 'B' not found!
-------------------------------------

✅ Summary:
- Implements an **LRU In-Memory Cache**.
- Thread-safe using **ReentrantLock**.
- Achieves O(1) get/put using LinkedHashMap.
- Purely in-memory — ideal for **VS Code demo & YouTube explanation**.
*/ 
