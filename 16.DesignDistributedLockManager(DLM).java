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

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

/*
    ------------------------------------------------------------
    LLD : Distributed Lock Manager (DLM)
    ------------------------------------------------------------
    Functional:
     - Acquire / Release locks on shared resources.
     - One client holds a lock at a time.
     - Locks expire automatically after timeout.
    Non-Functional:
     - Thread-safe using synchronized blocks.
     - In-memory design for low latency.
     - Auto-cleanup for expired locks.
*/

class DistributedLockManager {

    // Map to store current active locks: resource -> (owner, timestamp)
    private final Map<String, LockEntry> lockTable = new HashMap<>();
    private final long timeout;                   // lock expiration time
    private final Object lock = new Object();     // global synchronization object

    // Constructor starts cleanup thread automatically
    public DistributedLockManager(long timeoutSeconds) {
        this.timeout = timeoutSeconds * 1000; // convert to milliseconds
        Thread cleaner = new Thread(this::cleanupExpiredLocks);
        cleaner.setDaemon(true);
        cleaner.start();
    }

    // ----------------------------------------------------------
    // Try acquiring a lock for a given resource and client
    // ----------------------------------------------------------
    public boolean acquireLock(String resource, String clientId) {
        synchronized (lock) {
            if (!lockTable.containsKey(resource)) {
                // Lock is free — assign to client
                lockTable.put(resource, new LockEntry(clientId, System.currentTimeMillis()));
                System.out.println(clientId + " acquired lock on " + resource);
                return true;
            } else {
                // Resource already locked
                LockEntry entry = lockTable.get(resource);
                long currentTime = System.currentTimeMillis();
                if (currentTime - entry.timestamp > timeout) {
                    // Lock expired, reassign it
                    System.out.println(clientId + " took expired lock on " + resource);
                    lockTable.put(resource, new LockEntry(clientId, currentTime));
                    return true;
                } else {
                    // Still valid, client must wait
                    System.out.println(clientId + " waiting... " + resource + " locked by " + entry.owner);
                    return false;
                }
            }
        }
    }

    // ----------------------------------------------------------
    // Release a lock if owned by the requesting client
    // ----------------------------------------------------------
    public void releaseLock(String resource, String clientId) {
        synchronized (lock) {
            if (lockTable.containsKey(resource) && lockTable.get(resource).owner.equals(clientId)) {
                lockTable.remove(resource);
                System.out.println(clientId + " released lock on " + resource);
            }
        }
    }

    // ----------------------------------------------------------
    // Background thread to remove expired locks periodically
    // ----------------------------------------------------------
    private void cleanupExpiredLocks() {
        while (true) {
            synchronized (lock) {
                long now = System.currentTimeMillis();
                List<String> expired = new ArrayList<>();
                for (Map.Entry<String, LockEntry> e : lockTable.entrySet()) {
                    if (now - e.getValue().timestamp > timeout) {
                        expired.add(e.getKey());
                    }
                }
                for (String r : expired) {
                    System.out.println("Lock on " + r + " expired and removed.");
                    lockTable.remove(r);
                }
            }
            try {
                Thread.sleep(1000); // run every 1 second
            } catch (InterruptedException ignored) {}
        }
    }

    // ----------------------------------------------------------
    // Inner helper class representing a lock entry
    // ----------------------------------------------------------
    private static class LockEntry {
        String owner;
        long timestamp;
        LockEntry(String owner, long timestamp) {
            this.owner = owner;
            this.timestamp = timestamp;
        }
    }
}

/*
    ------------------------------------------------------------
    CLIENT SIMULATION SECTION
    ------------------------------------------------------------
    Each client tries to acquire a shared lock on same resource.
    If successful, holds it for 2 seconds then releases it.
*/

class ClientTask implements Runnable {
    private final DistributedLockManager dlm;
    private final String clientId;
    private final String resource;

    ClientTask(DistributedLockManager dlm, String clientId, String resource) {
        this.dlm = dlm;
        this.clientId = clientId;
        this.resource = resource;
    }

    @Override
    public void run() {
        for (int i = 0; i < 3; i++) {
            if (dlm.acquireLock(resource, clientId)) {
                try {
                    Thread.sleep(2000); // simulate work
                } catch (InterruptedException ignored) {}
                dlm.releaseLock(resource, clientId);
            } else {
                try {
                    Thread.sleep(1000); // retry later
                } catch (InterruptedException ignored) {}
            }
        }
    }
}

/*
    ------------------------------------------------------------
    MAIN EXECUTION SECTION
    ------------------------------------------------------------
    Runs multiple clients concurrently trying to acquire
    and release same resource lock.
*/

public class Main {
    public static void main(String[] args) {
        DistributedLockManager dlm = new DistributedLockManager(4); // timeout 4 seconds
        ExecutorService pool = Executors.newFixedThreadPool(3);

        pool.submit(new ClientTask(dlm, "Client-1", "Resource-A"));
        pool.submit(new ClientTask(dlm, "Client-2", "Resource-A"));
        pool.submit(new ClientTask(dlm, "Client-3", "Resource-A"));

        pool.shutdown();
    }
}

/*
    ------------------------------------------------------------
    EXPLANATION (INLINE SUMMARY)
    ------------------------------------------------------------
    1. DistributedLockManager keeps lock states in-memory (HashMap).
    2. acquireLock(): checks if resource is locked; if expired or free, grants it.
    3. releaseLock(): deletes entry if owned by requesting client.
    4. cleanupExpiredLocks(): runs every second, removes stale locks.
    5. ClientTask: simulates independent clients trying to acquire same lock.
    6. main(): runs 3 client threads on same resource — demonstrates concurrency.

    SAMPLE OUTPUT:
    Client-1 acquired lock on Resource-A
    Client-2 waiting... Resource-A locked by Client-1
    Client-3 waiting... Resource-A locked by Client-1
    Client-1 released lock on Resource-A
    Client-2 acquired lock on Resource-A
    Client-3 waiting... Resource-A locked by Client-2
    Lock on Resource-A expired and removed.
    Client-3 took expired lock on Resource-A
    Client-3 released lock on Resource-A
*/
