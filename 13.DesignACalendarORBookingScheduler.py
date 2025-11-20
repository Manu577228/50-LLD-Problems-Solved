# Design a simple Calendar/Booking Scheduler system that allows adding, 
# viewing, and deleting event bookings while ensuring no overlapping time slots. 
# The system should run entirely in Python (no external database) and demonstrate 
# basic scheduling logic.

class Booking:
    def __init__(self, bid, title, start, end):
        self.id = bid
        self.title = title
        self.start = start
        self.end = end

class Calendar:
    def __init__(self):
        self.bookings = []

    def add_booking(self, bid, title, start, end):
        new_booking = Booking(bid, title, start, end)

        for b in self.bookings:
            if not (end <= b.start or start >= b.end):
                print(f"Conflict with booking: {b.title}")
                return False
            
        self.bookings.append(new_booking)
        self.bookings.sort(key = lambda x : x.start)
        print(f" Booking added: {title} ({start}--{end})")
        return True
    
    def remove_booking(self, bid):
        for b in self.bookings:
            if b.id == bid:
                self.bookings.remove(b)
                print(f" Booking removed: {b.title}")
                return True
        print(" Booking ID not found.")
        return False
    
    def show_bookings(self):
        if not self.bookings:
            print(" No Booking Scheduled.")
        else:
            print("Current Schedule:")
            for b in self.bookings:
                print(f" ID: {b.id} | {b.title} ({b.start}--{b.end})")
    
if __name__ == "__main__":
    cal = Calendar()

    # Add some demo bookings
    cal.add_booking(1, "Team Meeting", 9, 10)
    cal.add_booking(2, "Client Call", 10, 11)
    cal.add_booking(3, "Lunch", 12, 13)
    cal.add_booking(4, "Overlapping Task", 9.5, 10.5)  # Should fail

    # Display all bookings
    cal.show_bookings()

    # Remove one booking
    cal.remove_booking(2)

    # Display after removal
    cal.show_bookings()
        