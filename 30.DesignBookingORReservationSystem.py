# Design an in-memory Booking/Reservation System that allows users to create,
# cancel, and view bookings for any resource while preventing overlapping time slots.
# Implement the system using a thread-safe Singleton manager and demonstrate
# it with sample bookings in Python.

import threading


class Booking:
    def __init__(self, user, resource, start, end):
        self.user = user
        self.resource = resource
        self.start = start
        self.end = end

    def __repr__(self):
        return f"[{self.user} -> {self.resource} ({self.start} -- {self.end})]"


class BookingManager:
    _instance = None
    _lock = threading.Lock()

    def __new__(cls):
        with cls._lock:
            if cls._instance is None:
                cls._instance = super().__new__(cls)
                cls._instance.bookings = {}
                cls._instance.manager_lock = threading.Lock()
            return cls._instance

    def _conflict(self, resource, start, end):
        if resource not in self.bookings:
            return False
        for b in self.bookings[resource]:
            if not (end <= b.start or start >= b.end):
                return True
        return False

    def create_booking(self, user, resource, start, end):
        with self.manager_lock:
            if self._conflict(resource, start, end):
                return False, "Conflict: Resource already booked in this time slot."

            b = Booking(user, resource, start, end)
            self.bookings.setdefault(resource, []).append(b)
            return True, "Booking Confirmed."

    def cancel_booking(self, user, resource):
        with self.manager_lock:
            if resource not in self.bookings:
                return False, "No Booking Found."

            for b in list(self.bookings[resource]):
                if b.user == user:
                    self.bookings[resource].remove(b)
                    return True, "Booking Cancelled."
            return False, "Booking not found."

    def list_bookings(self):
        return self.bookings


if __name__ == "__main__":
    # get the singleton BookingManager instance
    bm = BookingManager()

    # show that we are creating bookings
    print("Creating bookings:")

    # attempt to create a booking for Alice in Room1 from 10 to 12
    print(bm.create_booking("Alice", "Room1", 10, 12))   # expected: success

    # attempt to create a booking for Bob in Room1 from 12 to 14
    # expected: success (no overlap)
    print(bm.create_booking("Bob", "Room1", 12, 14))

    # attempt to create a booking for Charlie in Room1 from 11 to 13
    # this overlaps Alice's 10-12 slot and should therefore fail
    print(bm.create_booking("Charlie", "Room1", 11, 13))  # expected: conflict

    # print a blank line to separate output sections
    print("\nCurrent Bookings:")

    # show current state of bookings (resource -> list of Booking)
    print(bm.list_bookings())

    # show a cancellation attempt
    print("\nCancelling booking:")

    # cancel Alice's booking on Room1
    print(bm.cancel_booking("Alice", "Room1"))  # expected: success

    # final bookings after cancellation
    print("\nFinal Bookings:")
    print(bm.list_bookings())
