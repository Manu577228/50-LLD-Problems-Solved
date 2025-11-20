# Design and implement a console-based ATM Machine System that allows users to log in using an 
# account number and PIN, check their balance, deposit or withdraw money, 
# and maintain ATM cash — all in a single-file Python demo without any database, 
# following proper LLD structure and thread-safe design.

import threading, time

class Account:
    def __init__(self, acc_no, pin, balance = 0):
        self.acc_no = acc_no
        self.pin = pin
        self.balance = balance

    def verifyPin(self, pin):
        return self.pin == pin
    
    def deposit(self, amt):
        if amt <= 0: return "Invalid Amount"
        self.balance += amt
        return f"Deposited ₹{amt}. New Balance: ₹{self.balance}"
    
    def withdraw(self, amt):
        if amt <= 0: return "invalid Amount"
        if amt > self.balance: return "insufficient balance"
        self.balance -= amt
        return f"Withdraw ₹{amt}. Remaining Balance: ₹{self.balance}"
    
    def getBalance(self):
        return f"Current Balance: ₹{self.balance}"
    
class ATM:
    def __init__(self, total_cash):
        self.total_cash = total_cash
        self.accounts = {}
        self.lock = threading.Lock()

    def addAccount(self, acc):
        self.accounts[acc.acc_no] = acc

    def authenticate(self, acc_no, pin):
        acc = self.accounts.get(acc_no)
        if acc and acc.verifyPin(pin):
            print("✅ Login Successful!\n")
            return acc
        print("❌ Invalid Account or PIN.\n")
        return None
    
    def withdraw(self, acc, amt):
        with self.lock:
            if amt > self.total_cash:
                return "ATM out of cash."
            result = acc.withdraw(amt)
            if "Withdraw" in result:
                self.total_cash -= amt
            return result
        
    def deposit(self, acc, amt):
        with self.lock:
            result = acc.deposit(amt)
            if "Deposited" in result:
                self.total_cash += amt
            return result
        
    def checkBalance(self, acc):
        return acc.getBalance()
    
if __name__ == "__main__":
    atm = ATM(10000)
    acc1 = Account(1234, 1111, 5000)
    acc2 = Account(5678, 2222, 3000)
    atm.addAccount(acc1)
    atm.addAccount(acc2)

    print("\n===== ATM MACHINE DEMO =====\n")

    # Simulate a user session
    user = atm.authenticate(1234, 1111)
    if user:
        print(atm.checkBalance(user))
        print(atm.withdraw(user, 1000))
        print(atm.deposit(user, 2000))
        print(atm.checkBalance(user))
        print("\nATM Remaining Cash:", atm.total_cash)
        

        