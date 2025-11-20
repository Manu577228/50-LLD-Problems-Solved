# Design a Dependency Injection–based Counter system where the counter’s 
# storage mechanism is fully replaceable using an interface. 
# Implement an in-memory storage, inject it into the Counter, and 
# demonstrate increment and read operations in a single-file Python program.

from abc import ABC, abstractmethod

class IStorage:

    @abstractmethod
    def get_value(self):
        pass

    @abstractmethod
    def set_value(self, v):
        pass

class InMemoryStorage(IStorage):
    def __init__(self):
        self.value = 0

    def get_value(self):
        return self.value
    
    def set_value(self, v):
        self.value = v

class Counter:
    def __init__(self, storage: IStorage):
        self.storage = storage

    def increment(self):
        current = self.storage.get_value()
        self.storage.set_value(current + 1)

    def value(self):
        return self.storage.get_value()
    
if __name__ == "__main__":                   # run demo only when file executed directly
    storage = InMemoryStorage()               # create an instance of InMemoryStorage
    counter = Counter(storage)                # inject storage into Counter via constructor

    counter.increment()                       # increment once (0 -> 1)
    counter.increment()                       # increment again (1 -> 2)
    counter.increment()                       # increment again (2 -> 3)

    print("Final Counter Value:", counter.value())  # print the final counter value (expected: 3)

        
