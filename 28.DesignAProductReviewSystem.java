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

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

// ------------------------------------------------------------
// 1) REQUIREMENTS
// ------------------------------------------------------------

// Functional Requirements:
// - Users can add products and reviews.
// - Each review contains rating (1–5) and comment.
// - System shows all reviews and average rating for each product.

// Non-Functional Requirements:
// - Thread-safe review addition.
// - Pure in-memory model (no DB).
// - Modular, readable, and easily extendable.

// ------------------------------------------------------------
// 2) ALGORITHM & DESIGN PATTERN DISCUSSION
// ------------------------------------------------------------
// Algorithm: O(1) insertion and O(n) for average computation.
// Design Patterns:
// - Singleton-like ReviewSystem (single entry).
// - Observer-like Product reacting to added reviews.

// ------------------------------------------------------------
// 3) CONCURRENCY & DATA MODEL DISCUSSION
// ------------------------------------------------------------
// - ReentrantLock ensures thread-safe updates.
// - Map<Integer, Product> stores product list.
// - Each product maintains its reviews and total rating.

// ------------------------------------------------------------
// 4) UML DIAGRAM (TEXT REPRESENTATION)
// ------------------------------------------------------------
/*
              ┌────────────────────────────┐
              │        ReviewSystem        │
              │----------------------------│
              │ + products: Map            │
              │ + addProduct()             │
              │ + addReview()              │
              │ + showProductReviews()     │
              └───────────▲────────────────┘
                          │
              ┌───────────┴────────────┐
              │        Product         │
              │------------------------│
              │ + id                   │
              │ + name                 │
              │ + reviews: List        │
              │ + addReview()          │
              │ + averageRating()      │
              └───────────▲────────────┘
                          │
              ┌───────────┴────────────┐
              │        Review          │
              │------------------------│
              │ + userName             │
              │ + rating               │
              │ + comment              │
              └────────────────────────┘
*/

// ------------------------------------------------------------
// 5) CODE IMPLEMENTATION WITH LINE-BY-LINE EXPLANATION
// ------------------------------------------------------------

class Review {
    String userName;  // user name who wrote review
    int rating;       // rating between 1 to 5
    String comment;   // review comment text

    // Constructor initializes the review
    Review(String userName, int rating, String comment) {
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
    }
}

class Product {
    int id;                          // unique product id
    String name;                     // product name
    List<Review> reviews;            // list of reviews
    int totalRating;                 // sum of all ratings
    ReentrantLock lock;              // ensures thread safety

    // Constructor initializes a product with given id and name
    Product(int id, String name) {
        this.id = id;
        this.name = name;
        this.reviews = new ArrayList<>();
        this.totalRating = 0;
        this.lock = new ReentrantLock();
    }

    // Adds a new review safely with locking
    void addReview(Review review) {
        lock.lock();
        try {
            reviews.add(review);
            totalRating += review.rating;
        } finally {
            lock.unlock();
        }
    }

    // Calculates average rating safely
    double averageRating() {
        lock.lock();
        try {
            return reviews.isEmpty() ? 0.0 : Math.round((totalRating * 100.0) / reviews.size()) / 100.0;
        } finally {
            lock.unlock();
        }
    }

    // Returns all reviews in formatted list
    List<String> getAllReviews() {
        lock.lock();
        try {
            List<String> result = new ArrayList<>();
            for (Review r : reviews) {
                result.add(r.userName + ": " + r.rating + "/5 → " + r.comment);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }
}

class ReviewSystem {
    Map<Integer, Product> products; // all products stored here

    ReviewSystem() {
        products = new HashMap<>();
    }

    // Adds a product to system
    void addProduct(int id, String name) {
        if (!products.containsKey(id)) {
            products.put(id, new Product(id, name));
            System.out.println("✅ Product '" + name + "' added successfully.");
        }
    }

    // Adds a review to a specific product
    void addReview(int id, String user, int rating, String comment) {
        if (products.containsKey(id)) {
            Review review = new Review(user, rating, comment);
            products.get(id).addReview(review);
            System.out.println("💬 Review added for '" + products.get(id).name + "' by " + user + ".");
        }
    }

    // Displays product details with all reviews
    void showProductReviews(int id) {
        if (products.containsKey(id)) {
            Product p = products.get(id);
            System.out.println("\n📦 Product: " + p.name);
            System.out.println("⭐ Average Rating: " + p.averageRating());
            System.out.println("📝 Reviews:");
            for (String s : p.getAllReviews()) {
                System.out.println(" - " + s);
            }
        } else {
            System.out.println("❌ Product not found.");
        }
    }
}

// ------------------------------------------------------------
//  DEMO EXECUTION
// ------------------------------------------------------------
public class ProductReviewSystem {
    public static void main(String[] args) {

        // Create the review system
        ReviewSystem system = new ReviewSystem();

        // Add sample products
        system.addProduct(1, "MacBook Air M2");
        system.addProduct(2, "Noise Cancelling Headphones");

        // Add some reviews
        system.addReview(1, "Alice", 5, "Absolutely love it!");
        system.addReview(1, "Bob", 4, "Great performance.");
        system.addReview(2, "Charlie", 3, "Good but pricey.");

        // Display results
        system.showProductReviews(1);
        system.showProductReviews(2);
    }
}

/*
---------------- OUTPUT ----------------
✅ Product 'MacBook Air M2' added successfully.
✅ Product 'Noise Cancelling Headphones' added successfully.
💬 Review added for 'MacBook Air M2' by Alice.
💬 Review added for 'MacBook Air M2' by Bob.
💬 Review added for 'Noise Cancelling Headphones' by Charlie.

📦 Product: MacBook Air M2
⭐ Average Rating: 4.5
📝 Reviews:
 - Alice: 5/5 → Absolutely love it!
 - Bob: 4/5 → Great performance.

📦 Product: Noise Cancelling Headphones
⭐ Average Rating: 3.0
📝 Reviews:
 - Charlie: 3/5 → Good but pricey.
-----------------------------------------
*/

// ------------------------------------------------------------
// 6) LIMITATIONS OF CURRENT CODE
// ------------------------------------------------------------
// - No persistence (data lost after shutdown).
// - No product deletion or authentication.
// - Limited scalability due to in-memory storage.
// - CLI demo, not API-based.

// ------------------------------------------------------------
// 7) ALTERNATIVE ALGORITHMS & FUTURE DISCUSSIONS
// ------------------------------------------------------------
// - Use database for persistence (e.g., SQLite, PostgreSQL).
// - Implement caching (LRU) for top-rated products.
// - Add RESTful APIs for distributed use.
// - Introduce text indexing for fast search in comments.
// - Could integrate async queues for concurrent large review loads.
