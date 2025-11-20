# Design a simple Password Vault System in Python that securely stores, retrieves, 
# lists, and deletes account credentials. All passwords must be encrypted 
# and decrypted using symmetric key encryption (Fernet) without using any external database.

from cryptography.fernet import Fernet

class PasswordVault:
    def __init__(self):
        self.key = Fernet.generate_key()
        self.cipher = Fernet(self.key)
        self.vault_data = {}

    def add_account(self, service, username, password):
        encrypted_pw = self.cipher.encrypt(password.encode())
        self.vault_data[service] = (username, encrypted_pw)
        print(f"[+] Added {service} account securely.")

    def get_password(self, service):
        if service in self.vault_data:
            username, encrypted_pw = self.vault_data[service]
            decrypted_pw = self.cipher.decrypt(encrypted_pw).decode()

            print(f"Service: {service}")
            print(f"Username: {username}")
            print(f"Password: {decrypted_pw}")
        else:
            print("[-] No such service found.")

    def list_accounts(self):
        if not self.vault_data:
            print("[-] Vault is empty.")
            return
        
        print("Stored accounts:")
        for s in self.vault_data:
            print(" -", s)

    def delete_account(self, service):
        if service in self.vault_data:
            del self.vault_data[service]
            print(f"[x] Deleted {service} from vault.")
        else:
            print("[-] Service not found.")

if __name__ == "__main__":

    vault = PasswordVault()

    vault.add_account("Gmail", "bharadwaj@gmail.com", "SecurePass123")
    vault.add_account("GitHub", "manu577228", "CodeBuff@2025")

    vault.list_accounts()

    vault.get_password("Gmail")

    vault.delete_account("GitHub")
    
    vault.list_accounts()


