# Design a simple in-memory Health Monitor System that tracks user vitals
# (heart rate, temperature, blood pressure), checks them against thresholds, and
# generates alerts when values exceed safe limits. The system must support adding users,
# updating vitals, storing logs, and retrieving the latest status — all without any external database.

import threading

class AlertStrategy:
    def check(self, vitals, thresholds):
        raise NotImplementedError()


class TresholdAlertStrategy(AlertStrategy):
    def check(self, v, t):
        alerts = []

        if v["heart"] > t["heart"]:
            alerts.append("High Heart rate")

        if v["temp"] > t["temp"]:
            alerts.append("High Temperature")

        if v["bp"] > t["bp"]:
            alerts.append("High Boold Pressure")

        return alerts


class UserProfile:
    def __init__(self, thresholds):
        self.thresholds = thresholds
        self.lastVitals = None
        self.logs = []
        self.lock = threading.Lock()


class HealthMonitor:
    __instance = None

    def __new__(cls):
        if not cls.__instance:
            cls.__instance = super().__new__(cls)
            cls.__instance.users = {}
            cls.__instance.alert_strategy = TresholdAlertStrategy()
        return cls.__instance

    def add_user(self, uid, thresholds):
        self.users[uid] = UserProfile(thresholds)

    def update_vitals(self, uid, vitals):
        user = self.users.get(uid)
        if not user:
            return

        with user.lock:
            user.lastVitals = vitals
            user.logs.append(vitals)

            alerts = self.alert_strategy.check(vitals, user.thresholds)

        return alerts

    def get_status(self, uid):
        u = self.users.get(uid)
        if not u:
            return None

        return {
            "lastVitals": u.lastVitals,
            "logs": u.logs[-5:]
        }


# Get the single instance of HealthMonitor
monitor = HealthMonitor()

# Add a user with threshold values
monitor.add_user("user1", {"heart": 100, "temp": 99, "bp": 140})

# First vitals: all normal
out1 = monitor.update_vitals("user1", {"heart": 95, "temp": 98, "bp": 135})

# Second vitals: exceed thresholds → alerts expected
out2 = monitor.update_vitals("user1", {"heart": 120, "temp": 101, "bp": 150})

# Print results
print("Alerts 1:", out1)
print("Alerts 2:", out2)
print("Status:", monitor.get_status("user1"))