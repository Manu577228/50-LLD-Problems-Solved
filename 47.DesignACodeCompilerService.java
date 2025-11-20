/* -------------------------------------------------------- */
/*   ( The Authentic JS/JAVA CodeBuff )
 ___ _                      _              _ 
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                        |__/ 
 */
/* ---------------------------------------------------------   */
/*    Youtube: https://youtube.com/@code-with-Bharadwaj        */
/*    Github :  https://github.com/Manu577228                  */
/* ----------------------------------------------------------- */

import javax.tools.JavaCompiler;                 // JavaCompiler for compiling source files
import javax.tools.ToolProvider;                 // To obtain system Java compiler
import java.io.*;                                // IO utilities: File, PrintWriter, BufferedReader
import java.nio.file.*;                          // Files and Paths utilities
import java.util.*;                              // Collections and utilities
import java.util.concurrent.TimeUnit;            // For process timeout

// Single-file demo: CompilerServiceDemo
public class CompilerServiceDemo {

    // ---------------- Data Model: CodeSnippet ----------------
    public static class CodeSnippet {
        public final int id;                      // snippet id
        public final String code;                 // full java source text

        public CodeSnippet(int id, String code) {
            this.id = id;
            this.code = code;
        }
    }

    // ---------------- Data Model: ExecutionResult -------------
    public static class ExecutionResult {
        public final boolean success;             // true if run succeeded (no compile/runtime error)
        public final String output;               // captured stdout
        public final String error;                // compile or runtime error text

        public ExecutionResult(boolean success, String output, String error) {
            this.success = success;
            this.output = output;
            this.error = error;
        }
    }

    // ---------------- Repository ------------------------------
    public static class Repository {
        private final List<CodeSnippet> snippets = new ArrayList<>();  // in-memory store

        // synchronized add to be thread-safe for concurrent submissions
        public synchronized int add(String code) {
            int id = snippets.size();
            snippets.add(new CodeSnippet(id, code));
            return id;
        }

        // synchronized get to read safely
        public synchronized CodeSnippet get(int id) {
            if (id >= 0 && id < snippets.size()) return snippets.get(id);
            return null;
        }

        // history snapshot (id and first-line preview)
        public synchronized List<String> history() {
            List<String> h = new ArrayList<>();
            for (CodeSnippet s : snippets) {
                String preview = s.code.split("\\R", 2)[0]; // first line
                h.add("id=" + s.id + " preview=\"" + preview + "\"");
            }
            return h;
        }
    }

    // ---------------- Execution Engine ------------------------
    public static class ExecutionEngine {

        // compile sourceFile and run with timeoutSeconds, return ExecutionResult
        public ExecutionResult compileAndRun(Path srcFile, String className, int timeoutSeconds) {
            // compile using system JavaCompiler
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) { // no compiler available (likely running on JRE)
                return new ExecutionResult(false, "", "JavaCompiler not available. Run with a JDK, not JRE.");
            }

            // prepare compilation output directory (same dir as source)
            Path workDir = srcFile.getParent();
            StringWriter compileErrors = new StringWriter();
            int compileResult = compiler.run(null, null, new PrintWriter(compileErrors), srcFile.toString());

            if (compileResult != 0) {
                // compilation failed -> return errors
                return new ExecutionResult(false, "", compileErrors.toString());
            }

            // if compilation succeeded, run the class using 'java' process
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", workDir.toString(), className);
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(true); // merge stderr with stdout

            try {
                Process proc = pb.start();

                // read process output asynchronously (but simple buffered read is fine here)
                StringBuilder out = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    // wait with timeout
                    boolean finished = proc.waitFor(timeoutSeconds, TimeUnit.SECONDS);
                    // drain output (non-blocking if process finished)
                    while (reader.ready()) {
                        out.append(reader.readLine()).append(System.lineSeparator());
                    }
                    if (!finished) {
                        proc.destroyForcibly(); // kill long-running process
                        return new ExecutionResult(false, out.toString(),
                                "Timeout / Possible infinite loop (killed after " + timeoutSeconds + "s).");
                    }
                }

                int exit = proc.exitValue();
                if (exit != 0) {
                    // non-zero exit but output captured in out
                    return new ExecutionResult(false, out.toString(), "Process exited with code " + exit);
                }
                return new ExecutionResult(true, out.toString(), "");

            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ExecutionResult(false, "", "Execution failed: " + e.getMessage());
            } finally {
                // cleanup: class and source cleanup is caller's responsibility (we do not delete here)
            }
        }
    }

    // ---------------- Facade: CompilerService -----------------
    public static class CompilerService {
        private final Repository repo = new Repository();
        private final ExecutionEngine engine = new ExecutionEngine();

        // Submit code -> returns snippet id
        public int submit(String code) {
            return repo.add(code);
        }

        // Run snippet by id with timeoutSeconds -> returns ExecutionResult
        public ExecutionResult run(int id, int timeoutSeconds) {
            CodeSnippet s = repo.get(id);
            if (s == null) return new ExecutionResult(false, "", "Snippet not found");

            // Determine class name: require user to provide a public class with same name
            // For demo convenience, we attempt to extract "public class X" name,
            // if absent, we wrap user code into public class AutoClass{ public static void main... }
            String source = s.code;
            String className = extractPublicClassName(source);
            boolean wrapped = false;

            if (className == null) {
                // wrap the code into a public class named AutoClass<ID>
                className = "AutoClass" + id;
                String wrappedSource = "public class " + className + " {\n"
                        + "    public static void main(String[] args) throws Exception {\n"
                        + indent(source, "        ") + "\n"
                        + "    }\n"
                        + "}\n";
                source = wrappedSource;
                wrapped = true;
            }

            // create temp directory for this run
            try {
                Path tempDir = Files.createTempDirectory("cs_demo_" + id + "_");
                Path srcFile = tempDir.resolve(className + ".java");

                // write source file using PrintWriter / BufferedWriter
                try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(srcFile))) {
                    pw.write(source);
                }

                // compile and run
                ExecutionResult result = engine.compileAndRun(srcFile, className, timeoutSeconds);

                // cleanup: delete class files and source
                try {
                    // delete recursively
                    Files.walk(tempDir)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException ignored) {
                }

                return result;

            } catch (IOException e) {
                return new ExecutionResult(false, "", "IO error preparing run: " + e.getMessage());
            }
        }

        // Return history preview list
        public List<String> history() {
            return repo.history();
        }

        // Helper to extract public class name from source (very simple regex-like scan)
        private static String extractPublicClassName(String src) {
            String[] lines = src.split("\\R");
            for (String ln : lines) {
                ln = ln.trim();
                if (ln.startsWith("public class ")) {
                    String remainder = ln.substring("public class ".length()).trim();
                    String[] parts = remainder.split("\\s|\\{");
                    if (parts.length > 0) return parts[0];
                }
            }
            return null;
        }

        // Helper to indent multiple lines
        private static String indent(String text, String prefix) {
            String[] lines = text.split("\\R");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                sb.append(prefix).append(lines[i]);
                if (i + 1 < lines.length) sb.append("\n");
            }
            return sb.toString();
        }
    }

    // ---------------- Demo main ------------------------------
    public static void main(String[] args) {
        // create service instance
        CompilerService service = new CompilerService();

        // Example 1: simple print (no public class required)
        String code1 = "System.out.println(\"Hello Compiler Service!\");";
        int id1 = service.submit(code1);
        System.out.println("Submitted id=" + id1);

        // Run snippet with 3-second timeout
        ExecutionResult res1 = service.run(id1, 3);
        System.out.println("Success: " + res1.success);
        System.out.println("Output:\n" + res1.output);
        System.out.println("Error:\n" + res1.error);

        // Example 2: user provides full public class
        String code2 =
                "public class MyApp {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello from MyApp\");\n" +
                "    }\n" +
                "}\n";
        int id2 = service.submit(code2);
        System.out.println("Submitted id=" + id2);

        ExecutionResult res2 = service.run(id2, 3);
        System.out.println("Success: " + res2.success);
        System.out.println("Output:\n" + res2.output);
        System.out.println("Error:\n" + res2.error);

        // Show history
        System.out.println("History:");
        for (String h : service.history()) System.out.println(h);
    }
}
