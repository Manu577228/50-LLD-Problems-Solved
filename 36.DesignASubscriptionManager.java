import java.time.LocalDate;
import java.util.*;

class Subscription {
    private int subId;
    private int userId;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;  // ACTIVE, CANCELLED

    public Subscription(int subId, int userId, String planName, LocalDate startDate, LocalDate endDate) {
        this.subId = subId;
        this.userId = userId;
        this.planName = planName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "ACTIVE";
    }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return status.equals("ACTIVE") && !today.isAfter(endDate);
    }

    public void renew(int days) {
        this.endDate = this.endDate.plusDays(days);
    }

    public void cancel() {
        this.status = "CANCELLED";
    }

    public int getUserId() {
        return userId;
    }

    public int getSubId() {
        return subId;
    }

    public String getPlanName() {
        return planName;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}


class SubscriptionManager {
    private static SubscriptionManager instance;
    private Map<Integer, List<Subscription>> subscriptions;

    private SubscriptionManager() {
        subscriptions = new HashMap<>();
    }

    public static SubscriptionManager getInstance() {
        if (instance == null) {
            instance = new SubscriptionManager();
        }
        return instance;
    }

    public void addSubscription(Subscription s) {
        subscriptions.computeIfAbsent(s.getUserId(), k -> new ArrayList<>()).add(s);
    }

    public void cancelSubscription(int userId, int subId) {
        List<Subscription> list = subscriptions.getOrDefault(userId, Collections.emptyList());
        for (Subscription s : list) {
            if (s.getSubId() == subId) {
                s.cancel();
            }
        }
    }

    public void renewSubscription(int userId, int subId, int days) {
        List<Subscription> list = subscriptions.getOrDefault(userId, Collections.emptyList());
        for (Subscription s : list) {
            if (s.getSubId() == subId) {
                s.renew(days);
            }
        }
    }

    public List<Subscription> getActive(int userId) {
        List<Subscription> res = new ArrayList<>();
        List<Subscription> list = subscriptions.getOrDefault(userId, Collections.emptyList());
        for (Subscription s : list) {
            if (s.isActive()) res.add(s);
        }
        return res;
    }
}


public class SubscriptionDemo {
    public static void main(String[] args) {

        SubscriptionManager manager = SubscriptionManager.getInstance();

        Subscription s1 = new Subscription(
                1,
                101,
                "Premium",
                LocalDate.now(),
                LocalDate.now().plusDays(7)
        );

        manager.addSubscription(s1);

        System.out.println("Active subs before cancel: ");
        for (Subscription s : manager.getActive(101)) {
            System.out.println(s.getPlanName());
        }

        manager.cancelSubscription(101, 1);

        System.out.println("Active subs after cancel: ");
        for (Subscription s : manager.getActive(101)) {
            System.out.println(s.getPlanName());
        }

        manager.renewSubscription(101, 1, 10);
        System.out.println("Renew attempted → Status: " + s1.getStatus() +
                " | End date: " + s1.getEndDate());
    }
}
