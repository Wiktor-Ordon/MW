import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerState implements Serializable {
    public int day;
    public double budget;
    public int happiness;
    public int comfort;

    public List<String> inventory;
    public List<String> playedEventsHistory;
    public String lastEventDescription;

    public PlayerState() {
        this.day = 1;
        this.budget = 2000.0;
        this.happiness = 50;
        this.comfort = 50;
        this.inventory = new ArrayList<>();
        this.playedEventsHistory = new ArrayList<>();
        this.lastEventDescription = "";
    }
}