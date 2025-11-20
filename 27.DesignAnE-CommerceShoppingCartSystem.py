# Design a simple E-Commerce Shopping Cart System where users can add, remove, view, and 
# checkout products. Each product has an ID, name, and price, and the cart 
# should dynamically compute the total amount with thread-safe operations — 
# all implemented in a single Python file without using any database.

import threading

class Product:
    def __init__(self, pid, name, price):
        self.id = pid
        self.name = name
        self.price = price

class CartItem:
    def __init__(self, product, quantity = 1):
        self.product = product
        self.quantity = quantity

    def total_price(self):
        return self.product.price * self.quantity
    
class ShoppingCart:
    def __init__(self):
        self.items = {}
        self.lock = threading.Lock()

    def add_product(self, product, qty = 1):
        with self.lock:
            if product.id in self.items:
                self.items[product.id].quantity += qty
            else:
                self.items[product.id] = CartItem(product, qty)
            print(f"Added {qty} x {product.name} to cart.")

    def remove_product(self, product_id):
        with self.lock:
            if product_id in self.items:
                removed = self.items.pop(product_id)
                print(f"Removed {removed.product.name} from cart.")
            else:
                print("Product not found in cart.")

    def view_cart(self):
        print("\n Cart Contents:")
        if not self.items:
            print("Cart is empty.")
            return
        total = 0
        for item in self.items.values():
            cost = item.total_price()
            total += cost
            print(f"{item.product.name} x {item.quantity} = ${cost:.2f}")
        print(f"Total: ${total:.2f}\n")

    def checkout(self):
        with self.lock:
            total = sum(item.total_price() for item in self.items.values())
            print(f" Checkout Successful! Total Amount: ${total:.2f}")
            self.items.clear()

if __name__ == "__main__":
    # Create sample product objects
    p1 = Product(1, "Laptop", 1200.00)     # Product 1
    p2 = Product(2, "Headphones", 150.00)  # Product 2
    p3 = Product(3, "Mouse", 40.00)        # Product 3

    # Create an instance of ShoppingCart
    cart = ShoppingCart()

    # Add products to cart
    cart.add_product(p1, 1)  # Add 1 Laptop
    cart.add_product(p2, 2)  # Add 2 Headphones
    cart.add_product(p3, 3)  # Add 3 Mouse

    # View cart contents after additions
    cart.view_cart()

    # Remove product by ID (Headphones)
    cart.remove_product(2)

    # View updated cart contents
    cart.view_cart()

    # Proceed to checkout
    cart.checkout()

        
        
        

