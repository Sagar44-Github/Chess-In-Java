import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    public Knight(PieceColor color) {
        super(color);
    }

    @Override
    public List<Move> getPossibleMoves(Piece[][] board, int row, int col, Move lastMove) {
        List<Move> moves = new ArrayList<>();
        int[][] deltas = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};

        for (int[] delta : deltas) {
            int r = row + delta[0];
            int c = col + delta[1];
            if (r >= 0 && r < 8 && c >= 0 && c < 8 && (board[r][c] == null || board[r][c].color != this.color)) {
                moves.add(new Move(row, col, r, c));
            }
        }
        return moves;
    }

    @Override
    public String getCharacter() {
        return color == PieceColor.WHITE ? "♘" : "♞";
    }
}
