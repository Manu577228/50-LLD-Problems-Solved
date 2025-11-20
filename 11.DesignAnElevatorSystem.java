/*
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
*/

import java.io.*;
import java.util.*;

/*
===================================================================
1) SYSTEM REQUIREMENTS
===================================================================

a) Functional Requirements
---------------------------
 - Elevator should handle multiple floor requests (up/down).
 - Should move sequentially in the direction of current requests before reversing.
 - Should allow user to request floors dynamically.
 - Should display current floor and direction in real time.

b) Non-Functional Requirements
-------------------------------
 - Must simulate real-time elevator movement (using sleep delay).
 - Single-file executable (no DB, no GUI).
 - Easily extendable for multiple elevators later.
 - Should be efficient and memory-friendly.
*/


/*
===================================================================
2) ALGORITHM CHOICE DISCUSSION
===================================================================
We use the SCAN (LOOK) scheduling algorithm:
 - Elevator serves all requests in one direction before reversing.
 - Balances fairness and minimizes total movement.
*/


/*
===================================================================
3) CONCURRENCY AND DATA MODEL DISCUSSION
===================================================================
Concurrency:
 - Movement simulated using a background thread to emulate asynchronous behavior.

Data Model:
 - Elevator class maintains current floor, direction, and request list.
 - Uses synchronized methods to avoid concurrency conflicts.
*/


/*
===================================================================
4) UML DIAGRAM (Text-based)
===================================================================

              +----------------------+
              |      Elevator        |
              +----------------------+
              | - currentFloor:int   |
              | - direction:String   |
              | - requests:List<Integer> |
              +----------------------+
              | + addRequest(int)    |
              | + move()             |
              | + run()              |
              +----------------------+
*/


/*
===================================================================
5) JAVA IMPLEMENTATION (With inline explanation)
===================================================================
*/

class Elevator implements Runnable {
    private int currentFloor;                // current elevator position
    private String direction;                // "UP" or "DOWN"
    private final List<Integer> requests;    // list of requested floors
    private final int totalFloors;           // total number of floors
    private volatile boolean running;        // controls thread execution

    public Elevator(int floors) {
        this.currentFloor = 0;
        this.direction = "UP";
        this.requests = new ArrayList<>();
        this.totalFloors = floors;
        this.running = true;
    }

    // Adds a new floor request if valid
    public synchronized void addRequest(int floor) {
        if (floor >= 0 && floor < totalFloors) {
            if (!requests.contains(floor)) {
                requests.add(floor);
                Collections.sort(requests);
                System.out.println("Request added for floor " + floor);
            }
        } else {
            System.out.println("Invalid floor request.");
        }
    }

    // Simulates elevator movement continuously
    private void move() {
        try {
            while (running) {
                if (requests.isEmpty()) {
                    Thread.sleep(1000);
                    continue;
                }

                int targetFloor = requests.get(0);

                // Determine direction and move accordingly
                if (currentFloor < targetFloor) {
                    direction = "UP";
                    currentFloor++;
                } else if (currentFloor > targetFloor) {
                    direction = "DOWN";
                    currentFloor--;
                } else {
                    System.out.println("Reached floor " + currentFloor + ". Doors opening...");
                    requests.remove(0);
                    Thread.sleep(1000); // simulate door open time
                    System.out.println("Doors closing...");
                    continue;
                }

                // Print elevator state
                System.out.println("Moving " + direction + " | Current Floor: " + currentFloor);
                Thread.sleep(700); // simulate movement delay
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Thread run method
    @Override
    public void run() {
        move();
    }

    // Stop simulation
    public void stop() {
        running = false;
    }
}


/*
===================================================================
DEMO RUN (Simple VS Code simulation)
===================================================================
*/

public class ElevatorSystem {
    public static void main(String[] args) throws Exception {
        Elevator elevator = new Elevator(10); // create elevator with 10 floors
        Thread elevatorThread = new Thread(elevator);
        elevatorThread.start(); // start elevator movement

        // Add some floor requests dynamically
        elevator.addRequest(3);
        elevator.addRequest(6);
        elevator.addRequest(2);

        // Let simulation run for 20 seconds
        Thread.sleep(20000);
        elevator.stop();
        System.out.println("Simulation ended.");
    }
}


/*
===================================================================
6) LIMITATIONS OF CURRENT CODE
===================================================================
 - No multi-elevator handling or request prioritization.
 - Simple SCAN logic (no optimization for direction grouping).
 - Thread safety is minimal.
 - Fixed timing; no adaptive delay or emergency controls.
 - No UI, only console-based simulation.
*/


/*
===================================================================
7) ALTERNATIVE ALGORITHMS & TRADE-OFFS
===================================================================
Algorithm     | Description                                   | Trade-off
--------------|-----------------------------------------------|-----------------------------
FCFS          | Serve requests in order received              | Simple but inefficient
SCAN / LOOK   | Move one direction then reverse (current)     | Balanced and realistic
SMART (AI)    | Predict optimal route with analytics          | Complex, computation-heavy
*/
