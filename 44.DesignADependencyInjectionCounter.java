/* --------------------------------------------------------------------------
    Dependency Injection Counter - Single File Java Demo
    (LLD Compliant + Line-by-Line Explanation)
 -------------------------------------------------------------------------- */

interface IStorage {
    int getValue();      // gets current stored value
    void setValue(int v); // sets updated value
}

/* --------------------------------------------------------------------------
    In-Memory Storage implementing IStorage
 -------------------------------------------------------------------------- */
class InMemoryStorage implements IStorage {

    private int val;  // stores counter value

    public InMemoryStorage() {
        this.val = 0; // initialize storage with zero
    }

    @Override
    public int getValue() {
        return this.val; // return stored number
    }

    @Override
    public void setValue(int v) {
        this.val = v; // update stored number
    }
}

/* --------------------------------------------------------------------------
    Counter class depends on IStorage (Dependency Injection)
 -------------------------------------------------------------------------- */
class Counter {

    private final IStorage storage; // reference to injected storage

    public Counter(IStorage storage) {
        this.storage = storage; // DI via constructor
    }

    public void increment() {
        int current = storage.getValue(); // read value from storage
        storage.setValue(current + 1);    // write updated value back
    }

    public int value() {
        return storage.getValue(); // return current value from storage
    }
}

/* --------------------------------------------------------------------------
    MAIN DEMO (Everything running in a single file)
 -------------------------------------------------------------------------- */
public class Main {

    public static void main(String[] args) {

        IStorage storage = new InMemoryStorage(); // Dependency Injection
        Counter counter = new Counter(storage);   // Injected into Counter

        counter.increment();  // +1
        counter.increment();  // +1
        counter.increment();  // +1

        System.out.println("Final Counter Value: " + counter.value());
        // Expected Output: 3
    }
}
