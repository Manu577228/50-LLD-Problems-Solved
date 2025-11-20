# Design a simple Library Management System that allows adding books, registering users, 
# borrowing and returning books, and displaying available books — all in-memory without 
# using any external database.

class Book:
    def __init__(self, book_id, title, author):
        self.book_id = book_id
        self.title = title
        self.author = author
        self.is_borrowed = False

    def __str__(self):
        status = "Borrowed" if self.is_borrowed else "Available"
        return f"{self.book_id} | {self.title} by {self.author} [{status}]"
    
class User:
    def __init__(self, user_id, name):
        self.user_id = user_id
        self.name = name
        self.borrowed_books = []

    def __str__(self):
        return f"{self.user_id} | {self.name} | Borrowed: {len(self.borrowed_books)}"
    
class Library:
    def __init__(self):
        self.books = {}
        self.users = {}

    def add_book(self, book_id, title, author):
        if book_id not in self.books:
            self.books[book_id] = Book(book_id, title, author)
            print(f"Book '{title}' added successfully!")
        else:
            print("Book ID already exists.")

    def register_user(self, user_id, name):
        if user_id not in self.users:
            self.users[user_id] = User(user_id, name)
            print(f"User '{name}' registered successfully")
        else:
            print("User ID already exists.")

    def borrow_book(self, user_id, book_id):
        if user_id not in self.users:
            print("Invalid User ID.")
            return
        if book_id not in self.books:
            print("Invalid Book ID.")
            return
        
        book = self.books[book_id]
        user = self.users[user_id]

        if book.is_borrowed:
            print(f"Sorry, '{book.title}' is already borrowed.")
        else:
            book.is_borrowed = True
            user.borrowed_books.append(book)
            print(f"'{book.title}' borrowed by {user.name}.")

    def return_book(self, user_id, book_id):
        if user_id not in self.users or book_id not in self.books:
            print("Invalid ID's.")
            return
        
        user = self.users[user_id]
        book = self.books[book_id]

        if book in user.borrowed_books:
            book.is_borrowed = False
            user.borrowed_books.remove(book)
            print(f"'{book.title}' returned by {user.name}.")
        else:
            print("Book not borrowed by the user.")

    def show_books(self):
        print("\n--- Library Books ---")
        for b in self.books.values():
            print(b)

if __name__ == "__main__":
    library = Library()

    # Add sample books
    library.add_book(1, "Atomic Habits", "James Clear")
    library.add_book(2, "Clean Code", "Robert Martin")

    # Register users
    library.register_user(101, "Alice")
    library.register_user(102, "Bob")

    # Borrow & return flow
    library.borrow_book(101, 1)
    library.show_books()

    library.borrow_book(102, 1)
    library.return_book(101, 1)
    library.borrow_book(102, 1)
    library.show_books()



        

        

