/* ----------------------------------------------------------------------------  
( The Authentic JS/JAVA CodeBuff )
 ___ _                      _              _ 
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                        |__/ 
-------------------------------------------------------------------------------
   Youtube : https://youtube.com/@code-with-Bharadwaj
   Github  : https://github.com/Manu577228
   Portfolio: https://manu-bharadwaj-portfolio.vercel.app/portfolio
-------------------------------------------------------------------------------
*/

import java.io.*;
import java.util.*;

/* ======================================================================
   1) REQUIREMENTS
   ======================================================================

   a) Functional Requirements:
      1. Control traffic lights (RED, YELLOW, GREEN) for each direction.
      2. Simulate automatic light cycling using timers.
      3. Allow emergency override to prioritize one direction.
      4. Log every state transition on console.
      5. Handle concurrency using threads for each light.

   b) Non-Functional Requirements:
      1. High reliability, simple maintainable design.
      2. Local, no external database.
      3. Thread-safe simulation for multiple lights.
      4. Pure console-based demo for VS Code execution.
*/

/* ======================================================================
   2) ALGORITHM CHOICE DISCUSSION
   ======================================================================

   Algorithm: Finite State Machine (FSM) + Timer Scheduling
   - Each TrafficLight cycles between GREEN → YELLOW → RED.
   - Controller manages all lights and can trigger emergency override.
   - Simple, time-driven system (deterministic).

   Complexity:
   - Time: O(n) per cycle (n = number of directions)
   - Space: O(n) for lights and states
*/

/* ======================================================================
   3) CONCURRENCY AND DATA MODEL DISCUSSION
   ======================================================================

   - Data Model:
       class TrafficLight {
           String name;
           String currentState;
           Map<String, Integer> durations;
       }

   - Concurrency:
       Each light runs as a thread (Runnable) cycling independently.
       Controller handles synchronization when override happens.
*/

/* ======================================================================
   4) UML DIAGRAM (Text-based)
   ======================================================================

              +------------------------+
              |   TrafficController    |
              |------------------------|
              | +lights: List          |
              | +startCycle()          |
              | +emergencyOverride()   |
              +-----------+------------+
                          |
                          |
              +-----------v-----------+
              |     TrafficLight      |
              |------------------------|
              | -name                 |
              | -currentState         |
              | -durations            |
              | +run()                |
              | +setState()           |
              +------------------------+
*/

/* ======================================================================
   5) JAVA IMPLEMENTATION WITH INLINE EXPLANATION
   ====================================================================== */

class TrafficLight implements Runnable {
    private String name;
    private String currentState;
    private final Map<String, Integer> durations;
    private volatile boolean running = true;

    TrafficLight(String name) {
        this.name = name;
        this.currentState = "RED";
        durations = new HashMap<>();
        durations.put("GREEN", 5000);  // 5 seconds
        durations.put("YELLOW", 2000); // 2 seconds
        durations.put("RED", 5000);    // 5 seconds
    }

    public void setState(String state) {
        currentState = state;
        System.out.println("[" + name + "] Light -> " + state);
    }

    @Override
    public void run() {
        try {
            while (running) {
                // Cycle: GREEN -> YELLOW -> RED
                for (String state : new String[]{"GREEN", "YELLOW", "RED"}) {
                    setState(state);
                    Thread.sleep(durations.get(state));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopLight() {
        running = false;
    }

    public String getName() {
        return name;
    }
}

class TrafficController {
    private final List<TrafficLight> lights = new ArrayList<>();
    private final List<Thread> threads = new ArrayList<>();

    public TrafficController() {
        lights.add(new TrafficLight("North"));
        lights.add(new TrafficLight("East"));
    }

    public void startCycle() {
        for (TrafficLight light : lights) {
            Thread t = new Thread(light);
            threads.add(t);
            t.start();
        }
    }

    public void emergencyOverride(String direction) {
        System.out.println("\n🚨 Emergency on " + direction + "! Giving priority...\n");
        for (TrafficLight light : lights) {
            if (light.getName().equals(direction))
                light.setState("GREEN");
            else
                light.setState("RED");
        }
    }

    public void stopAll() {
        for (TrafficLight light : lights)
            light.stopLight();
    }
}

/* ======================================================================
   MAIN EXECUTION (Demo Simulation)
   ====================================================================== */

public class Main {
    public static void main(String[] args) throws Exception {
        TrafficController controller = new TrafficController();
        controller.startCycle();

        // Wait few seconds then trigger emergency
        Thread.sleep(6000);
        controller.emergencyOverride("East");

        // Let simulation run for a short demo period
        Thread.sleep(10000);
        controller.stopAll();

        System.out.println("\n✅ Traffic simulation complete.");
    }
}

/* ======================================================================
   OUTPUT (Sample)
   ======================================================================

   [North] Light -> GREEN
   [East] Light -> GREEN
   [North] Light -> YELLOW
   [East] Light -> YELLOW
   🚨 Emergency on East! Giving priority...

   [North] Light -> RED
   [East] Light -> GREEN
   ✅ Traffic simulation complete.

   ====================================================================== */

/* ======================================================================
   6) LIMITATIONS OF CURRENT CODE
   ======================================================================

   1. No adaptive sensor input (static timing).
   2. No persistence or GUI visualization.
   3. Threads are unsynchronized; only demo-safe.
   4. Not optimized for large intersection networks.
*/

/* ======================================================================
   7) ALTERNATIVE ALGORITHMS & TRADE-OFFS
   ======================================================================

   | Algorithm              | Advantage                     | Trade-Off                 |
   |------------------------|--------------------------------|---------------------------|
   | Adaptive AI Control    | Smart, real-time decisions     | Needs training/data       |
   | Token Ring Scheduling  | Fair for multiple intersections| Slight delay propagation  |
   | Async Event-driven I/O | Efficient concurrency          | Complex design overhead   |
   | Priority-based FSM     | Emergency handling flexibility | Requires fine tuning      |
*/
