/* ----------------------------------------------------------------------------  
   ( The Authentic JS/JAVA CodeBuff )
 ___ _                      _              _ 
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                        |__/ 
 ----------------------------------------------------------------------------
   Youtube: https://youtube.com/@code-with-Bharadwaj
   Github : https://github.com/Manu577228
   Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio
 --------------------------------------------------------------------------- */

import java.io.*;
import java.util.*;

abstract class Piece {
    String color;  // 'W' or 'B'
    String name;   // 'P', 'R', 'N', 'B', 'Q', 'K'

    Piece(String color, String name) {
        this.color = color;
        this.name = name;
    }

    // Every piece defines its own valid movement rule
    abstract boolean validMove(int r1, int c1, int r2, int c2, Piece[][] board);
}

// ---------------- Subclasses for each piece ----------------
class Pawn extends Piece {
    Pawn(String color) { super(color, "P"); }

    boolean validMove(int r1, int c1, int r2, int c2, Piece[][] board) {
        int dir = color.equals("W") ? -1 : 1;
        // Move one step forward if empty
        return (c1 == c2 && board[r2][c2] == null && r2 - r1 == dir);
    }
}

class Rook extends Piece {
    Rook(String color) { super(color, "R"); }

    boolean validMove(int r1, int c1, int r2, int c2, Piece[][] board) {
        // Straight line move along row or column
        return (r1 == r2 || c1 == c2);
    }
}

class Knight extends Piece {
    Knight(String color) { super(color, "N"); }

    boolean validMove(int r1, int c1, int r2, int c2, Piece[][] board) {
        int dr = Math.abs(r1 - r2);
        int dc = Math.abs(c1 - c2);
        return (dr == 1 && dc == 2) || (dr == 2 && dc == 1);
    }
}

class Bishop extends Piece {
    Bishop(String color) { super(color, "B"); }

    boolean validMove(int r1, int c1, int r2, int c2, Piece[][] board) {
        // Move diagonally only
        return Math.abs(r1 - r2) == Math.abs(c1 - c2);
    }
}

class Queen extends Piece {
    Queen(String color) { super(color, "Q"); }

    boolean validMove(int r1, int c1, int r2, int c2, Piece[][] board) {
        // Combination of Rook and Bishop
        return (r1 == r2 || c1 == c2) || (Math.abs(r1 - r2) == Math.abs(c1 - c2));
    }
}

class King extends Piece {
    King(String color) { super(color, "K"); }

    boolean validMove(int r1, int c1, int r2, int c2, Piece[][] board) {
        // Move 1 square in any direction
        return Math.abs(r1 - r2) <= 1 && Math.abs(c1 - c2) <= 1;
    }
}

// ---------------- Board Class ----------------
class Board {
    Piece[][] grid = new Piece[8][8];

    Board() { setup(); }

    // Set initial positions
    void setup() {
        for (int i = 0; i < 8; i++) {
            grid[1][i] = new Pawn("B");
            grid[6][i] = new Pawn("W");
        }
        grid[0][0] = grid[0][7] = new Rook("B");
        grid[7][0] = grid[7][7] = new Rook("W");

        grid[0][1] = grid[0][6] = new Knight("B");
        grid[7][1] = grid[7][6] = new Knight("W");

        grid[0][2] = grid[0][5] = new Bishop("B");
        grid[7][2] = grid[7][5] = new Bishop("W");

        grid[0][3] = new Queen("B");
        grid[7][3] = new Queen("W");

        grid[0][4] = new King("B");
        grid[7][4] = new King("W");
    }

    // Display board in console
    void display() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                System.out.print((p != null ? p.color + p.name : "..") + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    // Move piece if valid
    boolean movePiece(int r1, int c1, int r2, int c2) {
        Piece p = grid[r1][c1];
        if (p != null && p.validMove(r1, c1, r2, c2, grid)) {
            grid[r2][c2] = p;
            grid[r1][c1] = null;
            return true;
        }
        return false;
    }
}

// ---------------- Game Class ----------------
public class ChessGame {
    Board board = new Board();
    String turn = "W"; // White starts

    void play() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            board.display();
            System.out.print(turn + "'s move (e.g., e2 e4): ");
            String line = br.readLine();
            if (line == null || line.trim().isEmpty()) break;
            String[] mv = line.trim().split(" ");
            if (mv.length != 2) continue;

            int r1 = 8 - Character.getNumericValue(mv[0].charAt(1));
            int c1 = mv[0].charAt(0) - 'a';
            int r2 = 8 - Character.getNumericValue(mv[1].charAt(1));
            int c2 = mv[1].charAt(0) - 'a';

            Piece p = board.grid[r1][c1];
            if (p != null && p.color.equals(turn)) {
                if (board.movePiece(r1, c1, r2, c2)) {
                    // Switch turn after valid move
                    turn = turn.equals("W") ? "B" : "W";
                } else {
                    System.out.println("Invalid move!\n");
                }
            } else {
                System.out.println("Wrong piece!\n");
            }
        }
    }

    // ------------------- MAIN -------------------
    public static void main(String[] args) throws IOException {
        ChessGame g = new ChessGame();
        g.play();  // Run and play in VS Code terminal
    }
}

/* -----------------------------------------------------------
   💡 EXPLANATION 
   - Each Piece subclass defines its validMove() rule.
   - Board initializes 8x8 setup and prints simple ASCII layout.
   - Game alternates turns and reads moves like "e2 e4".
   - Simple move validation, no check/checkmate logic yet.
   - Fully terminal-based; pure Java runnable in VS Code.
 ----------------------------------------------------------- */
