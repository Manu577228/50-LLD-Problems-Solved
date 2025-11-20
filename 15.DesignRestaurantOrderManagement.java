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

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
-----------------------------------------------------------
LLD Problem : Design a Restaurant Order Management System
-----------------------------------------------------------

1) REQUIREMENTS
-----------------------------------------------------------
a) Functional Requirements:
   - Add menu items.
   - Take customer orders.
   - View order summary.
   - Update order status (Pending → Preparing → Served).
   - Generate total bill per order.

b) Non-Functional Requirements:
   - In-memory only (no DB).
   - Thread-safe for concurrent updates.
   - Extendable and easy to read for demos.

2) ALGORITHM CHOICE DISCUSSION
-----------------------------------------------------------
- Object-Oriented Design using classes MenuItem, Order, Restaurant.
- HashMap for O(1) lookup for menu and orders.
- Synchronized updates using Lock for concurrency.

3) CONCURRENCY & DATA MODEL DISCUSSION
-----------------------------------------------------------
- Concurrency: ReentrantLock ensures atomic order updates.
- Data Model:
      MenuItem  → id, name, price
      Order     → id, List<MenuItem>, total, status
      Restaurant→ menus, orders, lock

4) UML DIAGRAM (Textual)
-----------------------------------------------------------
         +--------------------+
         |     Restaurant     |
         +--------------------+
         | - menuItems        |
         | - orders           |
         | - lock             |
         +--------------------+
         | + addMenuItem()    |
         | + placeOrder()     |
         | + updateOrder()    |
         | + showOrders()     |
         +--------------------+
                  |
        +---------+---------+
        |                   |
+--------------+    +----------------+
|   MenuItem   |    |     Order      |
+--------------+    +----------------+
| id, name,    |    | id, items,     |
| price        |    | total, status  |
+--------------+    +----------------+

-----------------------------------------------------------
5) JAVA SOLUTION WITH EXPLANATION & OUTPUT
-----------------------------------------------------------
*/

class MenuItem {
    int id;
    String name;
    double price;

    MenuItem(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}

class Order {
    int id;
    List<MenuItem> items;
    double total;
    String status;

    Order(int id, List<MenuItem> items) {
        this.id = id;
        this.items = items;
        this.total = items.stream().mapToDouble(i -> i.price).sum();
        this.status = "Pending";
    }
}

class Restaurant {
    private final Map<Integer, MenuItem> menuItems;
    private final Map<Integer, Order> orders;
    private final Lock lock;

    Restaurant() {
        this.menuItems = new HashMap<>();
        this.orders = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public void addMenuItem(int id, String name, double price) {
        menuItems.put(id, new MenuItem(id, name, price));
    }

    public Order placeOrder(int orderId, List<Integer> itemIds) {
        lock.lock();
        try {
            List<MenuItem> selectedItems = new ArrayList<>();
            for (int id : itemIds) {
                if (menuItems.containsKey(id)) {
                    selectedItems.add(menuItems.get(id));
                }
            }
            Order order = new Order(orderId, selectedItems);
            orders.put(orderId, order);
            return order;
        } finally {
            lock.unlock();
        }
    }

    public void updateOrderStatus(int orderId, String newStatus) {
        lock.lock();
        try {
            if (orders.containsKey(orderId)) {
                orders.get(orderId).status = newStatus;
            }
        } finally {
            lock.unlock();
        }
    }

    public void showOrders() {
        System.out.println("\n------ Current Orders ------");
        for (Order o : orders.values()) {
            List<String> itemNames = new ArrayList<>();
            for (MenuItem i : o.items) {
                itemNames.add(i.name);
            }
            System.out.println("Order #" + o.id + ": " + itemNames + " | Total: ₹" + o.total + " | Status: " + o.status);
        }
    }
}

public class RestaurantOrderManagement {
    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant();

        restaurant.addMenuItem(1, "Margherita Pizza", 250);
        restaurant.addMenuItem(2, "Pasta Alfredo", 180);
        restaurant.addMenuItem(3, "Cold Coffee", 90);

        restaurant.placeOrder(101, Arrays.asList(1, 3));
        restaurant.placeOrder(102, Arrays.asList(2));

        restaurant.showOrders();

        restaurant.updateOrderStatus(101, "Preparing");
        restaurant.updateOrderStatus(102, "Served");

        restaurant.showOrders();
    }
}

/*
OUTPUT:
------ Current Orders ------
Order #101: [Margherita Pizza, Cold Coffee] | Total: ₹340.0 | Status: Pending
Order #102: [Pasta Alfredo] | Total: ₹180.0 | Status: Pending

------ Current Orders ------
Order #101: [Margherita Pizza, Cold Coffee] | Total: ₹340.0 | Status: Preparing
Order #102: [Pasta Alfredo] | Total: ₹180.0 | Status: Served
*/

/*
-----------------------------------------------------------
6) LIMITATIONS OF CURRENT CODE
-----------------------------------------------------------
- No persistence (data lost on restart).
- Single-thread lock limits parallel scalability.
- No tax, discount, or category support.

7) ALTERNATIVE ALGORITHMS & TRADE-OFFS
-----------------------------------------------------------
- Future: Use ExecutorService for async order processing.
- Add lightweight DB (H2/SQLite) for persistence.
- Event-driven messaging for live kitchen updates.
- Decouple menu and order microservices.
-----------------------------------------------------------
*/
