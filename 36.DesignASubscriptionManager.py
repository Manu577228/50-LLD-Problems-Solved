# Design a Subscription Manager system that can create, cancel, renew, and 
# track user subscriptions entirely in-memory. The system should support multiple users, 
# allow checking active subscriptions, and manage subscription status based on dates.

from datetime import datetime, timedelta

class Subscription:
    def __init__(self, sub_id, user_id, plan_name, start_date, end_date):
        self.sub_id = sub_id
        self.user_id = user_id
        self.plan_name = plan_name
        self.start_date = start_date
        self.end_date = end_date
        self.status = "ACTIVE"

    def is_active(self):
        today = datetime.now().date()
        return self.status == "ACTIVE" and today <= self.end_date
    
    def renew(self, days):
        self.end_date += timedelta(days = days)

    def cancel(self):
        self.status = "CANCELLED"

class SubscriptionManager:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(SubscriptionManager, cls).__new__(cls)
            cls._instance.subscriptions = {}
        return cls._instance
    
    def add_subscription(self, subscription):
        if subscription.user_id not in self.subscriptions:
            self.subscriptions[subscription.user_id] = []
        self.subscriptions[subscription.user_id].append(subscription)

    def cancel_subscription(self, user_id, sub_id):
        for s in self.subscriptions.get(user_id, []):
            if s.sub_id == sub_id:
                s.cancel()

    def renew_subscription(self, user_id, sub_id, days):
        for s in self.subscriptions.get(user_id, []):
            if s.sub_id == sub_id:
                s.renew(days)

    def get_active(self, user_id):
        return [s for s in self.subscriptions.get(user_id, []) if s.is_active()]
    
if __name__ == "__main__":
    m = SubscriptionManager()  # Create singleton instance

    # Create new subscription for user 101
    s1 = Subscription(
        sub_id=1,
        user_id=101,
        plan_name="Premium",
        start_date=datetime.now().date(),
        end_date=datetime.now().date() + timedelta(days=7)
    )

    # Add subscription
    m.add_subscription(s1)

    # Check active subscriptions before cancellation
    print("Active subs before cancel:", [s.plan_name for s in m.get_active(101)])

    # Cancel the subscription
    m.cancel_subscription(101, 1)

    # Check active subscriptions after cancellation
    print("Active subs after cancel:", [s.plan_name for s in m.get_active(101)])

    # Attempt to renew (status remains CANCELLED)
    m.renew_subscription(101, 1, 10)

    # Show updated data after renewal attempt
    print("Renew attempted → Status:", s1.status, "| End date:", s1.end_date)
        