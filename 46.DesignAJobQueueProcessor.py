# Design and implement an in-memory Job Queue Processor that can accept jobs, 
# execute them asynchronously using a background worker thread, and 
# track their statuses (PENDING, RUNNING, COMPLETED, FAILED).
# The system should support retrying failed jobs and allow users to 
# query job results—all without using any external database.

import threading
import time
from collections import deque
from enum import Enum

class Status(Enum):
    PENDING = "PENDING"
    RUNNING = "RUNNING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"

class Job:
    def __init__(self, job_id):
        self.id = job_id
        self.status = Status.PENDING
        self.result = None

    def execute(self):
        raise NotImplementedError("Subclasses implement this.")
    
class PrintJob(Job):
    def __init__(self, job_id, message):
        super().__init__(job_id)
        self.message = message

    def execute(self):
        time.sleep(0.5)
        print(f"Executing PrintJob({self.id}): {self.message}")

        return f"Printed: {self.message}"
    
class JobQueue:
    def __init__(self):
        self.queue = deque()
        self.jobs = {}
        self.lock = threading.Lock()
        self.job_counter = 1

    def add_job(self, job):
        with self.lock:
            self.jobs[job.id] = job
            self.queue.append(job.id)

    def create_print_job(self, message):
        job = PrintJob(self.job_counter, message)

        self.job_counter += 1
        self.add_job(job)
        return job.id
    
    def get_status(self, job_id):
        with self.lock:
            job = self.jobs.get(job_id)
            return job.status, job.result
        
    def retry(self, job_id):
        with self.lock:
            job = self.jobs[job_id]
            job.status = Status.PENDING
            job.result = None
            self.queue.append(job_id)

class WorkerThread(threading.Thread):
    def __init__(self, queue_ref):
        super().__init__(daemon=True)
        self.queue_ref = queue_ref

    def run(self):
        while True:
            job = None
            with self.queue_ref.lock:
                if self.queue_ref.queue:
                    job_id = self.queue_ref.queue.popleft()
                    job = self.queue_ref.jobs[job_id]
                    job.status = Status.RUNNING

            if job:
                try:
                    result = job.execute()
                    job.result = result
                    job.status = Status.COMPLETED
                except Exception as e:
                    job.result = str(e)
                    job.status = Status.FAILED
            else:
                time.sleep(0.1)

# ------------------ DEMO ------------------

if __name__ == "__main__":
    q = JobQueue()
    worker = WorkerThread(q)
    worker.start()

    j1 = q.create_print_job("Hello World")
    j2 = q.create_print_job("Learning Job Queue")

    time.sleep(2)

    print("Job 1:", q.get_status(j1))
    print("Job 2:", q.get_status(j2))
        
