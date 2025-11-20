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
/* --------------------------------------------------------------------------   */

import java.util.*;                 // Import basic utilities for queue, map
import java.util.concurrent.*;      // Import for locks and concurrent structures

// -------------------------------------------------------------
// ENUM DEFINING JOB STATUSES
// -------------------------------------------------------------
enum Status {
    PENDING,     // Job has been added to queue but not processed yet
    RUNNING,     // Worker is currently executing this job
    COMPLETED,   // Job finished successfully
    FAILED       // Job execution threw an exception
}

// -------------------------------------------------------------
// BASE JOB CLASS (COMMAND PATTERN)
// -------------------------------------------------------------
abstract class Job {
    int id;                     // Unique ID to identify this job
    Status status;              // Current status of job
    Object result;              // Job output or exception message

    public Job(int id) {
        this.id = id;                     // Assign job ID
        this.status = Status.PENDING;     // Newly created job defaults to PENDING
        this.result = null;               // No result before execution
    }

    abstract Object execute() throws Exception;   // Subclasses override to perform task
}

// -------------------------------------------------------------
// A SIMPLE JOB TYPE: PRINT JOB
// -------------------------------------------------------------
class PrintJob extends Job {
    String message;          // The message to be printed

    public PrintJob(int id, String message) {
        super(id);           // Initialize base job fields
        this.message = message;
    }

    @Override
    Object execute() throws Exception {
        Thread.sleep(500);   // Simulate processing delay
        System.out.println("Executing PrintJob(" + id + "): " + message);
        return "Printed: " + message;  // Output stored in result
    }
}

// -------------------------------------------------------------
// JOB QUEUE (PRODUCER SIDE)
// -------------------------------------------------------------
class JobQueue {
    private Deque<Integer> queue = new ArrayDeque<>();      // FIFO queue storing job IDs
    private Map<Integer, Job> jobs = new HashMap<>();       // Map job ID → Job object
    private ReentrantLock lock = new ReentrantLock();       // Ensures thread safety
    private int counter = 1;                                // Generates new job IDs

    public int createPrintJob(String message) {
        lock.lock();                     // Protect shared state
        try {
            int id = counter++;          // Generate ID
            Job job = new PrintJob(id, message);  
            jobs.put(id, job);           // Store job object
            queue.addLast(id);           // Enqueue job to FIFO queue
            return id;                   // Return ID for tracking
        } finally {
            lock.unlock();               // Always release lock
        }
    }

    public Job fetchNextJob() {
        lock.lock();                     // Protect queue operations
        try {
            if (queue.isEmpty()) return null; 
            int id = queue.removeFirst();      // Remove FIFO job ID
            return jobs.get(id);               // Return Job object
        } finally {
            lock.unlock();
        }
    }

    public void retry(int id) {
        lock.lock();                     // Protect job map and queue
        try {
            Job job = jobs.get(id);      // Get job to retry
            job.status = Status.PENDING; // Reset status
            job.result = null;           // Clear previous result
            queue.addLast(id);           // Add back to queue
        } finally {
            lock.unlock();
        }
    }

    public String getStatus(int id) {
        lock.lock();                     // Protect state
        try {
            Job job = jobs.get(id);
            return job.status + " | " + job.result;
        } finally {
            lock.unlock();
        }
    }
}

// -------------------------------------------------------------
// WORKER THREAD (CONSUMER SIDE)
// -------------------------------------------------------------
class WorkerThread extends Thread {
    private JobQueue queueRef;       // Reference to global JobQueue

    public WorkerThread(JobQueue queueRef) {
        this.queueRef = queueRef;  
        setDaemon(true);             // Daemon thread stops with program
    }

    @Override
    public void run() {
        while (true) {               // Keep listening for incoming jobs
            Job job = queueRef.fetchNextJob();  // Thread-safe fetch

            if (job == null) {       // If no job, sleep briefly
                try { Thread.sleep(100); } 
                catch (InterruptedException ignored) {}
                continue;
            }

            try {
                job.status = Status.RUNNING;     // Mark job running
                Object result = job.execute();   // Execute job
                job.result = result;             // Save output
                job.status = Status.COMPLETED;   // Mark success
            } catch (Exception e) {
                job.status = Status.FAILED;      // Mark failure
                job.result = e.getMessage();     // Save error
            }
        }
    }
}

// -------------------------------------------------------------
// MAIN DEMO (RUN THIS IN VS CODE TO SHOW OUTPUT)
// -------------------------------------------------------------
public class JobQueueProcessorDemo {
    public static void main(String[] args) throws Exception {

        JobQueue q = new JobQueue();        // Create central queue
        WorkerThread worker = new WorkerThread(q);
        worker.start();                     // Start worker in background

        int job1 = q.createPrintJob("Hello World");         // Add job 1
        int job2 = q.createPrintJob("Learning Job Queue");  // Add job 2

        Thread.sleep(2000);                 // Allow worker to finish jobs

        System.out.println("Job 1 → " + q.getStatus(job1));  // Print job 1 result
        System.out.println("Job 2 → " + q.getStatus(job2));  // Print job 2 result
    }
}
