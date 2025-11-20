# Design a simple distributed counter system where multiple nodes maintain their own 
# partial counts and synchronize through a shared network. Each node updates only its own value, 
# broadcasts changes, and merges incoming states so that all nodes eventually 
# converge to the same final counter value.

class Node:
    def __init__(self, node_id, total_nodes, network):
        self.id = node_id
        self.counter = [0] * total_nodes
        self.network = network
        self.network.register(self)

    def increment(self):
        self.counter[self.id] += 1
        self.network.broadcast(self, self.counter)

    def decrement(self):
        self.counter[self.id] -= 1
        self.network.broadcast(self, self.counter)

    def merge(self, incoming):
        for i in range(len(self.counter)):
            self.counter[i] = max(self.counter[i], incoming[i])

    def get_value(self):
        return sum(self.counter)
    
class Network:
    def __init__(self):
        self.nodes = []

    def register(self, node):
        self.nodes.append(node)

    def broadcast(self, sender, state):
        for node in self.nodes:
            if node != sender:
                node.merge(state)

# Create a network object
net = Network()

# Create 3 distributed nodes that share the same network
A = Node(0, 3, net)  # Node A owns index 0
B = Node(1, 3, net)  # Node B owns index 1
C = Node(2, 3, net)  # Node C owns index 2

# Perform some distributed operations
A.increment()  # A adds +1 to its index
A.increment()  # A adds +1 again
B.increment()  # B adds +1
C.decrement()  # C adds -1 to its index

# Print final converged values across all nodes
print("A value:", A.get_value())  # Should match B and C
print("B value:", B.get_value())  # Should match A and C
print("C value:", C.get_value())  # Should match A and B
        
        
