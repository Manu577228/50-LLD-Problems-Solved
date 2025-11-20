import java.util.*;

class Node {
    private int id;
    private int[] counter;
    private Network network;

    public Node(int id, int totalNodes, Network network) {
        this.id = id;
        this.counter = new int[totalNodes];
        this.network = network;
        this.network.register(this);
    }

    public void increment() {
        counter[id]++;
        network.broadcast(this, counter);
    }

    public void decrement() {
        counter[id]--;
        network.broadcast(this, counter);
    }

    public void merge(int[] incoming) {
        for (int i = 0; i < counter.length; i++) {
            counter[i] = Math.max(counter[i], incoming[i]);
        }
    }

    public int getValue() {
        int sum = 0;
        for (int x : counter) sum += x;
        return sum;
    }

    public int[] getState() {
        return counter.clone();
    }
}

class Network {
    private List<Node> nodes = new ArrayList<>();

    public void register(Node node) {
        nodes.add(node);
    }

    public void broadcast(Node sender, int[] state) {
        for (Node n : nodes) {
            if (n != sender) {
                n.merge(state);
            }
        }
    }
}

public class DistributedCounterDemo {
    public static void main(String[] args) {
        Network net = new Network();

        Node A = new Node(0, 3, net);
        Node B = new Node(1, 3, net);
        Node C = new Node(2, 3, net);

        A.increment();
        A.increment();
        B.increment();
        C.decrement();

        System.out.println("A value: " + A.getValue());
        System.out.println("B value: " + B.getValue());
        System.out.println("C value: " + C.getValue());
    }
}
