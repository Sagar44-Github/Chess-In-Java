import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ChessGame extends JFrame {
    private Piece[][] board = new Piece[8][8];
    private PieceColor currentPlayer = PieceColor.WHITE;
    private JLabel statusLabel;
    private JPanel chessBoardPanel;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private List<Move> possibleMoves = new ArrayList<>();
    private Move lastMove;
    private boolean isAI = true; // Default to AI mode (AI plays as Black)
    private boolean isAIPhase = true; // Track if AI should act in current phase
    private ChessAI ai = new ChessAI();
    private GameHistory history;
    private ChessTimer timer;

    public ChessGame() {
        initializeBoard();
        history = new GameHistory(board);
        timer = new ChessTimer(this);
        setupGUI();
    }

    private void initializeBoard() {
        for (int col = 0; col < 8; col++) {
            board[1][col] = new Pawn(PieceColor.BLACK);
            board[6][col] = new Pawn(PieceColor.WHITE);
        }
        board[0][0] = new Rook(PieceColor.BLACK);
        board[0][7] = new Rook(PieceColor.BLACK);
        board[7][0] = new Rook(PieceColor.WHITE);
        board[7][7] = new Rook(PieceColor.WHITE);
        board[0][1] = new Knight(PieceColor.BLACK);
        board[0][6] = new Knight(PieceColor.BLACK);
        board[7][1] = new Knight(PieceColor.WHITE);
        board[7][6] = new Knight(PieceColor.WHITE);
        board[0][2] = new Bishop(PieceColor.BLACK);
        board[0][5] = new Bishop(PieceColor.BLACK);
        board[7][2] = new Bishop(PieceColor.WHITE);
        board[7][5] = new Bishop(PieceColor.WHITE);
        board[0][3] = new Queen(PieceColor.BLACK);
        board[7][3] = new Queen(PieceColor.WHITE);
        board[0][4] = new King(PieceColor.BLACK);
        board[7][4] = new King(PieceColor.WHITE);
    }

    private void setupGUI() {
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chessBoardPanel = new JPanel(new GridLayout(8, 8));
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JLabel label = new JLabel();
                label.setOpaque(true);
                label.setBackground((row + col) % 2 == 0 ? new Color(245, 245, 220) : new Color(139, 69, 19));
                if (board[row][col] != null) {
                    label.setText(board[row][col].getCharacter());
                }
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Serif", Font.PLAIN, 40));
                final int r = row;
                final int c = col;
                label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleClick(r, c);
                    }
                });
                chessBoardPanel.add(label);
            }
        }
        add(chessBoardPanel, BorderLayout.CENTER);
        statusLabel = new JLabel("White's turn");
        add(statusLabel, BorderLayout.SOUTH);

        // Add menu bar for mode toggle, Undo/Redo, and Timer reset
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JCheckBoxMenuItem aiModeItem = new JCheckBoxMenuItem("AI Mode (vs. AI)", true);
        aiModeItem.addActionListener(e -> {
            isAI = aiModeItem.isSelected();
            isAIPhase = isAI && currentPlayer == PieceColor.BLACK; // Reset AI phase based on mode and turn
            statusLabel.setText(currentPlayer + "'s turn" + (isAI && currentPlayer == PieceColor.BLACK ? " (AI Thinking)" : ""));
        });
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.addActionListener(e -> {
            if (history.undo(board)) {
                currentPlayer = currentPlayer == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
                isAIPhase = isAI && currentPlayer == PieceColor.BLACK; // Update AI phase after undo
                statusLabel.setText(currentPlayer + "'s turn" + (isAIPhase ? " (AI Thinking)" : ""));
                timer.switchPlayer(); // Sync timer with player switch
                updateBoard();
            }
        });
        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.addActionListener(e -> {
            if (history.redo(board)) {
                currentPlayer = currentPlayer == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
                isAIPhase = isAI && currentPlayer == PieceColor.BLACK; // Update AI phase after redo
                statusLabel.setText(currentPlayer + "'s turn" + (isAIPhase ? " (AI Thinking)" : ""));
                timer.switchPlayer(); // Sync timer with player switch
                updateBoard();
            }
        });
        JMenuItem resetTimerItem = new JMenuItem("Reset Timer");
        resetTimerItem.addActionListener(e -> timer.reset());
        gameMenu.add(aiModeItem);
        gameMenu.add(undoItem);
        gameMenu.add(redoItem);
        gameMenu.add(resetTimerItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        pack();
        setVisible(true);
    }

    private void handleClick(int row, int col) {
        if (isAIPhase) {
            statusLabel.setText("Please wait for AI to move...");
            return; // Block human input during AI's turn
        }

        if (selectedRow == -1) {
            if (board[row][col] != null && board[row][col].getColor() == currentPlayer) {
                selectedRow = row;
                selectedCol = col;
                possibleMoves = board[row][col].getPossibleMoves(board, row, col, lastMove);
                possibleMoves = filterLegalMoves(possibleMoves);
                highlightPossibleMoves();
            }
        } else {
            for (Move move : possibleMoves) {
                if (move.endRow == row && move.endCol == col) {
                    lastMove = move;
                    applyMove(board, move);
                    history.addMove(move, board);
                    currentPlayer = currentPlayer == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
                    isAIPhase = isAI && currentPlayer == PieceColor.BLACK;
                    statusLabel.setText(currentPlayer + "'s turn" + (isAIPhase ? " (AI Thinking)" : ""));
                    timer.switchPlayer();
                    checkGameState();
                    updateBoard();
                    if (isAIPhase) {
                        // AI makes move for Black
                        Timer aiTimer = new Timer(1000, e -> {
                            Move aiMove = ai.getBestMove(board, PieceColor.BLACK);
                            if (aiMove != null) {
                                applyMove(board, aiMove);
                                lastMove = aiMove;
                                history.addMove(aiMove, board);
                                currentPlayer = PieceColor.WHITE;
                                isAIPhase = false;
                                statusLabel.setText(currentPlayer + "'s turn");
                                timer.switchPlayer();
                                checkGameState();
                                updateBoard();
                            }
                            ((Timer) e.getSource()).stop();
                        });
                        aiTimer.setRepeats(false);
                        aiTimer.start();
                    }
                    break;
                }
            }
            selectedRow = -1;
            selectedCol = -1;
            possibleMoves.clear();
        }
    }

    private List<Move> filterLegalMoves(List<Move> moves) {
        List<Move> legalMoves = new ArrayList<>();
        for (Move move : moves) {
            Piece[][] tempBoard = cloneBoard(board);
            applyMove(tempBoard, move);
            if (!isInCheck(tempBoard, currentPlayer)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    private void highlightPossibleMoves() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JLabel label = (JLabel) chessBoardPanel.getComponent(row * 8 + col);
                label.setBackground((row + col) % 2 == 0 ? new Color(245, 245, 220) : new Color(139, 69, 19));
                if (row == selectedRow && col == selectedCol) {
                    label.setBackground(Color.CYAN);
                }
                for (Move move : possibleMoves) {
                    if (move.endRow == row && move.endCol == col) {
                        label.setBackground(Color.YELLOW);
                    }
                }
            }
        }
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

        // Castling
        if (piece instanceof King && Math.abs(move.endCol - move.startCol) == 2) {
            int rookCol = move.endCol > move.startCol ? 7 : 0;
            int rookNewCol = move.endCol > move.startCol ? 5 : 3;
            board[move.startRow][rookNewCol] = board[move.startRow][rookCol];
            board[move.startRow][rookNewCol].setHasMoved(true);
            board[move.startRow][rookCol] = null;
        }
        // En passant
        else if (piece instanceof Pawn && move.endCol != move.startCol && board[move.endRow][move.endCol] == null) {
            board[move.startRow][move.endCol] = null;
        }
        // Promotion
        else if (piece instanceof Pawn && (move.endRow == 0 || move.endRow == 7)) {
            String[] options = {"Queen", "Rook", "Bishop", "Knight"};
            String choice = (String) JOptionPane.showInputDialog(this, "Promote to:", "Pawn Promotion",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            switch (choice) {
                case "Queen": piece = new Queen(piece.color); break;
                case "Rook": piece = new Rook(piece.color); break;
                case "Bishop": piece = new Bishop(piece.color); break;
                case "Knight": piece = new Knight(piece.color); break;
            }
        }

        board[move.endRow][move.endCol] = piece;
        board[move.startRow][move.startCol] = null;
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
                        List<Move> moves = board[r][c].getPossibleMoves(board, r, c, lastMove);
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

    private boolean hasLegalMoves(Piece[][] board, PieceColor color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null && board[r][c].color == color) {
                    List<Move> moves = board[r][c].getPossibleMoves(board, r, c, lastMove);
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

    private void checkGameState() {
        if (isInCheck(board, currentPlayer)) {
            if (!hasLegalMoves(board, currentPlayer)) {
                JOptionPane.showMessageDialog(this, "Checkmate! " + (currentPlayer == PieceColor.WHITE ? "Black" : "White") + " wins!");
            } else {
                JOptionPane.showMessageDialog(this, "Check!");
            }
        } else if (!hasLegalMoves(board, currentPlayer)) {
            JOptionPane.showMessageDialog(this, "Stalemate!");
        }
    }

    private void updateBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JLabel label = (JLabel) chessBoardPanel.getComponent(row * 8 + col);
                label.setText(board[row][col] != null ? board[row][col].getCharacter() : "");
                label.setBackground((row + col) % 2 == 0 ? new Color(245, 245, 220) : new Color(139, 69, 19));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGame::new);
    }
}