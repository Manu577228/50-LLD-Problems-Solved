# Build a simple in-memory text editor where multiple users can create, 
# edit, undo, and redo changes to their text documents.
# The editor should maintain each user’s document history and 
# support fast undo/redo operations using the Command Pattern, without any external database.

class Command:
    def __init__(self, execute_func, undo_func):
        self.execute_func = execute_func
        self.undo_func = undo_func

    def execute(self):
        self.execute_func()

    def undo(self):
        self.undo_func()

class TextEditor:
    def __init__(self):
        self.documents = {}
        self.undo_stack = []
        self.redo_stack = []

    def create_doc(self, user, doc_name, text = ""):
        if user not in self.documents:
            self.documents[user] = {}
        self.documents[user][doc_name] = text
        print(f"Document '{doc_name}' created for {user}.")

    def edit_doc(self, user, doc_name, new_text):
        prev_text = self.documents[user][doc_name]

        def execute():
            self.documents[user][doc_name] = new_text

        def undo():
            self.documents[user][doc_name] = prev_text

        cmd = Command(execute, undo)
        cmd.execute()
        self.undo_stack.append(cmd)
        self.redo_stack.clear()
        print(f"Edited '{doc_name}' for {user}.")

    def undo(self):
        if not self.undo_stack:
            print("Nothing to undo.")
            return
        
        cmd = self.undo_stack.pop()
        cmd.undo()

        self.redo_stack.append(cmd)
        print("Undo operation performed.")

    def redo(self):
        if not self.redo_stack:
            print("Nothing to redo.")
            return
        
        cmd = self.redo_stack.pop()
        cmd.execute()

        self.undo_stack.append(cmd)
        print("Redo operation performed.")

    def show_doc(self, user, doc_name):
        print(f"Current Text of '{doc_name}': {self.documents[user][doc_name]}")

if __name__ == "__main__":
    editor = TextEditor()

    editor.create_doc("user1", "doc1", "Hello")
    editor.edit_doc("user1", "doc1", "Hello World!")
    editor.show_doc("user1", "doc1")

    editor.undo()
    editor.show_doc("user1", "doc1")

    editor.redo()
    editor.show_doc("user1", "doc1")
        
        

        