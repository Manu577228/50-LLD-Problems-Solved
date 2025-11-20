/* ------------------------------------------------------------
   Undo / Redo Functionality (Java Single File)
   Fully LLD-Compliant + Line-by-Line Explanation
------------------------------------------------------------- */

import java.util.*;

// ------------------------- DOCUMENT -------------------------
class Document {
    String text;                         // holds the current text of document

    Document() {
        this.text = "";                  // initialize with empty document
    }

    void show() {
        System.out.println("Document Text: " + this.text);   // print current state
    }
}


// ----------------------- COMMAND PATTERN --------------------
interface Command {                      // base interface for all commands
    void execute();                      // perform the action
    void unexecute();                    // undo the action
}

// Concrete command: Add text to document
class AddTextCommand implements Command {

    private Document doc;                // reference to shared document
    private String text;                 // text that will be added

    AddTextCommand(Document doc, String text) {
        this.doc = doc;                  // store document
        this.text = text;                // store content to add
    }

    @Override
    public void execute() {
        doc.text = doc.text + text;      // append text to document
    }

    @Override
    public void unexecute() {
        doc.text = doc.text.substring(0, doc.text.length() - text.length());
                                         // remove added text from end
    }
}


// ----------------------- UNDO / REDO MANAGER -----------------
class UndoManager {

    private Stack<Command> undoStack;    // stores commands that CAN be undone
    private Stack<Command> redoStack;    // stores commands that CAN be redone

    UndoManager() {
        undoStack = new Stack<>();       // initialize stacks
        redoStack = new Stack<>();
    }

    void execute(Command cmd) {
        cmd.execute();                   // perform the action
        undoStack.push(cmd);             // store it for undo
        redoStack.clear();               // new action kills all redo history
    }

    void undo() {
        if (undoStack.isEmpty()) {       // no actions to undo
            System.out.println("Nothing to undo.");
            return;
        }
        Command cmd = undoStack.pop();   // pop last action
        cmd.unexecute();                 // revert it
        redoStack.push(cmd);             // push into redo history
    }

    void redo() {
        if (redoStack.isEmpty()) {       // no actions to redo
            System.out.println("Nothing to redo.");
            return;
        }
        Command cmd = redoStack.pop();   // take top undone command
        cmd.execute();                   // reapply it
        undoStack.push(cmd);             // push back to undo stack
    }
}


// ----------------------------- DEMO ---------------------------
public class UndoRedoDemo {

    public static void main(String[] args) {

        Document doc = new Document();          // create shared document
        UndoManager manager = new UndoManager();// create undo/redo manager

        System.out.println("\n--- Performing Actions ---");
        manager.execute(new AddTextCommand(doc, "Hello "));  // add "Hello "
        manager.execute(new AddTextCommand(doc, "World"));   // add "World"
        doc.show();                                          // → Hello World

        System.out.println("\n--- Undo 1 ---");
        manager.undo();                                      // remove "World"
        doc.show();                                          // → Hello

        System.out.println("\n--- Undo 2 ---");
        manager.undo();                                      // remove "Hello "
        doc.show();                                          // → (empty)

        System.out.println("\n--- Redo 1 ---");
        manager.redo();                                      // redo "Hello "
        doc.show();                                          // → Hello

        System.out.println("\n--- Redo 2 ---");
        manager.redo();                                      // redo "World"
        doc.show();                                          // → Hello World
    }
}
