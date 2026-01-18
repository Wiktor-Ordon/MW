import java.util.List;

public class GameEvent {
    String description;
    List<Choice> choices;

    boolean isRepeatable;
    String requiredItem;
    String forbiddenItem;
    MinigameType minigameType;

    public GameEvent(String description, List<Choice> choices) {
        this.description = description;
        this.choices = choices;
        this.isRepeatable = false;
        this.requiredItem = null;
        this.forbiddenItem = null;
        this.minigameType = MinigameType.NONE;
    }


    public enum MinigameType {
        NONE,
        REFLEX_LIGHTS,
        MOUSE_CATCH
    }


    public GameEvent setRepeatable(boolean repeatable) {
        this.isRepeatable = repeatable;
        return this;
    }
    public GameEvent setRequiredItem(String item) {
        this.requiredItem = item;
        return this;
    }
    public GameEvent setForbiddenItem(String item) {
        this.forbiddenItem = item;
        return this;
    }
    public GameEvent setMinigame(MinigameType type) {
        this.minigameType = type;
        return this;
    }

    public static class Choice {
        String label;
        double cost;
        int happinessEffect;
        int comfortEffect;
        String flagToAdd;
        String flagToRemove;

        public Choice(String label, double cost, int hap, int com, String flagToAdd) {
            this(label, cost, hap, com, flagToAdd, null);
        }

        public Choice(String label, double cost, int hap, int com, String flagToAdd, String flagToRemove) {
            this.label = label;
            this.cost = cost;
            this.happinessEffect = hap;
            this.comfortEffect = com;
            this.flagToAdd = flagToAdd;
            this.flagToRemove = flagToRemove;
        }
    }
}