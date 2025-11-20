# Design a File Uploader Utility that allows users to upload, list, and delete files — 
# all stored in-memory. The system must support file size validation (max 5 MB) and 
# handle concurrent uploads safely without using any external database.

import threading
import time
import sys

class FileUploader:
    def __init__(self, max_size = 5 * 1024 * 1024):
        self.storage = {}
        self.lock = threading.Lock()
        self.max_size = max_size

    def upload(self, name, data):
        with self.lock:
            size = sys.getsizeof(data)
            if size > self.max_size:
                print(f"Upload failed: '{name}' exceeds {self.max_size/1024/1024:.1f} MB limit")
                return
            if name in self.storage:
                print(f" File '{name}' already exists. Overwriting...")

            self.storage[name] = {
                "data": data,
                "size": size,
                "timestamp": time.ctime()
            }

            print(f" Upload '{name}' ({size} bytes) at {self.storage[name]['timestamp']}")

    def list_files(self):
        with self.lock:
            if not self.storage:
                print("No Files Uploaded yet.")
                return
            print("\n Uploaded Files:")
            for name, meta in self.storage.items():
                print(f" - {name} | {meta['size']} bytes | Uploaded: {meta['timestamp']}")

    def delete(self, name):
        with self.lock:
            if name in self.storage:
                del self.storage[name]
                print(f" Deleted File '{name}'")
            else:
                print(f" File '{name}' not found")

if __name__ == "__main__":
    uploader = FileUploader()  # create FileUploader instance

    # upload two normal files
    uploader.upload("demo.txt", "Hello Bharadwaj!")  
    uploader.upload("data.json", '{"user":"Bharadwaj","lang":"Python"}')

    # display uploaded files
    uploader.list_files()

    # attempt to upload a 6MB oversized file
    uploader.upload("big_file.bin", "x" * (6 * 1024 * 1024))  

    # delete one file
    uploader.delete("demo.txt")

    # list remaining files
    uploader.list_files()

        

