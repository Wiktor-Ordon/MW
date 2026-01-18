import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class GameWindow extends JFrame {

    private JPanel mainPanel;
    private JPanel rightInfoPanel;
    private JPanel eventPanel;
    private JTextArea eventDescription;
    private JPanel choicesContainer;
    private JLabel dayLabel, budgetLabel;
    private RoundedPanel bonusPanel;
    private JProgressBar barHappiness, barComfort;

    private GameLogic logic;
    private MinigameManager minigameManager;

    public GameWindow() {
        super("Magia Wydawania");
        setSize(1280, 1024);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        this.logic = new GameLogic();
        initMainMenu();
    }

    private void initMainMenu() {
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(40, 44, 52));

        JButton btnNew = createStyledButton("Nowa Gra");
        JButton btnLoad = createStyledButton("Wczytaj Grę");
        JButton btnExit = createStyledButton("Wyjdź");

        btnNew.addActionListener(e -> {
            logic.startNewGame();
            startGameUI();
        });

        btnLoad.addActionListener(e -> {
            if (logic.loadGame()) {
                startGameUI();
            } else {
                JOptionPane.showMessageDialog(this, "Brak zapisu gry!");
            }
        });

        btnExit.addActionListener(e -> System.exit(0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; mainPanel.add(btnNew, gbc);
        gbc.gridy = 1; mainPanel.add(btnLoad, gbc);
        gbc.gridy = 2; mainPanel.add(btnExit, gbc);

        setContentPane(mainPanel);
        revalidate();
    }

    private void startGameUI() {
        mainPanel = new JPanel(new BorderLayout());
        initRightPanel();

        eventPanel = new JPanel();
        eventPanel.setBorder(new EmptyBorder(40, 60, 40, 60));
        eventPanel.setLayout(new BorderLayout());

        minigameManager = new MinigameManager(this, eventPanel, logic);

        eventDescription = new JTextArea("Opis...");
        eventDescription.setFont(new Font("Arial", Font.PLAIN, 26));
        eventDescription.setLineWrap(true);
        eventDescription.setWrapStyleWord(true);
        eventDescription.setEditable(false);
        eventDescription.setOpaque(false);

        choicesContainer = new JPanel(new GridLayout(4, 1, 15, 15));

        mainPanel.add(eventPanel, BorderLayout.CENTER);
        mainPanel.add(rightInfoPanel, BorderLayout.EAST);

        setContentPane(mainPanel);
        updateStatsUI();
        updateBonusUI();
        nextTurn();
    }

    public void nextTurn() {
        if (logic.getPlayer().day > 30) {
            handleEndOfMonth();
            return;
        }

        GameEvent event = logic.drawNextEvent();

        if (event.minigameType == GameEvent.MinigameType.REFLEX_LIGHTS) {
            minigameManager.startReflexGame(event);
        } else if (event.minigameType == GameEvent.MinigameType.MOUSE_CATCH) {
            minigameManager.startMouseGame(event);
        } else {
            displayEvent(event);
        }
    }

    public void displayEvent(GameEvent event) {
        eventPanel.removeAll();
        eventPanel.setLayout(new BorderLayout());

        eventDescription.setText(event.description);
        choicesContainer.removeAll();
        choicesContainer.setLayout(new GridLayout(4, 1, 15, 15));

        for (GameEvent.Choice choice : event.choices) {
            JButton btn = createStyledButton(choice.label);
            btn.addActionListener(e -> makeChoice(choice));
            choicesContainer.add(btn);
        }

        eventPanel.add(eventDescription, BorderLayout.CENTER);
        eventPanel.add(choicesContainer, BorderLayout.SOUTH);

        eventPanel.revalidate();
        eventPanel.repaint();
    }

    private void makeChoice(GameEvent.Choice choice) {
        logic.applyChoice(choice);

        updateStatsUI();
        updateBonusUI();

        StringBuilder resultMsg = new StringBuilder();
        resultMsg.append("DECYZJA: ").append(choice.label).append("\n\n");

        if(choice.cost > 0) resultMsg.append("- Koszt: ").append(choice.cost).append(" PLN\n");
        if(choice.cost < 0) resultMsg.append("+ Zysk: ").append(Math.abs(choice.cost)).append(" PLN\n");

        appendStatChange(resultMsg, "Szczęście", choice.happinessEffect);
        appendStatChange(resultMsg, "Komfort", choice.comfortEffect);

        if (choice.flagToAdd != null && logic.getPlayer().inventory.contains(choice.flagToAdd)) {
            resultMsg.append("\nOTRZYMANO: ").append(" ").append(choice.flagToAdd);
        }
        if (choice.flagToRemove != null && !logic.getPlayer().inventory.contains(choice.flagToRemove)) {
            resultMsg.append("\nUTRACONO: ").append(" ").append(choice.flagToRemove);
        }
        if (logic.getPlayer().budget < 0) resultMsg.append("\nUWAGA: Debet!");

        showResultInFrame(resultMsg.toString());
    }

    public void showResultInFrame(String message) {
        eventPanel.removeAll();
        eventPanel.setLayout(new GridBagLayout());

        JPanel resultFrame = new JPanel();
        resultFrame.setLayout(new BorderLayout());
        resultFrame.setPreferredSize(new Dimension(600, 400));
        resultFrame.setBackground(new Color(255, 252, 240));
        resultFrame.setBorder(new CompoundBorder(new LineBorder(new Color(70, 130, 180), 4, true), new EmptyBorder(20, 20, 20, 20)));

        JTextArea resText = new JTextArea(message);
        resText.setFont(new Font("Arial", Font.BOLD, 20));
        resText.setEditable(false);
        resText.setLineWrap(true);
        resText.setWrapStyleWord(true);
        resText.setOpaque(false);

        JButton nextBtn = createStyledButton("Dalej >>");
        nextBtn.setBackground(new Color(173, 216, 230));
        nextBtn.addActionListener(e -> {
            String gameOverMsg = logic.checkGameOver();
            if (gameOverMsg != null) {
                JOptionPane.showMessageDialog(this, gameOverMsg, "Koniec Gry", JOptionPane.ERROR_MESSAGE);
                initMainMenu();
            } else {
                logic.nextDay();
                updateStatsUI();
                nextTurn();
            }
        });

        resultFrame.add(resText, BorderLayout.CENTER);
        resultFrame.add(nextBtn, BorderLayout.SOUTH);
        eventPanel.add(resultFrame);
        eventPanel.revalidate();
        eventPanel.repaint();
    }

    public void updateStatsUI() {
        PlayerState p = logic.getPlayer();
        if (p == null) return;
        dayLabel.setText("Dzień: " + p.day);
        budgetLabel.setText(String.format("%.2f PLN", p.budget));
        barHappiness.setValue(p.happiness);
        barComfort.setValue(p.comfort);
    }

    public void updateBonusUI() {
        bonusPanel.removeAll();
        PlayerState p = logic.getPlayer();
        if (p == null || p.inventory.isEmpty()) {
            JLabel emptyLabel = new JLabel("Brak bonusów");
            emptyLabel.setForeground(Color.LIGHT_GRAY);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            bonusPanel.add(emptyLabel);
        } else {
            for (String item : p.inventory) {
                JLabel itemLabel = new JLabel(getEmojiForItem(item));
                itemLabel.setToolTipText(item);
                itemLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
                itemLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
                bonusPanel.add(itemLabel);
            }
        }
        bonusPanel.revalidate();
        bonusPanel.repaint();
    }

    private void handleEndOfMonth() {
        PlayerState p = logic.getPlayer();
        int response = JOptionPane.showOptionDialog(this,
                "Koniec miesiąca! Budżet: " + String.format("%.2f", p.budget),
                "Podsumowanie",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                new String[]{"Kolejny miesiąc", "Menu", "Wyjdź"}, "Kolejny miesiąc");

        if (response == 0) {
            logic.nextMonth();
            JOptionPane.showMessageDialog(this, "Nowy miesiąc! Otrzymujesz: +2000 PLN.", "Wypłata",JOptionPane.INFORMATION_MESSAGE);
            updateStatsUI();
            nextTurn();
        } else if (response == 1) {
            initMainMenu();
        } else {
            System.exit(0);
        }
    }

    private void initRightPanel() {
        rightInfoPanel = new JPanel();
        rightInfoPanel.setLayout(new BoxLayout(rightInfoPanel, BoxLayout.Y_AXIS));
        rightInfoPanel.setPreferredSize(new Dimension(320, 0));
        rightInfoPanel.setBackground(new Color(230, 230, 250));
        rightInfoPanel.setBorder(new EmptyBorder(30, 20, 30, 20));

        JPanel dayFrame = new JPanel();
        dayFrame.setBackground(Color.WHITE);
        dayFrame.setBorder(new LineBorder(Color.BLACK, 3, true));
        dayFrame.setMaximumSize(new Dimension(200, 50));
        dayLabel = new JLabel("Dzień: 1");
        dayLabel.setFont(new Font("Arial", Font.BOLD, 22));
        dayLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        dayFrame.add(dayLabel);

        budgetLabel = new JLabel("0.00 PLN");
        budgetLabel.setFont(new Font("Arial", Font.BOLD, 24));
        budgetLabel.setForeground(new Color(0, 100, 180));
        budgetLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightInfoPanel.add(dayFrame);
        rightInfoPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        rightInfoPanel.add(budgetLabel);
        rightInfoPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel lblBonus = new JLabel("BONUSY");
        lblBonus.setFont(new Font("Arial", Font.BOLD, 14));
        lblBonus.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightInfoPanel.add(lblBonus);
        rightInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        bonusPanel = new RoundedPanel(30, Color.WHITE);
        bonusPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bonusPanel.setMaximumSize(new Dimension(280, 150));
        bonusPanel.setPreferredSize(new Dimension(280, 120));
        bonusPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        rightInfoPanel.add(bonusPanel);
        rightInfoPanel.add(Box.createVerticalGlue());

        JLabel lblHappy = new JLabel("Szczęście");
        lblHappy.setFont(new Font("Arial", Font.BOLD, 16));
        lblHappy.setAlignmentX(Component.CENTER_ALIGNMENT);
        barHappiness = createStyledBar(new Color(213, 203, 12));

        JLabel lblComfort = new JLabel("Komfort");
        lblComfort.setFont(new Font("Arial", Font.BOLD, 16));
        lblComfort.setAlignmentX(Component.CENTER_ALIGNMENT);
        barComfort = createStyledBar(new Color(38, 80, 225));

        rightInfoPanel.add(lblHappy);
        rightInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightInfoPanel.add(barHappiness);
        rightInfoPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        rightInfoPanel.add(lblComfort);
        rightInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightInfoPanel.add(barComfort);
        rightInfoPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    }

    private String getEmojiForItem(String itemCode) {
        if (itemCode == null) return "";
        switch (itemCode) {
            case "Bilet Miesięczny": return "🚌";
            case "Ząb Nieleczony": return "🦷";
            case "Ząb Nieleczony II": return "🤕";
            case "Brak Zęba": return "😶";
            case "Ryzyko Awarii": return "🔧";
            case "Myszy": return "🐭";
            case "Gołębie": return "🐦";
            case "Brak Auta": return "🚶";
            case "Choroba": return "🦠";
            default: return "📦";
        }
    }

    private void appendStatChange(StringBuilder sb, String name, int val) {
        if (val == 0) return; sb.append(val > 0 ? "+ " : "- ").append(Math.abs(val)).append(" ").append(name).append("\n");
    }

    private JProgressBar createStyledBar(Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(50); bar.setStringPainted(true);
        bar.setForeground(color);
        bar.setMaximumSize(new Dimension(250, 30));
        bar.setFont(new Font("Arial", Font.BOLD, 14)); return bar;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 22));
        btn.setFocusPainted(false); return btn;
    }

    static class RoundedPanel extends JPanel {
        private final  int cornerRadius;
        private final Color backgroundColor;
        public RoundedPanel(int radius, Color bgColor) {
            super();
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            int height = getHeight();

            Graphics2D graphics = (Graphics2D) g;

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (backgroundColor != null) {
                graphics.setColor(backgroundColor);
            }
            else {
                graphics.setColor(getBackground());
            }

            graphics.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.setStroke(new BasicStroke(2));
            graphics.drawRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
        }
    }
}