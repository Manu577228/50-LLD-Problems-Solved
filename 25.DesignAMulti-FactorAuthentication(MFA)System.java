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
import java.security.*;
import java.util.concurrent.locks.*;
import java.time.*;

/**
-------------------------------------------------------------
Design: Multi-Factor Authentication (MFA) System (LLD)
-------------------------------------------------------------
*/

public class MultiFactorAuthSystem {

    // ============================================================
    // 1) REQUIREMENTS
    // ============================================================
    //
    // a) Functional Requirements:
    //    - User can register with username and password.
    //    - On login, verify password.
    //    - Generate OTP and store temporarily.
    //    - User must verify OTP within expiry window.
    //    - Grant access only if both steps succeed.
    //
    // b) Non-Functional Requirements:
    //    - In-memory simulation (no DB).
    //    - Secure SHA-256 password hashing.
    //    - Simple, thread-safe, extendable.

    // ============================================================
    // 2) ALGORITHM CHOICE DISCUSSION
    // ============================================================
    //
    // - Password verification: SHA-256
    // - OTP: 6-digit random + expiry timer
    // - Data: HashMap<User, Info>
    // - Time Complexity: O(1)
    // - Space Complexity: O(n)

    // ============================================================
    // 3) CONCURRENCY AND DATA MODEL DISCUSSION
    // ============================================================
    //
    // - ReentrantLock ensures atomic updates for multi-user operations.
    // - Model:
    //      users = { username : { passwordHash, otp, otpExpiry } }

    // ============================================================
    // 4) UML DIAGRAM (ASCII Representation)
    // ============================================================
    /*
                    +------------------------+
                    |      AuthSystem        |
                    +------------------------+
                    | + registerUser()       |
                    | + login()              |
                    | + verifyOtp()          |
                    +-----------+------------+
                                |
                                |
                        +-------v--------+
                        |    UserStore   |
                        +----------------+
                        | + users: Map   |
                        +----------------+
    */

    // ============================================================
    // 5) JAVA IMPLEMENTATION WITH EXPLANATION + OUTPUT
    // ============================================================

    private static class UserData {
        String passwordHash;
        int otp;
        long otpExpiry;
        UserData(String hash) { this.passwordHash = hash; }
    }

    private final Map<String, UserData> users = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String registerUser(String username, String password) {
        lock.lock();
        try {
            if (users.containsKey(username)) {
                return "User already exists.";
            }
            users.put(username, new UserData(hashPassword(password)));
            return "User '" + username + "' registered successfully.";
        } finally {
            lock.unlock();
        }
    }

    public String login(String username, String password) {
        lock.lock();
        try {
            UserData user = users.get(username);
            if (user == null) return "User not found.";
            if (!user.passwordHash.equals(hashPassword(password))) {
                return "Invalid password.";
            }
            int otp = new Random().nextInt(900000) + 100000;
            long expiry = System.currentTimeMillis() + 10000; // 10 seconds
            user.otp = otp;
            user.otpExpiry = expiry;
            System.out.println("[SYSTEM] OTP for " + username + ": " + otp + " (valid 10s)");
            return "Password verified. OTP sent.";
        } finally {
            lock.unlock();
        }
    }

    public String verifyOtp(String username, int otp) {
        lock.lock();
        try {
            UserData user = users.get(username);
            if (user == null || user.otp == 0) return "OTP not generated. Please login first.";
            if (System.currentTimeMillis() > user.otpExpiry) return "OTP expired.";
            if (user.otp != otp) return "Invalid OTP.";
            return "Access Granted to " + username + "!";
        } finally {
            lock.unlock();
        }
    }

    // ============================================================
    // DEMO EXECUTION (Simulated Output)
    // ============================================================

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        MultiFactorAuthSystem auth = new MultiFactorAuthSystem();

        System.out.println(auth.registerUser("bharadwaj", "secure123"));
        System.out.println(auth.login("bharadwaj", "secure123"));

        System.out.print("Enter received OTP: ");
        int otp = sc.nextInt();
        System.out.println(auth.verifyOtp("bharadwaj", otp));
        sc.close();
    }
}

/*
============================================================
✅ OUTPUT DEMO (Sample Run)
============================================================

User 'bharadwaj' registered successfully.
[SYSTEM] OTP for bharadwaj: 472189 (valid 10s)
Password verified. OTP sent.
Enter received OTP: 472189
Access Granted to bharadwaj!

============================================================
6) LIMITATIONS OF CURRENT CODE
============================================================

- OTP printed to console (not secure, real-world: SMS/Email).
- Data lost on restart (no DB persistence).
- Expiry depends on system time, no distributed clock sync.
- No brute-force protection for OTP retries.

============================================================
7) ALTERNATIVE ALGORITHMS AND TRADE-OFFs (FUTURE DISCUSSIONS)
============================================================

Feature         | Alternative                 | Trade-Off
------------------------------------------------------------
OTP Generation  | TOTP (RFC 6238)             | Secure, needs clock sync
Storage         | DB (Redis/Postgres)         | Persistent, adds complexity
Security Tokens | JWT + HMAC                  | Stateless, more setup needed
Concurrency     | ExecutorService / Async API | Faster, but complex threading

============================================================
*/
