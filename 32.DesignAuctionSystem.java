import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class AuctionSystem {

    // =============================================================
    // Auction Class
    // =============================================================
    static class Auction {
        private String id;
        private String itemName;
        private int startPrice;
        private int highestBid;
        private String highestBidder;
        private long startTime;
        private long endTime;
        private List<String> bidHistory;
        private final ReentrantLock lock;

        public Auction(String id, String itemName, int startPrice, int durationSeconds) {
            this.id = id;
            this.itemName = itemName;
            this.startPrice = startPrice;
            this.highestBid = startPrice;
            this.highestBidder = null;
            this.startTime = System.currentTimeMillis();
            this.endTime = this.startTime + durationSeconds * 1000L;
            this.bidHistory = new ArrayList<>();
            this.lock = new ReentrantLock();
        }

        public boolean isActive() {
            return System.currentTimeMillis() < endTime;
        }

        public String placeBid(String user, int amount) {
            lock.lock();
            try {
                if (!isActive()) {
                    return "Auction already ended.";
                }

                if (amount <= highestBid) {
                    return "Bid too low.";
                }

                highestBid = amount;
                highestBidder = user;
                bidHistory.add(user + " -> " + amount);
                return "Bid accepted: " + user + " -> " + amount;

            } finally {
                lock.unlock();
            }
        }

        public String getHighestBidder() {
            return highestBidder;
        }

        public int getHighestBid() {
            return highestBid;
        }
    }

    // =============================================================
    // AuctionManager (Singleton)
    // =============================================================
    static class AuctionManager {
        private static AuctionManager instance;
        private Map<String, Auction> auctions;

        private AuctionManager() {
            auctions = new ConcurrentHashMap<>();
        }

        public static synchronized AuctionManager getInstance() {
            if (instance == null) {
                instance = new AuctionManager();
            }
            return instance;
        }

        public Auction createAuction(String id, String item, int startPrice, int durationSeconds) {
            Auction a = new Auction(id, item, startPrice, durationSeconds);
            auctions.put(id, a);
            return a;
        }

        public Auction getAuction(String id) {
            return auctions.get(id);
        }

        public String placeBid(String id, String user, int amount) {
            Auction a = auctions.get(id);
            if (a == null) return "Auction not found.";
            return a.placeBid(user, amount);
        }

        public String endAuction(String id) {
            Auction a = auctions.get(id);
            if (a == null) return "Auction not found.";

            if (a.getHighestBidder() == null) {
                return "No bids placed. Auction ended with no winner.";
            }

            return "Winner: " + a.getHighestBidder() + " with bid " + a.getHighestBid();
        }
    }

    // =============================================================
    // Demo Run
    // =============================================================
    public static void main(String[] args) throws InterruptedException {
        AuctionManager manager = AuctionManager.getInstance();

        manager.createAuction("A1", "Laptop", 500, 5);

        System.out.println(manager.placeBid("A1", "Alice", 550));
        System.out.println(manager.placeBid("A1", "Bob", 600));

        Thread.sleep(6000);

        System.out.println(manager.placeBid("A1", "Charlie", 700));

        System.out.println(manager.endAuction("A1"));
    }
}
