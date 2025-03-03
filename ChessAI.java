import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChessAI {
    private static final int DEPTH = 3; // Depth for Minimax search
    private static final int CHECKMATE_SCORE = 1000000;
    private static final int STALEMATE_SCORE = 0;
    private static final Random random = new Random();

    public Move getBestMove(Piece[][] board, PieceColor aiColor) {
        PieceColor opponentColor = aiColor == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        List<Move> allMoves = getAllPossibleMoves(board, aiColor);
        if (allMoves.isEmpty()) return null;

        int bestScore = Integer.MIN_VALUE;
        Move bestMove = allMoves.get(0);
        for (Move move : allMoves) {
            Piece[][] tempBoard = cloneBoard(board);
            applyMove(tempBoard, move);
            int score = minimax(tempBoard, DEPTH - 1, false, aiColor, opponentColor, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private int evaluateBoard(Piece[][] board, PieceColor aiColor) {
        int score = 0;
        PieceColor opponentColor = aiColor == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null) {
                    int value = getPieceValue(board[r][c]);
                    if (board[r][c].color == aiColor) {
                        score += value;
                    } else {
                        score -= value;
                    }
                }
            }
        }

        // Add positional bonuses (simple example)
        if (aiColor == PieceColor.WHITE) {
            score += countMaterial(board, PieceColor.WHITE) - countMaterial(board, PieceColor.BLACK);
        } else {
            score += countMaterial(board, PieceColor.BLACK) - countMaterial(board, PieceColor.WHITE);
        }

        return score;
    }

    private int getPieceValue(Piece piece) {
        if (piece instanceof Pawn) return 1;
        if (piece instanceof Knight || piece instanceof Bishop) return 3;
        if (piece instanceof Rook) return 5;
        if (piece instanceof Queen) return 9;
        if (piece instanceof King) return 1000; // King is critical, but can't be captured
        return 0;
    }

    private int countMaterial(Piece[][] board, PieceColor color) {
        int material = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null && board[r][c].color == color) {
                    material += getPieceValue(board[r][c]);
                }
            }
        }
        return material;
    }

    private List<Move> getAllPossibleMoves(Piece[][] board, PieceColor color) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null && board[r][c].color == color) {
                    moves.addAll(board[r][c].getPossibleMoves(board, r, c, null));
                }
            }
        }
        return moves;
    }

    private int minimax(Piece[][] board, int depth, boolean isMaximizing, PieceColor aiColor, PieceColor opponentColor,
                        int alpha, int beta) {
        if (depth == 0 || isGameOver(board, aiColor)) {
            return evaluateBoard(board, aiColor);
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            List<Move> moves = getAllPossibleMoves(board, aiColor);
            for (Move move : moves) {
                Piece[][] tempBoard = cloneBoard(board);
                applyMove(tempBoard, move);
                int eval = minimax(tempBoard, depth - 1, false, aiColor, opponentColor, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // Beta cutoff
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            List<Move> moves = getAllPossibleMoves(board, opponentColor);
            for (Move move : moves) {
                Piece[][] tempBoard = cloneBoard(board);
                applyMove(tempBoard, move);
                int eval = minimax(tempBoard, depth - 1, true, aiColor, opponentColor, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // Alpha cutoff
            }
            return minEval;
        }
    }

    private boolean isGameOver(Piece[][] board, PieceColor aiColor) {
        return !hasLegalMoves(board, aiColor);
    }

    private boolean hasLegalMoves(Piece[][] board, PieceColor color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null && board[r][c].color == color) {
                    List<Move> moves = board[r][c].getPossibleMoves(board, r, c, null);
                    for (Move move : moves) {
                        Piece[][] tempBoard = cloneBoard(board);
                        applyMove(tempBoard, move);
                        if (!isInCheck(tempBoard, color)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
                    if (!(board[r][c] instanceof King)) {
                        List<Move> moves = board[r][c].getPossibleMoves(board, r, c, null);
                        for (Move m : moves) {
                            if (m.endRow == kingRow && m.endCol == kingCol) {
                                return true;
                            }
                        }
                    } else {
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
            String[] options = {"Queen", "Rook", "Bishop", "Knight"};
            // Simulate promotion (in real game, this would be handled by GUI)
            piece = new Queen(piece.color); // Default to Queen for AI simplicity
        }

        board[move.endRow][move.endCol] = piece;
        board[move.startRow][move.startCol] = null;
    }
}
