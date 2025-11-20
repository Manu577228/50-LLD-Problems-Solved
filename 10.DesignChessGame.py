# Design a simple terminal-based Chess Game simulator in Python that supports standard piece movements, 
# alternating turns between White and Black, and displays the chessboard 
# after each move (no check/checkmate logic)

class Piece:
    def __init__(self, color, name):
        self.color = color
        self.name = name

    def valid_move(self, start, end, board):
        return False
    
class Pawn(Piece):
    def valid_move(self, start, end, board):
        r1, c1 = start; r2, c2 = end
        direction = -1 if self.color == 'W' else 1
        if c1 == c2 and board[r2][c2] is None and r2 - r1 == direction:
            return True
        return False

class Rook(Piece):
    def valid_move(self, start, end, board):
        r1, c1 = start; r2, c2 = end
        if r1 == r2 or c1 == c2:
            return True
        return False

class Knight(Piece):
    def valid_move(self, start, end, board):
        r1, c1 = start; r2, c2 = end
        return (abs(r1-r2), abs(c1-c2)) in [(1,2),(2,1)]

class Bishop(Piece):
    def valid_move(self, start, end, board):
        return abs(start[0]-end[0]) == abs(start[1]-end[1])

class Queen(Piece):
    def valid_move(self, start, end, board):
        return Rook.valid_move(self, start, end, board) or Bishop.valid_move(self, start, end, board)

class King(Piece):
    def valid_move(self, start, end, board):
        return abs(start[0]-end[0]) <= 1 and abs(start[1]-end[1]) <= 1
    
class Board:
    def __init__(self):
        self.grid = [[None] * 8 for _ in range(8)]
        self.setup()


    def setup(self):
        for i in range(8):
            self.grid[1][i] = Pawn('B','P')
            self.grid[6][i] = Pawn('W','P')

        self.grid[0][0] = self.grid[0][7] = Rook('B','R')
        self.grid[7][0] = self.grid[7][7] = Rook('W','R')

        self.grid[0][1] = self.grid[0][6] = Knight('B','N')
        self.grid[7][1] = self.grid[7][6] = Knight('W','N')

        self.grid[0][2] = self.grid[0][5] = Bishop('B','B')
        self.grid[7][2] = self.grid[7][5] = Bishop('W','B')

        self.grid[0][3] = Queen('B','Q')
        self.grid[7][3] = Queen('W','Q')
        self.grid[0][4] = King('B','K')
        self.grid[7][4] = King('W','K')

    def display(self):
        for r in range(8):
            row = ''
            for c in range(8):
                piece = self.grid[r][c]
                row += (piece.color + piece.name if piece else '..') + ' '
            print(row)
        print()

    def get_piece(self, r, c):
        return self.grid[r][c]
    
    def move_piece(self, start, end):
        r1, c1 = start; r2, c2 = end
        p = self.grid[r1][c1]
        if p and p.valid_move(start, end, self.grid):
            self.grid[r2][c2] = p
            self.grid[r1][c1] = None
            return True
        return False
    
class Game:
    def __init__(self):
        self.board = Board()
        self.turn = 'W'

    def play(self):
        while True:
            self.board.display()
            print(f"{self.turn}'s move  (e.g., e2, e4):", end = '')
            move = input().strip().split()
            if len(move) != 2: break

            s, e = move
            start = (8 - int(s[1]), ord(s[0]) - 97)
            end = (8 - int(e[1]), ord(e[0]) - 97)

            piece = self.board.get_piece(*start)
            if piece and piece.color == self.turn:
                if self.board.move_piece(start, end):
                    self.turn = 'B' if self.turn == 'W' else 'W'
                else:
                    print("invalid Move!\n")
            else:
                print("Wrong piece!\n")

if __name__ == "__main__":
    game = Game()    # Create a new chess game
    game.play()      # Start the interactive game loop

   
        

