# Design an in-memory Coupon/Discount Manager system where admins can
# create percentage or flat-value coupons, and users can apply them to a cart total.
# The system must validate expiry, usage limits, and minimum order
# requirements, and then compute the final discounted amount.

import datetime
import threading


class DiscountStrategy:
    def apply(self, total):
        raise NotImplementedError()


class PercentageDiscount:
    def __init__(self, percent):
        self.percent = percent

    def apply(self, total):
        return total - (total * self.percent / 100)


class FlatDiscount:
    def __init__(self, amount):
        self.amount = amount

    def apply(self, total):
        return max(0, total - self.amount)


class Coupon:
    def __init__(self, cid, strategy, expiry, min_order, max_uses):
        self.cid = cid
        self.strategy = strategy
        self.expiry = expiry
        self.min_order = min_order
        self.max_uses = max_uses
        self.current_uses = 0


class CouponManager:
    _instance = None
    _lock = threading.Lock()

    def __init__(self):
        self.coupons = {}
        self.use_lock = threading.Lock()

    @classmethod
    def instance(cls):
        if not cls._instance:
            cls._instance = CouponManager()
        return cls._instance

    def create_coupon(self, cid, ctype, amount, days_valid, min_order, max_uses):
        expiry = datetime.datetime.now() + datetime.timedelta(days=days_valid)

        if ctype == "percent":
            strategy = PercentageDiscount(amount)
        else:
            strategy = FlatDiscount(amount)

        coupon = Coupon(cid, strategy, expiry, min_order, max_uses)
        self.coupons[cid] = coupon

    def validate(self, coupon, total):
        if datetime.datetime.now() > coupon.expiry:
            return False, "Expired"

        if coupon.current_uses >= coupon.max_uses:
            return False, "Usage limit reached"

        if total < coupon.min_order:
            return False, "Minimum order not yet"

        return True, "Valid"

    def apply_coupon(self, cid, total):
        if cid not in self.coupons:
            return False, "Invalid Coupon", total

        c = self.coupons[cid]

        valid, msg = self.validate(c, total)

        if not valid:
            return False, msg, total

        with self.use_lock:
            c.current_uses += 1

        discounted_total = c.strategy.apply(total)

        return True, "Apllied", discounted_total


if __name__ == "__main__":

    mgr = CouponManager.instance()   # Get singleton manager

    # Create first coupon: 10% off, valid 3 days, min order 100, max uses 5
    mgr.create_coupon("DISC10", "percent", 10, 3, 100, 5)

    # Create second coupon: flat ₹50 off, valid 5 days, min order 200, max uses 2
    mgr.create_coupon("FLAT50", "flat", 50, 5, 200, 2)

    # Test applying coupons
    print(mgr.apply_coupon("DISC10", 150))   # Expect ~135 after 10% off
    print(mgr.apply_coupon("FLAT50", 220))   # Expect 170 after flat ₹50 off
    print(mgr.apply_coupon("FLAT50", 150))   # Min order fail → unchanged total
