import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

// ---------- STRATEGY PATTERN ----------
interface DiscountStrategy {
    double apply(double total);
}

class PercentageDiscount implements DiscountStrategy {
    private final double percent;

    public PercentageDiscount(double percent) {
        this.percent = percent;
    }

    @Override
    public double apply(double total) {
        return total - (total * percent / 100);
    }
}

class FlatDiscount implements DiscountStrategy {
    private final double amount;

    public FlatDiscount(double amount) {
        this.amount = amount;
    }

    @Override
    public double apply(double total) {
        return Math.max(0, total - amount);
    }
}

// ---------- COUPON ENTITY ----------
class Coupon {
    String id;
    DiscountStrategy strategy;
    LocalDateTime expiry;
    double minOrder;
    int maxUses;
    int currentUses = 0;

    public Coupon(String id, DiscountStrategy strategy, LocalDateTime expiry,
                  double minOrder, int maxUses) {
        this.id = id;
        this.strategy = strategy;
        this.expiry = expiry;
        this.minOrder = minOrder;
        this.maxUses = maxUses;
    }
}

// ---------- SINGLETON MANAGER ----------
class CouponManager {
    private static CouponManager instance;
    private final Map<String, Coupon> coupons = new HashMap<>();

    private CouponManager() {}

    public static CouponManager getInstance() {
        if (instance == null) {
            instance = new CouponManager();
        }
        return instance;
    }

    // Factory-based coupon creation
    public void createCoupon(String id, String type, double amount,
                             int daysValid, double minOrder, int maxUses) {

        LocalDateTime expiry = LocalDateTime.now().plus(daysValid, ChronoUnit.DAYS);

        DiscountStrategy strategy = type.equals("percent")
                ? new PercentageDiscount(amount)
                : new FlatDiscount(amount);

        Coupon c = new Coupon(id, strategy, expiry, minOrder, maxUses);
        coupons.put(id, c);
    }

    // Validate coupon rules
    private String validate(Coupon c, double total) {
        if (LocalDateTime.now().isAfter(c.expiry)) return "Expired";
        if (c.currentUses >= c.maxUses) return "Usage limit reached";
        if (total < c.minOrder) return "Minimum order not met";
        return "Valid";
    }

    // Apply coupon
    public synchronized Result applyCoupon(String id, double total) {
        if (!coupons.containsKey(id)) {
            return new Result(false, "Invalid Coupon", total);
        }

        Coupon c = coupons.get(id);
        String status = validate(c, total);

        if (!status.equals("Valid")) {
            return new Result(false, status, total);
        }

        c.currentUses++;

        double discounted = c.strategy.apply(total);
        return new Result(true, "Applied", discounted);
    }
}

// ---------- RESULT STRUCT ----------
class Result {
    boolean success;
    String message;
    double finalAmount;

    public Result(boolean success, String message, double finalAmount) {
        this.success = success;
        this.message = message;
        this.finalAmount = finalAmount;
    }

    public String toString() {
        return "(" + success + ", " + message + ", " + finalAmount + ")";
    }
}

// ---------- DEMO MAIN ----------
public class Main {
    public static void main(String[] args) {

        CouponManager mgr = CouponManager.getInstance();

        mgr.createCoupon("DISC10", "percent", 10, 3, 100, 5);
        mgr.createCoupon("FLAT50", "flat", 50, 5, 200, 2);

        System.out.println(mgr.applyCoupon("DISC10", 150)); // approx 135
        System.out.println(mgr.applyCoupon("FLAT50", 220)); // 170
        System.out.println(mgr.applyCoupon("FLAT50", 150)); // minimum order fail
    }
}
