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

/*
------------------------------------------------------------
       LLD : Movie / Video Streaming System
------------------------------------------------------------

1) a) Functional Requirements
   ----------------------------
   - Users can browse and search movies.
   - Users can play, pause, and stop videos.
   - Admin can upload or remove movies.
   - System should recommend movies based on genre.
   - Maintain basic playback history for a user.

   b) Non-Functional Requirements
   -------------------------------
   - High availability and responsiveness.
   - Scalability to handle multiple concurrent users.
   - Data consistency for movie catalog.
   - Modular and easily extensible design.

------------------------------------------------------------
2) Algorithm Choice & Design Pattern Choice Discussion
------------------------------------------------------------
- Algorithm Choice:
  - Linear Search for simplicity (no DB indexing).
  - FIFO Queue for recent playback history.
- Design Pattern:
  - Singleton Pattern → For StreamingServer (only one instance).
  - Factory Pattern → To create Movie objects dynamically.
  - Observer Pattern (conceptual) → Could be used for playback status notifications.

------------------------------------------------------------
3) Concurrency and Data Model Discussion
------------------------------------------------------------
- Concurrency:
  - Thread-based simulation for multiple playback sessions.
- Data Model:
  - In-memory storage using lists and maps.
  - No external database; uses in-memory mock catalog.

------------------------------------------------------------
4) UML Diagram (ASCII Representation)
------------------------------------------------------------
           ┌─────────────────────────┐
           │        User             │
           └───────┬─────────────────┘
                   │
                   │ interacts with
                   ▼
        ┌────────────────────────────┐
        │     StreamingServer        │
        └───────┬────────────────────┘
                │
      ┌─────────┴──────────┐
      ▼                    ▼
┌──────────────┐    ┌──────────────┐
│   Movie       │    │  Playback    │
└──────────────┘    └──────────────┘

------------------------------------------------------------
5) Correct Java Solution with Explanation & Output
------------------------------------------------------------
*/

import java.util.*;
import java.io.*;

class Movie {
    String title;
    String genre;
    int duration;

    Movie(String title, String genre, int duration) {
        this.title = title;
        this.genre = genre;
        this.duration = duration;
    }
}

// ---------------- Factory Pattern ----------------
class MovieFactory {
    public static Movie createMovie(String title, String genre, int duration) {
        return new Movie(title, genre, duration);
    }
}

// ---------------- Singleton Pattern ----------------
class StreamingServer {
    private static StreamingServer instance;
    private List<Movie> catalog;
    private Map<String, User> users;

    private StreamingServer() {
        catalog = new ArrayList<>();
        users = new HashMap<>();
    }

    public static synchronized StreamingServer getInstance() {
        if (instance == null) {
            instance = new StreamingServer();
        }
        return instance;
    }

    public void uploadMovie(Movie movie) {
        catalog.add(movie);
        System.out.println("Uploaded: " + movie.title);
    }

    public void removeMovie(String title) {
        catalog.removeIf(m -> m.title.equalsIgnoreCase(title));
        System.out.println("Removed: " + title);
    }

    public List<Movie> searchMovie(String keyword) {
        List<Movie> result = new ArrayList<>();
        for (Movie m : catalog) {
            if (m.title.toLowerCase().contains(keyword.toLowerCase())) {
                result.add(m);
            }
        }
        return result;
    }
}

// ---------------- Playback and User ----------------
class Playback implements Runnable {
    Movie movie;
    String status;

    Playback(Movie movie) {
        this.movie = movie;
        this.status = "Stopped";
    }

    public void play() {
        status = "Playing";
        System.out.println("▶️  Now Playing: " + movie.title);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        stop();
    }

    public void pause() {
        status = "Paused";
        System.out.println("⏸️  Paused: " + movie.title);
    }

    public void stop() {
        status = "Stopped";
        System.out.println("⏹️  Stopped: " + movie.title);
    }

    @Override
    public void run() {
        play();
    }
}

class User {
    String username;
    List<String> history;

    User(String username) {
        this.username = username;
        this.history = new LinkedList<>();
    }

    public void watch(Movie movie) {
        Playback playback = new Playback(movie);
        Thread playbackThread = new Thread(playback);
        playbackThread.start();
        try {
            playbackThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        history.add(movie.title);
    }

    public void showHistory() {
        System.out.println("User Playback History: " + history);
    }
}

// ---------------- Main Demo ----------------
public class MovieStreamingSystem {
    public static void main(String[] args) {
        System.out.println("\n--- Movie Streaming System Demo ---\n");

        StreamingServer server = StreamingServer.getInstance();

        Movie m1 = MovieFactory.createMovie("Inception", "Sci-Fi", 148);
        Movie m2 = MovieFactory.createMovie("Interstellar", "Sci-Fi", 169);
        Movie m3 = MovieFactory.createMovie("The Dark Knight", "Action", 152);

        server.uploadMovie(m1);
        server.uploadMovie(m2);
        server.uploadMovie(m3);

        User user = new User("Bharadwaj");

        List<Movie> results = server.searchMovie("Inception");
        if (!results.isEmpty()) {
            user.watch(results.get(0));
        }

        user.showHistory();
    }
}

/*
Expected Output:
----------------
--- Movie Streaming System Demo ---

Uploaded: Inception
Uploaded: Interstellar
Uploaded: The Dark Knight
▶️  Now Playing: Inception
⏹️  Stopped: Inception
User Playback History: [Inception]
*/

 /*
------------------------------------------------------------
6) Limitations of Current Code
------------------------------------------------------------
- No real video streaming or network layer simulation.
- No recommendation engine beyond basic search.
- No authentication or role-based access.
- Thread safety not enforced for shared resources.
- Purely in-memory, no persistent storage.

------------------------------------------------------------
7) Alternative Algorithms and Trade Off's (Future Discussions)
------------------------------------------------------------
- Search Optimization: Use Trie or Hash Index.
- Recommendation System: Collaborative Filtering / ML models.
- Concurrency: Use Executors or Reactive Streams.
- Storage: Add lightweight DB like SQLite for persistence.
- Trade-Off: Simple design but not scalable for production.
------------------------------------------------------------
*/
