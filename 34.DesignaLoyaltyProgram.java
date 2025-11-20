/* -------------------------------------------------------------
   LOYALTY PROGRAM - LLD DEMO (JAVA)
   -------------------------------------------------------------
   This file contains:
   - Strategy Pattern for point calculation
   - Singleton LoyaltyService
   - In-memory User & Transaction Model
   - Thread-safe earn/redeem operations
   - Fully explained line-by-line for teaching on YouTube
   ------------------------------------------------------------- */

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

// -------------------------------------------------------------
// STRATEGY PATTERN BASE CLASS
// -------------------------------------------------------------
interface PointStrategy {
    int calculate(int amount);    // Interface method for point calculation
}

// -------------------------------------------------------------
// DEFAULT STRATEGY: 1 point for every 10 units spent
// -------------------------------------------------------------
class DefaultPointStrategy implements PointStrategy {
    public int calculate(int amount) {
        return amount / 10;      // Integer division for loyalty points
    }
}

// -------------------------------------------------------------
// USER ENTITY (MODEL)
// -------------------------------------------------------------
class User {
    private String userId;        // Stores unique user ID
    private String name;          // Stores user name
    private int points;           // Tracks loyalty points

    public User(String userId, String name) {
        this.userId = userId;    // Assign ID
        this.name = name;        // Assign name
        this.points = 0;         // Default points = 0
    }

    public void addPoints(int p) {
        this.points += p;        // Increase user's points
    }

    public boolean deductPoints(int p) {
        if (this.points >= p) {  // Check if enough points
            this.points -= p;    // Deduct
            return true;         // Success
        }
        return false;            // Failure
    }

    public int getPoints() {
        return this.points;      // Get current balance
    }
}

// -------------------------------------------------------------
// SINGLETON LOYALTY SERVICE (CORE ENGINE)
// -------------------------------------------------------------
class LoyaltyService {

    private static LoyaltyService instance;       // Stores single instance
    private static final ReentrantLock instanceLock = new ReentrantLock();

    private Map<String, User> users;              // User storage
    private List<String> transactions;            // Simple transaction log
    private ReentrantLock lock;                   // Lock for operations
    private PointStrategy strategy;               // Strategy for point calculation

    // Private constructor for Singleton
    private LoyaltyService() {
        users = new HashMap<>();                 // Create user map
        transactions = new ArrayList<>();         // Create transaction log
        lock = new ReentrantLock();               // Initialize lock
        strategy = new DefaultPointStrategy();    // Default strategy
    }

    // Public method to get the Singleton instance
    public static LoyaltyService getInstance() {
        instanceLock.lock();                      // Lock for safe instance creation
        try {
            if (instance == null) {
                instance = new LoyaltyService();  // Create new instance
            }
            return instance;                      // Return singleton
        } finally {
            instanceLock.unlock();                // Release lock
        }
    }

    // ---------------------------------------------------------
    // Enroll a new user
    // ---------------------------------------------------------
    public void enrollUser(String userId, String name) {
        users.put(userId, new User(userId, name));   // Create and store user
    }

    // ---------------------------------------------------------
    // Earn points (thread-safe)
    // ---------------------------------------------------------
    public int earnPoints(String userId, int amount) {
        int pts = strategy.calculate(amount);        // Calculate points

        lock.lock();                                 // Begin critical section
        try {
            User user = users.get(userId);           // Get user
            user.addPoints(pts);                     // Add points
            transactions.add("earn:" + userId + ":" + pts + ":" + System.currentTimeMillis());
        } finally {
            lock.unlock();                           // End critical section
        }

        return pts;                                  // Return earned points
    }

    // ---------------------------------------------------------
    // Redeem points (thread-safe)
    // ---------------------------------------------------------
    public boolean redeemPoints(String userId, int ptsRequired) {
        lock.lock();                                 // Lock for safety
        try {
            User user = users.get(userId);           // Fetch user
            if (user.deductPoints(ptsRequired)) {    // Try redeeming
                transactions.add("redeem:" + userId + ":" + ptsRequired + ":" + System.currentTimeMillis());
                return true;                         // Success
            }
            return false;                            // Not enough points
        } finally {
            lock.unlock();                           // Unlock
        }
    }

    // ---------------------------------------------------------
    // Get current points for a user
    // ---------------------------------------------------------
    public int getUserPoints(String userId) {
        return users.get(userId).getPoints();        // Fetch points
    }
}

// -------------------------------------------------------------
// DEMO / TESTING BLOCK (MAIN METHOD)
// -------------------------------------------------------------
public class LoyaltyProgramDemo {

    public static void main(String[] args) {

        LoyaltyService service = LoyaltyService.getInstance(); // Get Singleton service

        service.enrollUser("U1", "Bharadwaj");                 // Register user

        int earned = service.earnPoints("U1", 250);             // Earn points
        System.out.println("Points earned: " + earned);         // Expect 25

        boolean redeemed = service.redeemPoints("U1", 10);      // Redeem
        System.out.println("Redeemed: " + redeemed);            // Expect true

        System.out.println("Current Points: " +
                service.getUserPoints("U1"));                   // Expect 15
    }
}
