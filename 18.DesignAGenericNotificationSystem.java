/* ----------------------------------------------------------------------------
   LLD - Generic Notification System
   (For YouTube Explanation by Bharadwaj)
-------------------------------------------------------------------------------

1) a) Functional Requirements
   ----------------------------
   - System should send notifications via multiple channels (Email, SMS, Push).
   - Each channel should follow a common interface for extensibility.
   - Allow adding new channels easily (Open/Closed principle).
   - Should support sending messages to multiple users.

   b) Non-Functional Requirements
   ------------------------------
   - Extensible design (easy to add new channels)
   - Maintainable and loosely coupled
   - Lightweight (no external DB)
   - Thread-safe for concurrent sends

-------------------------------------------------------------------------------
2) Algorithm Choice Discussion
------------------------------
   - Core algorithm: Observer Pattern
     (Each notification channel observes and reacts to a common event.)
   - Strategy Pattern applies since each channel implements its own send strategy.
   - Time complexity: O(n) per notification dispatch.
   - Space complexity: O(n) for channel registry.

-------------------------------------------------------------------------------
3) Concurrency and Data Model Discussion
----------------------------------------
   - Uses in-memory list (no database dependency).
   - Thread-safe using synchronized blocks.
   - Each send operation runs concurrently using ExecutorService.

-------------------------------------------------------------------------------
4) UML Diagram (ASCII Representation)
-------------------------------------

          +----------------------+
          |  NotificationManager |
          +----------------------+
          | - channels: List     |
          +----------+-----------+
                     |
           +---------+----------+
           |                    |
   +---------------+    +---------------+
   | EmailNotifier |    | SMSNotifier   |
   +---------------+    +---------------+
   | +send(msg)    |    | +send(msg)    |
   +---------------+    +---------------+
           |                    |
           +---------+----------+
                     |
           +--------------------+
           | PushNotifier       |
           +--------------------+
           | +send(msg)         |
           +--------------------+

-------------------------------------------------------------------------------
5) Correct Solution in Java (Runnable in VS Code)
-------------------------------------------------------------------------------
*/

import java.util.*;
import java.util.concurrent.*;

// 1️⃣ Common Interface for all Notifiers
interface Notifier {
    void send(String message);
}

// 2️⃣ Concrete Implementations

class EmailNotifier implements Notifier {
    @Override
    public void send(String message) {
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        System.out.println("📧 Email Sent: " + message);
    }
}

class SMSNotifier implements Notifier {
    @Override
    public void send(String message) {
        try { Thread.sleep(300); } catch (InterruptedException e) {}
        System.out.println("📱 SMS Sent: " + message);
    }
}

class PushNotifier implements Notifier {
    @Override
    public void send(String message) {
        try { Thread.sleep(200); } catch (InterruptedException e) {}
        System.out.println("🔔 Push Notification Sent: " + message);
    }
}

// 3️⃣ Notification Manager - Controller class
class NotificationManager {
    private final List<Notifier> channels = new ArrayList<>();

    // Thread-safe channel registration
    public synchronized void registerChannel(Notifier notifier) {
        channels.add(notifier);
    }

    // Concurrently send notifications via ExecutorService
    public void sendNotification(String message) {
        ExecutorService executor = Executors.newFixedThreadPool(channels.size());
        for (Notifier ch : channels) {
            executor.submit(() -> ch.send(message));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// 4️⃣ Demo Execution (Main Class)
public class NotificationSystemDemo {
    public static void main(String[] args) {
        NotificationManager manager = new NotificationManager();
        manager.registerChannel(new EmailNotifier());
        manager.registerChannel(new SMSNotifier());
        manager.registerChannel(new PushNotifier());

        System.out.println("🚀 Sending Notifications...\n");
        manager.sendNotification("Hello, Bharadwaj Subscribers! 🎥🔥");
        System.out.println("\n✅ Notification dispatch complete!");
    }
}

/*
-------------------------------------------------------------------------------
Expected Output:
-------------------------------------------------------------------------------
🚀 Sending Notifications...

📧 Email Sent: Hello, Bharadwaj Subscribers! 🎥🔥
📱 SMS Sent: Hello, Bharadwaj Subscribers! 🎥🔥
🔔 Push Notification Sent: Hello, Bharadwaj Subscribers! 🎥🔥

✅ Notification dispatch complete!
-------------------------------------------------------------------------------

6) Limitations of Current Code
------------------------------
- No retry mechanism for failed sends.
- No message queue or scheduling.
- In-memory only (no persistence layer).
- Simple text message handling (no templates or attachments).

-------------------------------------------------------------------------------
7) Alternative Algorithms and Trade Off's (Future Discussions)
--------------------------------------------------------------
- Event-driven architecture using Pub/Sub (Highly scalable, but adds infra complexity)
- Use Kafka/RabbitMQ for async notifications (Reliable but heavy setup)
- Template engine + user preferences (Customizable but more complex)
- Persistent storage for logs (Adds reliability but more overhead)

-------------------------------------------------------------------------------
END OF FILE
-------------------------------------------------------------------------------
*/
