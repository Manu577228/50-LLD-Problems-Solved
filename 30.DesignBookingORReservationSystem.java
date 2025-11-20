/*
UML DIAGRAM
-----------
+---------------------+
|     BookingManager  |  (Singleton)
+---------------------+
| - instance          |
| - lock              |
| - bookings: Map     |
+---------------------+
| + createBooking()   |
| + cancelBooking()   |
| + listBookings()    |
+----------+----------+
           |
           |
+----------v----------+
|      Booking        |
+---------------------+
| - user              |
| - resource          |
| - start             |
| - end               |
+---------------------+
*/

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class Booking {
    String user;
    String resource;
    int start;
    int end;

    Booking(String user, String resource, int start, int end) {
        this.user = user;
        this.resource = resource;
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "[" + user + " -> " + resource + " (" + start + "-" + end + ")]";
    }
}

class BookingManager {
    private static BookingManager instance;
    private static final ReentrantLock creationLock = new ReentrantLock();

    private final ReentrantLock managerLock = new ReentrantLock();
    private final Map<String, List<Booking>> bookings = new HashMap<>();

    private BookingManager() {}

    public static BookingManager getInstance() {
        creationLock.lock();
        try {
            if (instance == null) {
                instance = new BookingManager();
            }
            return instance;
        } finally {
            creationLock.unlock();
        }
    }

    private boolean hasConflict(String resource, int start, int end) {
        if (!bookings.containsKey(resource)) return false;

        for (Booking b : bookings.get(resource)) {
            if (!(end <= b.start || start >= b.end)) {
                return true;
            }
        }
        return false;
    }

    public synchronized String createBooking(String user, String resource, int start, int end) {
        managerLock.lock();
        try {
            if (hasConflict(resource, start, end)) {
                return "False, Conflict: Resource already booked in this slot.";
            }

            Booking b = new Booking(user, resource, start, end);
            bookings.computeIfAbsent(resource, r -> new ArrayList<>()).add(b);
            return "True, Booking confirmed.";
        } finally {
            managerLock.unlock();
        }
    }

    public synchronized String cancelBooking(String user, String resource) {
        managerLock.lock();
        try {
            if (!bookings.containsKey(resource)) {
                return "False, No booking found.";
            }

            List<Booking> list = bookings.get(resource);
            for (Booking b : list) {
                if (b.user.equals(user)) {
                    list.remove(b);
                    return "True, Booking cancelled.";
                }
            }
            return "False, Booking not found.";
        } finally {
            managerLock.unlock();
        }
    }

    public Map<String, List<Booking>> listBookings() {
        return bookings;
    }
}

public class BookingSystem {
    public static void main(String[] args) {
        BookingManager bm = BookingManager.getInstance();

        System.out.println("Creating bookings:");
        System.out.println(bm.createBooking("Alice", "Room1", 10, 12));
        System.out.println(bm.createBooking("Bob",   "Room1", 12, 14));
        System.out.println(bm.createBooking("Charlie", "Room1", 11, 13)); // conflict

        System.out.println("\nCurrent Bookings:");
        System.out.println(bm.listBookings());

        System.out.println("\nCancelling booking:");
        System.out.println(bm.cancelBooking("Alice", "Room1"));

        System.out.println("\nFinal Bookings:");
        System.out.println(bm.listBookings());
    }
}
