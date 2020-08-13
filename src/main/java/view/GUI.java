package view;

import database.DatabaseAccess;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import logic.Game_Impl;
import logic.Player;
import logic.Rules_Impl;
import model.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * main-class, hidden behind view.Launcher
 * handles main window and CLI
 */
public class GUI extends Application {
    private static ThreadPoolExecutor executor;
    private DatabaseAccess dbAccess;
    private boolean openDBSettings = false;
    private boolean dbTablesValid = false;
    private boolean checkTablesFinished = false;
    private boolean initialDBHandlingFinished = false;
    private static final HashMap<String, Color> stringColorHashMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui-standard.fxml"));
        Parent root = loader.load();
        GUIController controller = loader.getController();
        primaryStage.setTitle("Das verrückte Labyrinth");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.setMinHeight(759);
        primaryStage.setMinWidth(1296);
        primaryStage.show();

        // initialize database access
        dbAccess = new DatabaseAccess();
        // pass dbAccess along to GUIController
        controller.setDatabaseAccess(dbAccess);


        // new thread that checks if:
        // 1. a connection can be established
        // 2. all tables are in place (and tries to create them if not)
        // sets openDBSettings true if connection test fails
        // sets dbTablesValid true/false depending on the test
        Thread thread = new Thread(() -> {
            openDBSettings = false;
            // test connection - open database settings in case it fails
            // if connection is fine: check and create table structure
            if (!dbAccess.testCon()) {
                openDBSettings = true;
                //controller.autoCallMenuDatabaseSettings(true);
            } else {
                //only go here if connection was established
                dbAccess.openConn();
                dbTablesValid = dbAccess.checkTables();
                dbAccess.closeConn();

                // this means all checks were made (does not mean checkTables was successful)
                checkTablesFinished = true;
            }
        });
        thread.start();


        // timer checks for openDBSettings and checkTablesFinished
        // opens DB-Settings if connection test failed
        // shows table-error message if checkTables failed
        // both cases are handled with Platform.runLater to make the GUI available during the tests
        Timer checkValidateDBSettings = new Timer();
        TimerTask checkValidateDBSettingsTask = new TimerTask() {
            @Override
            public void run() {
                if (openDBSettings) {
                    Platform.runLater(
                            () -> {
                                try {
                                    controller.autoCallMenuDatabaseSettings(true);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    System.out.println("Couldn't call Database Settings at start caused by misconfiguration of preferences.");
                                }
                            }
                    );
                    openDBSettings = false;
                    checkValidateDBSettings.cancel();
                    controller.setInitialDBHandlingFinished(true);
                }
                if (checkTablesFinished) {
                    if (!dbTablesValid) {
                        Platform.runLater(
                                () -> showAlertCheckTablesFail()
                        );
                    }
                    checkValidateDBSettings.cancel();
                    controller.setInitialDBHandlingFinished(true);
                }
            }
        };
        checkValidateDBSettings.scheduleAtFixedRate(checkValidateDBSettingsTask, 250, 250);

        primaryStage.setOnCloseRequest(windowEvent -> {
            // closeConn just in case app gets closed when connection was opened but before it reaches closeConn statement
            dbAccess.closeConn();
            System.exit(0);
        });
    }

    /**
     * Alert that shows up when checkTables() fails (is false)
     */
    private void showAlertCheckTablesFail() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database error");

        // Header Text: null
        alert.setHeaderText("Database schema test failed.");
        alert.setContentText("The schema of the current database seems to be corrupted and could not be fixed automatically.\n\n" +
                "To completely reset the database go to: \nStatistics -> Completely reset database.");

        alert.showAndWait();
    }


    public static void main(String[] args) {
        stringColorHashMap.put("blue", Color.Blue);
        stringColorHashMap.put("green", Color.Green);
        stringColorHashMap.put("yellow", Color.Yellow);
        stringColorHashMap.put("red", Color.Red);

        Locale.setDefault(Locale.ENGLISH);

        if (args.length == 0) {
            launch(args);
        } else {
            // https://stackoverflow.com/questions/7341683/parsing-arguments-to-a-java-command-line-program
            HashMap<String, List<String>> readArgs = new HashMap<>();
            HashMap<String, String> gameConfig = new HashMap<>();
            HashMap<String, String> dbSettings = new HashMap<>();

            List<String> options = null;
            for (int i = 0; i < args.length; i++) {
                final String a = args[i];

                if (a.charAt(0) == '-') {
                    if (a.length() < 2) {
                        System.out.println("ERROR! Check arg " + i + ": Current value is invalid (" + a + ").");
                        System.exit(-1);
                    }
                    options = new ArrayList<>();
                    readArgs.put(a.substring(1), options);
                } else if (options != null) {
                    options.add(a);
                } else {
                    System.out.println("ERROR!");
                    System.exit(-1);
                }
            }

            if (readArgs.containsKey("db")) {
                List<String> dbParams = Arrays.asList("protocol", "host", "port", "dbname", "user", "pwd");
                for (String param : dbParams) {
                    if (readArgs.containsKey(param)) {
                        if (readArgs.get(param).size() == 1) {
                            dbSettings.put(param, readArgs.get(param).get(0));
                        } else {
                            System.out.println("ERROR! Too many/not enough params for '" + param + "': " + Arrays.toString(readArgs.get(param).toArray()));
                            System.exit(-1);
                        }
                    } else {
                        System.out.println("ERROR! No " + param + " given!");
                        System.exit(-1);
                    }
                }

                // check db connection
                DatabaseAccess dbAccess = new DatabaseAccess();
                if (!dbAccess.testCon(dbSettings.get("protocol"),
                        dbSettings.get("host"),
                        dbSettings.get("port"),
                        dbSettings.get("dbname"),
                        dbSettings.get("user"),
                        dbSettings.get("pwd"))) {
                    // check fails: re-prompt
                    System.out.println("ERROR! Database-Settings are incorrect, no connection established!");
                } else {
                    // all good: exit
                    dbAccess.setServerPrefs(dbSettings.get("protocol"),
                            dbSettings.get("host"),
                            dbSettings.get("port"),
                            dbSettings.get("dbname"),
                            dbSettings.get("user"),
                            dbSettings.get("pwd"));
                    System.out.println("Database-Settings successfully changed!");
                }
                System.exit(0);
            }

            if (readArgs.containsKey("dbreset")) {
                System.out.println("Do you really wish to reset the database to its default state? (y/n)");
                Scanner scanner = new Scanner(System.in);
                String input = scanner.next();
                if (input.equals("y") || input.equals("Y")) {
                    DatabaseAccess dbAccess = new DatabaseAccess();
                    if (!dbAccess.testCon()) {
                        // if connection fails
                        System.out.println("ERROR! Database-Settings are incorrect, no connection established!");
                        System.out.println("Database was not reset.");
                        System.exit(-1);
                    } else {
                        // resetting database
                        if (!dbAccess.completeReset()) {
                            // on fail
                            System.out.println("ERROR! Database could not be reset. This may be due to insufficient user rights.");
                            System.exit(-1);
                        } else {
                            // on success
                            System.out.println("Success! Database was reset.");
                            System.exit(0);
                        }
                    }
                } else if (input.equals("n") || input.equals("N")) {
                    System.out.println("Database was not reset.");
                    System.exit(0);
                } else {
                    System.out.println("Invalid input! Database was not reset.");
                    System.exit(-1);
                }
            }

            // count players in game and set gameConfig players
            int countPlayers = 0;
            List<String> allColors = new ArrayList<>(Arrays.asList("blue", "green", "yellow", "red"));
            for (String color : allColors) {
                if (readArgs.containsKey(color)) {
                    if (readArgs.get(color).size() == 1) {
                        countPlayers++;
                        try{
                            Player.createNewPlayer(readArgs.get(color).get(0), stringColorHashMap.get(color));
                        }catch (IllegalArgumentException e)
                        {

                        }
                        gameConfig.put(color, readArgs.get(color).get(0));
                    } else {
                        System.out.println("ERROR! Player " + color + " needs exactly one argument!");
                        System.exit(-1);
                    }
                }
            }
            if (countPlayers < 2) {
                System.out.println("ERROR! Not enough players for simulation! (at least two)");
                System.exit(-1);
            }

            // get number of games to simulate
            long number = 0;
            if (readArgs.containsKey("number") && readArgs.get("number") != null) {
                try {
                    number = Long.parseLong(readArgs.get("number").get(0));
                } catch (NumberFormatException e) {
                    System.out.println("ERROR! Parsing of number not possible!");
                    System.exit(-1);
                }
                gameConfig.put("number", String.valueOf(number));
            } else {
                System.out.println("ERROR! Not set how many games to simulate! (-number LONG)");
                System.exit(-1);
            }
            long threadCount = 0;
            if (readArgs.containsKey("thread") && readArgs.get("thread") != null) {
                try {
                    threadCount = Integer.parseInt(readArgs.get("thread").get(0));
                } catch (NumberFormatException e) {
                    System.out.println("ERROR! Parsing of thread(Count) not possible");
                    System.exit(-1);
                }
                gameConfig.put("thread", String.valueOf(threadCount));
            } else {
                System.out.println("ERROR! Not set how many threads to use for simulation! (-thread INT)");
                System.exit(-1);
            }

            System.out.println("Trying to start simulation with the following values:");
            for (String color : allColors) {
                if (gameConfig.containsKey(color)) {
                    System.out.println("Player '" + color + "' should be played by instance of '" + gameConfig.get(color) + "' if available.");
                }
            }
            System.out.println("Number of games to simulate: " + gameConfig.get("number"));

            simulateGames(gameConfig);

            // wait for simulation to be ready to complete exit
            Timer checkExecution = new Timer();
            final long[] startTime = {System.currentTimeMillis()};
            TimerTask checkExecutionTask = new TimerTask() {
                @Override
                public void run() {
                    long currentTime = System.currentTimeMillis();
                    long numberOfTasks = Long.parseLong(gameConfig.get("number"));
                    long completedTaskCount = executor.getCompletedTaskCount();
                    long queuedTaskCount = executor.getQueue().size();
                    long estimatedDuration;
                    estimatedDuration = ((currentTime - startTime[0]) / completedTaskCount) * queuedTaskCount + 1000;
                    System.out.print("\r"
                            + String.format("%1$" + gameConfig.get("number").length() + "s", completedTaskCount)
                            + " simulated games ready, "
                            + String.format("%1$" + gameConfig.get("number").length() + "s", queuedTaskCount)
                            + " to go. (ETA: "
                            + String.format("%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(estimatedDuration),
                            TimeUnit.MILLISECONDS.toMinutes(estimatedDuration) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(estimatedDuration)),
                            TimeUnit.MILLISECONDS.toSeconds(estimatedDuration) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(estimatedDuration)))
                            + ")"
                    );
                    if (executor.getCompletedTaskCount() == numberOfTasks) {
                        executor.shutdown();
                        checkExecution.cancel();
                        System.out.println("\nLast simulations running. Please wait for winner and writing to database!");
                    }
                }
            };
            checkExecution.scheduleAtFixedRate(checkExecutionTask, 1000, 2000);
        }
    }

    /**
     * @param gameConfig HashMap for gameConfig - see further documentation
     */
    private static void simulateGames(HashMap<String, String> gameConfig) {
        long number = Long.parseLong(gameConfig.get("number"));
        int threadCount = Integer.parseInt(gameConfig.get("thread"));
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);
        // check if players exist in db and insert if not
        // create DatabaseAccess for the check
        DatabaseAccess dbAccess = new DatabaseAccess();
        dbAccess.openConn();
        //get all players from db and store in players List
        List<Map<String, Object>> players = dbAccess.selectSQL("SELECT name FROM players;");
        if (players == null) {
            // db error
            System.out.println("ERROR! Could not write data into database.");
            System.exit(-1);
        }
        // maybe fourColors and allColors redundant, maybe list from here can be used in loop?
        // don't know why it gets created every time in loop of executor
        // list of all colors
        List<String> fourColors = new ArrayList<>(Arrays.asList("blue", "green", "yellow", "red"));
        // list of players that will get inserted into the database
        List<String> playersToAddToDB = new ArrayList<>();
        // iterate over colors and get the AI-name that should play it
        for (String color : fourColors) {
            boolean playerExists = false;
            if (gameConfig.get(color) != null) {
                for (Map<String, Object> row : players) {
                    String playerName = (String) row.get("name");
                    // check if player already exists in DB
                    if (playerName.equals(gameConfig.get(color))) {
                        playerExists = true;
                    }
                }
                // if not in DB already save into a list (elements of this list will later get inserted into DB)
                if (!playerExists) {
                    // each player only has to be added once, since the same AI might play multiple colors check if it is already in the list
                    if (!playersToAddToDB.contains(gameConfig.get(color))) {
                        try {
                            Player playerTest = Player.createNewPlayer(gameConfig.get(color), stringColorHashMap.get(color));
                        }catch(IllegalArgumentException e){
                            // if AI name was invalid
                            System.out.println("ERROR! This AI is not implemented.");
                            System.exit(-1);
                        }
                        playersToAddToDB.add(gameConfig.get(color));
                    }
                }
            }
        }
        // now insert all elements/players of the list into DB
        for (String playerName : playersToAddToDB) {
            if (!dbAccess.insertSQL("INSERT INTO players (name, type, description, is_active, create_date) " +
                    "VALUES ('" + playerName + "', " + 1 + ", " + null + ", " + 1 + ", '" + dbAccess.createTimestamp() + "')")) {
                // if insert fails:
                System.out.println("ERROR! Could not write data into database.");
                System.exit(-1);
            }
        }
        dbAccess.closeConn();

        for (int i = 0; i < number; i++) {
            executor.submit(() -> {

                Game_Impl game = new Game_Impl();
                game.setIsSimulationGame(true);
                game.setDBAccess(new DatabaseAccess());

                for (String color : fourColors) {
                    if (gameConfig.containsKey(color)) {
                        try {
                            Player player = Player.createNewPlayer(gameConfig.get(color), stringColorHashMap.get(color));
                            String playerName = player.getClass().getName();
                            player.setName(playerName.substring(playerName.lastIndexOf(".") + 1));
                            game.join(player, stringColorHashMap.get(color));
                        } catch (IllegalArgumentException e) {
                            System.out.println("Cannot create player " + stringColorHashMap.get(color).toString() + ": '" + gameConfig.get(color) + "' is not registered in Player-Interface!");
                            System.exit(-1);
                        }
                    }
                }

                Rules_Impl rules = new Rules_Impl(game.getPlayedColors());
                game.setRules(rules);
                game.start(rules.getInitialGameStateRandom());
            });
        }
    }
}
