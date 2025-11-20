# Design a service that generates globally unique, time-sortable IDs in a thread-safe
# manner without relying on any external database. The service should support
# high-throughput concurrent requests and provide a simple demo of ID generation

import threading
import time


class UniqueIDGenerator:
    def __init__(self, machine_id=1):
        self.machine_id = machine_id & 0x3FF
        self.sequence = 0
        self.last_timestamp = -1
        self.lock = threading.Lock()

    def _current_millis(self):
        return int(time.time() * 1000)

    def _wait_next_millis(self, last_ts):
        ts = self._current_millis()
        while ts <= last_ts:
            ts = self._current_millis()
        return ts

    def get_id(self):
        with self.lock:
            ts = self._current_millis()
            if ts == self.last_timestamp:
                self.sequence = (self.sequence + 1) & 0xFFF
                if self.sequence == 0:
                    ts = self._wait_next_millis(self.last_timestamp)
            else:
                self.sequence = 0
            self.last_timestamp = ts
            unique_id = ((ts << 22) | (self.machine_id << 12) | self.sequence)
            return unique_id


generator = UniqueIDGenerator(machine_id=42)

print("Generating 5 unique IDs:")
for _ in range(5):
    uid = generator.get_id()
    print(uid)
