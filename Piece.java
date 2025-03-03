import java.util.List;

public abstract class Piece {
    protected PieceColor color;
    protected boolean hasMoved;

    public Piece(PieceColor color) {
        this.color = color;
        this.hasMoved = false;
    }

    public PieceColor getColor() {
        return color;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public abstract List<Move> getPossibleMoves(Piece[][] board, int row, int col, Move lastMove);

    public abstract String getCharacter();
}
