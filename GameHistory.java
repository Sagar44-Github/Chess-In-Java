import java.util.Stack;

public class GameHistory {
    private Stack<Move> moves = new Stack<>();
    private Stack<Move> undos = new Stack<>();
    private Piece[][] board;

    public GameHistory(Piece[][] initialBoard) {
        this.board = cloneBoard(initialBoard);
    }

    public void addMove(Move move, Piece[][] currentBoard) {
        moves.push(move);
        undos.clear(); // Clear redos when a new move is made
        this.board = cloneBoard(currentBoard);
    }

    public boolean undo(Piece[][] board) {
        if (moves.isEmpty()) return false;
        Move lastMove = moves.pop();
        undos.push(lastMove);
        revertMove(board, lastMove);
        return true;
    }

    public boolean redo(Piece[][] board) {
        if (undos.isEmpty()) return false;
        Move nextMove = undos.pop();
        applyMove(board, nextMove);
        moves.push(nextMove);
        return true;
    }

    private void applyMove(Piece[][] board, Move move) {
        Piece piece = board[move.startRow][move.startCol];
        piece.setHasMoved(true);

        if (piece instanceof King && Math.abs(move.endCol - move.startCol) == 2) {
            int rookCol = move.endCol > move.startCol ? 7 : 0;
            int rookNewCol = move.endCol > move.startCol ? 5 : 3;
            board[move.startRow][rookNewCol] = board[move.startRow][rookCol];
            board[move.startRow][rookNewCol].setHasMoved(true);
            board[move.startRow][rookCol] = null;
        } else if (piece instanceof Pawn && move.endCol != move.startCol && board[move.endRow][move.endCol] == null) {
            board[move.startRow][move.endCol] = null;
        } else if (piece instanceof Pawn && (move.endRow == 0 || move.endRow == 7)) {
            // Simulate promotion (default to Queen for simplicity)
            piece = new Queen(piece.color);
        }

        board[move.endRow][move.endCol] = piece;
        board[move.startRow][move.startCol] = null;
    }

    private void revertMove(Piece[][] board, Move move) {
        Piece piece = board[move.endRow][move.endCol];
        piece.setHasMoved(false); // Reset hasMoved for castling/en passant

        if (piece instanceof King && Math.abs(move.endCol - move.startCol) == 2) {
            int rookCol = move.endCol > move.startCol ? 5 : 3;
            int rookNewCol = move.endCol > move.startCol ? 7 : 0;
            board[move.startRow][rookNewCol] = board[move.startRow][rookCol];
            board[move.startRow][rookCol] = null;
        } else if (piece instanceof Pawn && move.endCol != move.startCol && board[move.startRow][move.endCol] != null) {
            // Restore pawn for en passant
            board[move.startRow][move.endCol] = new Pawn(piece.color == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE);
        }

        board[move.startRow][move.startCol] = piece;
        board[move.endRow][move.endCol] = null;
    }

    private Piece[][] cloneBoard(Piece[][] original) {
        Piece[][] copy = new Piece[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (original[i][j] != null) {
                    if (original[i][j] instanceof Pawn) {
                        copy[i][j] = new Pawn(original[i][j].color);
                    } else if (original[i][j] instanceof Rook) {
                        copy[i][j] = new Rook(original[i][j].color);
                    } else if (original[i][j] instanceof Knight) {
                        copy[i][j] = new Knight(original[i][j].color);
                    } else if (original[i][j] instanceof Bishop) {
                        copy[i][j] = new Bishop(original[i][j].color);
                    } else if (original[i][j] instanceof Queen) {
                        copy[i][j] = new Queen(original[i][j].color);
                    } else if (original[i][j] instanceof King) {
                        copy[i][j] = new King(original[i][j].color);
                    }
                    copy[i][j].setHasMoved(original[i][j].hasMoved());
                }
            }
        }
        return copy;
    }
}