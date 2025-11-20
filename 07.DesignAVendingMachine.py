# Design and implement a simple Vending Machine system in Java/Python that allows users to insert money, 
# select products, dispense items, and return change. 
# The machine should support restocking products and refilling coins, 
# all managed within a single file using in-memory data structures only (no external DB).

import threading
from dataclasses import dataclass
from typing import Dict, Tuple, List

@dataclass
class Product:
    code: str
    name: str
    price_cents: int
    qty: int

    def __str__(self):
        return f"{self.code}: {self.name} - ₹{self.price_cents / 100: .2f} ({self.qty} left)"
    
class Inventory:
    def __init__(self):
        self.products: Dict[str, Product] = {}

    def add_product(self, p: Product):
        if p.code in self.products:
            self.products[p.code].qty += p.qty
        else:
            self.products[p.code] = p

    def get_product(self, code: str) -> Product:
        return self.products.get(code)
    
    def list_products(self) -> List[Product]:
        return list(self.products.values())
    
class CashBox:
    def __init__(self, denominations: list[int]):
        self.coins: Dict[int, int] = {d: 0 for d in denominations}
        self.sorted_denoms = sorted(denominations, reverse=True)

    def add_coins(self, denom: int, count: int):
        if denom not in self.coins:
            raise ValueError("Unsupported denomination")
        self.coins[denom] += count

    def total_cents(self) -> int:
        return sum(d * c for d, c in self.coins.items())
    
    def make_change(self, amount_cents: int) -> Tuple[bool, Dict[int, int]]:
        if amount_cents < 0:
            return False, {}
        to_give, remaining = {}, amount_cents
        temp = self.coins.copy()
        for d in self.sorted_denoms:
            if remaining <= 0:
                break
            take = min(remaining // d, temp.get(d, 0))
            if take:
                to_give[d] = take
                temp[d] -= take
                remaining -= d * take

        if remaining != 0:
            return False, {}
        for d, c in to_give.items():
            self.coins[d] -= c
        return True, to_give
    
    def accept_payment(self, payment: Dict[int, int]):
        for d, c in payment.items():
            if d not in self.coins:
                raise ValueError("Unsupported denomination")
            self.coins[d] += c

class VendingMachine:
    def __init__(self):
        self.denoms = [10000, 5000, 2000, 1000, 500, 100, 50, 20, 10, 5, 1]
        self.cashbox = CashBox(self.denoms)
        self.inventory = Inventory()
        self.lock = threading.Lock()
        self._seed_demo()

    def _seed_demo(self):
        self.inventory.add_product(Product("A1", "Water Bottle", 300, 10))
        self.inventory.add_product(Product("A2", "Soda Can", 450, 5))
        self.inventory.add_product(Product("B1", "Chips", 150, 7))

        initial_coins = {100: 30, 50: 20, 20: 30, 10: 50, 5: 100, 1: 200}
        for d, c in initial_coins.items():
            if d in self.cashbox.coins:
                self.cashbox.add_coins(d, c)

    def list_products(self):
        for p in self.inventory.list_products():
            print(p)

    def restock(self, code: str, qty: int):
        with self.lock:
            p = self.inventory.get_product(code)
            if not p:
                print("product not found.")
                return
            p.qty += qty
            print(f"Restocked {code}. New Qty: {p.qty}")

    def refill_coins(self, denom: int, count: int):
        with self.lock:
            self.cashbox.add_coins(denom, count)
            print(f"Refilled {count} coins of {denom} cents")

    def purchase(self, code:str, payment: Dict[int, int]):
        total = sum(d * c for d, c in payment.items())
        with self.lock:
            product = self.inventory.get_product(code)
            if not product:
                return False, "Product not Found."
            if product.qty <= 0:
                return False, "Out of Stock."
            price = product.price_cents
            if total < price:
                return False, "Insufficient money."
            change_needed = total - price
            self.cashbox.accept_payment(payment)
            success, change_map = self.cashbox.make_change(change_needed)
            if not success:
                for d, c in payment.items():
                    self.cashbox.coins[d] -= c
                return False, "Cannot provide change."
            product.qty -= 1
            return True, {"product": product.name, "change_map": change_map}

def demo():
    vm = VendingMachine()
    print("Welcome to Demo Vending Machine (values in ₹).")
    while True:
        print("\n-- Menu --")
        print("1) List products")
        print("2) Purchase")
        print("3) Restock (admin)")
        print("4) Refill coins (admin)")
        print("5) Exit")
        choice = input("Choose: ").strip()
        if choice == "1":
            vm.list_products()
        elif choice == "2":
            vm.list_products()
            code = input("Enter product code: ").strip()
            print("Enter payment denominations as 'denom_in_cents:count' separated by commas.")
            print("Example: 100:2,50:1 -> ₹2.50")
            entry = input("Payment: ").strip()
            try:
                payment = {}
                for part in filter(None, map(str.strip, entry.split(","))):
                    d_s, c_s = part.split(":")
                    d = int(d_s); c = int(c_s)
                    payment[d] = payment.get(d, 0) + c
                ok, result = vm.purchase(code, payment)
                if not ok:
                    print("FAILED:", result)
                else:
                    print(f"Dispensed: {result['product']}")
                    if result["change_map"]:
                        print("Change returned:")
                        for d, c in sorted(result["change_map"].items(), reverse=True):
                            print(f"  {d}c x {c}")
                    else:
                        print("No change.")
            except Exception as e:
                print("Invalid payment format or error:", e)
        elif choice == "3":
            code = input("Product code: ").strip()
            qty = int(input("Quantity to add: ").strip())
            vm.restock(code, qty)
        elif choice == "4":
            denom = int(input("Denomination (in cents): ").strip())
            count = int(input("Count: ").strip())
            vm.refill_coins(denom, count)
        elif choice == "5":
            print("Bye.")
            break
        else:
            print("Unknown option.")

if __name__ == "__main__":
    demo()
        

        