# Design a simple in-memory auction system where users can create auctions, 
# place bids while the auction is active, and automatically determine the winner 
# when the auction ends. The system must support concurrent bidding safely and 
# maintain complete bid history, all without using any external databases.

import time
import threading

class Auction:
    def __init__(self, auction_id, item_name, start_price, duration_seconds):
        self.id = auction_id
        self.item_name = item_name
        self.highest_bid = start_price
        self.highest_bidder = None
        self.start_time = time.time()
        self.end_time = self.start_time + duration_seconds
        self.bid_history = []
        self.lock = threading.Lock()

    def is_active(self):
        return time.time() < self.end_time
    
    def place_bid(self, user, amount):
        with self.lock:
            if not self.is_active():
                return "Auction already ended."
            
            if amount <= self.highest_bid:
                return "Bid too low."
            
            self.highest_bid = amount
            self.highest_bidder = user

            self.bid_history.append((user, amount, time.time()))

            return f"Bid accepted: {user} -> {amount}."
        
class AuctionManager:
    __instance = None

    def __new__(cls):
        if cls.__instance is None:
            cls.__instance = super(AuctionManager, cls).__new__(cls)
            cls.__instance.auctions = {}
        return cls.__instance
    
    def create_auction(self, auction_id, item, start_price, duration_seconds):
        auction = Auction(auction_id, item, start_price, duration_seconds)
        self.auctions[auction_id] = auction

        return auction
    
    def get_auction(self, auction_id):
        return self.auctions.get(auction_id)
    
    def place_bid(self, auction_id, user, amount):
        auc = self.get_auction(auction_id)

        if not auc:
            return "Auction not found."
        
        return auc.place_bid(user, amount)
    
    def end_auction(self, auction_id):
        auc = self.get_auction(auction_id)

        if not auc:
            return "Auction not found."
        
        if auc.highest_bidder is None:
            return "No bids placed. Auction ended with no winner."
        
        return f"Winner: {auc.highest_bidder} with bid {auc.highest_bid}"
    
if __name__ == "__main__":

    # Create a single AuctionManager instance
    manager = AuctionManager()

    # Create an auction for a laptop starting at 500, lasting 5 seconds
    auction = manager.create_auction("A1", "Laptop", 500, 5)

    # Alice places a bid of 550 (accepted)
    print(manager.place_bid("A1", "Alice", 550))

    # Bob places a higher bid of 600 (accepted)
    print(manager.place_bid("A1", "Bob", 600))

    # Wait 6 seconds so auction expires
    time.sleep(6)

    # Charlie attempts a bid after auction end (rejected)
    print(manager.place_bid("A1", "Charlie", 700))

    # Print final winner (Bob → 600)
    print(manager.end_auction("A1"))
        

