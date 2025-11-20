# Design a Generic Notification System that can send messages through multiple channels 
# (Email, SMS, Push) concurrently, using an extensible and thread-safe architecture — 
# without any external database.

from abc import ABC, abstractmethod
from concurrent.futures import ThreadPoolExecutor
from threading import Lock
import time

class Notifier(ABC):
    @abstractmethod
    def send(self, message: str) -> None:
        pass

class EmailNotifier(Notifier):
    def send(self, message : str) -> None:
        time.sleep(0.5)
        print(f"Email sent: {message}")

class SMSNotifier(Notifier):
    def send(self, message : str) -> None:
        time.sleep(0.3)
        print(f"SMS sent: {message}")

class PushNotifier(Notifier):
    def send(self, message : str) -> None:
        time.sleep(0.2)
        print(f"Push Notification sent: {message}")

class NotificationManager:
    def __init__(self):
        self.channels = []
        self.lock = Lock()

    def register_channel(self, notifier: Notifier):
        with self.lock:
            self.channels.append(notifier)

    def send_notification(self, message: str):
        with ThreadPoolExecutor(max_workers = len(self.channels)) as executor:
            for ch in self.channels:
                executor.submit(ch.send, message)

if __name__ == "__main__":
    manager = NotificationManager()
    manager.register_channel(EmailNotifier())
    manager.register_channel(SMSNotifier())
    manager.register_channel(PushNotifier())

    print("🚀 Sending Notifications...\n")
    manager.send_notification("Hello, Bharadwaj Subscribers! 🎥🔥")
    print("\n✅ Notification dispatch complete!")





    



