import javax.swing.*;

void main() {
    SwingUtilities.invokeLater(() -> {
        GameWindow game = new GameWindow();
        game.setVisible(true);
    });
}