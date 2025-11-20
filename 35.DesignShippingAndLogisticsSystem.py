# Design a simple in-memory Shipping and Logistics System that can create shipments, 
# assign available delivery agents, calculate shipping costs, and track status updates. 
# The system must run fully in Python without any external database and 
# must support fast lookups using a tracking ID.

import threading
import uuid

class CostStrategy:
    def calculate(self, weight, distance):
        return 50 + (10 * weight) + (2 * distance)
    
class ShipmentFactory:
    @staticmethod
    def create(sender, receiver, origin, destination, weight, distance, strategy):
        tracking_id = str(uuid.uuid4())[:8]
        cost = strategy.calculate(weight, distance)
        return Shipment(tracking_id, sender, receiver, origin, destination, weight, distance, cost)

class Shipment:
    def __init__(self, tid, sender, receiver, origin, destination, weight, distance, cost):
        self.tracking_id = tid
        self.sender = sender
        self.receiver = receiver
        self.origin = origin
        self.destination = destination
        self.weight = weight
        self.distance = distance
        self.cost = cost
        self.status = "Pending"
        self.agent = None
        self.lock = threading.Lock()

    def update_status(self, new_status):
        with self.lock:
            self.status = new_status

class Agent:
    def __init__(self, aid, name):
        self.id = aid
        self.name = name
        self.available = True
        self.lock = threading.Lock()

    def assign(self):
        with self.lock:
            if self.available:
                self.available = False
                return True
            return False
        
    def release(self):
        with self.lock:
            self.available = True

class LogisticsManager:
    _instance = None
    _lock = threading.Lock()

    @staticmethod
    def getInstance():
        with LogisticsManager._lock:
            if LogisticsManager._instance is None:
                LogisticsManager._instance = LogisticsManager()
            return LogisticsManager._instance

    def __init__(self):
        self.shipments = {}
        self.agents = [
            Agent(1, "Agent A"),
            Agent(2, "Agent B")
        ]
        self.cost_strategy = CostStrategy()

    def createShipment(self, sender, receiver, origin, destination, weight, distance):
        shipment = ShipmentFactory.create(sender, receiver, origin, destination, weight, distance, self.cost_strategy)
        self.shipments[shipment.tracking_id] = shipment
        return shipment.tracking_id
    
    def assignAgent(self, trackingId):
        shipment = self.shipments.get(trackingId)

        for agent in self.agents:
            if agent.assign():
                shipment.agent = agent
                return agent.name
        return None
    
    def updateStatus(self, tracking_id, status):
        shipment = self.shipments.get(tracking_id)
        shipment.update_status(status)

    def getShipment(self, tracking_id):
        return self.shipments.get(tracking_id)
    
if __name__ == "__main__":
    manager = LogisticsManager.getInstance()   # Get singleton instance of logistics manager

    tid = manager.createShipment("Alice", "Bob", "NY", "LA", weight=10, distance=450)
    print("Tracking ID:", tid)                 # Print generated tracking ID

    assigned = manager.assignAgent(tid)        # Assign agent to shipment
    print("Assigned Agent:", assigned)

    manager.updateStatus(tid, "In-Transit")    # Update shipment status
    shipment = manager.getShipment(tid)        # Retrieve full shipment object

    print("Status:", shipment.status)          # Print updated status
    print("Cost:", shipment.cost)              # Print shipping cost




            


        

        
