import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {
    public Pawn(PieceColor color) {
        super(color);
    }

    @Override
    public List<Move> getPossibleMoves(Piece[][] board, int row, int col, Move lastMove) {
        List<Move> moves = new ArrayList<>();
        int direction = color == PieceColor.WHITE ? -1 : 1;
        int startRow = color == PieceColor.WHITE ? 6 : 1;

        // Move forward
        if (row + direction >= 0 && row + direction < 8 && board[row + direction][col] == null) {
            moves.add(new Move(row, col, row + direction, col));
            // Two-square move from starting position
            if (row == startRow && board[row + 2 * direction][col] == null) {
                moves.add(new Move(row, col, row + 2 * direction, col));
            }
        }

        // Capture diagonally
        if (col > 0 && row + direction >= 0 && row + direction < 8) {
            if (board[row + direction][col - 1] != null && board[row + direction][col - 1].color != this.color) {
                moves.add(new Move(row, col, row + direction, col - 1));
            }
        }
        if (col < 7 && row + direction >= 0 && row + direction < 8) {
            if (board[row + direction][col + 1] != null && board[row + direction][col + 1].color != this.color) {
                moves.add(new Move(row, col, row + direction, col + 1));
            }
        }

        // En passant
        if (lastMove != null && lastMove.endRow == row && Math.abs(lastMove.startRow - lastMove.endRow) == 2) {
            if (board[lastMove.endRow][lastMove.endCol] instanceof Pawn &&
                    board[lastMove.endRow][lastMove.endCol].color != this.color) {
                if (Math.abs(lastMove.endCol - col) == 1) {
                    moves.add(new Move(row, col, row + direction, lastMove.endCol));
                }
            }
        }

        return moves;
    }

    @Override
    public String getCharacter() {
        return color == PieceColor.WHITE ? "♙" : "♟";
    }
}
