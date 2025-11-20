import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class Item {
    private int id;
    private String name;
    private int quantity;
    private double price;

    public Item(int id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public void setQuantity(int q) {
        this.quantity = q;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "[ID=" + id + "] " + name + " | Qty=" + quantity + " | Price=" + price;
    }
}

// -------------------------
// Inventory Singleton
// -------------------------
class Inventory {
    private static Inventory instance = null;
    private final HashMap<Integer, Item> items;
    private int idCounter;
    private final ReentrantLock lock;

    private Inventory() {
        items = new HashMap<>();
        idCounter = 1;
        lock = new ReentrantLock();
    }

    public static Inventory getInstance() {
        if (instance == null) {
            synchronized (Inventory.class) {
                if (instance == null) {
                    instance = new Inventory();
                }
            }
        }
        return instance;
    }

    public int addItem(String name, int qty, double price) {
        lock.lock();
        try {
            int newId = idCounter++;
            items.put(newId, new Item(newId, name, qty, price));
            return newId;
        } finally {
            lock.unlock();
        }
    }

    public boolean updateQuantity(int itemId, int qty) {
        lock.lock();
        try {
            Item it = items.get(itemId);
            if (it != null) {
                it.setQuantity(qty);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public Item getItem(int itemId) {
        return items.get(itemId);
    }

    public Item deleteItem(int itemId) {
        lock.lock();
        try {
            return items.remove(itemId);
        } finally {
            lock.unlock();
        }
    }

    public List<Item> listItems() {
        return new ArrayList<>(items.values());
    }
}

// -------------------------
// DEMO EXECUTION
// -------------------------
public class InventorySystem {
    public static void main(String[] args) {

        Inventory inv = Inventory.getInstance();

        int id1 = inv.addItem("Apple", 50, 2.5);
        int id2 = inv.addItem("Banana", 100, 1.2);

        System.out.println("Initial Items:");
        System.out.println(inv.listItems());

        inv.updateQuantity(id1, 70);

        System.out.println("\nAfter Updating Apple Qty:");
        System.out.println(inv.getItem(id1));

        inv.deleteItem(id2);

        System.out.println("\nAfter Removing Banana:");
        System.out.println(inv.listItems());
    }
}
