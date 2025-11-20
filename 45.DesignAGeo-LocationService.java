/* ---------------- GEO LOCATION SERVICE DEMO (Java, Single File) ---------------- */

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/* -------- Distance Strategy Interface -------- */
interface DistanceStrategy {
    // Line: Defines a method to compute distance between two lat/lon points.
    double calculate(double lat1, double lon1, double lat2, double lon2);
}

/* -------- Haversine Strategy Implementation -------- */
class HaversineDistance implements DistanceStrategy {
    // Line: Implements accurate spherical distance.
    public double calculate(double lat1, double lon1, double lat2, double lon2) {

        double R = 6371; // Earth radius in km

        // Line: Convert deltas to radians.
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Line: Apply Haversine formula.
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Line: Final distance in km.
    }
}

/* -------- Location Model -------- */
class Location {
    // Line: Each location object stores name & coordinates.
    String name;
    double lat;
    double lon;

    Location(String name, double lat, double lon) {
        // Line: Assign fields.
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }
}

/* -------- Singleton GeoService -------- */
class GeoService {

    // Line: SINGLETON instance storage.
    private static GeoService instance;

    // Line: In-memory location storage.
    private final Map<Integer, Location> locations = new HashMap<>();

    // Line: Global ID counter for locations.
    private int idCounter = 1;

    // Line: Lock for thread-safe writes.
    private final ReentrantLock lock = new ReentrantLock();

    // Line: Pluggable distance strategy.
    private DistanceStrategy distanceStrategy = new HaversineDistance();

    // Line: Private constructor ensures Singleton pattern.
    private GeoService() {}

    // Line: Static method to get ONE instance only.
    public static GeoService getInstance() {
        if (instance == null) {
            synchronized (GeoService.class) { // Line: Double-checked locking.
                if (instance == null) instance = new GeoService();
            }
        }
        return instance;
    }

    /* -------- Add Location -------- */
    public int addLocation(String name, double lat, double lon) {
        lock.lock(); // Line: Ensure thread-safe write.
        try {
            int id = idCounter++;
            locations.put(id, new Location(name, lat, lon)); // Line: Store new entry.
            return id;
        } finally {
            lock.unlock();
        }
    }

    /* -------- Update Location -------- */
    public void updateLocation(int id, String name, Double lat, Double lon) {
        lock.lock(); // Line: Protect modification.
        try {
            if (locations.containsKey(id)) {
                Location loc = locations.get(id);
                if (name != null) loc.name = name;
                if (lat != null) loc.lat = lat;
                if (lon != null) loc.lon = lon;
            }
        } finally {
            lock.unlock();
        }
    }

    /* -------- Delete Location -------- */
    public void deleteLocation(int id) {
        lock.lock();
        try {
            locations.remove(id); // Line: Remove entry if present.
        } finally {
            lock.unlock();
        }
    }

    /* -------- Get Nearby -------- */
    public List<String> getNearby(double lat, double lon, double radiusKm) {
        // Line: Results stored as sorted list of: "name (distance km)".
        List<Map.Entry<Double, String>> res = new ArrayList<>();

        // Line: Linear scan across stored locations.
        for (Location loc : locations.values()) {
            double dist = distanceStrategy.calculate(lat, lon, loc.lat, loc.lon);
            if (dist <= radiusKm) {
                res.add(Map.entry(dist, loc.name));
            }
        }

        // Line: Sort by distance.
        res.sort(Map.Entry.comparingByKey());

        // Line: Format output list.
        List<String> finalList = new ArrayList<>();
        for (var e : res) {
            finalList.add(e.getValue() + " (" + e.getKey() + " km)");
        }
        return finalList;
    }
}

/* -------- DEMO MAIN (Runs in VS Code) -------- */
public class GeoLocationDemo {
    public static void main(String[] args) {

        GeoService geo = GeoService.getInstance(); // Line: Get singleton instance.

        // Line: Add two example locations.
        int id1 = geo.addLocation("Restaurant A", 28.6139, 77.2090);
        int id2 = geo.addLocation("Cafe B", 28.7041, 77.1025);

        // Line: Query within 15 km radius.
        List<String> nearby = geo.getNearby(28.61, 77.20, 15);

        // Line: Print results.
        System.out.println("Nearby locations:");
        for (String s : nearby) System.out.println(s);
    }
}
