import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {
    public Rook(PieceColor color) {
        super(color);
    }

    @Override
    public List<Move> getPossibleMoves(Piece[][] board, int row, int col, Move lastMove) {
        List<Move> moves = new ArrayList<>();
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                if (board[r][c] == null) {
                    moves.add(new Move(row, col, r, c));
                } else {
                    if (board[r][c].color != this.color) {
                        moves.add(new Move(row, col, r, c));
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }

    @Override
    public String getCharacter() {
        return color == PieceColor.WHITE ? "♖" : "♜";
    }
}
