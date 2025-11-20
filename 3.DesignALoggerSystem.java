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
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio      */
/* -----------------------------------------------------------------------  */

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/* ------------------------ ENUM for Log Levels ------------------------ */
enum Level {
    DEBUG(10), INFO(20), WARNING(30), ERROR(40), CRITICAL(50);
    private final int value;
    Level(int v) { this.value = v; }
    public int getValue() { return value; }
}

/* ------------------------ LogRecord Class ------------------------ */
class LogRecord {
    String loggerName;
    Level level;
    String message;
    Map<String, Object> metadata;
    long timestamp;
    long seq;

    LogRecord(String name, Level lvl, String msg, Map<String, Object> meta) {
        this.loggerName = name;
        this.level = lvl;
        this.message = msg;
        this.metadata = meta != null ? meta : new HashMap<>();
        this.timestamp = System.currentTimeMillis();
        this.seq = System.identityHashCode(this);
    }
}

/* ------------------------ Formatter ------------------------ */
class Formatter {
    private final String fmt;

    Formatter(String fmt) {
        this.fmt = fmt;
    }

    String format(LogRecord record) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String ts = sdf.format(new Date(record.timestamp));
        StringBuilder metaStr = new StringBuilder();
        for (Map.Entry<String, Object> e : record.metadata.entrySet()) {
            metaStr.append(e.getKey()).append("=").append(e.getValue()).append(" ");
        }
        return fmt
                .replace("{asctime}", ts)
                .replace("{level}", record.level.name())
                .replace("{name}", record.loggerName)
                .replace("{msg}", record.message)
                .replace("{meta}", metaStr.toString().trim());
    }
}

/* ------------------------ Handler Base Class ------------------------ */
abstract class Handler {
    protected Level level;
    protected Formatter formatter;

    Handler(Level lvl, Formatter fmt) {
        this.level = lvl;
        this.formatter = fmt != null ? fmt : new Formatter("{asctime} [{level}] {name}: {msg} {meta}");
    }

    void handle(LogRecord record) {
        if (record.level.getValue() >= level.getValue()) {
            try {
                emit(record);
            } catch (Exception e) {
                System.err.println("Handler error: " + e.getMessage());
            }
        }
    }

    abstract void emit(LogRecord record) throws Exception;
}

/* ------------------------ ConsoleHandler ------------------------ */
class ConsoleHandler extends Handler {
    ConsoleHandler(Level lvl, Formatter fmt) {
        super(lvl, fmt);
    }

    @Override
    void emit(LogRecord record) {
        System.out.println(formatter.format(record));
    }
}

/* ------------------------ RotatingFileHandler ------------------------ */
class RotatingFileHandler extends Handler {
    private final String filename;
    private final long maxBytes;
    private final int backupCount;
    private BufferedWriter writer;

    RotatingFileHandler(String filename, long maxBytes, int backupCount, Level lvl, Formatter fmt) throws IOException {
        super(lvl, fmt);
        this.filename = filename;
        this.maxBytes = maxBytes;
        this.backupCount = backupCount;
        openFile();
    }

    private void openFile() throws IOException {
        File file = new File(filename);
        file.getParentFile().mkdirs();
        this.writer = new BufferedWriter(new FileWriter(file, true));
    }

    private boolean shouldRotate() {
        File f = new File(filename);
        return f.exists() && f.length() >= maxBytes;
    }

    private void rotate() throws IOException {
        writer.close();
        for (int i = backupCount - 1; i >= 1; i--) {
            File src = new File(filename + "." + i);
            File dest = new File(filename + "." + (i + 1));
            if (src.exists()) src.renameTo(dest);
        }
        File current = new File(filename);
        if (current.exists()) current.renameTo(new File(filename + ".1"));
        openFile();
    }

    @Override
    void emit(LogRecord record) throws IOException {
        writer.write(formatter.format(record));
        writer.newLine();
        writer.flush();
        if (shouldRotate()) rotate();
    }
}

/* ------------------------ AsyncDispatcher ------------------------ */
class AsyncDispatcher {
    private final BlockingQueue<LogRecord> queue;
    private final List<Handler> handlers;
    private final int batchSize;
    private final long flushIntervalMs;
    private final String dropPolicy;
    private final Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private int dropped = 0;

    AsyncDispatcher(List<Handler> handlers, int queueSize, int batchSize, long flushIntervalMs, String dropPolicy) {
        this.handlers = handlers;
        this.queue = new ArrayBlockingQueue<>(queueSize);
        this.batchSize = batchSize;
        this.flushIntervalMs = flushIntervalMs;
        this.dropPolicy = dropPolicy;
        this.worker = new Thread(this::runWorker, "LoggerWorker");
        this.worker.setDaemon(true);
    }

    void start() {
        running.set(true);
        worker.start();
    }

    void stop() {
        running.set(false);
        try {
            worker.join();
        } catch (InterruptedException ignored) {}
    }

    boolean enqueue(LogRecord record) {
        try {
            if ("drop_oldest".equals(dropPolicy)) {
                while (!queue.offer(record)) {
                    queue.poll(); // drop oldest
                    dropped++;
                }
            } else if ("drop_new".equals(dropPolicy)) {
                if (!queue.offer(record)) {
                    dropped++;
                    return false;
                }
            } else { // block briefly
                queue.offer(record, 100, TimeUnit.MILLISECONDS);
            }
            return true;
        } catch (Exception e) {
            dropped++;
            return false;
        }
    }

    private void runWorker() {
        List<LogRecord> batch = new ArrayList<>();
        long lastFlush = System.currentTimeMillis();

        while (running.get() || !queue.isEmpty()) {
            try {
                LogRecord rec = queue.poll(flushIntervalMs, TimeUnit.MILLISECONDS);
                if (rec != null) batch.add(rec);
                if (!batch.isEmpty() && (batch.size() >= batchSize ||
                        System.currentTimeMillis() - lastFlush >= flushIntervalMs)) {
                    flush(batch);
                    batch.clear();
                    lastFlush = System.currentTimeMillis();
                }
            } catch (Exception ignored) {}
        }
        if (!batch.isEmpty()) flush(batch);
    }

    private void flush(List<LogRecord> batch) {
        for (LogRecord rec : batch) {
            for (Handler h : handlers) {
                h.handle(rec);
            }
        }
    }

    int getDroppedCount() {
        return dropped;
    }
}

/* ------------------------ Logger ------------------------ */
class Logger {
    private final String name;
    private final Level level;
    private final AsyncDispatcher dispatcher;

    Logger(String name, Level lvl, AsyncDispatcher disp) {
        this.name = name;
        this.level = lvl;
        this.dispatcher = disp;
    }

    private void log(Level lvl, String msg, Map<String, Object> meta) {
        if (lvl.getValue() < level.getValue()) return;
        dispatcher.enqueue(new LogRecord(name, lvl, msg, meta));
    }

    void debug(String msg, Map<String, Object> meta) { log(Level.DEBUG, msg, meta); }
    void info(String msg, Map<String, Object> meta) { log(Level.INFO, msg, meta); }
    void warning(String msg, Map<String, Object> meta) { log(Level.WARNING, msg, meta); }
    void error(String msg, Map<String, Object> meta) { log(Level.ERROR, msg, meta); }
    void critical(String msg, Map<String, Object> meta) { log(Level.CRITICAL, msg, meta); }
}

/* ------------------------ DEMO MAIN ------------------------ */
public class LoggerSystem {
    public static void main(String[] args) throws Exception {
        Formatter fmt = new Formatter("{asctime} [{level}] {name}: {msg} {meta}");
        ConsoleHandler console = new ConsoleHandler(Level.DEBUG, fmt);
        RotatingFileHandler fileh = new RotatingFileHandler("logs/app.log", 2048, 3, Level.DEBUG, fmt);

        AsyncDispatcher dispatcher = new AsyncDispatcher(
                Arrays.asList(console, fileh), 200, 20, 200, "drop_oldest"
        );
        dispatcher.start();

        Logger logger = new Logger("MyApp", Level.DEBUG, dispatcher);

        ExecutorService pool = Executors.newFixedThreadPool(3);
        for (int t = 0; t < 3; t++) {
            int tid = t;
            pool.submit(() -> {
                for (int i = 0; i < 50; i++) {
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("thread", tid);
                    meta.put("i", i);
                    logger.info("message " + i + " from thread " + tid, meta);
                    try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(3, TimeUnit.SECONDS);

        Thread.sleep(500);
        dispatcher.stop();
        System.out.println("Demo complete. Dropped messages: " + dispatcher.getDroppedCount());
    }
}
