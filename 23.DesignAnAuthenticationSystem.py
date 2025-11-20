# Design an in-memory Authentication System that allows users to register, 
# log in, and log out securely using password hashing, without any external database. 
# The system should also display active sessions and 
# ensure thread-safe access for concurrent operations.

import hashlib
import threading
import time

class User:
    def __init__(self, username, password):
        self.username = username
        self.password_hash = hashlib.sha256(password.encode()).hexdigest()
        self.is_logged_in = False

    def verify_password(self, password):
        return self.password_hash == hashlib.sha256(password.encode()).hexdigest()
    
class AuthSystem:
    def __init__(self):
        self.users = {}
        self.sessions = {}
        self.lock = threading.Lock()

    def register(self, username, password):
        with self.lock:
            if username in self.users:
                print(f" Username '{username}' already exists.")
                return False
            self.users[username] = User(username, password)
            print(f" User '{username}' registered successfully.")
            return True
        
    def login(self, username, password):
        with self.lock:
            user = self.users.get(username)
            if not user:
                print(" User not found.")
                return False
            if not user.verify_password(password):
                print("Invalid Password.")
                return False
            user.is_logged_in = True
            self.sessions[username] = time.time()
            print(f" User '{username}' logged in successfully.")
            return True
        
    def logout(self, username):
        with self.lock:
            user = self.users.get(username)
            if user and user.is_logged_in:
                user.is_logged_in = False
                self.sessions.pop(username, None)
                print(f" User '{username}' logged out.")
                return True
            print(" No active session found.")
            return False
        
    def show_active_users(self):
        print(" Active Users:", list(self.sessions.keys()))

if __name__ == "__main__":
    auth = AuthSystem()

    auth.register("bharadwaj", "codebuff123")
    auth.register("manu", "password")
    
    auth.login("bharadwaj", "codebuff123")

    auth.login("manu", "wrongpass")

    auth.show_active_users()

    auth.logout("bharadwaj")

    auth.show_active_users()
            
        
        