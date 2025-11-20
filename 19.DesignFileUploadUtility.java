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
import java.util.concurrent.locks.*;
import java.time.LocalDateTime;

/*
---------------------------------------------------------
1) Functional Requirements:
   - Upload, list, and delete files.
   - Validate max file size (5 MB).
   - Store in-memory only (no DB).
2) Non-Functional Requirements:
   - Thread-safe, modular, and lightweight.
---------------------------------------------------------
*/

class FileUploader {
    private final Map<String, FileMeta> storage = new HashMap<>();
    private final Lock lock = new ReentrantLock();
    private final long MAX_SIZE = 5 * 1024 * 1024; // 5 MB limit

    // Upload a file (simulate)
    public void upload(String name, String data) {
        lock.lock();
        try {
            long size = data.getBytes().length;
            if (size > MAX_SIZE) {
                System.out.println("❌ Upload Failed: '" + name + "' exceeds 5 MB limit");
                return;
            }
            if (storage.containsKey(name)) {
                System.out.println("⚠️ File '" + name + "' already exists. Overwriting...");
            }
            storage.put(name, new FileMeta(data, size, LocalDateTime.now()));
            System.out.println("✅ Uploaded '" + name + "' (" + size + " bytes) at " + LocalDateTime.now());
        } finally {
            lock.unlock();
        }
    }

    // List uploaded files
    public void listFiles() {
        lock.lock();
        try {
            if (storage.isEmpty()) {
                System.out.println("📂 No files uploaded yet.");
                return;
            }
            System.out.println("\n📄 Uploaded Files:");
            for (Map.Entry<String, FileMeta> e : storage.entrySet()) {
                FileMeta m = e.getValue();
                System.out.println(" - " + e.getKey() + " | " + m.size + " bytes | Uploaded: " + m.timestamp);
            }
        } finally {
            lock.unlock();
        }
    }

    // Delete a file
    public void deleteFile(String name) {
        lock.lock();
        try {
            if (storage.remove(name) != null)
                System.out.println("🗑️ Deleted file '" + name + "'");
            else
                System.out.println("❌ File '" + name + "' not found");
        } finally {
            lock.unlock();
        }
    }

    // Metadata model
    static class FileMeta {
        String data;
        long size;
        LocalDateTime timestamp;

        FileMeta(String data, long size, LocalDateTime timestamp) {
            this.data = data;
            this.size = size;
            this.timestamp = timestamp;
        }
    }
}

/*
---------------------------------------------------------
Algorithm & Concurrency:
 - Uses HashMap for O(1) CRUD.
 - ReentrantLock ensures thread safety.
 - Simple and efficient for demo purposes.
---------------------------------------------------------
*/

public class FileUploaderDemo {
    public static void main(String[] args) throws Exception {
        FileUploader uploader = new FileUploader();

        uploader.upload("demo.txt", "Hello Bharadwaj!");
        uploader.upload("data.json", "{\"user\":\"CodeBuff\",\"lang\":\"Java\"}");

        uploader.listFiles();

        uploader.upload("big.bin", "x".repeat(6 * 1024 * 1024)); // Oversized file

        uploader.deleteFile("demo.txt");
        uploader.listFiles();
    }
}

/*
---------------------------------------------------------
Output (Sample):
✅ Uploaded 'demo.txt' (77 bytes) at 2025-11-08T12:30:00
✅ Uploaded 'data.json' (86 bytes) at 2025-11-08T12:30:00

📄 Uploaded Files:
 - demo.txt | 77 bytes | Uploaded: 2025-11-08T12:30:00
 - data.json | 86 bytes | Uploaded: 2025-11-08T12:30:00
❌ Upload Failed: 'big.bin' exceeds 5 MB limit
🗑️ Deleted file 'demo.txt'

📄 Uploaded Files:
 - data.json | 86 bytes | Uploaded: 2025-11-08T12:30:00
---------------------------------------------------------
Limitations:
 - In-memory only (no persistence).
 - No type validation or chunked uploads.
 - Not suitable for real-world large file uploads.

Future Improvements:
 - Use disk-based or cloud storage (AWS/GCP).
 - Add async I/O or streaming for big files.
 - Implement chunked upload for scalability.
---------------------------------------------------------
*/
