# Design a lightweight in-memory Feature Flag Manager that supports creating feature flags, 
# enabling/disabling them globally, enabling them for specific users, 
# applying percentage rollouts, and checking if a feature is enabled for a given user — 
# all in a thread-safe, single-file Python implementation.

import threading

class FeatureFlag:
    def __init__(self, name):
        self.name= name
        self.enabled = False
        self.per_user_overrides = set()
        self.rollout_percentage = 0

    def is_enabled(self, user_id):
        if user_id in self.per_user_overrides:
            return True
        
        if self.enabled:
            return True
        
        return (user_id % 100) < self.rollout_percentage
    
class FeatureFlagManager:
    _instance = None
    _lock = threading.Lock()

    def __new__(cls):
        with cls._lock:
            if cls._instance is None:
                cls._instance = super().__new__(cls)
                cls._instance.flags = {}
                cls._instance.flags_lock = threading.Lock()
        return cls._instance
    
    def create_flag(self, name):
        with self.flags_lock:
            self.flags[name] = FeatureFlag(name)

    def update_flag_state(self, name, state):
        with self.flags_lock:
            self.flags[name].enabled = state

    def enable_for_user(self, name, user_id):
        with self.flags_lock:
            self.flags[name].per_user_overrides.add(user_id)

    def disable_for_user(self, name, user_id):
        with self.flags_lock:
            f = self.flags[name]
            if user_id in f.per_user_overrides:
                f.per_user_overrides.remove(user_id)

    def set_rollout(self, name, pct):
        with self.flags_lock:
            self.flags[name].rollout_percentage = pct

    def is_enabled(self, name, user_id):
        return self.flags[name].is_enabled(user_id)
    
if __name__ == "__main__":
    mgr = FeatureFlagManager()

    mgr.create_flag("new_ui")
    mgr.update_flag_state("new_ui", True)

    mgr.create_flag("beta_feature")
    mgr.set_rollout("beta_feature", 20)
    mgr.enable_for_user("beta_feature", 101)

    test_users = [10, 25, 50, 101]

    print("Feature Checks:")
    for u in test_users:
        print(f"User {u}: new_ui=", mgr.is_enabled("new_ui", u),
              " beta_feature=", mgr.is_enabled("beta_feature", u))

        