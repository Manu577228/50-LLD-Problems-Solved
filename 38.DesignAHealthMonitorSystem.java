/* -------------------------------------------------------- */
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
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio      */
/* -----------------------------------------------------------------------  */

import java.util.*;

public class HealthMonitorSystem {

    // --------------------- Strategy Pattern ---------------------
    interface AlertStrategy {
        List<String> check(Map<String, Integer> vitals, Map<String, Integer> thr);
    }

    // Concrete Strategy
    static class ThresholdAlertStrategy implements AlertStrategy {
        public List<String> check(Map<String, Integer> v, Map<String, Integer> t) {
            List<String> alerts = new ArrayList<>();
            if (v.get("heart") > t.get("heart")) alerts.add("High Heart Rate");
            if (v.get("temp") > t.get("temp")) alerts.add("High Temperature");
            if (v.get("bp") > t.get("bp")) alerts.add("High Blood Pressure");
            return alerts;
        }
    }

    // --------------------- User Profile Model ---------------------
    static class UserProfile {
        Map<String, Integer> thresholds;
        Map<String, Integer> lastVitals;
        List<Map<String, Integer>> logs = new ArrayList<>();

        UserProfile(Map<String, Integer> thr) {
            this.thresholds = thr;
        }
    }

    // --------------------- Singleton Health Monitor ---------------------
    static class HealthMonitor {
        private static HealthMonitor instance;
        private Map<String, UserProfile> users = new HashMap<>();
        private AlertStrategy alertStrategy = new ThresholdAlertStrategy();

        private HealthMonitor() {}

        public static synchronized HealthMonitor getInstance() {
            if (instance == null) instance = new HealthMonitor();
            return instance;
        }

        public void addUser(String uid, Map<String, Integer> thr) {
            users.put(uid, new UserProfile(thr));
        }

        public synchronized List<String> updateVitals(String uid, Map<String, Integer> vitals) {
            UserProfile user = users.get(uid);
            if (user == null) return null;

            user.lastVitals = vitals;
            user.logs.add(vitals);

            return alertStrategy.check(vitals, user.thresholds);
        }

        public Map<String, Object> getStatus(String uid) {
            UserProfile u = users.get(uid);
            if (u == null) return null;

            Map<String, Object> out = new HashMap<>();
            out.put("lastVitals", u.lastVitals);

            int size = u.logs.size();
            out.put("logs", u.logs.subList(Math.max(0, size - 5), size));
            return out;
        }
    }

    // ----------------------------- DEMO ---------------------------------
    public static void main(String[] args) {
        HealthMonitor monitor = HealthMonitor.getInstance();

        Map<String, Integer> thr = new HashMap<>();
        thr.put("heart", 100);
        thr.put("temp", 99);
        thr.put("bp", 140);

        monitor.addUser("user1", thr);

        Map<String, Integer> v1 = Map.of("heart", 95, "temp", 98, "bp", 135);
        Map<String, Integer> v2 = Map.of("heart", 120, "temp", 101, "bp", 150);

        System.out.println("Alerts 1: " + monitor.updateVitals("user1", v1));
        System.out.println("Alerts 2: " + monitor.updateVitals("user1", v2));
        System.out.println("Status:   " + monitor.getStatus("user1"));
    }
}
