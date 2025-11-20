# Design a simple Payment Gateway Integration System that supports multiple 
# payment methods (Card, UPI, Wallet), processes transactions concurrently, and 
# maintains an in-memory transaction log using the Strategy Design Pattern — 
# without using any external database.

import threading, random, time

class PaymentStrategy:
    def process_payment(self, amount, transaction_id):
        pass

class CardPayment(PaymentStrategy):
    def process_payment(self, amount, transaction_id):
        print (f"[Card] Transaction {transaction_id}: Processing ₹{amount} via Credit/Debit Card... ")
        time.sleep(1)
        print(f"[Card] Transaction {transaction_id}: Payment Successful")

class UPIPayment(PaymentStrategy):
    def process_payment(self, amount, transaction_id):
        print (f"[UPI] Transaction {transaction_id}: Paying ₹{amount} via UPI... ")
        time.sleep(1)
        print(f"[UPI] Transaction {transaction_id}: Payment Successful")

class WalletPayment(PaymentStrategy):
    def process_payment(self, amount, transaction_id):
        print (f"[Wallet] Transaction {transaction_id}: Deducting ₹{amount} from Wallet... ")
        time.sleep(1)
        print(f"[Wallet] Transaction {transaction_id}: Payment Successful")

class PaymentGateway:
    def __init__(self):
        self.strategies = {
            "card": CardPayment(),
            "upi": UPIPayment(),
            "wallet": WalletPayment(),
        }
        self.transactions = {}
        self.lock = threading.Lock()

    def pay(self, mode, amount):
        transaction_id = random.randint(1000, 9999)
        print(f"\n[Gateway] Initiating Transaction {transaction_id} via {mode.upper()}...")
        if mode not in self.strategies:
            print("[Gateway] Invalid Payment Mode")
            return
        
        with self.lock:
            self.transactions[transaction_id] = {"amount": amount, "status": "PENDING"}

        self.strategies[mode].process_payment(amount, transaction_id)

        with self.lock:
            self.transactions[transaction_id]["status"] = "SUCCESS"

        print(f"[Gateway] Transaction {transaction_id} Completed \n")

if __name__ == "__main__":
    # Create a PaymentGateway instance
    pg = PaymentGateway()

    # Simulate multiple concurrent payments using threads
    t1 = threading.Thread(target=pg.pay, args=("card", 2500))
    t2 = threading.Thread(target=pg.pay, args=("upi", 999))
    t3 = threading.Thread(target=pg.pay, args=("wallet", 1500))

    # Start all transactions simultaneously
    t1.start(); t2.start(); t3.start()
    # Wait until all threads complete
    t1.join(); t2.join(); t3.join()

    # Print all final transaction logs after completion
    print("Final Transaction Logs:", pg.transactions)

    
        

