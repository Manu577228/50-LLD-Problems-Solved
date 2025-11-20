# Design an in-memory Product Review System where users can add products and write reviews 
# (rating 1–5 with comments). The system should display all reviews and compute the average rating 
# for each product in a thread-safe manner without using any external database.

import threading

class Review:
    def __init__(self, user_name, rating, comment):
        self.user_name = user_name
        self.rating = rating
        self.comment = comment

class Product:
    def __init__(self, pid, name):
        self.id = pid
        self.name = name
        self.reviews = []
        self.total_rating = 0
        self.lock = threading.Lock()

    def add_review(self, review):
        with self.lock:
            self.reviews.append(review)
            self.total_rating += review.rating

    def average_rating(self):
        with self.lock:
            return round(self.total_rating / len(self.reviews), 2) if self.reviews else 0.0
        
    def get_all_reviews(self):
        with self.lock:
            return [(r.user_name, r.rating, r.comment) for r in self.reviews]
        
class ReviewSystem:
    def __init__(self):
        self.products = {}

    def add_product(self, pid, name):
        if pid not in self.products:
            self.products[pid] = Product(pid, name)
            print(f" Product '{name}' added successfully.")

    def add_review(self, pid, user_name, rating, comment):
        if pid in self.products:
            review = Review(user_name, rating, comment)
            self.products[pid].add_review(review)
            print(f" Review added for '{self.products[pid].name}' by {user_name}.")
    
    def show_product_reviews(self, pid):
        if pid in self.products:
            product = self.products[pid]
            print(f"\n📦 Product: {product.name}")
            print(f"⭐ Average Rating: {product.average_rating()}")
            print("📝 Reviews:")

            for user, rating, comment in product.get_all_reviews():
                print(f" - {user}: {rating}/5 → {comment}")
        else:
            print("❌ Product not found.")

if __name__ == "__main__":
    # Create ReviewSystem instance
    system = ReviewSystem()

    # Add two products
    system.add_product(1, "MacBook Air M2")
    system.add_product(2, "Noise Cancelling Headphones")

    # Add some reviews for products
    system.add_review(1, "Alice", 5, "Absolutely love it!")
    system.add_review(1, "Bob", 4, "Great performance.")
    system.add_review(2, "Charlie", 3, "Good but pricey.")

    # Display reviews and average ratings
    system.show_product_reviews(1)
    system.show_product_reviews(2)

        

        

