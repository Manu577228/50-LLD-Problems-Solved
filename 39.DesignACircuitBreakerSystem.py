# Design a Circuit Breaker mechanism that wraps external service calls, tracks 
# consecutive failures, and automatically transitions between CLOSED, OPEN, and 
# HALF-OPEN states. The breaker must block calls when the service is unhealthy and 
# allow recovery after a timeout, all implemented in a fully in-memory Python demo.

import time
import threading
from enum import Enum

class State(Enum):
    CLOSED = 1
    OPEN = 2
    HALF_OPEN = 3

class CircuitBreaker:
    def __init__(self, failure_threshold = 3, recovery_timeout = 5):
        self.failure_threshold = failure_threshold
        self.recovery_timeout = recovery_timeout

        self.failure_count = 0
        self.state = State.CLOSED
        self.last_failure_time = None
        self.lock = threading.Lock()

    def _move_to_open(self):
        self.state = State.OPEN
        self.last_failure_time = time.time()
        print(" STATE CHANGE -> OPEN")

    def _move_to_half(self):
        self.state = State.HALF_OPEN
        print(" STATE CAHNGE -> HALF-OPEN")

    def _move_to_closed(self):
        self.state = State.CLOSED
        self.failure_count = 0
        self.last_failure_time = None
        print(" STATE CAHNGE -> CLOSED")

    def _can_pass(self):
        if self.state == State.CLOSED:
            return True

        if self.state == State.OPEN:
            if time.time() - self.last_failure_time >= self.recovery_timeout:
                self._move_to_half()
                return True
            return False

        if self.state == State.HALF_OPEN:
            return True

    def call(self, func):
        with self.lock:
            if not self._can_pass():
                print("❌ Request BLOCKED (OPEN STATE)")
                return "BLOCKED"

        try:
            result = func() 
            
            with self.lock:
                if self.state == State.HALF_OPEN:
                    self._move_to_closed()
                self.failure_count = 0
            return result

        except Exception as e:
            with self.lock:
                self.failure_count += 1
                print(f"⚠️ Failure #{self.failure_count}")

                if self.failure_count >= self.failure_threshold:
                    self._move_to_open()

            return f"ERROR: {str(e)}"
        
class FlakyService:
    def __init__(self):
        self.counter = 0

    def __call__(self):
        self.counter += 1
        if self.counter <= 3:
            raise Exception("Service Down")
        return "SUCCESS"
    
if __name__ == "__main__":
    # Create circuit breaker with threshold=3 failures, recovery timeout=3 seconds
    cb = CircuitBreaker(failure_threshold = 3, recovery_timeout = 3)

    # Create simulated flaky service instance (callable)
    service = FlakyService()

    print("\n--- Demo Start ---\n")

    # Make several calls spaced by 1 second to show state transitions
    for i in range(8):
        print(f"Call {i+1} →", cb.call(service))
        time.sleep(1)

    print("\n--- Demo End ---")
        
        

