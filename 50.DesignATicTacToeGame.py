# “Design a console-based TicTacToe game for two players.
# The system should validate moves, update a 3×3 board, detect wins or draws, and 
# display the game state after every turn.
# Implement the full game loop in a single Python file without external dependencies.”

class TicTacToe:
    def __init__(self):
        self.board = [[" " for _ in range(3)] for _ in range(3)]
        self.current_player = "X"

    def display_board(self):
        for row in self.board:
            print("|".join(row))
        print("...........")

    def make_move(self, r, c):
        if r < 0 or r >= 3 or c < 0 or c >= 3:
            return False, "Invalid Position."
        
        if self.board[r][c] != " ":
            return False, "Cell already taken."
        
        self.board[r][c] = self.current_player
        return True, "Move accepted."
    
    def switch_player(self):
        self.current_player = "O" if self.current_player == "X" else "X"

    def check_win(self):
        b = self.board
        p = self.current_player

        for row in b:
            if all(cell == p for cell in row):
                return True
            
        for c in range(3):
            if all(b[r][c] == p for r in range(3)):
                return True
            
        if all(b[i][i] == p for i in range(3)):
            return True
        
        if all(b[i][2 - i] == p for i in range(3)):
            return True
        
        return False

    def check_draw(self):
        for r in range(3):
            for c in range(3):
                if self.board[r][c] == " ":
                    return False
        return True
    
game = TicTacToe()                                                 # Create game instance

# Predefined move sequence showing X winning diagonally
moves = [(0, 0), (0, 1), (1, 1), (0, 2), (2, 2)]

for r, c in moves:
    print(f"Player {game.current_player} move ({r},{c})")          # Show whose turn & position

    success, msg = game.make_move(r, c)                            # Attempt move
    print(msg)                                                     # Print move result
    game.display_board()                                           # Show updated board

    if game.check_win():                                           # Check winning condition
        print(f"Player {game.current_player} wins!")
        break

    if game.check_draw():                                          # Check draw condition
        print("Game is a Draw!")
        break

    game.switch_player()  


