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
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio       */
/* -----------------------------------------------------------------------  */

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/*
    ============================= UML ================================

    +---------------------------+
    |    ConfigurationManager   |
    +---------------------------+
    | - instance                |
    | - configStore : Map       |
    | - versions : List<Map>    |
    | - lock : ReentrantLock    |
    +---------------------------+
    | + get(key)                |
    | + set(key, value)         |
    | + delete(key)             |
    | + rollback(version)       |
    | + exportConfig()          |
    +---------------------------+

*/

class ConfigurationManager {

    // Singleton instance (only one object)
    private static ConfigurationManager instance;

    // Stores current configuration key–value pairs
    private Map<String, Object> configStore;

    // Holds previous snapshots (mementos)
    private List<Map<String, Object>> versions;

    // Lock for thread-safety during writes
    private ReentrantLock lock;

    // Private constructor → ensures Singleton
    private ConfigurationManager() {
        configStore = new HashMap<>();
        versions = new ArrayList<>();
        lock = new ReentrantLock();
    }

    // Public accessor for Singleton object
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager(); // create instance once
        }
        return instance;
    }

    // Read a configuration value
    public Object get(String key) {
        return configStore.get(key);
    }

    // Set or update a configuration key
    public void set(String key, Object value) {
        lock.lock();                  // lock for thread-safe write
        try {
            configStore.put(key, value); 
            saveVersion();            // create new version snapshot
        } finally {
            lock.unlock();            // release lock
        }
    }

    // Delete a key from config
    public void delete(String key) {
        lock.lock();
        try {
            if (configStore.containsKey(key)) {
                configStore.remove(key);
                saveVersion();
            }
        } finally {
            lock.unlock();
        }
    }

    // Create a deep snapshot (memento)
    private void saveVersion() {
        versions.add(new HashMap<>(configStore));  // shallow copy is enough
    }

    // Rollback to a previous version index
    public void rollback(int versionIndex) {
        lock.lock();
        try {
            if (versionIndex >= 0 && versionIndex < versions.size()) {
                configStore = new HashMap<>(versions.get(versionIndex));
                saveVersion();        // save rollback state as new version
            }
        } finally {
            lock.unlock();
        }
    }

    // Export entire configuration as copy
    public Map<String, Object> exportConfig() {
        return new HashMap<>(configStore); // return snapshot
    }

    // Display all versions (for demo)
    public void printVersions() {
        for (int i = 0; i < versions.size(); i++) {
            System.out.println("Version " + i + ": " + versions.get(i));
        }
    }
}


/* ======================= DEMO EXECUTION ========================== */

public class Main {
    public static void main(String[] args) {

        // Grab Singleton instance
        ConfigurationManager mgr = ConfigurationManager.getInstance();

        // Set initial configs
        mgr.set("theme", "dark");         // version 0
        mgr.set("timeout", 30);           // version 1
        mgr.set("volume", 80);            // version 2

        // Print all versions so far
        mgr.printVersions();

        // Update theme to create version 3
        mgr.set("theme", "light");
        System.out.println("\nAfter new theme:");
        mgr.printVersions();

        // Rollback to version 1
        mgr.rollback(1);
        System.out.println("\nAfter rollback to version 1:");
        System.out.println(mgr.exportConfig());
    }
}
