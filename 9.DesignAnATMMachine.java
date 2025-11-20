/* ----------------------------------------------------------------------------  
   ( The Authentic JS/JAVA CodeBuff )
    ___ _                      _              _ 
    | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
    | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
    |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                           |__/ 
-------------------------------------------------------------------------------  
   Youtube : https://youtube.com/@code-with-Bharadwaj                        
   Github  : https://github.com/Manu577228                                  
   Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio        
-------------------------------------------------------------------------------  
*/

/* ============================= 1) REQUIREMENTS ==============================

   a) Functional Requirements
   --------------------------
   1. User can login (simulate card insert)
   2. Validate PIN before transactions
   3. Withdraw, Deposit, and Check Balance options
   4. ATM maintains total cash
   5. Safe exit from session

   b) Non-Functional Requirements
   -------------------------------
   - Console-based, single-file demo
   - No external DB or dependencies
   - Thread-safe shared resources
   - Simple, extendable, and readable
*/


/* ===================== 2) ALGORITHM CHOICE DISCUSSION =======================

   - Accounts stored in HashMap → O(1) lookup
   - ATM operations use synchronized blocks → ensure thread-safety
   - Each transaction (withdraw/deposit) modifies shared state safely
*/


/* ========== 3) CONCURRENCY AND DATA MODEL DISCUSSION =======================

   Data Model:
   ------------
   Account → (accNo, pin, balance)
   ATM → maintains totalCash + Map of accounts

   Concurrency:
   ------------
   - Shared data: ATM cash and Account balances
   - synchronized keyword prevents race conditions
*/


/* ============================ 4) UML DIAGRAM ===============================

                ┌────────────────┐
                │    Account     │
                ├────────────────┤
                │ - accNo        │
                │ - pin          │
                │ - balance      │
                ├────────────────┤
                │ + verifyPin()  │
                │ + deposit()    │
                │ + withdraw()   │
                │ + getBalance() │
                └────────────────┘
                         ▲
                         │
                ┌────────────────┐
                │      ATM       │
                ├────────────────┤
                │ - totalCash    │
                │ - accounts{}   │
                ├────────────────┤
                │ + authenticate()│
                │ + withdraw()    │
                │ + deposit()     │
                │ + checkBalance()│
                └────────────────┘
*/


/* ========================= 5) JAVA IMPLEMENTATION =========================== */

import java.util.*;
import java.io.*;

class Account {
    private int accNo;
    private int pin;
    private int balance;

    public Account(int accNo, int pin, int balance) {
        this.accNo = accNo;
        this.pin = pin;
        this.balance = balance;
    }

    public boolean verifyPin(int enteredPin) {
        return this.pin == enteredPin;
    }

    public synchronized String deposit(int amt) {
        if (amt <= 0) return "Invalid Amount";
        balance += amt;
        return "Deposited ₹" + amt + ". New Balance: ₹" + balance;
    }

    public synchronized String withdraw(int amt) {
        if (amt <= 0) return "Invalid Amount";
        if (amt > balance) return "Insufficient Balance";
        balance -= amt;
        return "Withdrew ₹" + amt + ". Remaining Balance: ₹" + balance;
    }

    public synchronized String getBalance() {
        return "Current Balance: ₹" + balance;
    }

    public int getAccNo() {
        return accNo;
    }
}

class ATM {
    private int totalCash;
    private final Map<Integer, Account> accounts = new HashMap<>();

    public ATM(int totalCash) {
        this.totalCash = totalCash;
    }

    public void addAccount(Account acc) {
        accounts.put(acc.getAccNo(), acc);
    }

    public Account authenticate(int accNo, int pin) {
        Account acc = accounts.get(accNo);
        if (acc != null && acc.verifyPin(pin)) {
            System.out.println("✅ Login Successful!\n");
            return acc;
        }
        System.out.println("❌ Invalid Account or PIN.\n");
        return null;
    }

    public synchronized String withdraw(Account acc, int amt) {
        if (amt > totalCash) return "ATM out of cash.";
        String result = acc.withdraw(amt);
        if (result.contains("Withdrew")) totalCash -= amt;
        return result;
    }

    public synchronized String deposit(Account acc, int amt) {
        String result = acc.deposit(amt);
        if (result.contains("Deposited")) totalCash += amt;
        return result;
    }

    public String checkBalance(Account acc) {
        return acc.getBalance();
    }

    public int getTotalCash() {
        return totalCash;
    }
}


/* ========================== DEMO EXECUTION (main) =========================== */

public class ATMMachineDemo {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        ATM atm = new ATM(10000);
        Account acc1 = new Account(1234, 1111, 5000);
        Account acc2 = new Account(5678, 2222, 3000);
        atm.addAccount(acc1);
        atm.addAccount(acc2);

        System.out.println("\n===== ATM MACHINE DEMO =====\n");
        System.out.print("Enter Account Number: ");
        int accNo = Integer.parseInt(br.readLine());
        System.out.print("Enter PIN: ");
        int pin = Integer.parseInt(br.readLine());

        Account user = atm.authenticate(accNo, pin);
        if (user != null) {
            System.out.println(atm.checkBalance(user));
            System.out.println(atm.withdraw(user, 1000));
            System.out.println(atm.deposit(user, 2000));
            System.out.println(atm.checkBalance(user));
            System.out.println("\nATM Remaining Cash: " + atm.getTotalCash());
        }
    }
}


/* ====================== OUTPUT EXPLANATION (Console) =========================

✅ Login Successful!
Current Balance: ₹5000
Withdrew ₹1000. Remaining Balance: ₹4000
Deposited ₹2000. New Balance: ₹6000
Current Balance: ₹6000
ATM Remaining Cash: 11000
*/


/* ===================== 6) LIMITATIONS OF CURRENT CODE ========================

 - No persistent storage (data resets on restart)
 - No encrypted PIN or network simulation
 - Single-threaded demo (no real parallel ATM users)
 - Console I/O (no GUI for usability)
*/


/* ========== 7) ALTERNATIVE ALGORITHMS & TRADE-OFFs (Future Scope) ===========

 a) Use Thread class → simulate multiple ATM users concurrently
 b) Add file-based persistence → +Data retention, -Slight overhead
 c) Use GUI (Swing/JavaFX) → +Better UX, -Complex for LLD demo
 d) Integrate SQL DB → +Scalability, -External dependency
*/

/* ============================= END OF FILE ================================= */
