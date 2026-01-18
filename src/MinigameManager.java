import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MinigameManager {

    private GameWindow gameWindow;
    private JPanel eventPanel;
    private GameLogic logic;

    public MinigameManager(GameWindow gameWindow, JPanel eventPanel, GameLogic logic) {
        this.gameWindow = gameWindow;
        this.eventPanel = eventPanel;
        this.logic = logic;
    }

    public void startReflexGame(GameEvent event) {
        eventPanel.removeAll();
        eventPanel.setLayout(new GridBagLayout());

        JPanel gamePanel = createMinigameFrame(null);

        CircleButton lightBtn = new CircleButton("");
        lightBtn.setPreferredSize(new Dimension(150, 150));
        lightBtn.setMaximumSize(new Dimension(150, 150));
        lightBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        lightBtn.setBackground(Color.RED);

        Timer timer = new Timer();
        long delay = new Random().nextInt(2000) + 1000;
        final boolean[] isGreen = {false};

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    lightBtn.setBackground(Color.GREEN);
                    lightBtn.repaint();
                    isGreen[0] = true;
                });
            }
        }, delay);

        lightBtn.addActionListener(e -> {
            timer.cancel();

            StringBuilder resultMsg = new StringBuilder();
            resultMsg.append("Zaczekaj na zielone światło\n\n");

            if (isGreen[0]) {
                resultMsg.append("SUKCES!\nPrzeszedłeś bezpiecznie na zielonym świetle.\n\n(Brak negatywnych skutków)");
                logic.saveGame();
            } else {
                logic.applyMandate(150);

                resultMsg.append("PORAŻKA!\nPrzeszedłeś na czerwonym świetle.\n\n");
                resultMsg.append("- Koszt: 150 PLN (Mandat)");
            }

            gameWindow.updateStatsUI();
            gameWindow.showResultInFrame(resultMsg.toString());
        });

        gamePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        gamePanel.add(lightBtn);

        eventPanel.add(gamePanel);
        eventPanel.revalidate();
        eventPanel.repaint();
    }

    public void startMouseGame(GameEvent event) {
        eventPanel.removeAll();
        eventPanel.setLayout(null);

        JLabel title = new JLabel("Znajdź (kliknij) wszystkie myszy!");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBounds(150, 20, 600, 30);
        eventPanel.add(title);

        Random rand = new Random();
        int miceCount = rand.nextInt(2) + 4;
        final int[] miceRemaining = {miceCount};

        for (int i = 0; i < miceCount; i++) {
            JButton mouseBtn = new JButton("🐭");
            mouseBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            mouseBtn.setMargin(new Insets(0, 0, 0, 0));
            mouseBtn.setBorderPainted(false);
            mouseBtn.setContentAreaFilled(false);
            mouseBtn.setFocusPainted(false);
            mouseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            int x = rand.nextInt(600) + 50;
            int y = rand.nextInt(350) + 60;
            mouseBtn.setBounds(x, y, 80, 80);

            mouseBtn.addActionListener(e -> {
                eventPanel.remove(mouseBtn);
                eventPanel.repaint();
                miceRemaining[0]--;

                if (miceRemaining[0] <= 0) {
                    gameWindow.displayEvent(event);
                }
            });

            eventPanel.add(mouseBtn);
        }

        eventPanel.revalidate();
        eventPanel.repaint();
    }

    private JPanel createMinigameFrame(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(500, 300));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(Color.BLUE, 3, true));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Arial", Font.BOLD, 24));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(lbl);
        return panel;
    }

    public static class CircleButton extends JButton {
        public CircleButton(String label) {
            super(label);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int diameter = Math.min(getWidth(), getHeight());
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;
            if (getModel().isArmed()) {
                g2.setColor(getBackground().darker());
            } else {
                g2.setColor(getBackground());
            }
            g2.fillOval(x, y, diameter, diameter);
            g2.dispose();
        }
        @Override
        public boolean contains(int x, int y) {
            int radius = Math.min(getWidth(), getHeight()) / 2;
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            return Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) <= Math.pow(radius, 2);
        }
    }
}