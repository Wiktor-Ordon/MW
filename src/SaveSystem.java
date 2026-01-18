import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SaveSystem {
    private static final String FILE_NAME = "savegame.txt";

    public static void saveGame(PlayerState state) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            writer.println(state.day);
            writer.println(state.budget);
            writer.println(state.happiness);
            writer.println(state.comfort);

            if (state.inventory.isEmpty()) writer.println("BRAK");
            else writer.println(String.join(" ,", state.inventory));

            if (state.playedEventsHistory.isEmpty()) {
                writer.println("BRAK_HISTORII");
            } else {
                String history = state.playedEventsHistory.stream()
                        .map(s -> s.replace("\n", " "))
                        .collect(Collectors.joining(";"));
                writer.println(history);
            }

            if (state.lastEventDescription == null || state.lastEventDescription.isEmpty()) {
                writer.println("BRAK_OSTATNIEGO");
            } else {
                writer.println(state.lastEventDescription.replace("\n", " "));
            }

            System.out.println("Zapisano grę.");
        } catch (IOException error) {
            System.out.println("Błąd zapisu: " + error.getMessage());
        }
    }

    public static PlayerState loadGame() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            PlayerState state = new PlayerState();

            String dayLine = reader.readLine();
            String budgetLine = reader.readLine();
            String happyLine = reader.readLine();
            String comfortLine = reader.readLine();
            String inventoryLine = reader.readLine();
            String historyLine = reader.readLine();
            String lastEventLine = reader.readLine();

            if (dayLine != null) state.day = Integer.parseInt(dayLine);
            if (budgetLine != null) state.budget = Double.parseDouble(budgetLine);
            if (happyLine != null) state.happiness = Integer.parseInt(happyLine);
            if (comfortLine != null) state.comfort = Integer.parseInt(comfortLine);

            state.inventory.clear();
            if (inventoryLine != null && !inventoryLine.equals("BRAK") && !inventoryLine.isEmpty()) {
                state.inventory.addAll(Arrays.asList(inventoryLine.split(" ,")));
            }

            state.playedEventsHistory.clear();
            if (historyLine != null && !historyLine.equals("BRAK_HISTORII") && !historyLine.isEmpty()) {
                state.playedEventsHistory.addAll(Arrays.asList(historyLine.split(";")));
            }

            if (lastEventLine != null && !lastEventLine.equals("BRAK_OSTATNIEGO")) {
                state.lastEventDescription = lastEventLine;
            } else {
                state.lastEventDescription = "";
            }

            return state;
        } catch (IOException | NumberFormatException error) {
            System.out.println("Błąd odczytu: " + error.getMessage());
            return null;
        }
    }
}