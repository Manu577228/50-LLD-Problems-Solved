# Design and implement a Traffic Control System that simulates multiple 
# traffic lights operating concurrently.
# The system should automatically cycle through light states (RED, YELLOW, GREEN) and 
# handle emergency overrides giving priority to a selected direction — 
# all within a console-based, single-file Python demo.

import time, threading

class TrafficLight:
    def __init__(self, name):
        self.name = name
        self.current_state = "RED"
        self.durations = {"GREEN": 5, "YELLOW": 2, "RED": 5}
        self.running = True

    def set_state(self, state):
        self.current_state = state
        print(f"[{self.name}] Light -> {state}")

    def run_cycle(self):
        while self.running:
            for state in ["GREEN", "YELLOW", "RED"]:
                self.set_state(state)
                time.sleep(self.durations[state])

class TrafficController:
    def __init__(self):
        self.lights = [
            TrafficLight("North"),
            TrafficLight("East")
        ]
        self.threads = []

    def start_cycle(self):
        for l in self.lights:
            t = threading.Thread(target = l.run_cycle)
            self.threads.append(t)
            t.start()

    def emergency_override(self, direction):
        print(f"\n🚨 Emergency on {direction}! Giving Priority...\n")
        for light in self.lights:
            if light.name == direction:
                light.set_state("GREEN")
            else:
                light.set_state("RED")

if __name__ == "__main__":
    controller = TrafficController()
    controller.start_cycle()

    # simulate an emergency override
    time.sleep(6)
    controller.emergency_override("East")

    # demo runtime for a short period
    time.sleep(10)
    for l in controller.lights:
        l.running = False
    print("\n✅ Traffic simulation complete.")
        

