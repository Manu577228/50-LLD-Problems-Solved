# Design a simple in-memory text editor that supports adding text 
# along with Undo and Redo operations. Each action must be reversible, 
# and the system should maintain full undo/redo history without using 
# any external storage. Implement it using the Command Pattern with clear execution 
# and unexecution logic.

class Document:
    def __init__(self):
        self.text = ""

    def show(self):
        print("Document Text:", self.text)

class Command:
    def execute(self):
        pass

    def unexecute(self):
        pass

class AddTextCommand(Command):
    def __init__(self, document, text):
        self.doc = document
        self.text = text

    def execute(self):
        self.doc.text += self.text

    def unexecute(self):
        self.doc.text = self.doc.text[: -len(self.text)]

class UndoManager:
    def __init__(self):
        self.undo_stack = []
        self.redo_stack = []

    def execute(self, command):
        command.execute()
        self.undo_stack.append(command)
        self.redo_stack.clear()

    def undo(self):
        if not self.undo_stack:
            print("Nothing to undo.")
            return
        cmd = self.undo_stack.pop()
        cmd.unexecute()
        self.redo_stack.append(cmd)

    def redo(self):
        if not self.redo_stack:
            print("Nothing to redo.")
            return
        cmd = self.redo_stack.pop()
        cmd.execute()
        self.undo_stack.append(cmd)

if __name__ == "__main__":

    doc = Document()                           # create document object
    manager = UndoManager()                   # create undo/redo controller

    print("\n--- Performing Actions ---")
    manager.execute(AddTextCommand(doc, "Hello "))  # add "Hello "
    manager.execute(AddTextCommand(doc, "World"))   # add "World"
    doc.show()                                     # prints: Hello World

    print("\n--- Undo 1 ---")
    manager.undo()                                 # undo "World"
    doc.show()                                     # prints: Hello

    print("\n--- Undo 2 ---")
    manager.undo()                                 # undo "Hello "
    doc.show()                                     # prints empty string

    print("\n--- Redo 1 ---")
    manager.redo()                                 # redo "Hello "
    doc.show()                                     # prints: Hello

    print("\n--- Redo 2 ---")
    manager.redo()                                 # redo "World"
    doc.show()        

    