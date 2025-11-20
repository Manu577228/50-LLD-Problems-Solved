# Design a lightweight workflow/state-machine system that supports defining states, 
# allowed transitions, and executing workflow instances. The system must validate 
# transitions, maintain history, ensure thread safety, and run entirely 
# in a single Python file without any external database.

import threading

class State:
    def __init__(self, name):
        self.name = name

class WorkflowDefinition:
    def __init__(self):
        self.states = {}
        self.transitions = {}

    def add_state(self, name):
        self.states[name] = State(name)

    def add_transition(self, from_state, to_state):
        if from_state not in self.transitions:
            self.transitions[from_state] = []
        self.transitions[from_state].append(to_state)

    def is_valid_transition(self, from_state, to_state):
        return to_state in self.transitions.get(from_state, [])
    
class WorkFlowInstance:
    def __init__(self, definition, start_state):
        self.definition = definition
        self.current_state = definition.states[start_state]
        self.history = [start_state]
        self.lock = threading.Lock()

    def move_to(self, next_state_name):
        with self.lock:
            if self.definition.is_valid_transition(self.current_state.name, next_state_name):
                self.current_state = self.definition.states[next_state_name]
                self.history.append(next_state_name)
                print(f"Transitioned to: {next_state_name}")
            else:
                print(f"Invalid Transition: {next_state_name}")

    def get_available_states(self):
        return self.definition.transitions.get(self.current_state.name, [])
    
    def get_history(self):
        return self.history
    
if __name__ == "__main__":

    # Step 1: Create workflow definition
    w = WorkflowDefinition()

    # Add states to the workflow
    for s in ["START", "IN_REVIEW", "APPROVED", "REJECTED"]:
        w.add_state(s)                            # Register each state

    # Define allowed transitions (state → next possible states)
    w.add_transition("START", "IN_REVIEW")        # START → IN_REVIEW
    w.add_transition("IN_REVIEW", "APPROVED")     # IN_REVIEW → APPROVED
    w.add_transition("IN_REVIEW", "REJECTED")     # IN_REVIEW → REJECTED

    # Step 2: Create a workflow instance beginning at "START"
    inst = WorkFlowInstance(w, "START")

    # Step 3: Demo transitions
    print("\nAvailable next:", inst.get_available_states())  # Show next possible moves

    inst.move_to("IN_REVIEW")                                # Move to IN_REVIEW
    print("Available next:", inst.get_available_states())     # Display next possible moves

    inst.move_to("APPROVED")                                  # Move to APPROVED (valid)

    # Step 4: End by printing the transition history
    print("\nTransition History:", inst.get_history())
        

    
        

