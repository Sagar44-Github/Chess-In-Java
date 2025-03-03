import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    public King(PieceColor color) {
        super(color);
    }

    @Override
    public List<Move> getPossibleMoves(Piece[][] board, int row, int col, Move lastMove) {
        List<Move> moves = new ArrayList<>();
        int[][] deltas = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

        for (int[] delta : deltas) {
            int r = row + delta[0];
            int c = col + delta[1];
            if (r >= 0 && r < 8 && c >= 0 && c < 8 && (board[r][c] == null || board[r][c].color != this.color)) {
                moves.add(new Move(row, col, r, c));
            }
        }

        // Castling
        if (!hasMoved && !isInCheck(board, this.color)) { // Pass null for lastMove to avoid recursion
            // Kingside
            if (col == 4 && board[row][7] instanceof Rook && !board[row][7].hasMoved() &&
                    board[row][5] == null && board[row][6] == null) {
                Piece[][] temp = cloneBoard(board);
                temp[row][5] = temp[row][4];
                temp[row][4] = null;
                if (!isInCheck(temp, this.color)) {
                    moves.add(new Move(row, col, row, 6));
                }
            }
            // Queenside
            if (col == 4 && board[row][0] instanceof Rook && !board[row][0].hasMoved() &&
                    board[row][1] == null && board[row][2] == null && board[row][3] == null) {
                Piece[][] temp = cloneBoard(board);
                temp[row][3] = temp[row][4];
                temp[row][4] = null;
                if (!isInCheck(temp, this.color)) {
                    moves.add(new Move(row, col, row, 2));
                }
            }
        }

        return moves;
    }

    @Override
    public String getCharacter() {
        return color == PieceColor.WHITE ? "♔" : "♚";
    }

    private boolean isInCheck(Piece[][] board, PieceColor color) {
        int kingRow = -1, kingCol = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] instanceof King && board[r][c].color == color) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
        }
        PieceColor opponent = color == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null && board[r][c].color == opponent) {
                    // Check if the piece is not the opponent's King to avoid recursion
                    if (!(board[r][c] instanceof King)) {
                        List<Move> moves = board[r][c].getPossibleMoves(board, r, c, null);
                        for (Move m : moves) {
                            if (m.endRow == kingRow && m.endCol == kingCol) {
                                return true;
                            }
                        }
                    } else {
                        // Handle opponent's King manually to prevent recursion
                        int[][] kingDeltas = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
                        for (int[] delta : kingDeltas) {
                            int kr = r + delta[0];
                            int kc = c + delta[1];
                            if (kr >= 0 && kr < 8 && kc >= 0 && kc < 8 && kr == kingRow && kc == kingCol) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
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
