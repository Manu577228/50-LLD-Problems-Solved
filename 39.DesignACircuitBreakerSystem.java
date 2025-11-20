/* --------------------- CIRCUIT BREAKER (JAVA DEMO) --------------------------
   Fully self-contained, no DB, runnable in VS Code.
   Includes line-by-line explanation for YouTube teaching.
---------------------------------------------------------------------------- */

import java.util.function.Supplier;

// ----------------------------------------
// 1. Enum representing the 3 states
// ----------------------------------------
enum State {
    CLOSED,        // Normal state
    OPEN,          // Reject calls
    HALF_OPEN      // Allow one test call
}

// ----------------------------------------
// 2. Circuit Breaker Implementation
// ----------------------------------------
class CircuitBreaker {

    private final int failureThreshold;          // Number of failures allowed
    private final long recoveryTimeoutMillis;    // Time before trying HALF-OPEN

    private int failureCount = 0;                // Track failures
    private State currentState = State.CLOSED;   // Start in CLOSED
    private long lastFailureTime = 0;            // Track when OPEN occurred

    // Constructor
    public CircuitBreaker(int failureThreshold, long recoveryTimeoutMillis) {
        this.failureThreshold = failureThreshold;          // Set allowed failures
        this.recoveryTimeoutMillis = recoveryTimeoutMillis; // Cooldown time
    }

    // ----------------------------------------
    // 3. Move to OPEN state
    // ----------------------------------------
    private synchronized void moveToOpen() {
        currentState = State.OPEN;                  // Change state to OPEN
        lastFailureTime = System.currentTimeMillis(); // Record time of failure
        System.out.println("➡️ STATE CHANGE → OPEN");
    }

    // ----------------------------------------
    // 4. Move to HALF-OPEN state
    // ----------------------------------------
    private synchronized void moveToHalfOpen() {
        currentState = State.HALF_OPEN;   // Move to HALF-OPEN
        System.out.println("➡️ STATE CHANGE → HALF-OPEN");
    }

    // ----------------------------------------
    // 5. Move to CLOSED state
    // ----------------------------------------
    private synchronized void moveToClosed() {
        currentState = State.CLOSED;      // Reset to CLOSED
        failureCount = 0;                 // Reset failures
        System.out.println("➡️ STATE CHANGE → CLOSED");
    }

    // ----------------------------------------
    // 6. Should we allow the call?
    // ----------------------------------------
    private synchronized boolean canPass() {
        if (currentState == State.CLOSED) {
            return true;                                  // Normal operation
        }
        if (currentState == State.OPEN) {
            long now = System.currentTimeMillis();        // Check time
            if (now - lastFailureTime >= recoveryTimeoutMillis) {
                moveToHalfOpen();                         // Try HALF-OPEN
                return true;
            }
            return false;                                 // Still OPEN
        }
        return true;  // HALF-OPEN → allow exactly one call
    }

    // ----------------------------------------
    // 7. The main call wrapper
    // ----------------------------------------
    public synchronized String call(Supplier<String> externalService) {

        if (!canPass()) {                                // If OPEN → block
            System.out.println("❌ Request BLOCKED (OPEN)");
            return "BLOCKED";
        }

        try {
            String result = externalService.get();       // Execute service

            if (currentState == State.HALF_OPEN) {       // If test succeeded
                moveToClosed();
            }

            failureCount = 0;                            // Reset failure count
            return result;                               // Return success

        } catch (Exception e) {
            failureCount++;                              // Track failure
            System.out.println("⚠️ Failure #" + failureCount);

            if (failureCount >= failureThreshold) {      // Exceeded limit?
                moveToOpen();
            }
            return "ERROR: " + e.getMessage();           // Return error
        }
    }
}

// ----------------------------------------
// 8. Simulated external service
// ----------------------------------------
class FlakyService {
    private int counter = 0; // fails first 3 times

    public String execute() {
        counter++;
        if (counter <= 3) {
            throw new RuntimeException("Service Down"); // Fail first 3 calls
        }
        return "SUCCESS";                              // After that → success
    }
}

// ----------------------------------------
// 9. DEMO main function
// ----------------------------------------
public class Main {
    public static void main(String[] args) throws Exception {

        CircuitBreaker cb = new CircuitBreaker(3, 3000); // 3 failures, 3 secs
        FlakyService service = new FlakyService();        // Create test service

        System.out.println("\n----- DEMO START -----\n");

        for (int i = 1; i <= 8; i++) {
            System.out.print("Call " + i + " → ");
            String result = cb.call(() -> service.execute());
            System.out.println(result);
            Thread.sleep(1000); // 1 second interval
        }

        System.out.println("\n----- DEMO END -----");
    }
}
