package view;

import database.DatabaseAccess;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This is the controller to statistics.fxml. Used to evaluate data in database and visualize it.
 */
public class StatisticsController {
    private DatabaseAccess dbAccess;
    private PieChart chart;
    private Alert waitAlert;
    private ObservableList<PieChart.Data> pieChartData;
    private Label lblTotalGames = new Label("");
    private Label lblGamesWithoutWinner = new Label("");
    private Label lblAverageTurns = new Label("");
    private Label lblCaption = new Label("");
    private List<String> playerNameList;
    private int totalGames;
    private Map<String, Integer> nameToWins;
    private int gamesWithoutWinner;
    private int averageTurns;
    private int numberOfParticipants;

    @FXML
    private Pane root;
    @FXML
    private Button btnCompleteReset;
    @FXML
    private Button btnClose;
    @FXML
    private Button btnApply;
    @FXML
    private ComboBox boxPlayer1;
    @FXML
    private ComboBox boxPlayer2;
    @FXML
    private ComboBox boxPlayer3;
    @FXML
    private ComboBox boxPlayer4;
    @FXML
    private VBox diagramBox, vBoxDesc, vBoxNumbers;


    /**
     * The constructor which receives a DatabaseAccess object.
     *
     * @param dbAccess a DatabaseAccess object to open connections and execute statements
     */
    public StatisticsController(DatabaseAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    /**
     * Sets initial values to GUI elements.
     */
    public void initialize() {
        // create pieChart
        chart = new PieChart(pieChartData);
        chart.setTranslateX(-80);
        chart.setTranslateY(20);
        chart.setLegendVisible(false);
        // adjust boxes
        vBoxDesc.setTranslateX(-80);
        vBoxNumbers.setTranslateX(-80);
        // reset labels
        resetLabels();
        // reset Drop Down Boxes
        resetComboBoxes();
        // init waitAlert
        waitAlert = new Alert(Alert.AlertType.NONE);
        waitAlert.initStyle(StageStyle.UNDECORATED);
        waitAlert.initModality(Modality.APPLICATION_MODAL);
    }

    /**
     * Tries to completely reset the database.
     * Shows messages in case it fails.
     *
     * @return true if the reset was successful
     */
    public boolean completeReset() {
        Alert resetConfirmationAlert = new Alert(Alert.AlertType.WARNING,
                "Do you really wish to reset the database to its default state?",
                ButtonType.OK,
                ButtonType.CANCEL);
        resetConfirmationAlert.setTitle("Database Reset");
        Optional<ButtonType> result = resetConfirmationAlert.showAndWait();

        if (result.get() == ButtonType.OK) {
            if (!dbAccess.testCon()) {
                showAlertResetFail("Could not establish a connection to the database. Please visit the database settings to verify your connection.");
                return false;
            }
            if (!dbAccess.completeReset()) {
                showAlertResetFail("The current user you specified in the database settings has insufficient rights to reset the database.");
                return false;
            } else {
                Alert resetSuccessAlert = new Alert(Alert.AlertType.NONE,
                        "Success! Database was reset.",
                        ButtonType.OK);
                resetSuccessAlert.setTitle("Database Reset");
                resetComboBoxes();
                resetLabels();
                resetPieChart();
                resetSuccessAlert.showAndWait();
                return true;
            }
        }
        return true;
    }

    /**
     * Shows an alert message if the user tried to reset the database but had no connection or not sufficient user rights.
     *
     * @param msg the message that will be shown in the alert
     */
    private void showAlertResetFail(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database error");

        // Header Text: null
        alert.setHeaderText("Could not reset database");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Checks if the selection in the ComboBoxes is valid - If not: shows an alert - If yes: starts evaluation.
     */
    public void clickApply() {
        // check if more than one player is selected
        playerNameList = new ArrayList<>();
        if (!boxPlayer1.getValue().equals("None"))
            playerNameList.add(boxPlayer1.getValue().toString());
        if (!boxPlayer2.getValue().equals("None"))
            playerNameList.add(boxPlayer2.getValue().toString());
        if (!boxPlayer3.getValue().equals("None"))
            playerNameList.add(boxPlayer3.getValue().toString());
        if (!boxPlayer4.getValue().equals("None"))
            playerNameList.add(boxPlayer4.getValue().toString());
        // sort list
        Collections.sort(playerNameList);
        if (playerNameList.size() < 2) {
            resetPieChart();
            resetLabels();
            showAlertSelectionFail("Selection error", "Please select at least two players.");
            return;
        }else {
            numberOfParticipants = playerNameList.size();
        }

        // open window with wait message and center it
        Window window = btnApply.getScene().getWindow();
        showWaitWindow(window);

        // run task/thread with evaluation, then close message
        Task checkEvaluateDataTask = new Task<Void>() {
            @Override
            public Void call() {
                Platform.runLater(
                        () -> {
                            // TIMESTAMP
                            //System.out.println("#### " + this.getClass() + ": method " + new Object(){}.getClass().getEnclosingMethod().getName() + "() is running now at line XXXX. Time: " + java.time.Clock.systemUTC().instant());
                            //evaluateDataClientHeavy();
                            evaluateDataDBHeavy();
                            waitAlert.setResult(ButtonType.OK);
                            waitAlert.close();
                            // TIMESTAMP
                            //System.out.println("#### " + this.getClass() + ": method " + new Object(){}.getClass().getEnclosingMethod().getName() + "() is running now at line XXXX. Time: " + java.time.Clock.systemUTC().instant());

                        }
                );
                return null;
            }
        };
        new Thread(checkEvaluateDataTask).start();
        // run timer to check if this task is finished. If finished run update on gui and close waitAlert
    }

    /**
     * Resets all labels and selects data from database. Calls updateGUI() to fill GUI with new information.
     *
     * @return false if a database operation fails, otherwise true
     */
    public boolean evaluateDataDBHeavy() {
        // reset labels
        resetLabels();

        // create Map with player-name and count how often he participates
        Map<String, Integer> nameToPart = new HashMap<>();
        for (String playerName : playerNameList) {
            if (nameToPart.containsKey(playerName)) {
                nameToPart.put(playerName, nameToPart.get(playerName) + 1);
            } else {
                nameToPart.put(playerName, 1);
            }
        }

        // open conn
        if (!dbAccess.openConn()) {
            showAlertDBError();
            return false;
        }

        // get List with all games that finished (end_time != null) and have
        // the right number of each participant -> goes to totalGames
        String strSQL = "SELECT max_turn.game, max_turn.turn, players.name, participants.winner FROM max_turn INNER JOIN participants ON max_turn.game = participants.game " +
                "INNER JOIN players ON participants.player = players.id " +
                "INNER JOIN games ON games.id = max_turn.game WHERE end_time IS NOT null GROUP BY max_turn.game " +
                "HAVING ";
        // add criteria of how often a player participates in a game in a loop (for every player)
        int counter = 0;
        for (Map.Entry<String, Integer> entry : nameToPart.entrySet()) {
            if (counter != 0)
                strSQL += "AND ";
            strSQL += "(SELECT COUNT(participants.player) FROM participants INNER JOIN players ON players.id = participants.player WHERE participants.game = max_turn.game AND players.name = '";
            strSQL += entry.getKey() + "') = ";
            strSQL += entry.getValue() + " ";
            counter++;
        }
        // add criteria total amount of participants in game (numberOfParticipants)
        strSQL += "AND (SELECT COUNT(participants.player) FROM participants INNER JOIN players ON players.id = participants.player " +
                "WHERE participants.game = max_turn.game) = " + numberOfParticipants +";";
        // do select on db
        List<Map<String, Object>> resultList = dbAccess.selectSQL(strSQL);
        // check for dbError
        if(resultList == null){
            showAlertDBError();
            return false;
        }
        totalGames = resultList.size();


        // first new map to save wins - copy names from nameToPart
        nameToWins = new HashMap<>();
        for(Map.Entry<String, Integer> entry : nameToPart.entrySet()){
            nameToWins.put(entry.getKey(), 0);
        }

        // get list with winners and average turns - iterate through resultList of all games WITH A WINNER and the max turns
        strSQL = "SELECT max_turn.game, max_turn.turn, players.name, participants.winner FROM max_turn INNER JOIN participants ON max_turn.game = participants.game " +
                "INNER JOIN players ON participants.player = players.id " +
                "INNER JOIN games ON games.id = max_turn.game WHERE end_time IS NOT null AND winner = 1 " +
                "HAVING ";
        // add criteria of how often a player participates in a game in a loop (for every player)
        counter = 0;
        for (Map.Entry<String, Integer> entry : nameToPart.entrySet()) {
            if (counter != 0)
                strSQL += "AND ";
            strSQL += "(SELECT COUNT(participants.player) FROM participants INNER JOIN players ON players.id = participants.player WHERE participants.game = max_turn.game AND players.name = '";
            strSQL += entry.getKey() + "') = ";
            strSQL += entry.getValue() + " ";
            counter++;
        }
        // add criteria total amount of participants in game (numberOfParticipants)
        strSQL += "AND (SELECT COUNT(participants.player) FROM participants INNER JOIN players ON players.id = participants.player " +
                "WHERE participants.game = max_turn.game) = " + numberOfParticipants +";";
        // do select on db
        resultList = dbAccess.selectSQL(strSQL);
        // check for dbError
        if(resultList == null){
            showAlertDBError();
            return false;
        }

        // close conn
        dbAccess.closeConn();

        // iterate resultList with winners, write winners, sum up turns
        int turnSum = 0;
        for(Map<String, Object> row : resultList){
            String name = (String) row.get("name");
            nameToWins.put(name, nameToWins.get(name) + 1);
            int maxTurn = (int) row.get("turn");
            turnSum += maxTurn;
        }
        // until now nameToWins only holds winners - adding players without wins to the map with (with 0 as wins)
        for(Map.Entry<String, Integer> entry : nameToPart.entrySet()){
            if(!nameToWins.containsKey(entry.getKey()))
                nameToWins.put(entry.getKey(), 0);
        }

        // calculate games without winner
        int gamesWithWinner = resultList.size();
        gamesWithoutWinner = totalGames-gamesWithWinner;
        // calculate average turns
        if(gamesWithWinner > 0){
            averageTurns = Math.round(turnSum/gamesWithWinner);
        }else{
            averageTurns = 0;
        }
        // update gui and end
        updateGUI();
        return true;
    }

    /**
     * A former approach to evaluate data, loads tables with all their data from db into ArrayLists and evaluates
     * them in java - this method is currently out of use.
     *
     * @return false if a database operation fails, otherwise true
     */
    public boolean evaluateDataClientHeavy() {
        // reset labels
        resetLabels();
        // get IDs of players
        // create one map with playerName and playerID
        // create one map with playerID and the count how often this player participates in this game
        List<Map<String, Object>> returnList;
        Map<String, Integer> mapNameToID = new HashMap<>();
        HashMap<Integer, Integer> mapIDToCount = new HashMap<>();
        if (!dbAccess.openConn()) {
            showAlertDBError();
            return false;
        }
        for (String playerName : playerNameList) {
            returnList = dbAccess.selectSQL("SELECT players.id FROM players WHERE players.name = '" + playerName + "';");
            if (returnList == null || returnList.isEmpty()) {
                showAlertDBError();
                return false;
            } else {
                int playerID = (int) returnList.get(0).get("id");
                if (!mapNameToID.containsKey(playerName)) {
                    mapNameToID.put(playerName, playerID);
                }
                if (!mapIDToCount.containsKey(playerID)) {
                    mapIDToCount.put(playerID, 1);
                } else {
                    mapIDToCount.put(playerID, mapIDToCount.get(playerID) + 1);
                }
            }
        }

        // get all games and all participants from DB
        List<Map<String, Object>> dbGames = dbAccess.selectSQL("SELECT * FROM games;");
        List<Map<String, Object>> dbParticipants = dbAccess.selectSQL("SELECT * FROM participants;");
        if (dbGames == null || dbParticipants == null) {
            showAlertDBError();
            return false;
        }
        dbAccess.closeConn();

        // create winners map
        // holds gameID and ID of winning participant, if game has no winner participantID is -1
        HashMap<Integer, Integer> mapGameIDToWinnerID = new HashMap<>();

        // iterate all games
        for (Map<String, Object> rowGames : dbGames) {
            boolean dumpGame = false;
            int gameId = (int) rowGames.get("id");
            String endTime = null;
            if (rowGames.get("end_time") != null)
                endTime = rowGames.get("end_time").toString();
            boolean isAIOnly = (boolean) rowGames.get("ai_only");
            // get deepCopy of mapIDToCount
            Map<Integer, Integer> idToCountCopy = copyHashMapIntInt(mapIDToCount);
            // check if game was aborted preemptively
            if (endTime == null)
                continue;
            int winner = -1;

            //iterate all participants
            for (Map<String, Object> rowParticipants : dbParticipants) {
                // check if game.id matches participants.game
                if (gameId == (int) rowParticipants.get("game")) {
                    // get participants.id
                    int playerID = (int) rowParticipants.get("player");
                    // check if participant is in the map of participants, if not skip to next game (break)
                    if (idToCountCopy.containsKey(playerID)) {
                        // check if the count in this map is higher than 0, if not skip to next game (break)
                        if (idToCountCopy.get(playerID) > 0) {
                            // lower value of count in map of participants for this participant
                            idToCountCopy.put(playerID, idToCountCopy.get(playerID) - 1);
                            // if this participant is winner - store it temporarily
                            if (rowParticipants.get("winner") != null) {
                                winner = playerID;
                            }
                        } else {
                            dumpGame = true;
                            break;
                        }
                    } else {
                        dumpGame = true;
                        break;
                    }
                }
            }
            // check if every value in idToCountCopy is 0
            // if yes -> save game and winner in winners map
            // if not -> dump game
            for (Map.Entry<Integer, Integer> entry : idToCountCopy.entrySet()) {
                if (entry.getValue() != 0) {
                    dumpGame = true;
                }
            }
            if (dumpGame == false) {
                mapGameIDToWinnerID.put(gameId, winner);
            }
        }

        // check if any games with this constellation of players was played
        if (mapGameIDToWinnerID.isEmpty()) {
            resetPieChart();
            resetLabels();
            showAlertSelectionFail("Selection error", "No games with this constellation were found.");
            return false;
        }

        // calculate average turns until winner is determined
        if (!dbAccess.openConn()) {
            showAlertDBError();
            return false;
        }
        Integer turnSum = 0;
        Integer gameCounter = 0;
        int averageTurns = 0;
        for (Map.Entry<Integer, Integer> entry : mapGameIDToWinnerID.entrySet()) {
            // check if game has winner
            if (entry.getValue() != -1) {
                // get number of turns for this game
                List<Map<String, Object>> resultList = dbAccess.selectSQL("SELECT MAX(turn) FROM moves WHERE game = " + entry.getKey() + ";");
                if (resultList == null || resultList.isEmpty()) {
                    showAlertDBError();
                    return false;
                } else {
                    turnSum += (int) resultList.get(0).get("MAX(turn)");
                    gameCounter++;
                }
            }
        }

        // calculate average turns
        String strAverageTurns = "";
        if (gameCounter > 0) {
            averageTurns = Math.round(turnSum / gameCounter);
        }
        strAverageTurns = String.valueOf(averageTurns);
        dbAccess.closeConn();

        // now process mapGameIDToWinnerID to pieChart and labels
        Map<String, Integer> mapNameToWins = new HashMap<>();
        // iterate over GameID-WinnerID map and count wins for each player
        int endlessGames = 0;
        for (Map.Entry<Integer, Integer> entryWins : mapGameIDToWinnerID.entrySet()) {
            if (entryWins.getValue() == -1)
                endlessGames++;
            for (Map.Entry<String, Integer> entryName : mapNameToID.entrySet()) {
                if (entryName.getValue() == entryWins.getValue()) {
                    if (!mapNameToWins.containsKey(entryName.getKey())) {
                        mapNameToWins.put(entryName.getKey(), 1);
                    } else {
                        mapNameToWins.put(entryName.getKey(), mapNameToWins.get(entryName.getKey()) + 1);
                    }
                }
            }
        }
        // add players without wins to the map
        for (Map.Entry<String, Integer> entry : mapNameToID.entrySet()) {
            if (!mapNameToWins.containsKey(entry.getKey()))
                mapNameToWins.put(entry.getKey(), 0);
        }

        // fill labels with numbers
        lblTotalGames.setText(String.valueOf(mapGameIDToWinnerID.size()));
        lblGamesWithoutWinner.setText(String.valueOf(endlessGames));
        if (averageTurns == 0) {
            lblAverageTurns.setText("Not available");
        } else {
            lblAverageTurns.setText(strAverageTurns);
        }
        int labelCounter = 1;
        for (Map.Entry<String, Integer> entry : mapNameToWins.entrySet()) {
            vBoxDesc.getChildren().add(labelCounter, new Label(entry.getKey() + ":"));
            vBoxNumbers.getChildren().add(labelCounter, new Label(String.valueOf(entry.getValue()) + " wins"));
        }

        // fill pieChart
        pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : mapNameToWins.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        if (endlessGames > 0) {
            pieChartData.add(new PieChart.Data("No winner", endlessGames));
        }
        chart.setData(pieChartData);
        chart.setTitle("Winners by percentage");

        lblCaption.setTextFill(Color.web("#404040", 1.0));
        lblCaption.setStyle("-fx-font: 24 arial;");

        // set percentages
        for (PieChart.Data data : chart.getData()) {
            data.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent e) {
                            lblCaption.setTranslateX(e.getSceneX());
                            lblCaption.setTranslateY(e.getSceneY());
                            double percentage = (data.getPieValue() * 100) / Integer.valueOf(mapGameIDToWinnerID.size()).doubleValue();
                            String strPercentage = formatDec(percentage);
                            lblCaption.setText(String.valueOf(strPercentage + "%"));
                        }
                    });
        }
        // add pieChart and caption
        if (!diagramBox.getChildren().contains(chart))
            diagramBox.getChildren().add(chart);
        if (!root.getChildren().contains(lblCaption))
            root.getChildren().add(lblCaption);
        return true;
    }

    /**
     * Creates a copy of a HashMap.
     * @param original the original HashMap
     * @return the copy of the HashMap
     */
    private HashMap<Integer, Integer> copyHashMapIntInt(HashMap<Integer, Integer> original) {
        HashMap<Integer, Integer> copy = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : original.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    /**
     * Closes the stage, gets called when btnClose gets clicked.
     */
    public void clickClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    /**
     * Resets labels to their initial state.
     */
    private void resetLabels() {
        lblCaption.setText("");
        vBoxDesc.getChildren().clear();
        vBoxNumbers.getChildren().clear();
        Label lblTotalGamesDesc = new Label("Total games:");
        vBoxDesc.getChildren().add(lblTotalGamesDesc);
        lblTotalGamesDesc.setMinWidth(200);
        vBoxDesc.getChildren().add(new Label("Games without winner:"));
        vBoxDesc.getChildren().add(new Label("Average number of turns:"));
        Label lblOnlyGamesWithWinnerDesc = new Label("(only games with winner)");
        vBoxDesc.getChildren().add(lblOnlyGamesWithWinnerDesc);
        lblOnlyGamesWithWinnerDesc.setTranslateY(-10);
        lblTotalGames.setText("");
        lblGamesWithoutWinner.setText("");
        lblAverageTurns.setText("");
        vBoxNumbers.getChildren().add(lblTotalGames);
        vBoxNumbers.getChildren().add(lblGamesWithoutWinner);
        vBoxNumbers.getChildren().add(lblAverageTurns);
    }

    /**
     * Resets ComboBoxes to their initial state.
     */
    private void resetComboBoxes() {
        // empty dropdowns
        boxPlayer1.getItems().clear();
        boxPlayer2.getItems().clear();
        boxPlayer3.getItems().clear();
        boxPlayer4.getItems().clear();

        // create list for dropdowns
        if (!dbAccess.openConn()) {
            showAlertDBError();
            return;
        }
        ObservableList<String> playersBox1 = FXCollections.observableArrayList();
        ObservableList<String> playersBox2 = FXCollections.observableArrayList();
        ObservableList<String> playersBox3 = FXCollections.observableArrayList();
        ObservableList<String> playersBox4 = FXCollections.observableArrayList();

        // VERSION 1 (at least one game finished)
        // get all players that have at least one finished game
        // selecting just all players would reduce time to open window a lot
//        List<Map<String, Object>> resultList = dbAccess.selectSQL("SELECT * FROM players " +
//                "INNER JOIN participants ON participants.player = players.id " +
//                "INNER JOIn games ON participants.game = games.id " +
//                "WHERE games.end_time IS NOT null GROUP BY players.name;");

        // VERSION 2 (all players)
        List<Map<String, Object>> resultList = dbAccess.selectSQL("SELECT * FROM players");
        if (resultList == null) {
            showAlertDBError();
            return;
        } else {
            for (Map<String, Object> row : resultList) {
                playersBox1.add((String) row.get("name"));
                playersBox2.add((String) row.get("name"));
                playersBox3.add((String) row.get("name"));
                playersBox4.add((String) row.get("name"));
            }
        }
        // sort lists
        Collections.sort(playersBox1);
        Collections.sort(playersBox2);
        Collections.sort(playersBox3);
        Collections.sort(playersBox4);
        playersBox1.add(0, "None");
        playersBox2.add(0, "None");
        playersBox3.add(0, "None");
        playersBox4.add(0, "None");
        // fill comboBoxes
        boxPlayer1.getItems().addAll(playersBox1);
        boxPlayer1.getSelectionModel().selectFirst();
        boxPlayer2.getItems().addAll(playersBox2);
        boxPlayer2.getSelectionModel().selectFirst();
        boxPlayer3.getItems().addAll(playersBox3);
        boxPlayer3.getSelectionModel().selectFirst();
        boxPlayer4.getItems().addAll(playersBox4);
        boxPlayer4.getSelectionModel().selectFirst();
    }

    /**
     * Resets the pie-chart to its initial state.
     */
    private void resetPieChart() {
        diagramBox.getChildren().clear();
    }

    /**
     * Creates an alert and shows it (ShowAndWait()).
     * @param title The title of the alert
     * @param msg The message of the alert
     */
    private void showAlertSelectionFail(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);

        // Header Text: null
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Calls showAlertSelectionFail with a predefined error message for database-errors.
     */
    private void showAlertDBError() {
        showAlertSelectionFail("Database error", "Could not establish a connection to the database. Please visit the database settings to verify your connection.");
    }

    /**
     * Formats a double to a two-decimals number and converts it to a String.
     * @param number the double to format and convert
     * @return the result as a String
     */
    private String formatDec(double number) {
        String returnString = "";
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        returnString = df.format(number);
        int dotPosition = returnString.lastIndexOf(".");
        if (dotPosition == -1) {
            returnString += ".00";
        } else {
            if (returnString.substring(dotPosition, returnString.length() - 1).length() == 1) {
                returnString += "0";
            }
        }
        return returnString;
    }

    /**
     * Creates and shows (show()) a messages while evaluating data, the message gets centered at the window it was called from.
     * @param window the window the message should get centered at
     * @return the alert object
     */
    private Alert showWaitWindow(Window window) {
        // modify alert pane
        //Alert waitAlert = new Alert(Alert.AlertType.NONE);
        waitAlert.setTitle(null);
        waitAlert.setHeaderText(null);
        waitAlert.setContentText("  Please wait while data is evaluated ...");
        waitAlert.getDialogPane().setBackground(new Background(new BackgroundFill(Color.DARKGRAY, null, null)));
        waitAlert.getDialogPane().setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        waitAlert.getDialogPane().setMaxWidth(250);
        waitAlert.getDialogPane().setMaxHeight(50);

        // center on window below (statistics window)
        final double windowCenterX = window.getX() + (window.getWidth() / 2);
        final double windowCenterY = window.getY() + (window.getHeight() / 2);
        // verify
        if (!Double.isNaN(windowCenterX)) {
            // set a temporary position
            waitAlert.setX(windowCenterX);
            waitAlert.setY(windowCenterY);

            // since the dialog doesn't have a width or height till it's shown, calculate its position after it's shown
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    waitAlert.setX(windowCenterX - (waitAlert.getWidth() / 2));
                    waitAlert.setY(windowCenterY - (waitAlert.getHeight() / 2));
                }
            });
        }
        waitAlert.show();
        return waitAlert;
    }

    /**
     * Calls a method to open and immediately close the waiting window again, this ensures the window will be positioned correctly when called the next time
     *
     * @param window the window the alert should be centered on
     */
    public void prePositionWaitAlert(Window window) {
        showWaitWindow(window);
        waitAlert.setResult(ButtonType.OK);
        waitAlert.close();
    }

    /**
     * Resets various GUI elements if no games that match the criteria were found in the database, otherwise fills GUI elements with data.
     *
     * @return false if no games were found, otherwise true
     */
    private boolean updateGUI(){
        // check if there were games found
        if(totalGames == 0){
            resetPieChart();
            resetLabels();
            showAlertSelectionFail("Selection error", "No games with this constellation were found.");
            return false;
        }

        // fill labels with text
        lblTotalGames.setText(String.valueOf(totalGames));
        lblGamesWithoutWinner.setText(String.valueOf(gamesWithoutWinner));
        if (averageTurns == 0) {
            lblAverageTurns.setText("Not available");
        } else {
            lblAverageTurns.setText(String.valueOf(averageTurns));
        }
        // remove duplicate players -> playerNameListNoDuplicates
        Set<String> playerSet = new HashSet<>(playerNameList);
        List<String> playerNameListNoDuplicates = new ArrayList<>();
        playerNameListNoDuplicates.addAll(playerSet);
        // sort new List
        Collections.sort(playerNameListNoDuplicates);
        int labelCounter = 1;
        for (String playerName : playerNameListNoDuplicates) {
            vBoxDesc.getChildren().add(labelCounter, new Label(playerName + ":"));
            vBoxNumbers.getChildren().add(labelCounter, new Label(String.valueOf(nameToWins.get(playerName)) + " wins"));
            labelCounter++;
        }

        // fill pieChart
        fillPieChart();

        return true;
    }

    /**
     * Fills the pie-chart with data from the database.
     */
    public void fillPieChart(){
        pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : nameToWins.entrySet()) {
            double percentage = (entry.getValue() * 100) / Integer.valueOf(totalGames).doubleValue();
            String strPercentage = formatDec(percentage);
            pieChartData.add(new PieChart.Data(entry.getKey() + " (" + strPercentage + "%)", entry.getValue()));
        }
        if (gamesWithoutWinner > 0) {
            pieChartData.add(new PieChart.Data("No winner", gamesWithoutWinner));
        }
        chart.setData(pieChartData);
        chart.setTitle("Winners by percentage");


        lblCaption.setTextFill(Color.web("#404040", 1.0));
        lblCaption.setStyle("-fx-font: 24 arial;");

        // set percentages
        for (PieChart.Data data : chart.getData()) {
            data.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent e) {
                            lblCaption.setTranslateX(e.getSceneX());
                            lblCaption.setTranslateY(e.getSceneY());
                            double percentage = (data.getPieValue() * 100) / Integer.valueOf(totalGames).doubleValue();
                            String strPercentage = formatDec(percentage);
                            lblCaption.setText(String.valueOf(strPercentage + "%"));
                        }
                    });
        }
        // add pieChart and caption
        if (!diagramBox.getChildren().contains(chart))
            diagramBox.getChildren().add(chart);
        if (!root.getChildren().contains(lblCaption))
            root.getChildren().add(lblCaption);
    }
}
