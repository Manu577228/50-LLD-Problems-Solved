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
import java.util.concurrent.locks.*;
import java.util.concurrent.*;

interface PaymentStrategy {
    void processPayment(double amount, int transactionId);
}

class CardPayment implements PaymentStrategy {
    public void processPayment(double amount, int transactionId) {
        System.out.println("[Card] Transaction " + transactionId + ": Processing ₹" + amount + " via Credit/Debit Card...");
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.out.println("[Card] Transaction " + transactionId + ": Payment Successful ✅");
    }
}

class UPIPayment implements PaymentStrategy {
    public void processPayment(double amount, int transactionId) {
        System.out.println("[UPI] Transaction " + transactionId + ": Paying ₹" + amount + " via UPI...");
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.out.println("[UPI] Transaction " + transactionId + ": Payment Successful ✅");
    }
}

class WalletPayment implements PaymentStrategy {
    public void processPayment(double amount, int transactionId) {
        System.out.println("[Wallet] Transaction " + transactionId + ": Deducting ₹" + amount + " from Wallet...");
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.out.println("[Wallet] Transaction " + transactionId + ": Payment Successful ✅");
    }
}

class PaymentGateway {
    private final Map<String, PaymentStrategy> strategies = new HashMap<>();
    private final Map<Integer, Map<String, Object>> transactions = new HashMap<>();
    private final Lock lock = new ReentrantLock();
    private final Random rand = new Random();

    public PaymentGateway() {
        strategies.put("card", new CardPayment());
        strategies.put("upi", new UPIPayment());
        strategies.put("wallet", new WalletPayment());
    }

    public void pay(String mode, double amount) {
        int transactionId = rand.nextInt(9000) + 1000;
        System.out.println("\n[Gateway] Initiating Transaction " + transactionId + " via " + mode.toUpperCase() + "...");
        if (!strategies.containsKey(mode)) {
            System.out.println("[Gateway] Invalid Payment Mode ❌");
            return;
        }
        lock.lock();
        try {
            Map<String, Object> record = new HashMap<>();
            record.put("amount", amount);
            record.put("status", "PENDING");
            transactions.put(transactionId, record);
        } finally {
            lock.unlock();
        }

        strategies.get(mode).processPayment(amount, transactionId);

        lock.lock();
        try {
            transactions.get(transactionId).put("status", "SUCCESS");
        } finally {
            lock.unlock();
        }

        System.out.println("[Gateway] Transaction " + transactionId + " Completed ✅\n");
    }

    public void printTransactions() {
        System.out.println("Final Transaction Logs:");
        for (Map.Entry<Integer, Map<String, Object>> e : transactions.entrySet()) {
            System.out.println("Transaction " + e.getKey() + " => " + e.getValue());
        }
    }
}

public class Main {
    public static void main(String[] args) {
        PaymentGateway pg = new PaymentGateway();
        Thread t1 = new Thread(() -> pg.pay("card", 2500));
        Thread t2 = new Thread(() -> pg.pay("upi", 999));
        Thread t3 = new Thread(() -> pg.pay("wallet", 1500));

        t1.start(); t2.start(); t3.start();
        try {
            t1.join(); t2.join(); t3.join();
        } catch (InterruptedException e) {}

        pg.printTransactions();
    }
}
