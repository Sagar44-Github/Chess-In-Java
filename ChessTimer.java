import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class ChessTimer {
    private JLabel whiteTimeLabel;
    private JLabel blackTimeLabel;
    private long whiteTime = 5 * 60 * 1000; // 5 minutes in milliseconds
    private long blackTime = 5 * 60 * 1000;
    private PieceColor currentPlayer;
    private Timer timer;
    private ChessGame game;

    public ChessTimer(ChessGame game) {
        this.game = game;
        this.currentPlayer = PieceColor.WHITE;
        setupLabels();
        startTimer();
    }

    private void setupLabels() {
        whiteTimeLabel = new JLabel(formatTime(whiteTime));
        blackTimeLabel = new JLabel(formatTime(blackTime));
        JPanel timePanel = new JPanel(new GridLayout(1, 2));
        timePanel.add(new JLabel("White: "));
        timePanel.add(whiteTimeLabel);
        timePanel.add(new JLabel("Black: "));
        timePanel.add(blackTimeLabel);
        game.add(timePanel, BorderLayout.NORTH);
    }

    public void switchPlayer() {
        if (timer != null) timer.cancel();
        currentPlayer = currentPlayer == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        startTimer();
        updateLabels();
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentPlayer == PieceColor.WHITE) {
                    whiteTime -= 100;
                    if (whiteTime <= 0) {
                        timer.cancel();
                        JOptionPane.showMessageDialog(game, "Time's up! Black wins!");
                        game.dispose();
                    }
                } else {
                    blackTime -= 100;
                    if (blackTime <= 0) {
                        timer.cancel();
                        JOptionPane.showMessageDialog(game, "Time's up! White wins!");
                        game.dispose();
                    }
                }
                updateLabels();
            }
        }, 0, 100);
    }

    private void updateLabels() {
        whiteTimeLabel.setText(formatTime(whiteTime));
        blackTimeLabel.setText(formatTime(blackTime));
    }

    private String formatTime(long millis) {
        long minutes = millis / (60 * 1000);
        long seconds = (millis % (60 * 1000)) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void reset() {
        whiteTime = 5 * 60 * 1000;
        blackTime = 5 * 60 * 1000;
        currentPlayer = PieceColor.WHITE;
        if (timer != null) timer.cancel();
        startTimer();
        updateLabels();
    }
}
