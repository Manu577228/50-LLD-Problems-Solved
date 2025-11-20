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

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/* ============================================================
   1) FUNCTIONAL & NON-FUNCTIONAL REQUIREMENTS
   ------------------------------------------------------------
   Functional:
   - Add, remove, view, and checkout products.
   - Each product has ID, name, and price.
   - Display cart total dynamically.
   
   Non-Functional:
   - Thread-safe operations.
   - Single-file, no external DB.
   - Modular, simple and elegant for demo.
   ============================================================ */


/* ============================================================
   2) ALGORITHM CHOICE DISCUSSION
   ------------------------------------------------------------
   - Data Structure: HashMap<Integer, CartItem> for O(1) lookup.
   - Price Calculation: Simple summation (O(n)).
   - Catalog: In-memory List<Product>.
   - Scalable for DB integration in future.
   ============================================================ */


/* ============================================================
   3) CONCURRENCY & DATA MODEL DISCUSSION
   ------------------------------------------------------------
   Concurrency:
   - Using ReentrantLock for thread safety on add/remove operations.
   
   Data Model:
   - Product: id, name, price.
   - CartItem: product + quantity.
   - ShoppingCart: manages items and handles cart operations.
   ============================================================ */


/* ============================================================
   4) UML DIAGRAM (ASCII)
   ------------------------------------------------------------
        +----------------+
        |    Product     |
        +----------------+
        | id: int        |
        | name: String   |
        | price: double  |
        +----------------+

        +----------------+
        |   CartItem     |
        +----------------+
        | product: Product |
        | quantity: int   |
        +----------------+

        +-----------------------+
        |   ShoppingCart        |
        +-----------------------+
        | items: Map<Integer, CartItem> |
        +-----------------------+
        | addProduct()          |
        | removeProduct()       |
        | viewCart()            |
        | checkout()            |
        +-----------------------+
   ============================================================ */


/* ============================================================
   5) JAVA IMPLEMENTATION WITH STEP-BY-STEP EXPLANATION
   ============================================================ */

// Product class represents an item in the store.
class Product {
    int id;
    String name;
    double price;

    Product(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}

// CartItem holds a product and its quantity in the cart.
class CartItem {
    Product product;
    int quantity;

    CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Calculate total price for this item
    double getTotalPrice() {
        return product.price * quantity;
    }
}

// ShoppingCart class manages all cart operations.
class ShoppingCart {
    private final Map<Integer, CartItem> items = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock(); // ensures thread-safety

    // Add product to cart
    public void addProduct(Product p, int qty) {
        lock.lock();
        try {
            if (items.containsKey(p.id)) {
                items.get(p.id).quantity += qty;
            } else {
                items.put(p.id, new CartItem(p, qty));
            }
            System.out.println("Added " + qty + " x " + p.name + " to cart.");
        } finally {
            lock.unlock();
        }
    }

    // Remove product by ID
    public void removeProduct(int productId) {
        lock.lock();
        try {
            if (items.containsKey(productId)) {
                String name = items.get(productId).product.name;
                items.remove(productId);
                System.out.println("Removed " + name + " from cart.");
            } else {
                System.out.println("Product not found in cart.");
            }
        } finally {
            lock.unlock();
        }
    }

    // Display all cart items
    public void viewCart() {
        System.out.println("\n🛒 Cart Contents:");
        if (items.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }
        double total = 0;
        for (CartItem item : items.values()) {
            double cost = item.getTotalPrice();
            total += cost;
            System.out.printf("%s x %d = $%.2f%n", item.product.name, item.quantity, cost);
        }
        System.out.printf("Total: $%.2f%n%n", total);
    }

    // Checkout and clear the cart
    public void checkout() {
        lock.lock();
        try {
            double total = 0;
            for (CartItem item : items.values()) {
                total += item.getTotalPrice();
            }
            System.out.printf("✅ Checkout Successful! Total Amount: $%.2f%n", total);
            items.clear();
        } finally {
            lock.unlock();
        }
    }
}


/* ============================================================
   6) DEMO SIMULATION (MAIN METHOD)
   ============================================================ */

public class ECommerceShoppingCartDemo {
    public static void main(String[] args) {
        // Step 1: Create sample products
        Product laptop = new Product(1, "Laptop", 1200.00);
        Product headphones = new Product(2, "Headphones", 150.00);
        Product mouse = new Product(3, "Mouse", 40.00);

        // Step 2: Create shopping cart
        ShoppingCart cart = new ShoppingCart();

        // Step 3: Perform operations
        cart.addProduct(laptop, 1);
        cart.addProduct(headphones, 2);
        cart.addProduct(mouse, 3);
        cart.viewCart();

        // Step 4: Remove product
        cart.removeProduct(2);
        cart.viewCart();

        // Step 5: Checkout
        cart.checkout();
    }
}


/* ============================================================
   🧠 EXPLANATION WITH OUTPUT (FOR YOUTUBE DEMO)
   ------------------------------------------------------------
   - Creates 3 products: Laptop, Headphones, Mouse
   - Adds them to cart and displays contents.
   - Removes Headphones, then checks out.
   ------------------------------------------------------------
   🧾 Sample Output:
   Added 1 x Laptop to cart.
   Added 2 x Headphones to cart.
   Added 3 x Mouse to cart.

   🛒 Cart Contents:
   Laptop x 1 = $1200.00
   Headphones x 2 = $300.00
   Mouse x 3 = $120.00
   Total: $1620.00

   Removed Headphones from cart.

   🛒 Cart Contents:
   Laptop x 1 = $1200.00
   Mouse x 3 = $120.00
   Total: $1320.00

   ✅ Checkout Successful! Total Amount: $1320.00
   ============================================================ */


/* ============================================================
   7) LIMITATIONS & FUTURE ENHANCEMENTS
   ------------------------------------------------------------
   Limitations:
   - No DB persistence (in-memory only)
   - No user authentication or sessions
   - No discounts/tax applied
   - Not optimized for large product catalogs

   Future Enhancements:
   - Integrate small DB (e.g., SQLite)
   - Add tax & coupon logic
   - Multi-user support
   - Integrate payment gateway simulation
   ============================================================ */
