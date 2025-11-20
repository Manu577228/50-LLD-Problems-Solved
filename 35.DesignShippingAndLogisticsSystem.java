// ---------------- SHIPPING & LOGISTICS SYSTEM (Single File Java Demo) ----------------

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.UUID;

// ======================= Strategy Pattern for Cost Calculation =======================
interface CostStrategy {
    double calculate(double weight, double distance);
}

class DefaultCostStrategy implements CostStrategy {
    public double calculate(double weight, double distance) {
        return 50 + (10 * weight) + (2 * distance);
    }
}

// ============================== Shipment Factory ======================================
class ShipmentFactory {
    public static Shipment create(String sender, String receiver, String origin,
            String destination, double weight, double distance, CostStrategy strategy) {

        String trackingId = UUID.randomUUID().toString().substring(0, 8);
        double cost = strategy.calculate(weight, distance);

        return new Shipment(trackingId, sender, receiver, origin, destination, weight, distance, cost);
    }
}

// ================================= Shipment ============================================
class Shipment {
    String trackingId, sender, receiver, origin, destination, status;
    double weight, distance, cost;
    Agent agent;
    private Lock lock = new ReentrantLock();

    Shipment(String tid, String s, String r, String o, String d, double w, double dist, double c) {
        this.trackingId = tid;
        this.sender = s;
        this.receiver = r;
        this.origin = o;
        this.destination = d;
        this.weight = w;
        this.distance = dist;
        this.cost = c;
        this.status = "Pending";
    }

    void updateStatus(String newStatus) {
        lock.lock();
        try {
            this.status = newStatus;
        } finally {
            lock.unlock();
        }
    }
}

// ================================== Agent ==============================================
class Agent {
    int id;
    String name;
    boolean available = true;
    private Lock lock = new ReentrantLock();

    Agent(int i, String n) {
        this.id = i;
        this.name = n;
    }

    boolean assign() {
        lock.lock();
        try {
            if (available) {
                available = false;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    void release() {
        lock.lock();
        try {
            available = true;
        } finally {
            lock.unlock();
        }
    }
}

// ============================= Singleton: Logistics Manager ==============================
class LogisticsManager {
    private static LogisticsManager instance;
    private static final Object lockObj = new Object();

    Map<String, Shipment> shipments = new HashMap<>();
    List<Agent> agents = new ArrayList<>();
    CostStrategy costStrategy = new DefaultCostStrategy();

    private LogisticsManager() {
        agents.add(new Agent(1, "Agent A"));
        agents.add(new Agent(2, "Agent B"));
    }

    public static LogisticsManager getInstance() {
        synchronized (lockObj) {
            if (instance == null) instance = new LogisticsManager();
            return instance;
        }
    }

    String createShipment(String sender, String receiver, String origin,
            String destination, double weight, double distance) {

        Shipment shipment = ShipmentFactory.create(sender, receiver, origin, destination, weight, distance, costStrategy);
        shipments.put(shipment.trackingId, shipment);
        return shipment.trackingId;
    }

    String assignAgent(String trackingId) {
        Shipment shipment = shipments.get(trackingId);
        for (Agent a : agents) {
            if (a.assign()) {
                shipment.agent = a;
                return a.name;
            }
        }
        return null;
    }

    void updateStatus(String trackingId, String status) {
        Shipment shipment = shipments.get(trackingId);
        if (shipment != null) {
            shipment.updateStatus(status);
        }
    }

    Shipment getShipment(String trackingId) {
        return shipments.get(trackingId);
    }
}

// =============================== DEMO RUN ===============================================
public class Main {
    public static void main(String[] args) {

        LogisticsManager manager = LogisticsManager.getInstance();

        String tid = manager.createShipment("Alice", "Bob", "NY", "LA", 10, 450);
        System.out.println("Tracking ID: " + tid);

        String agent = manager.assignAgent(tid);
        System.out.println("Assigned Agent: " + agent);

        manager.updateStatus(tid, "In-Transit");
        Shipment s = manager.getShipment(tid);

        System.out.println("Status: " + s.status);
        System.out.println("Cost: " + s.cost);
    }
}
