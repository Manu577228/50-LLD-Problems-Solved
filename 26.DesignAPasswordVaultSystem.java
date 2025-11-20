/* -------------------------------------------------------- */
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
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio      */
/* -----------------------------------------------------------------------  */

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class PasswordVault {

    // Inner class to represent each account
    static class Account {
        String username;
        String encryptedPassword;

        Account(String username, String encryptedPassword) {
            this.username = username;
            this.encryptedPassword = encryptedPassword;
        }
    }

    private Map<String, Account> vaultData; // in-memory vault
    private SecretKey secretKey;             // AES key for encryption

    public PasswordVault() throws Exception {
        this.vaultData = new HashMap<>();
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // AES-128 bit key
        this.secretKey = keyGen.generateKey();
    }

    // Encrypt a plain password
    private String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // Decrypt an encrypted password
    private String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        return new String(cipher.doFinal(decoded));
    }

    // Add a new account
    public void addAccount(String service, String username, String password) throws Exception {
        String encryptedPw = encrypt(password);
        vaultData.put(service, new Account(username, encryptedPw));
        System.out.println("[+] Added " + service + " account securely.");
    }

    // Retrieve password for a service
    public void getPassword(String service) throws Exception {
        if (vaultData.containsKey(service)) {
            Account acc = vaultData.get(service);
            String decryptedPw = decrypt(acc.encryptedPassword);
            System.out.println("Service: " + service);
            System.out.println("Username: " + acc.username);
            System.out.println("Password: " + decryptedPw);
        } else {
            System.out.println("[-] No such service found.");
        }
    }

    // List all stored accounts
    public void listAccounts() {
        if (vaultData.isEmpty()) {
            System.out.println("[-] Vault is empty.");
            return;
        }
        System.out.println("Stored accounts:");
        for (String s : vaultData.keySet()) {
            System.out.println(" - " + s);
        }
    }

    // Delete an account
    public void deleteAccount(String service) {
        if (vaultData.containsKey(service)) {
            vaultData.remove(service);
            System.out.println("[x] Deleted " + service + " from vault.");
        } else {
            System.out.println("[-] Service not found.");
        }
    }

    // Demo main method
    public static void main(String[] args) throws Exception {
        PasswordVault vault = new PasswordVault();
        vault.addAccount("Gmail", "bharadwaj@gmail.com", "SecurePass123");
        vault.addAccount("GitHub", "manu577228", "CodeBuff@2025");
        vault.listAccounts();
        vault.getPassword("Gmail");
        vault.deleteAccount("GitHub");
        vault.listAccounts();
    }
}
