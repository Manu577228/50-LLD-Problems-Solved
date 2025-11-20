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

import java.io.*;
import java.util.*;

/*
=========================================================
             📅  CALENDAR / BOOKING SCHEDULER  
=========================================================

Question:
Design a simple Calendar/Booking Scheduler system that allows
adding, viewing, and deleting event bookings while ensuring
no overlapping time slots — purely in Java console mode.
=========================================================
*/

class Booking {
    int id;             // Unique booking ID
    String title;       // Event title
    double start;       // Start time
    double end;         // End time

    Booking(int id, String title, double start, double end) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
    }
}

class CalendarScheduler {
    private List<Booking> bookings;

    CalendarScheduler() {
        bookings = new ArrayList<>();
    }

    // Adds a booking if no overlap exists
    public boolean addBooking(int id, String title, double start, double end) {
        Booking newBooking = new Booking(id, title, start, end);

        // Check overlap with existing bookings
        for (Booking b : bookings) {
            if (!(end <= b.start || start >= b.end)) {
                System.out.println("❌ Conflict with booking: " + b.title);
                return false;
            }
        }

        // Add booking and sort by start time
        bookings.add(newBooking);
        bookings.sort(Comparator.comparingDouble(b -> b.start));
        System.out.println("✅ Booking added: " + title + " (" + start + "-" + end + ")");
        return true;
    }

    // Remove booking by ID
    public boolean removeBooking(int id) {
        for (Booking b : bookings) {
            if (b.id == id) {
                bookings.remove(b);
                System.out.println("🗑️ Booking removed: " + b.title);
                return true;
            }
        }
        System.out.println("⚠️ Booking ID not found.");
        return false;
    }

    // Display all bookings
    public void showBookings() {
        if (bookings.isEmpty()) {
            System.out.println("📭 No bookings scheduled.");
        } else {
            System.out.println("📅 Current Schedule:");
            for (Booking b : bookings) {
                System.out.println("  ID:" + b.id + " | " + b.title + " (" + b.start + "-" + b.end + ")");
            }
        }
    }
}

public class CalendarDemo {
    public static void main(String[] args) {

        // Create a CalendarScheduler instance
        CalendarScheduler cal = new CalendarScheduler();

        // Add demo bookings
        cal.addBooking(1, "Team Meeting", 9, 10);
        cal.addBooking(2, "Client Call", 10, 11);
        cal.addBooking(3, "Lunch", 12, 13);
        cal.addBooking(4, "Overlapping Task", 9.5, 10.5); // Should fail due to overlap

        // Show current bookings
        cal.showBookings();

        // Remove one booking
        cal.removeBooking(2);

        // Show after removal
        cal.showBookings();
    }
}

/*
=========================================================
LINE-BY-LINE EXPLANATION
=========================================================

1. Booking class defines a booking with id, title, start, and end times.
2. CalendarScheduler manages all bookings using an ArrayList.
3. addBooking(): checks for overlaps before inserting a booking.
   - If overlap exists, prints conflict message and returns false.
   - Otherwise, adds and sorts bookings by start time.
4. removeBooking(): removes a booking by its unique ID.
5. showBookings(): prints all scheduled bookings in sorted order.
6. In main(), several sample bookings are added and tested.
7. The overlapping booking ("Overlapping Task") is rejected.
8. Output shows successful and failed bookings with final schedule.

=========================================================
OUTPUT (Demo Run)
=========================================================
✅ Booking added: Team Meeting (9.0-10.0)
✅ Booking added: Client Call (10.0-11.0)
✅ Booking added: Lunch (12.0-13.0)
❌ Conflict with booking: Team Meeting
📅 Current Schedule:
  ID:1 | Team Meeting (9.0-10.0)
  ID:2 | Client Call (10.0-11.0)
  ID:3 | Lunch (12.0-13.0)
🗑️ Booking removed: Client Call
📅 Current Schedule:
  ID:1 | Team Meeting (9.0-10.0)
  ID:3 | Lunch (12.0-13.0)

=========================================================
LIMITATIONS
=========================================================
- No persistence (data lost on program exit).
- No recurring event support.
- No user input validation or formatted time display.

=========================================================
ALTERNATIVE APPROACHES (Future Discussion)
=========================================================
1) Interval Tree → O(log n) overlap detection (efficient but complex).
2) Segment Tree → Faster range queries, ideal for large datasets.
3) Database-backed Scheduler → Multi-user, persistent system.

=========================================================
*/ 
