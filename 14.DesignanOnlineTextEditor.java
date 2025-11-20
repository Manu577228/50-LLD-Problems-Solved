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

import java.util.*;

// Command Pattern: Represents a reversible text operation
interface Command {
    void execute();  // apply the operation
    void undo();     // revert the operation
}

// Concrete Command implemented using Lambdas
class TextCommand implements Command {
    private Runnable executeAction;
    private Runnable undoAction;

    // Constructor to bind actions
    public TextCommand(Runnable executeAction, Runnable undoAction) {
        this.executeAction = executeAction;
        this.undoAction = undoAction;
    }

    // Execute command
    public void execute() {
        executeAction.run();
    }

    // Undo command
    public void undo() {
        undoAction.run();
    }
}

// Main TextEditor class
public class OnlineTextEditor {
    // Stores all user documents
    private Map<String, Map<String, String>> documents;
    // Undo and Redo stacks
    private Deque<Command> undoStack;
    private Deque<Command> redoStack;

    public OnlineTextEditor() {
        documents = new HashMap<>();
        undoStack = new ArrayDeque<>();
        redoStack = new ArrayDeque<>();
    }

    // Create a new document
    public void createDoc(String user, String docName, String text) {
        documents.putIfAbsent(user, new HashMap<>());
        documents.get(user).put(docName, text);
        System.out.println("Document '" + docName + "' created for " + user + ".");
    }

    // Edit a document and store command for undo/redo
    public void editDoc(String user, String docName, String newText) {
        String prevText = documents.get(user).get(docName);

        // Define execute and undo using lambdas
        Runnable execute = () -> documents.get(user).put(docName, newText);
        Runnable undo = () -> documents.get(user).put(docName, prevText);

        // Create command and execute
        Command cmd = new TextCommand(execute, undo);
        cmd.execute();

        // Push to undo stack
        undoStack.push(cmd);
        // Clear redo stack as new edit resets redo history
        redoStack.clear();

        System.out.println("Edited '" + docName + "' for " + user + ".");
    }

    // Undo the last operation
    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("Nothing to undo.");
            return;
        }
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        System.out.println("Undo operation performed.");
    }

    // Redo the last undone operation
    public void redo() {
        if (redoStack.isEmpty()) {
            System.out.println("Nothing to redo.");
            return;
        }
        Command cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        System.out.println("Redo operation performed.");
    }

    // Display current document text
    public void showDoc(String user, String docName) {
        System.out.println("Current Text of '" + docName + "': " + documents.get(user).get(docName));
    }

    // -----------------------
    //      DEMO MAIN
    // -----------------------
    public static void main(String[] args) {
        OnlineTextEditor editor = new OnlineTextEditor();

        editor.createDoc("user1", "doc1", "Hello");
        editor.editDoc("user1", "doc1", "Hello World!");
        editor.showDoc("user1", "doc1");

        editor.undo();
        editor.showDoc("user1", "doc1");

        editor.redo();
        editor.showDoc("user1", "doc1");
    }
}
