# Design a Restaurant Order Management System that allows adding menu items, placing and 
# tracking customer orders, updating their statuses, and calculating total bills — 
# all in-memory, without using any external database.

from threading import Lock

class MenuItem:
    def __init__(self, item_id, name, price):
        self.id = item_id
        self.name = name
        self.price = price

class Order:
    def __init__(self, order_id, items):
        self.id = order_id
        self.items = items
        self.total = sum(i.price for i in items)
        self.status = "Pending"

class Restaurant:
    def __init__(self,):
        self.menu_items = {}
        self.orders = {}
        self.lock = Lock()

    def add_menu_item(self, item_id, name, price):
        self.menu_items[item_id] = MenuItem(item_id, name, price)

    def place_order(self, order_id, item_ids):
        with self.lock:
            items = [self.menu_items[i] for i in item_ids if i in self.menu_items]
            order = Order(order_id, items)
            self.orders[order_id] = order
            return order
        
    def update_order_status(self, order_id, new_status):
        with self.lock:
            if order_id in self.orders:
                self.orders[order_id].status = new_status

    def show_orders(self):
        print("\n-------- Current Orders --------- ")
        for o in self.orders.values():
            item_names = [i.name for i in o.items]
            print(f"Order #{o.id}: {item_names} | Total: ₹{o.total} | Status: {o.status}")

if __name__ == "__main__":
    r = Restaurant()
    r.add_menu_item(1, "Margherita Pizza", 250)
    r.add_menu_item(2, "Pasta Alfredo", 180)
    r.add_menu_item(3, "Cold Coffee", 90)

    o1 = r.place_order(101, [1, 3])
    o2 = r.place_order(102, [2])

    r.show_orders()

    r.update_order_status(101, "Preparing")
    r.update_order_status(102, "Served")

    r.show_orders()
        
        

    
