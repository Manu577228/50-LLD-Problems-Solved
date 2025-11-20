/* ====================== LLD: TIC TAC TOE ===========================
   Single-file, runnable in VS Code, with line-by-line explanation
   ================================================================== */

public class TicTacToe {

    // Constructor initializes board and starting player
    private String[][] board;            // 2D board storing X / O / " "
    private String currentPlayer;        // Tracks whose turn it is

    public TicTacToe() {
        board = new String[3][3];        // Create 3x3 grid
        for (int i = 0; i < 3; i++) {    // Fill with spaces
            for (int j = 0; j < 3; j++) {
                board[i][j] = " ";
            }
        }
        currentPlayer = "X";             // X always starts
    }

    // Prints the board after each move
    public void displayBoard() {
        for (int i = 0; i < 3; i++) {               // Loop through rows
            System.out.println(
                board[i][0] + "|" + board[i][1] + "|" + board[i][2]
            );
        }
        System.out.println("-----");                // Divider
    }

    // Tries to place the player's mark on (r,c)
    public boolean makeMove(int r, int c) {

        if (r < 0 || r >= 3 || c < 0 || c >= 3) {   // Check boundaries
            System.out.println("Invalid position");
            return false;
        }

        if (!board[r][c].equals(" ")) {             // Cell must be empty
            System.out.println("Cell already taken");
            return false;
        }

        board[r][c] = currentPlayer;                // Place mark
        return true;
    }

    // Switch X → O or O → X
    public void switchPlayer() {
        currentPlayer = currentPlayer.equals("X") ? "O" : "X";
    }

    // Checks if current player won
    public boolean checkWin() {
        String p = currentPlayer;
        String[][] b = board;

        // Row check
        for (int i = 0; i < 3; i++) {
            if (b[i][0].equals(p) && b[i][1].equals(p) && b[i][2].equals(p))
                return true;
        }

        // Column check
        for (int c = 0; c < 3; c++) {
            if (b[0][c].equals(p) && b[1][c].equals(p) && b[2][c].equals(p))
                return true;
        }

        // Main diagonal
        if (b[0][0].equals(p) && b[1][1].equals(p) && b[2][2].equals(p))
            return true;

        // Anti diagonal
        if (b[0][2].equals(p) && b[1][1].equals(p) && b[2][0].equals(p))
            return true;

        return false;
    }

    // Draw occurs when no empty cell remains
    public boolean checkDraw() {
        for (int i = 0; i < 3; i++) {               // Scan whole board
            for (int j = 0; j < 3; j++) {
                if (board[i][j].equals(" ")) {
                    return false;                   // Empty cell → not draw
                }
            }
        }
        return true;
    }

    // ========================= MAIN DEMO =============================
    public static void main(String[] args) {

        TicTacToe game = new TicTacToe();          // Create game instance

        // Predefined moves to demo diagonal win by X
        int[][] moves = {
                {0, 0},   // X
                {0, 1},   // O
                {1, 1},   // X
                {0, 2},   // O
                {2, 2}    // X → Wins diagonally
        };

        for (int[] m : moves) {
            int r = m[0];                           // Extract r
            int c = m[1];                           // Extract c

            System.out.println(
                "Player " + game.currentPlayer +
                " plays (" + r + "," + c + ")"
            );

            boolean ok = game.makeMove(r, c);       // Attempt move
            game.displayBoard();                    // Print board

            if (!ok) {                              // If invalid move
                System.out.println("Try again...");
                continue;
            }

            if (game.checkWin()) {                  // Check win
                System.out.println("Player " + game.currentPlayer + " wins!");
                break;
            }

            if (game.checkDraw()) {                 // Check draw
                System.out.println("Game is a draw!");
                break;
            }

            game.switchPlayer();                    // Change turn
        }
    }
}
