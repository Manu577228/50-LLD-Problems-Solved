// ---------------- FEATURE FLAG MANAGER: SINGLE-FILE LLD DEMO (JAVA) -------------------

import java.util.*;
import java.util.concurrent.locks.*;

class FeatureFlag {
    // Name of the flag
    private String name;           // stores the flag's name
    private boolean enabled;       // global ON/OFF
    private Set<Integer> perUser;  // users who should explicitly get the feature
    private int rolloutPercentage; // % rollout

    // Constructor initializes empty feature flag
    public FeatureFlag(String name) {
        this.name = name;                  // assign name
        this.enabled = false;              // default: disabled
        this.perUser = new HashSet<>();    // set for user-specific overrides
        this.rolloutPercentage = 0;        // default 0% rollout
    }

    // Determine if feature is enabled for the given user
    public boolean isEnabled(int userId) {
        if (perUser.contains(userId)) {    // explicit override?
            return true;
        }
        if (enabled) {                     // global enable?
            return true;
        }
        return (userId % 100) < rolloutPercentage;  // percentage rollout logic
    }

    // Setters for manager
    public void setEnabled(boolean state) {
        this.enabled = state;
    }

    public void addUser(int userId) {
        this.perUser.add(userId);
    }

    public void removeUser(int userId) {
        this.perUser.remove(userId);
    }

    public void setRollout(int pct) {
        this.rolloutPercentage = pct;
    }
}


// ------------------------------ SINGLETON MANAGER -------------------------------------

class FeatureFlagManager {
    // Singleton instance
    private static FeatureFlagManager instance = null;

    // Lock for thread safety
    private static final ReentrantLock instanceLock = new ReentrantLock();
    private final ReentrantLock flagLock = new ReentrantLock();

    // In-memory store for all flags
    private Map<String, FeatureFlag> flags;

    // Private constructor
    private FeatureFlagManager() {
        this.flags = new HashMap<>();  // store flags in hashmap for O(1) lookup
    }

    // Singleton getter
    public static FeatureFlagManager getInstance() {
        instanceLock.lock();           // lock to ensure single instance creation
        try {
            if (instance == null) {    // create instance if not created
                instance = new FeatureFlagManager();
            }
            return instance;           // return global manager
        } finally {
            instanceLock.unlock();     // release lock
        }
    }

    // Create a new flag
    public void createFlag(String name) {
        flagLock.lock();               // lock write operations
        try {
            flags.put(name, new FeatureFlag(name)); // insert new flag
        } finally {
            flagLock.unlock();
        }
    }

    // Update global state
    public void updateFlagState(String name, boolean state) {
        flagLock.lock();
        try {
            flags.get(name).setEnabled(state);      // set on/off
        } finally {
            flagLock.unlock();
        }
    }

    // Enable specific user
    public void enableUser(String name, int userId) {
        flagLock.lock();
        try {
            flags.get(name).addUser(userId);        // add user override
        } finally {
            flagLock.unlock();
        }
    }

    // Disable specific user
    public void disableUser(String name, int userId) {
        flagLock.lock();
        try {
            flags.get(name).removeUser(userId);     // remove override
        } finally {
            flagLock.unlock();
        }
    }

    // Set rollout %
    public void setRollout(String name, int pct) {
        flagLock.lock();
        try {
            flags.get(name).setRollout(pct);        // set rollout percentage
        } finally {
            flagLock.unlock();
        }
    }

    // Public method to check flag state
    public boolean isEnabled(String name, int userId) {
        return flags.get(name).isEnabled(userId);   // delegate check to FeatureFlag
    }
}


// ------------------------------ DEMO MAIN CLASS ---------------------------------------

public class FeatureFlagDemo {
    public static void main(String[] args) {

        FeatureFlagManager mgr = FeatureFlagManager.getInstance();  // get singleton

        // Create first flag
        mgr.createFlag("new_ui");                    // create a flag
        mgr.updateFlagState("new_ui", true);         // globally enable it

        // Create second flag (beta)
        mgr.createFlag("beta_feature");              // create another flag
        mgr.setRollout("beta_feature", 30);          // enable 30% rollout
        mgr.enableUser("beta_feature", 101);         // explicit override for user 101

        // Test users
        int[] users = {10, 25, 50, 101};

        System.out.println("Feature Checks:");
        for (int u : users) {
            boolean newUI = mgr.isEnabled("new_ui", u);                 // check new_ui
            boolean beta = mgr.isEnabled("beta_feature", u);            // check beta flag
            System.out.println("User " + u + ":  new_ui=" + newUI +
                               "  beta_feature=" + beta);
        }
    }
}
