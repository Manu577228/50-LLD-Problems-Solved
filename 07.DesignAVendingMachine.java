/* ----------------------------------------------------------------------------  */
/*   ( The Authentic JS/JAVA CodeBuff )
 ___ _                      _              _ 
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                        |__/ 
/* --------------------------------------------------------------------------   */
/*    Youtube: https://youtube.com/@code-with-Bharadwaj                        */
/*    Github : https://github.com/Manu577228                                  */
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio      */
/* -----------------------------------------------------------------------  */

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

class Product {
    String code;
    String name;
    int pricePaise;
    int qty;

    Product(String code, String name, int pricePaise, int qty) {
        this.code = code;
        this.name = name;
        this.pricePaise = pricePaise;
        this.qty = qty;
    }

    public String toString() {
        return code + ": " + name + " - ₹" + (pricePaise / 100.0) + " (" + qty + " left)";
    }
}

class Inventory {
    private final Map<String, Product> products = new HashMap<>();

    public void addProduct(Product p) {
        products.putIfAbsent(p.code, p);
        if (products.containsKey(p.code)) {
            Product existing = products.get(p.code);
            existing.qty += p.qty;
        }
    }

    public Product getProduct(String code) {
        return products.get(code);
    }

    public Collection<Product> listProducts() {
        return products.values();
    }
}

class CashBox {
    private final Map<Integer, Integer> coins = new HashMap<>();
    private final List<Integer> denominations;

    CashBox(List<Integer> denominations) {
        this.denominations = new ArrayList<>(denominations);
        for (int d : denominations) coins.put(d, 0);
        this.denominations.sort(Collections.reverseOrder());
    }

    public void addCoins(int denom, int count) {
        coins.put(denom, coins.getOrDefault(denom, 0) + count);
    }

    public int totalPaise() {
        return coins.entrySet().stream().mapToInt(e -> e.getKey() * e.getValue()).sum();
    }

    public boolean makeChange(int amountPaise, Map<Integer, Integer> changeMap) {
        Map<Integer, Integer> temp = new HashMap<>(coins);
        int remaining = amountPaise;
        for (int d : denominations) {
            if (remaining <= 0) break;
            int need = remaining / d;
            int take = Math.min(need, temp.getOrDefault(d, 0));
            if (take > 0) {
                changeMap.put(d, take);
                temp.put(d, temp.get(d) - take);
                remaining -= d * take;
            }
        }
        if (remaining != 0) return false;
        for (Map.Entry<Integer, Integer> e : changeMap.entrySet()) {
            coins.put(e.getKey(), coins.get(e.getKey()) - e.getValue());
        }
        return true;
    }

    public void acceptPayment(Map<Integer, Integer> payment) {
        for (Map.Entry<Integer, Integer> e : payment.entrySet()) {
            coins.put(e.getKey(), coins.getOrDefault(e.getKey(), 0) + e.getValue());
        }
    }
}

class VendingMachine {
    private final Inventory inventory = new Inventory();
    private final CashBox cashbox;
    private final ReentrantLock lock = new ReentrantLock();
    private final List<Integer> denoms = Arrays.asList(10000, 5000, 2000, 1000, 500, 100, 50, 20, 10, 5, 1);

    VendingMachine() {
        this.cashbox = new CashBox(denoms);
        seedDemo();
    }

    private void seedDemo() {
        inventory.addProduct(new Product("A1", "Water Bottle", 300, 10));
        inventory.addProduct(new Product("A2", "Soda Can", 450, 5));
        inventory.addProduct(new Product("B1", "Chips", 150, 7));

        Map<Integer, Integer> initial = Map.of(100, 30, 50, 20, 20, 30, 10, 50, 5, 100, 1, 200);
        for (Map.Entry<Integer, Integer> e : initial.entrySet()) {
            cashbox.addCoins(e.getKey(), e.getValue());
        }
    }

    public void listProducts(PrintWriter out) {
        for (Product p : inventory.listProducts()) out.println(p);
    }

    public void restock(String code, int qty, PrintWriter out) {
        lock.lock();
        try {
            Product p = inventory.getProduct(code);
            if (p == null) {
                out.println("Product not found.");
                return;
            }
            p.qty += qty;
            out.println("Restocked " + code + ". New qty: " + p.qty);
        } finally {
            lock.unlock();
        }
    }

    public void refillCoins(int denom, int count, PrintWriter out) {
        lock.lock();
        try {
            cashbox.addCoins(denom, count);
            out.println("Refilled " + count + " coins of " + denom + " paise.");
        } finally {
            lock.unlock();
        }
    }

    public void purchase(String code, Map<Integer, Integer> payment, PrintWriter out) {
        int total = payment.entrySet().stream().mapToInt(e -> e.getKey() * e.getValue()).sum();
        lock.lock();
        try {
            Product p = inventory.getProduct(code);
            if (p == null) {
                out.println("FAILED: Product not found.");
                return;
            }
            if (p.qty <= 0) {
                out.println("FAILED: Out of stock.");
                return;
            }
            int price = p.pricePaise;
            if (total < price) {
                out.println("FAILED: Insufficient money. Inserted ₹" + (total / 100.0) + ", price ₹" + (price / 100.0));
                return;
            }
            int changeNeeded = total - price;
            cashbox.acceptPayment(payment);
            Map<Integer, Integer> changeMap = new LinkedHashMap<>();
            if (!cashbox.makeChange(changeNeeded, changeMap)) {
                out.println("FAILED: Cannot provide change. Transaction canceled.");
                return;
            }
            p.qty--;
            out.println("Dispensed: " + p.name);
            if (changeMap.isEmpty()) out.println("No change.");
            else {
                out.println("Change returned:");
                for (Map.Entry<Integer, Integer> e : changeMap.entrySet())
                    out.println("  " + e.getKey() + "p x " + e.getValue());
            }
        } finally {
            lock.unlock();
        }
    }
}

public class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);
        VendingMachine vm = new VendingMachine();

        out.println("Welcome to Demo Vending Machine (values in ₹).");
        while (true) {
            out.println("\n-- Menu --");
            out.println("1) List products");
            out.println("2) Purchase");
            out.println("3) Restock (admin)");
            out.println("4) Refill coins (admin)");
            out.println("5) Exit");
            out.print("Choose: ");
            out.flush();
            String ch = br.readLine();
            if (ch.equals("1")) {
                vm.listProducts(out);
            } else if (ch.equals("2")) {
                vm.listProducts(out);
                out.print("Enter product code: ");
                out.flush();
                String code = br.readLine().trim();
                out.println("Enter payment as 'denom_in_paise:count' (e.g. 500:1,100:2):");
                String entry = br.readLine();
                Map<Integer, Integer> payment = new HashMap<>();
                for (String part : entry.split(",")) {
                    if (part.trim().isEmpty()) continue;
                    String[] kv = part.split(":");
                    int d = Integer.parseInt(kv[0].trim());
                    int c = Integer.parseInt(kv[1].trim());
                    payment.put(d, payment.getOrDefault(d, 0) + c);
                }
                vm.purchase(code, payment, out);
            } else if (ch.equals("3")) {
                out.print("Product code: "); out.flush();
                String code = br.readLine().trim();
                out.print("Quantity: "); out.flush();
                int q = Integer.parseInt(br.readLine().trim());
                vm.restock(code, q, out);
            } else if (ch.equals("4")) {
                out.print("Denomination (paise): "); out.flush();
                int denom = Integer.parseInt(br.readLine().trim());
                out.print("Count: "); out.flush();
                int count = Integer.parseInt(br.readLine().trim());
                vm.refillCoins(denom, count, out);
            } else if (ch.equals("5")) {
                out.println("Bye.");
                break;
            } else out.println("Invalid choice.");
        }
    }
}
