import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EventManager { // Zarządza wydarzeniami

    private List<GameEvent> eventPool;
    private Random random;

    public EventManager() {
        this.eventPool = new ArrayList<>();
        this.random = new Random();
        resetEvents();
    }

    public void resetEvents() { //Odświeża dostępna póle po cyklu
        this.eventPool.clear();
        initEventPool();
    }

    public void removePlayedEvents(List<String> history) { //Zapobiega rozegraniu wydarzenia powtarzalnego więcej razy
        if (history == null || history.isEmpty()) return;
        eventPool.removeIf(event -> history.contains(event.description));
    }

    public GameEvent getRandomEvent(PlayerState player) { //Losuje i sprawdza warunki wydarzenia
        if (eventPool.isEmpty()) {
            return new GameEvent("Spokojny dzień. Brak wydarzeń.",
                    List.of(new GameEvent.Choice("Odpoczywam", 0, 5, 5, null)));
        }

        List<GameEvent> validCandidates = eventPool.stream()
                .filter(e -> {
                    if (e.requiredItem != null && !player.inventory.contains(e.requiredItem)) {
                        return false;
                    }
                    if (e.forbiddenItem != null && player.inventory.contains(e.forbiddenItem)) {
                        return false;
                    }
                    if (player.lastEventDescription != null && !player.lastEventDescription.isEmpty()) {
                        if (e.description.equals(player.lastEventDescription)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        if (validCandidates.isEmpty()) {
            return new GameEvent("Dzień wolny. Odpoczywasz.",
                    List.of(new GameEvent.Choice("Super", 0, 5, 5, null)));
        }

        int index = random.nextInt(validCandidates.size());
        GameEvent selectedEvent = validCandidates.get(index);

        if (!selectedEvent.isRepeatable) {
            eventPool.remove(selectedEvent);
        }

        return selectedEvent;
    }

    private void addEvent(String desc, GameEvent.Choice... choices) { //Wydarzenie wypadające z póli
        List<GameEvent.Choice> choiceList = new ArrayList<>();
        for (GameEvent.Choice c : choices) choiceList.add(c);
        eventPool.add(new GameEvent(desc, choiceList));
    }

    private void addRepeatableEvent(String desc, GameEvent.Choice... choices) { //Powtarzalne wydarzenie
        List<GameEvent.Choice> choiceList = new ArrayList<>();
        for (GameEvent.Choice c : choices) choiceList.add(c);
        GameEvent e = new GameEvent(desc, choiceList);
        e.setRepeatable(true);
        eventPool.add(e);
    }

    private void addComplexEvent(String require, String forbid, String desc, GameEvent.Choice... choices) { //Niepowtarzalne wydarzenie warunkowe
        List<GameEvent.Choice> choiceList = new ArrayList<>();
        for (GameEvent.Choice c : choices) choiceList.add(c);
        GameEvent e = new GameEvent(desc, choiceList);
        e.setRequiredItem(require);
        e.setForbiddenItem(forbid);
        eventPool.add(e);
    }

    private void addRepairEvent(String brokenItem, String desc, GameEvent.Choice... choices) { //Powtarzalne wydarzenie zależne
        List<GameEvent.Choice> choiceList = new ArrayList<>();
        for (GameEvent.Choice c : choices) choiceList.add(c);

        GameEvent e = new GameEvent(desc, choiceList);
        e.setRequiredItem(brokenItem);
        e.setRepeatable(true);

        eventPool.add(e);
    }



    private void initEventPool() { //Przechowuje wszystkie dostępne wydarzenia

        // Wydarzenia powtarzające się
        // 1.
        addRepeatableEvent("Promocja w markecie spożywczym.",
                new GameEvent.Choice("Robię duże zakupy", 80, 2, 6, null),
                new GameEvent.Choice("Tylko niezbędne", 30, 0, 0, null),
                new GameEvent.Choice("Nic nie kupuję", 0, -2, -2, null));

        // 2.
        addRepeatableEvent("Gotujesz, ale zabrakło ci składnika.",
                new GameEvent.Choice("Idę po niego do sklepu", 10, 2, 2, null),
                new GameEvent.Choice("Poradzę sobie bez niego", 0, -2, -2, null));

        // 3.
        addRepeatableEvent("Twój znajomy ma urodziny.",
                new GameEvent.Choice("Kupuję super prezent", 100, 5, 2, null),
                new GameEvent.Choice("Dorzucam się do prezentu ze znajomymi", 30, 2, 0, null),
                new GameEvent.Choice("Nie kupuję prezentu", 0, -3, -2, null));

        // 4.
        addRepeatableEvent("10zł na ulicy.",
                new GameEvent.Choice("Biorę je!", 10, 5, -1, null),
                new GameEvent.Choice("Poradzę sobię bez nich", 0, 0, 1, null));

        // 5.
        addRepeatableEvent("Znajomi zapraszają cię na kawę.",
                new GameEvent.Choice("Carmel macchiato z bitą śmietaną", 25, 8, 8, null),
                new GameEvent.Choice("Zwykła czarna", 8, 4, 1, null),
                new GameEvent.Choice("Przyjdę dla towarzystwa", 0, 2, 0, null));

        // 6.
        addRepeatableEvent("Szef poprosił cię o zostanie dłużej w pracy.",
                new GameEvent.Choice("Biorę nadgodziny", -250, -2, -2, null),
                new GameEvent.Choice("Wracam do domu 16:00", 0, 2, 0, null));

        // 7.
        addRepeatableEvent("Dawno nie byłeś w kinie.",
                new GameEvent.Choice("Bilet VIP i duży popcorn", 80, 25, 20, null),
                new GameEvent.Choice("Zwykły bilet, bez jedzenia", 30, 15, 5, null),
                new GameEvent.Choice("Zostanę w domu", 0, -5, 0, null));

        // 8.
        GameEvent eReflex = new GameEvent("Stoisz na pasach. Światło zaraz się zmieni.", new ArrayList<>());
        eReflex.setRepeatable(true);
        eReflex.setMinigame(GameEvent.MinigameType.REFLEX_LIGHTS);
        eventPool.add(eReflex);


        // Wydarzenie zależne od wybory pojawiające się raz
        // 1.
        addComplexEvent("Brak Auta", null,"Musisz przesiąść się na autobus. Kupujesz bilet miesięczny?",
                new GameEvent.Choice("Kupuję bilet miesięczny", 120, 2, 2, "Bilet Miesięczny"),
                new GameEvent.Choice("Pojeżdżę na jednorazowych", 0, 0, 0, null));

        // 2.
        addComplexEvent("Brak Auta", "Bilet Miesięczny","Widzisz kontrolera na przystanku.",
                new GameEvent.Choice("Kupuję bilet", 3, 2, 2, null),
                new GameEvent.Choice("Mandat", 100, -5, -5, null),
                new GameEvent.Choice("Wysiadam na tym przystanku", 0, -10, -15,null ));

        // 3.
        addComplexEvent(null, "Ząb Nieleczony","Obudził Cię potworny ból zęba. Opuchlizna jest ogromna.",
                new GameEvent.Choice("Prywatny dentysta", 300, 10, 10, null),
                new GameEvent.Choice("Pójdę na NFZ", 0, -15, -15, null),
                new GameEvent.Choice("Tabletki przeciwbólowe", 30, -10, -10, "Ząb Nieleczony"));

        // 4.
        addComplexEvent("Zab Nieleczony II", null,"Twój ząb jest zupełnie zepsuty. Ból jest nie do zniesienia",
                new GameEvent.Choice("Usuwam ząb", 200, -60, -90, "Brak Zęba", "Ząb Nieleczony II"));





        // Wydarzenia powtarzające się w zależności od wyboru
        // 1.
        List<GameEvent.Choice> miceChoices = new ArrayList<>();
        miceChoices.add(new GameEvent.Choice("Tym razem dzwonię po specjalistów", 220, 0, 0, null, "Myszy"));
        miceChoices.add(new GameEvent.Choice("To nowa codzienność", 0, -10, -12, null));
        GameEvent eMyszy = new GameEvent("Znowu widzisz myszy, tym razem jest ich więcej.", miceChoices);
        eMyszy.setRequiredItem("Myszy");
        eMyszy.setRepeatable(true);
        eMyszy.setMinigame(GameEvent.MinigameType.MOUSE_CATCH);
        eventPool.add(eMyszy);

        // 2.
        addRepairEvent("Zepsute Auto", "Twój samochód jest zupełnie nie sprawny.",
                new GameEvent.Choice("Próbuję go odratować", 800, 1, 2, "Ryzyko Awarii", "Zepsute Auto"),
                new GameEvent.Choice("Teraz tylko złom", 0, -10, -12, "Brak Auta", "Zepsute Auto"));

        // 3.
        addRepairEvent("Ryzyko Awarii", "Pora rzeczywiście zająć się swoim samochodem.",
                new GameEvent.Choice("Naprawa w ASO", 800, 1, 2, null, "Ryzyko Awarii"),
                new GameEvent.Choice("Znowu Mirek", 250, -10, -12, null));

        // 4.
        addRepairEvent("Gołębie", "Gołębie regularnie wracają na twój balkon.",
                new GameEvent.Choice("Inwestuję w siatkę przeciw ptakom", 120, 10, 12, null, "Gołębie"),
                new GameEvent.Choice("Kupuję plastikowego kruka", 60, 2, 5, null, "Gołębie"),
                new GameEvent.Choice("Zostawiam je w spokoju", 0, 2, -12, null));

        // 5.
        addRepairEvent("Ząb Nieleczony", "Ząb nie przestaje boleć. Potrzebujesz leczenia kanałowego.",
                new GameEvent.Choice("Prywatny dentysta", 400, 0, 0, null, "Ząb Nieleczony"),
                new GameEvent.Choice("Pójdę na NFZ", 0, -15, -15, null, "Ząb Nieleczony"),
                new GameEvent.Choice("Dalej go ignoruję", 30, -25, -25, "Ząb Nieleczony II", "Ząb Nieleczony"));


        // Wydarzenia jednorazowe
        // 1.
        addEvent("Zauważyłeś, że twoje buty się rozklejają.",
                new GameEvent.Choice("Kupuję nowe firmowe", 250, 15, 15, null),
                new GameEvent.Choice("Kupuję ekonomiczne", 120, 10, 2, null),
                new GameEvent.Choice("Naprawiam swoje stare", 0, -5, -5, null));

        // 2.
        addEvent("Twój samochód wydaje dziwne dźwięki. To chyba silnik.",
                new GameEvent.Choice("Naprawa w ASO", 600, 5, 5, null),
                new GameEvent.Choice("Zaprzyjaźniony mechanik Mirek", 200, 2, -2, "Ryzyko Awarii"),
                new GameEvent.Choice("Jakie dźwięki?", 0, 1, -5, "Zepsute Auto"));


        // 3.
        addEvent("Zauważyłeś mysz w swoim mieszkaniu.",
                new GameEvent.Choice("Wezwę specjalistów", 200, 5, 5, null),
                new GameEvent.Choice("Kupuję i zakładam pułapkę", 20, 1, -2, null),
                new GameEvent.Choice("Ignorujesz ją", 0, 0, -3, "Myszy"));

        // 4.
        addEvent("Twój ulubiony artysta daje koncert w twoim mieście.",
                new GameEvent.Choice("Kupuję bilety", 120, 20, 10, null),
                new GameEvent.Choice("Biorę nadgodziny żeby kupić bilety", 40, 15, -2, null),
                new GameEvent.Choice("Siedzę w domu", 0, -5, -5, null));

        // 5.
        addEvent("W pracy zbierają na 'Szlachetną Paczkę'. Wypada się dorzucić.",
                new GameEvent.Choice("Daję 100 zł", 100, 10, 5, null),
                new GameEvent.Choice("Daję 20 zł", 20, 5, 0, null),
                new GameEvent.Choice("Mówię, że nie mam gotówki", 0, -5, -5, null));

        // 6.
        addEvent("Zorientowałeś się, że płacisz za 5 serwisów VOD, a oglądasz jeden.",
                new GameEvent.Choice("Zostawiam wszystko, może się przyda", 120, 10, 10, null),
                new GameEvent.Choice("Anuluję wszystko poza jednym", 30, 2, 0, null),
                new GameEvent.Choice("Anuluję wszystko, czytam książki", 0, -10, 5, null));

        // 7.
        addEvent("Dostałeś mail-a o wygranej w loterii.",
                new GameEvent.Choice("Klikam w link", 400, -20, -20, null),
                new GameEvent.Choice("Zgłaszam nadawcę", 0, 2, 5, null),
                new GameEvent.Choice("Ignoruję go", 0, 0, 3, null));

        // 8.
        addEvent("Gołębie uwiły sobie gniazdo na twoim balkonie.",
                new GameEvent.Choice("Inwestuję w siatkę przeciw ptakom", 120, 10, 12, null),
                new GameEvent.Choice("Kupuję plastikowego kruka", 60, 2, 5, null),
                new GameEvent.Choice("Niszczę gniazdo", 0, 0, 3, "Gołębie"));

        // 9.
        addEvent("Masz ochotę rozwinąć swoją pasję (np. malowanie, gry, sport).",
                new GameEvent.Choice("Kupuję profesjonalny sprzęt", 250, 30, 10, null),
                new GameEvent.Choice("Kupuję używane akcesoria", 90, 15, 5, null),
                new GameEvent.Choice("Rezygnuję, nie stać mnie", 0, -15, -10, null));

        // 10.
        addEvent("Nie chce ci się gotować po pracy. Pizza brzmi kusząco.",
                new GameEvent.Choice("Pizza z supermarketu", 15, 6, 10, null),
                new GameEvent.Choice("Zamawiam pizzę", 50, 10, 15, null),
                new GameEvent.Choice("Obejdę się smakiem", 0, -10, -10, null));

        // 11.
        addEvent("Czujesz się fatalnie. Gorączka i katar.",
                new GameEvent.Choice("Idę do apteki po komplet leków", 120, 10, 10, null),
                new GameEvent.Choice("Domowe sposoby (Czosnek)", 20, -5, -5, null),
                new GameEvent.Choice("Ignoruję i idę do pracy", 0, -20, -30, "Choroba"));

        // 12.
        addEvent("Poplamiłeś swoją ulubioną koszulkę olejem.",
                new GameEvent.Choice("Piorę ją w domu", 0, -2, -2, null),
                new GameEvent.Choice("Oddaję ją do pralni", 50, 10, 10, null));

        // 13.
        addEvent("Sąsiedzi zbierają na renowację elewacji w waszym bloku.",
                new GameEvent.Choice("Dołożę się ", 150, 15, 0, null),
                new GameEvent.Choice("Ignoruję ogłoszenia", 0, 0, -5, null));

        // 14.
        addEvent("Musisz iśc do fryzjera.",
                new GameEvent.Choice("Znajomy hobbysta", 10, 2, 0, null),
                new GameEvent.Choice("Profesjonalista", 80, 10, 10, null),
                new GameEvent.Choice("Zrób to samemu", 0, -5, -7, null));

        // 15.
        addEvent("Musisz zapłacić rachunki.",
                new GameEvent.Choice("Płacę", 480, 10, 10, null),
                new GameEvent.Choice("Nie płacę", 0, -50, -80, null));

        // 16.
        addEvent("Znajomy poprosił cię o pożyczenie mu pieniędzy.",
                new GameEvent.Choice("Pożyczę pieniądze", 100, 20, 0, null),
                new GameEvent.Choice("Nie pożyczę", 0, -20, 0, null));
    }

}