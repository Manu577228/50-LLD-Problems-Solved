# Design a Logger System that can handle concurrent log requests efficiently.
# It should support multiple log levels (DEBUG, INFO, WARNING, ERROR, CRITICAL),
# output logs to both console and rotating files, and ensure asynchronous non-blocking
# logging for high-performance applications. The system must be thread-safe, allow log
# rotation by size, and support configurable formats and handlers.

import threading
import time
import os
import io
import traceback
from queue import Queue, Full, Empty
from datetime import datetime
from enum import IntEnum


class Level(IntEnum):
    DEBUG = 10
    INFO = 20
    WARNING = 30
    ERROR = 40
    CRITICAL = 50


class LogRecord:
    def __init__(self, logger_name, level, msg, metadata=None):
        self.timestamp = datetime.utcnow()
        self.logger_name = logger_name
        self.level = level
        self.msg = msg
        self.metadata = metadata or {}
        self.seq = id(self)


class Formatter:
    def __init__(self, fmt="{asctime} [{level}] {name}: {msg}"):
        self.fmt = fmt

    def format(self, record: LogRecord):
        asctime = record.timestamp.strftime("%Y-%m-%dT%H:%M:%S.%fZ")
        level = record.level.name
        name = record.logger_name
        msg = record.msg
        meta = " ".join(f"{k}={v}" for k, v in record.metadata.items())
        return self.fmt.format(asctime=asctime, level=level, name=name, msg=msg, meta=meta)


class Handler:
    def __init__(self, level=Level.DEBUG, formatter=None):
        self.level = level
        self.formatter = formatter or Formatter()

    def handle(self, record):
        try:
            self.emit(record)
        except Exception:
            print("Handler emit error", traceback.format_exc())

    def emit(self, record):
        raise NotImplementedError


class ConsoleHandler(Handler):
    def emit(self, record):
        line = self.formatter.format(record)
        print(line)


class RotatingFileHandler(Handler):
    def __init__(self, filename, max_bytes=10 * 1024, backup_count=3, level=Level.DEBUG, formatter=None):
        super().__init__(level=level, formatter=formatter)
        self.filename = filename
        self.max_bytes = max_bytes
        self.backup_count = backup_count
        self._open_file()

    def _open_file(self):
        os.makedirs(os.path.dirname(self.filename) or ".", exist_ok=True)
        self.stream = open(self.filename, "a", buffering=1, encoding="utf-8")

    def _should_rotate(self):
        try:
            self.stream.flush()
            size = os.path.getsize(self.filename)
            return size >= self.max_bytes
        except Exception:
            return False

    def rotate(self):
        try:
            self.stream.close()
            for i in range(self.backup_count - 1, 0, -1):
                s = f"{self.filename}.{i}"
                d = f"{self.filename}.{i + 1}"
                if os.path.exists(s):
                    os.replace(s, d)
            if os.path.exists(self.filename):
                os.replace(self.filename, f"{self.filename}.1")
            self._open_file()
        except Exception:
            print("Rotation failed:", traceback.format_exc())

    def emit(self, record):
        line = self.formatter.format(record) + "\n"
        self.stream.write(line)
        if self._should_rotate():
            self.rotate()


class AsyncDispatcher:
    def __init__(self, handlers, queue_size=1000, batch_size=50, flush_interval=0.5, drop_policy="drop_oldest"):
        self.handlers = handlers
        self.queue = Queue(maxsize=queue_size)
        self.batch_size = batch_size
        self.flush_interval = flush_interval
        self.drop_policy = drop_policy
        self._stop_event = threading.Event()
        self._worker = threading.Thread(
            target=self._run, name="LoggerWorker", daemon=True)
        self._dropped = 0

    def start(self):
        self._worker.start()

    def stop(self, timeout=2.0):
        self._stop_event.set()
        self._worker.join(timeout=timeout)

    def enqueue(self, record: LogRecord):
        try:
            if self.drop_policy == "drop_oldest":
                while True:
                    try:
                        self.queue.put(record, block=False)
                        return True
                    except Full:
                        try:
                            self.queue.get(block=False)
                            self._dropped += 1
                        except Empty:
                            pass
            elif self.drop_policy == "drop_new":
                try:
                    self.queue.put(record, block=False)
                    return True
                except Full:
                    self._dropped += 1
                    return False
            else:
                self.queue.put(record, block=True, timeout=0.1)
                return True
        except Exception:
            self._dropped += 1
            return False
    def _run(self):
        batch = []
        last_flush = time.time()
        while not (self._stop_event.is_set() and self.queue.empty()):
            try:
                rec = self.queue.get(timeout=self.flush_interval)
                batch.append(rec)
                if len(batch) >= self.batch_size:
                    self._flush(batch)
                    batch = []
                    last_flush = time.time()
            except Empty:
                if batch and (time.time() - last_flush) >= self.flush_interval:
                    self._flush(batch)
                    batch = []
                    last_flush = time.time()
        if batch:
            self._flush(batch)
    def _flush(self, batch):
        for rec in batch:
            for h in self.handlers:
                try:
                    h.handle(rec)
                except Exception:
                    print("Handler failure in flush:", traceback.format_exc())

class Logger:
    def __init__(self, name, level=Level.DEBUG, dispatcher=None):
        self.name = name
        self.level = level
        self.dispatcher = dispatcher
    def _log(self, level, msg, **meta):
        if level < self.level:
            return
        rec = LogRecord(self.name, level, msg, meta)
        self.dispatcher.enqueue(rec)
    def debug(self, msg, **meta): self._log(Level.DEBUG, msg, **meta)
    def info(self, msg, **meta): self._log(Level.INFO, msg, **meta)
    def warning(self, msg, **meta): self._log(Level.WARNING, msg, **meta)
    def error(self, msg, **meta): self._log(Level.ERROR, msg, **meta)
    def critical(self, msg, **meta): self._log(Level.CRITICAL, msg, **meta)

# --- Example usage / demo ---
if __name__ == "__main__":
    fmt = Formatter("{asctime} [{level}] {name}: {msg} {meta}")
    console = ConsoleHandler(level=Level.DEBUG, formatter=fmt)
    fileh = RotatingFileHandler("logs/app.log", max_bytes=1024*2, backup_count=3, level=Level.DEBUG, formatter=fmt)
    dispatcher = AsyncDispatcher([console, fileh], queue_size=200, batch_size=20, flush_interval=0.2, drop_policy="drop_oldest")
    dispatcher.start()
    app_logger = Logger("MyApp", level=Level.DEBUG, dispatcher=dispatcher)

    # Simulate concurrent logging
    def worker(thread_id):
        for i in range(50):
            app_logger.info(f"message {i} from thread {thread_id}", thread=thread_id, i=i)
            time.sleep(0.01)

    threads = [threading.Thread(target=worker, args=(tid,)) for tid in range(3)]
    for t in threads: t.start()
    for t in threads: t.join()

    # Allow dispatcher to flush
    time.sleep(0.5)
    dispatcher.stop()
    print("Demo complete. Dropped messages:", dispatcher._dropped)



        


            
        
        
        

        
