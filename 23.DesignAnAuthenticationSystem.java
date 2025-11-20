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
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio        */
/* -----------------------------------------------------------------------  */

import java.util.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// -------------------- USER CLASS --------------------
class User {
    private String username;        // Stores the username
    private String passwordHash;    // Stores SHA-256 hashed password
    private boolean isLoggedIn;     // Indicates login status

    // Constructor to initialize new user
    public User(String username, String password) {
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.isLoggedIn = false;
    }

    // Method to hash password using SHA-256 algorithm
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); // Creates hash generator
            byte[] bytes = md.digest(password.getBytes());            // Hashes password into bytes
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b)); // Converts byte to hex string
            }
            return sb.toString(); // Returns hashed password
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Verifies user password during login
    public boolean verifyPassword(String password) {
        return this.passwordHash.equals(hashPassword(password));
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean status) {
        this.isLoggedIn = status;
    }
}

// -------------------- AUTH SYSTEM CLASS --------------------
class AuthSystem {
    private final Map<String, User> users;    // Stores username -> User object
    private final Map<String, Long> sessions; // Stores username -> login timestamp
    private final Object lock = new Object(); // Used for thread safety

    // Constructor initializes empty user and session maps
    public AuthSystem() {
        users = new HashMap<>();
        sessions = new HashMap<>();
    }

    // Registers new user
    public boolean register(String username, String password) {
        synchronized (lock) { // Thread-safe block
            if (users.containsKey(username)) {
                System.out.println("❌ Username '" + username + "' already exists.");
                return false;
            }
            users.put(username, new User(username, password)); // Adds user to map
            System.out.println("✅ User '" + username + "' registered successfully.");
            return true;
        }
    }

    // Logs in existing user
    public boolean login(String username, String password) {
        synchronized (lock) { // Thread-safe block
            User user = users.get(username);
            if (user == null) {
                System.out.println("❌ User not found.");
                return false;
            }
            if (!user.verifyPassword(password)) {
                System.out.println("❌ Invalid password.");
                return false;
            }
            user.setLoggedIn(true); // Sets user login status
            sessions.put(username, System.currentTimeMillis()); // Adds to active sessions
            System.out.println("✅ User '" + username + "' logged in successfully.");
            return true;
        }
    }

    // Logs out an active user
    public boolean logout(String username) {
        synchronized (lock) {
            User user = users.get(username);
            if (user != null && user.isLoggedIn()) {
                user.setLoggedIn(false);
                sessions.remove(username);
                System.out.println("👋 User '" + username + "' logged out.");
                return true;
            }
            System.out.println("❌ No active session found for '" + username + "'.");
            return false;
        }
    }

    // Displays all active users
    public void showActiveUsers() {
        synchronized (lock) {
            System.out.println("🟢 Active Users: " + sessions.keySet());
        }
    }
}

// -------------------- DEMO MAIN CLASS --------------------
public class AuthenticationSystemDemo {

    public static void main(String[] args) throws IOException {
        // Using BufferedReader for fast console input (demo simplicity)
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        AuthSystem auth = new AuthSystem();

        // Sample flow for demo output
        auth.register("bharadwaj", "codebuff123");
        auth.register("manu", "password");
        auth.login("bharadwaj", "codebuff123");
        auth.login("manu", "wrongpass");
        auth.showActiveUsers();
        auth.logout("bharadwaj");
        auth.showActiveUsers();

        // Uncomment below for interactive console mode
        /*
        while (true) {
            System.out.println("\n1.Register  2.Login  3.Logout  4.Show Active  5.Exit");
            System.out.print("Choose: ");
            int choice = Integer.parseInt(br.readLine());
            if (choice == 5) break;

            System.out.print("Username: ");
            String user = br.readLine();
            System.out.print("Password: ");
            String pass = br.readLine();

            switch (choice) {
                case 1 -> auth.register(user, pass);
                case 2 -> auth.login(user, pass);
                case 3 -> auth.logout(user);
                case 4 -> auth.showActiveUsers();
                default -> System.out.println("Invalid option!");
            }
        }
        */
    }
}
