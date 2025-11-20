# Design an Inventory Management System that can add items, update quantities, 
# fetch item details, delete items, and list all items using an 
# in-memory data structure. The system must support thread-safe 
# operations and maintain a single global inventory instance (Singleton).

import threading

class Item:
    def __init__(self, item_id, name, qty, price):
        self.id = item_id
        self.name = name
        self.quantity = qty
        self.price = price

    def __repr__(self):
        return f"[ID = {self.id}] {self.name} | Qty = {self.quantity} | Price = {self.price}"
    
class Inventory:
    _instance = None
    _lock = threading.Lock()

    def __init__(self):
        self.items = {}
        self.id_counter = 1
        self.lock = threading.Lock()

    @classmethod
    def get_instance(cls):
        if not cls._instance:
            with cls._lock:
                if not cls._instance:
                    cls._instance = Inventory()
        return cls._instance
    
    def add_item(self, name, qty, price):
        with self.lock:
            item_id = self.id_counter
            self.id_counter += 1

            self.items[item_id] = Item(item_id, name, qty, price)
            return item_id
        
    def update_quantity(self, item_id, new_qty):
        with self.lock:
            if item_id in self.items:
                self.items[item_id].quantity = new_qty
                return True
            return False
        
    def get_item(self, item_id):
        return self.items.get(item_id, None)
    
    def delete_item(self, item_id):
        with self.lock:
            return self.items.pop(item_id, None)
        
    def list_items(self):
        return list(self.items.values())
    
if __name__ == "__main__":
    inv = Inventory.get_instance()         # Get the single Inventory instance.

    # Adding items to inventory
    id1 = inv.add_item("Apple", 50, 2.5)   # Add Apple with qty 50, price 2.5
    id2 = inv.add_item("Banana", 100, 1.2) # Add Banana with qty 100, price 1.2

    print("Initial Items:")                # Show initial inventory.
    print(inv.list_items())

    # Update quantity of Apple
    inv.update_quantity(id1, 70)           # Change Apple qty from 50 → 70

    print("\nAfter Updating Apple Qty:")   # Show updated Apple
    print(inv.get_item(id1))

    # Delete Banana
    inv.delete_item(id2)

    print("\nAfter Removing Banana:")      # Show inventory after deletion
    print(inv.list_items())   

        
