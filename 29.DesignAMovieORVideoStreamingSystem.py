# Design a simplified Movie / Video Streaming System where users can search and
# watch movies, admins can upload or remove movies, and the system maintains
# playback history per user — all using in-memory storage
# (no database) and Singleton + Factory design patterns for modularity.

from threading import Thread
import time


class Movie:
    def __init__(self, title, genre, duration):
        self.title = title
        self.genre = genre
        self.duration = duration


class MovieFactory:
    @staticmethod
    def create_movie(title, genre, duration):
        return Movie(title, genre, duration)


class StreamingServer:
    __instance = None

    def __init__(self):
        if StreamingServer.__instance is not None:
            raise Exception("Use get_instance() to access StreamingServer")
        self.catalog = []
        self.users = {}
        StreamingServer.__instance = self

    @staticmethod
    def get_instance():
        if StreamingServer.__instance is None:
            StreamingServer()
        return StreamingServer.__instance

    def upload_movie(self, movie):
        self.catalog.append(movie)
        print(f"Uploaded: {movie.title}")

    def remove_movie(self, title):
        self.catalog = [m for m in self.catalog if m.title != title]
        print(f"Removed: {title}")

    def search_movie(self, keyword):
        return [m for m in self.catalog if keyword.lower() in m.title.lower()]

class Playback:
    def __init__(self, movie):
        self.movie = movie
        self.status = "Stopped"

    def play(self):
        self.status = "Playing"
        print(f" Now Playing: {self.movie.title}")
        time.sleep(1)

    def pause(self):
        self.status = "Paused"
        print(f" Paused: {self.movie.title}")

    def stop(self):
        self.status = "Stopped"
        print(f" Stopped: {self.movie.title}")

class User:
    def __init__(self, username):
        self.username = username
        self.history = []

    def watch(self, movie):
        playback = Playback(movie)
        playback_thread = Thread(target=playback.play)
        playback_thread.start()
        playback_thread.join()
        self.history.append(movie.title)

if __name__ == "__main__":
    print("\n--- Movie Streaming System Demo ---\n")

    # Get singleton instance of streaming server
    server = StreamingServer.get_instance()

    # Admin uploads few movies using MovieFactory
    m1 = MovieFactory.create_movie("Inception", "Sci-Fi", 148)
    m2 = MovieFactory.create_movie("Interstellar", "Sci-Fi", 169)
    m3 = MovieFactory.create_movie("The Dark Knight", "Action", 152)

    server.upload_movie(m1)
    server.upload_movie(m2)
    server.upload_movie(m3)

    # Create a user
    user = User("Bharadwaj")

    # Search for a movie by keyword
    results = server.search_movie("Inception")

    # If found, user watches it
    if results:
        user.watch(results[0])

    # Display user's playback history
    print("\nUser Playback History:", user.history)
        
        