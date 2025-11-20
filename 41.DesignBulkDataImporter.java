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
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio        */
/* -----------------------------------------------------------------------  */

import java.io.*;                        // For file reading
import java.util.*;                      // For Lists, Maps
import java.util.concurrent.*;           // For ExecutorService & threads

// ---------------- In-Memory DataStore ----------------

// Thread-safe list to simulate database
class DataStore {
    private static final List<Map<String, Object>> DB =
            Collections.synchronizedList(new ArrayList<>());

    // Save a validated record
    public static void save(Map<String, Object> record) {
        DB.add(record); // synchronized list ensures thread safety
    }

    // Return all stored records
    public static List<Map<String, Object>> listAll() {
        return DB;
    }
}


// ---------------- BaseImporter (Template Method Pattern) ----------------

// Defines high-level flow: load → validate → process → retry → report
abstract class BaseImporter {
    protected String filePath;
    protected List<Map<String, Object>> failedRecords = new ArrayList<>();

    public BaseImporter(String path) {
        this.filePath = path;                   // Save file path
    }

    abstract List<Map<String, Object>> load() throws Exception;  // Load file data
    abstract boolean validate(Map<String, Object> record);        // Validate data

    // Default process simply stores to DataStore
    public void process(Map<String, Object> record) {
        DataStore.save(record);
    }

    // The main template method
    public Map<String, Object> run() throws Exception {

        List<Map<String, Object>> records = load();               // Load file

        ExecutorService service = Executors.newFixedThreadPool(4); // Thread pool

        // Submit validation tasks for parallel processing
        for (Map<String, Object> rec : records) {
            service.submit(() -> {
                if (validate(rec)) {
                    process(rec);                                 // Save if valid
                } else {
                    synchronized (failedRecords) {
                        failedRecords.add(rec);                   // Collect failures
                    }
                }
            });
        }

        service.shutdown();                                       // Stop new tasks
        service.awaitTermination(5, TimeUnit.SECONDS);            // Wait for threads

        // Retry failed records once more
        List<Map<String, Object>> retryFails = new ArrayList<>();
        for (Map<String, Object> rec : failedRecords) {
            if (validate(rec)) {
                process(rec);
            } else {
                retryFails.add(rec);
            }
        }

        // Prepare summary output
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", records.size());
        summary.put("success", records.size() - retryFails.size());
        summary.put("failed", retryFails.size());
        summary.put("data", DataStore.listAll());

        return summary;
    }
}


// ---------------- CSV Importer Strategy ----------------

class CSVImporter extends BaseImporter {

    public CSVImporter(String path) {
        super(path);
    }

    @Override
    List<Map<String, Object>> load() throws Exception {
        List<Map<String, Object>> out = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String headerLine = br.readLine();                // Read header
        String[] headers = headerLine.split(",");         // Split column names

        String line;
        while ((line = br.readLine()) != null) {          // Read each row
            String[] parts = line.split(",");
            Map<String, Object> row = new HashMap<>();

            for (int i = 0; i < headers.length; i++) {
                row.put(headers[i], parts.length > i ? parts[i] : "");
            }
            out.add(row);
        }

        br.close();
        return out;                                       // Return all rows
    }

    @Override
    boolean validate(Map<String, Object> record) {
        return record.get("id") != null && !record.get("id").toString().isEmpty();
    }
}


// ---------------- JSON Importer Strategy ----------------

class JSONImporter extends BaseImporter {

    public JSONImporter(String path) {
        super(path);
    }

    @Override
    List<Map<String, Object>> load() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);                               // Build JSON string
        }
        br.close();

        // VERY simple JSON array parser (no libs allowed)
        // Format expected: [ {"id": "1"}, {"id": "2"} ]
        String json = sb.toString().trim();

        List<Map<String, Object>> list = new ArrayList<>();

        json = json.substring(1, json.length() - 1);        // Remove [ ]
        String[] objects = json.split("\\},\\s*\\{");

        for (String obj : objects) {
            String clean = obj.replace("{", "").replace("}", "");

            Map<String, Object> map = new HashMap<>();
            String[] fields = clean.split(",");

            for (String f : fields) {
                String[] kv = f.split(":");
                map.put(kv[0].replace("\"","").trim(),
                        kv[1].replace("\"","").trim());
            }
            list.add(map);
        }
        return list;
    }

    @Override
    boolean validate(Map<String, Object> record) {
        return record.containsKey("id");
    }
}


// ---------------- Importer Factory ----------------

class ImporterFactory {
    public static BaseImporter getImporter(String filePath) {
        if (filePath.endsWith(".csv")) return new CSVImporter(filePath);
        if (filePath.endsWith(".json")) return new JSONImporter(filePath);
        throw new RuntimeException("Unsupported file format");
    }
}


// ---------------- DEMO MAIN ----------------

public class BulkImporter {
    public static void main(String[] args) throws Exception {

        // Create sample CSV
        FileWriter fw = new FileWriter("input.csv");
        fw.write("id,name\n1,Alice\n,Invalid\n2,Bob\n");
        fw.close();

        // Use factory to load correct importer
        BaseImporter importer = ImporterFactory.getImporter("input.csv");

        // Run import
        Map<String, Object> output = importer.run();

        // Print final summary
        System.out.println("=== Import Summary ===");
        System.out.println(output);
    }
}
