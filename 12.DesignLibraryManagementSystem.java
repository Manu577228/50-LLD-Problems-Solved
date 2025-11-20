/* ----------------------------------------------------------------------------  */
/*   ( The Authentic JS/JAVA CodeBuff )
 ___ _                      _              _ 
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                        |__/ 
 */
/* --------------------------------------------------------------------------   */
/*    Youtube: https://youtube.com/@code-with-Bharadwaj                        */
/*    Github : https://github.com/Manu577228                                  */
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio       */
/* -----------------------------------------------------------------------  */

import java.io.*;
import java.util.*;

/* ---------------------- Library Management System ---------------------- */
/* 1) Book, User, Library classes designed in-memory with HashMaps. */
/* 2) Demonstrates add, borrow, return, and list operations concisely. */

class Book {
    int bookId;
    String title;
    String author;
    boolean isBorrowed;

    Book(int bookId, String title, String author) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isBorrowed = false;
    }

    @Override
    public String toString() {
        return bookId + " | " + title + " by " + author + " [" + (isBorrowed ? "Borrowed" : "Available") + "]";
    }
}

class User {
    int userId;
    String name;
    List<Book> borrowedBooks;

    User(int userId, String name) {
        this.userId = userId;
        this.name = name;
        this.borrowedBooks = new ArrayList<>();
    }

    @Override
    public String toString() {
        return userId + " | " + name + " | Borrowed: " + borrowedBooks.size();
    }
}

class Library {
    Map<Integer, Book> books;
    Map<Integer, User> users;

    Library() {
        books = new HashMap<>();
        users = new HashMap<>();
    }

    // Add new book
    void addBook(int bookId, String title, String author) {
        if (!books.containsKey(bookId)) {
            books.put(bookId, new Book(bookId, title, author));
            System.out.println("Book '" + title + "' added successfully!");
        } else System.out.println("Book ID already exists.");
    }

    // Register user
    void registerUser(int userId, String name) {
        if (!users.containsKey(userId)) {
            users.put(userId, new User(userId, name));
            System.out.println("User '" + name + "' registered successfully!");
        } else System.out.println("User ID already exists.");
    }

    // Borrow book
    void borrowBook(int userId, int bookId) {
        if (!users.containsKey(userId)) {
            System.out.println("Invalid User ID.");
            return;
        }
        if (!books.containsKey(bookId)) {
            System.out.println("Invalid Book ID.");
            return;
        }

        Book book = books.get(bookId);
        User user = users.get(userId);

        if (book.isBorrowed)
            System.out.println("Sorry, '" + book.title + "' is already borrowed.");
        else {
            book.isBorrowed = true;
            user.borrowedBooks.add(book);
            System.out.println("'" + book.title + "' borrowed by " + user.name + ".");
        }
    }

    // Return book
    void returnBook(int userId, int bookId) {
        if (!users.containsKey(userId) || !books.containsKey(bookId)) {
            System.out.println("Invalid IDs.");
            return;
        }

        User user = users.get(userId);
        Book book = books.get(bookId);

        if (user.borrowedBooks.contains(book)) {
            book.isBorrowed = false;
            user.borrowedBooks.remove(book);
            System.out.println("'" + book.title + "' returned by " + user.name + ".");
        } else System.out.println("Book not borrowed by user.");
    }

    // Display all books
    void showBooks() {
        System.out.println("\n--- Library Books ---");
        for (Book b : books.values()) System.out.println(b);
    }
}

/* ---------------------- DEMO EXECUTION ---------------------- */

public class LibrarySystemDemo {
    public static void main(String[] args) throws Exception {
        Library library = new Library();

        // Add sample books
        library.addBook(1, "Atomic Habits", "James Clear");
        library.addBook(2, "Clean Code", "Robert Martin");

        // Register users
        library.registerUser(101, "Alice");
        library.registerUser(102, "Bob");

        // Borrow & Return flow
        library.borrowBook(101, 1);
        library.showBooks();

        library.borrowBook(102, 1);
        library.returnBook(101, 1);
        library.borrowBook(102, 1);
        library.showBooks();
    }
}

/* ---------------------- OUTPUT EXPLANATION ----------------------
Book 'Atomic Habits' added successfully!
Book 'Clean Code' added successfully!
User 'Alice' registered successfully!
User 'Bob' registered successfully!
'Atomic Habits' borrowed by Alice.

--- Library Books ---
1 | Atomic Habits by James Clear [Borrowed]
2 | Clean Code by Robert Martin [Available]
Sorry, 'Atomic Habits' is already borrowed.
'Atomic Habits' returned by Alice.
'Atomic Habits' borrowed by Bob.

--- Library Books ---
1 | Atomic Habits by James Clear [Borrowed]
2 | Clean Code by Robert Martin [Available]
----------------------------------------------------------------- */

/* ---------------------- EXPLANATION ----------------------
1) Each Book/User stored in HashMap for O(1) access.
2) Borrow/Return just flips a boolean and updates user list.
3) Library runs fully in-memory with clear readable logic.
----------------------------------------------------------------- */
