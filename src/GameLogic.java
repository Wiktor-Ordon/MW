public class GameLogic {

    private PlayerState player;
    private EventManager eventManager;
    private boolean gameRunning;

    public GameLogic() {
        this.eventManager = new EventManager();
        this.gameRunning = false;
    }

    public void startNewGame() {
        this.player = new PlayerState();
        this.eventManager.resetEvents();
        this.gameRunning = true;
    }

    public boolean loadGame() {
        PlayerState loaded = SaveSystem.loadGame();
        if (loaded != null) {
            this.player = loaded;
            this.eventManager.resetEvents();
            this.eventManager.removePlayedEvents(player.playedEventsHistory);
            this.gameRunning = true;
            return true;
        }
        return false;
    }

    public void saveGame() {
        if (player != null) {
            SaveSystem.saveGame(player);
        }
    }

    public GameEvent drawNextEvent() {
        GameEvent event = eventManager.getRandomEvent(player);
        player.lastEventDescription = event.description;
        if (!event.isRepeatable) {
            player.playedEventsHistory.add(event.description);
        }
        return event;
    }

    public void applyChoice(GameEvent.Choice choice) {
        player.budget -= choice.cost;
        player.happiness += choice.happinessEffect;
        player.comfort += choice.comfortEffect;

        if (choice.flagToRemove != null) {
            player.inventory.remove(choice.flagToRemove);
        }
        if (choice.flagToAdd != null && !player.inventory.contains(choice.flagToAdd)) {
            player.inventory.add(choice.flagToAdd);
        }
        clampStats();
        saveGame();
    }

    public void applyMandate(double amount) {
        player.budget -= amount;
        saveGame();
    }

    public void nextMonth() {
        player.day = 1;
        player.budget += 2000;
        eventManager.resetEvents();
    }

    public void nextDay() {
        player.day++;
    }

    public String checkGameOver() {
        if (player.budget <= 0) {
            gameRunning = false;
            return "BANKRUCTWO!\nNie stac cię na życie.";
        }
        if (player.happiness <= 0) {
            gameRunning = false;
            return "ZAŁAMANIE NERWOWE!\nTwój poziom szczęścia spadł do zera. Nie masz siły wstać z łóżka.";
        }
        if (player.comfort <= 0) {
            gameRunning = false;
            return "WYCIEŃCZENIE!\nTwój poziom komfortu spadł do zera. Nie da się żyć w takich warunkach.";
        }
        return null;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    private void clampStats() {
        player.happiness = Math.max(0, Math.min(100, player.happiness));
        player.comfort = Math.max(0, Math.min(100, player.comfort));
    }

    public PlayerState getPlayer() { return player; }
}