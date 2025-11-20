// --------------------------- WORKFLOW / STATE MACHINE SYSTEM ---------------------------

// Import required utilities
import java.util.*;

// ======================== STATE CLASS ========================
class State {
    String name; // name of the state

    // Constructor - sets the state's name
    State(String name) {
        this.name = name; // Stores the name
    }
}

// ===================== WORKFLOW DEFINITION =====================
class WorkflowDefinition {

    // Map of stateName -> State object
    Map<String, State> states = new HashMap<>();

    // Map of stateName -> list of allowed next states
    Map<String, List<String>> transitions = new HashMap<>();

    // Add a state to the definition
    void addState(String name) {
        states.put(name, new State(name)); // Create and store the state
    }

    // Add allowed transition
    void addTransition(String from, String to) {
        transitions.putIfAbsent(from, new ArrayList<>()); // Ensure list exists
        transitions.get(from).add(to); // Add the transition
    }

    // Validate a transition: check if "to" exists in allowed list
    boolean isValidTransition(String from, String to) {
        return transitions.getOrDefault(from, Collections.emptyList()).contains(to);
    }
}

// ======================== WORKFLOW INSTANCE ========================
class WorkflowInstance {

    WorkflowDefinition definition;     // Reference to workflow structure
    State currentState;               // Current state
    List<String> history;             // List of visited states
    final Object lock = new Object(); // Lock for concurrency

    // Constructor - sets starting state and initializes history
    WorkflowInstance(WorkflowDefinition def, String startState) {
        this.definition = def;                    // Save definition
        this.currentState = def.states.get(startState); // Set initial state
        this.history = new ArrayList<>();         // Initialize history list
        this.history.add(startState);             // Add start state to history
    }

    // Attempt to move to another state
    void moveTo(String next) {
        synchronized (lock) { // Thread safety
            // Check if transition is allowed
            if (definition.isValidTransition(currentState.name, next)) {
                currentState = definition.states.get(next); // Update state
                history.add(next);                          // Save in history
                System.out.println("Transitioned to: " + next);
            } else {
                System.out.println("Invalid Transition: " + currentState.name + " → " + next);
            }
        }
    }

    // Return list of possible next states
    List<String> getAvailableStates() {
        return definition.transitions.getOrDefault(currentState.name, Collections.emptyList());
    }

    // Return full transition history
    List<String> getHistory() {
        return history;
    }
}

// ============================== DEMO MAIN CLASS ==============================
public class WorkflowSystem {

    public static void main(String[] args) {

        // 1) Create workflow definition
        WorkflowDefinition wf = new WorkflowDefinition();

        // Add states
        for (String s : Arrays.asList("START", "IN_REVIEW", "APPROVED", "REJECTED")) {
            wf.addState(s); // Store each state
        }

        // Define transitions
        wf.addTransition("START", "IN_REVIEW");     // START → IN_REVIEW
        wf.addTransition("IN_REVIEW", "APPROVED");  // IN_REVIEW → APPROVED
        wf.addTransition("IN_REVIEW", "REJECTED");  // IN_REVIEW → REJECTED

        // 2) Create workflow instance starting at "START"
        WorkflowInstance inst = new WorkflowInstance(wf, "START");

        // Print available next moves
        System.out.println("Available next: " + inst.getAvailableStates());

        // Perform transitions
        inst.moveTo("IN_REVIEW");
        System.out.println("Available next: " + inst.getAvailableStates());
        inst.moveTo("APPROVED");

        // Print full history
        System.out.println("History: " + inst.getHistory());
    }
}
