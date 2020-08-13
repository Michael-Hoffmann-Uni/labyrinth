package logic;

import database.DatabaseAccess;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.*;
import view.GUIController;

import java.math.BigInteger;
import java.util.*;

import static java.lang.Thread.sleep;

/**
 * Die Hauptklasse des Spiels. Hier laufen alle Informationen über den GameState, die Spieler und die Rules zusammen und die von den Spielern angeforderten Züge werden aufgeführt.
 */

public class Game_Impl implements Game {
    public HashMap<Color, Player> players;
    private HashMap<Color, ArrayList<Symbol>> wonCardsOfColor;
    public GameState gameState;
    public Rules rules;
    private boolean running = false;
    private int infinityCounter = 0;
    private int infinityCondition = 1000;
    private ThreadUpdate thread;
    private Object synchronizer;
    private GUIController guiController;
    private DatabaseAccess dbAccess;
    private List<String> sqlStringsList;
    private BigInteger dbGameID;
    private boolean isSimulationGame;
    private boolean suppressDBWarnings = false;

    public Game_Impl() {
        players = new HashMap<>();
        isSimulationGame = false;
        sqlStringsList = new ArrayList<>();
        wonCardsOfColor = new HashMap<>();
    }


    @Override
    public HashMap<Color, Player> getPlayers() {
        return players;
    }

    @Override
    public ArrayList<Symbol> getWonCards(Color color) {
        return wonCardsOfColor.get(color);
    }


    @Override
    public ArrayList<Color> getPlayedColors() {
        ArrayList<Color> playedColors = new ArrayList<>();
        for (Map.Entry<Color, Player> entry : players.entrySet()) {
            playedColors.add(entry.getKey());
        }
        return playedColors;
    }

    public ThreadUpdate getThread() {
        return thread;
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public void setRules(Rules rules) {
        this.rules = rules;
        //initialize wonCardsOfColor
        ArrayList<Color> playedColors = rules.getPlayedColors();
        for (Color c : playedColors) {
            wonCardsOfColor.put(c, new ArrayList<Symbol>());
        }
    }


    @Override
    public void start(GameState initialGameState) {
        //Wenn weniger als 2 Spieler eingetragen sind, macht die Methode nichts.
        if (players.size() < 2) throw new IllegalArgumentException("you can't start a game with less than two players");
        //verteilt die Rules an alle Spieler
        for (Map.Entry<Color, Player> entry : players.entrySet()) {
            entry.getValue().setRules(rules);
        }
        //Zieht eine Karte für jeden Spieler
        for (Map.Entry<Color, Player> entry : players.entrySet()) {
            entry.getValue().setActiveCard(rules.drawCard(entry.getKey()));
        }
        gameState = initialGameState;
        running = true;

        thread = new ThreadUpdate(this);

        //setThread for Players
        for (Map.Entry<Color, Player> entry : players.entrySet()) {
            entry.getValue().setThread(thread);
        }

        synchronizer = thread.getSynchronizer();
        //set synchronizer for Players
        for (Map.Entry<Color, Player> entry : players.entrySet()) {
            entry.getValue().setSynchronizer(synchronizer);
        }
        if (guiController != null) {
            guiController.setSynchronizer(synchronizer);
        }
        writeGameToDB();
        thread.start();
    }

    @Override
    public Rules getRules() {
        return rules;
    }

    /* Color ist als Übergabeparameter wichtig, falls ein KIPlayer ohne Color-Attribut erstellt wird */
    @Override
    public boolean join(Player player, Color color) {
        if (players.containsKey(color) || players.size() >= 4) return false;
        players.put(color, player);
        return true;
    }

    @Override
    public boolean leave(Player player) {
        int z = players.size();
        for (Map.Entry<Color, Player> entry : players.entrySet()) {
            if (entry.getValue().equals(player)) {
                players.remove(entry.getKey());
            } else {
                z--;
            }
        }
        if (z == 0) return false;
        return true;
    }

    @Override
    public boolean leave(Color color) {
        if (!players.containsKey(color)) return false;
        players.remove(color);
        return true;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void delay(long time) {
        try {
            sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Karte ziehen falls Spieler eine Karte gewinnt??
    //GameState auch immer an GUI verteilen

    /**
     * 1. Verteilt aktuellen GameState an alle Spieler
     * 2. Fordert den aktuellen Spieler auf TileMove zu machen (requestAction)
     * 2.1 Überprüfen, ob der Zug gültig ist
     * 3. Wendet Zug auf das Spielfeld an
     * 4. GameState verteilen
     * (ActionsPlayerMove erst hier berechnen)
     * 5. fordert den aktuellen Spieler auf GamePieceMove zu machen
     * 5.1 Überprüfen, ob der Zug gültig ist
     * 5.2 Wendet Zug auf das Spielfeld an
     * 6.   6.1 überprüft ob activePlayer auf dem Tile steht mit Symbol == Player.getActiveCard()
     * 6.2 wenn ja:
     * notifyPlayers über gewonnene Karte des activePlayer
     * Player.setActiveCard(rules.drawCard(activeColor))
     * 7. nochmal GameState verteilen
     * 8. Überprüft auf Spielende oder Endlosspiel (!) und beendet falls ja
     * 9. ruft updateActiveColor in GameState auf
     * <p>
     * zu 6.
     * 6.1 überprüft ob activePlayer auf dem Tile steht mit Symbol == Player.getActiveCard()
     * 6.2 wenn ja:
     * notifyPlayers über gewonnene Karte des activePlayer
     * Player.setActiveCard(rules.drawCard(activeColor))
     */

    @Override
    public boolean update() {
        try {
            Symbol wonCard = Symbol.None;
            /* 1. Verteilt aktuellen GameState an alle Spieler*/
            if (guiController != null) {
                guiController.setShowWayOfGamePiece(true);
            }
            distributeGameStateToPlayers();
            Color activeColor = gameState.getActiveColor();
            Player activePlayer = players.get(activeColor);
            GamePiece activeGamePiece = gameState.getGamePiece(activeColor);


            /* 2. Fordert den aktuellen Spieler auf TileMove zu machen (requestAction) */
            GameState actionTile;
            actionTile = activePlayer.requestActionTile();

            /*  2.1 Überprüfen, ob der Zug gültig ist */
            if (!rules.getActionsTileMove(activeColor, gameState).contains(actionTile))
                throw new IllegalArgumentException("Invalid Move! Implement your AI according to the rules.");
            /* 3. Wendet Zug auf das Spielfeld an */
            gameState = actionTile;

            //activeColor, acticePlayer und activeGamePiece nach jedem aktualisieren von gameState auch aktualisieren
            activeColor = gameState.getActiveColor();
            activePlayer = players.get(activeColor);
            activeGamePiece = gameState.getGamePiece(activeColor);

            /* 4. GameState verteilen */
            if (guiController != null) {
                guiController.setSaveLastPos(true);
            }
            distributeGameStateToPlayers();

            /* 5. fordert den aktuellen Spieler auf GamePieceMove zu machen */
            GameState actionGamePiece = activePlayer.requestActionGamePiece();
            /*  5.1 Überprüfen, ob der Zug gültig ist */
            if (!rules.getActionsGamePieceMove(activeColor, gameState).contains(actionGamePiece))
                throw new IllegalArgumentException("Invalid Move! Implement your AI according to the rules.");
            /* 5.2 Wendet Zug auf das Spielfeld an */
            gameState = actionGamePiece;
            //activeColor, activePlayer und activeGamePiece nach jedem aktualisieren von gameState auch aktualisieren
            activeColor = gameState.getActiveColor();
            activePlayer = players.get(activeColor);
            activeGamePiece = gameState.getGamePiece(activeColor);


            /* 6. notify Players über gewonne Karten des Spielers der am Zug ist
               6.1 überprüft ob Player der activeColor auf dem Tile steht mit Symbol == Player.getActiveCard() */

            if (activeGamePiece.getX() == gameState.getPositionOfSymbol(activePlayer.getActiveCard())[0] &&
                    activeGamePiece.getY() == gameState.getPositionOfSymbol(activePlayer.getActiveCard())[1]) {
                /*        6.2 wenn ja:
                              notifyPlayers über gewonnene Karte des Spielers */
                notifyPlayersWonCard(activeColor, activePlayer.getActiveCard());
                wonCardsOfColor.get(activeColor).add(activePlayer.getActiveCard());
                wonCard = activePlayer.getActiveCard();
                /*        6.2      */
                activePlayer.setActiveCard(rules.drawCard(activeColor));
            } else {
                wonCard = Symbol.None;
            }

            /* Gather information about the move and write it to DB */
            saveMovesForDB(wonCard);

            /* 7. nochmal GameState verteilen */
            if (guiController != null) {
                guiController.setShowWayOfGamePiece(true);
            }
            distributeGameStateToPlayers();


            /* 8. Überprüft auf Spielende
                und beendet falls ja  */
            if (rules.isEndState(gameState)) {
                //System.out.println("Das Spiel ist vorbei! "+ rules.getWinner(gameState) + " hat gewonnen!");
                running = false;
                distributeGameStateToPlayers();
                writeMovesAndEndGameToDB(rules.getWinner(gameState));
                return false;
            }
            /* 8. Überprüft auf Endlosspiel */
            infinityCounter++;
            if (infinityCounter > infinityCondition) {
                writeMovesAndEndGameToDB(null);
                throw new IllegalStateException("Your implemented AI's games take an infinite amount of time.");
            }

            /* 9. ruft updateActiveColor in GameState auf */
            gameState.updateActiveColor();
            return true;
        }
        catch(IllegalArgumentException e){
            System.out.println("Invalid Move! Implement your AI according to the rules.");
        }
        catch(IllegalStateException e){
            System.out.println("Your implemented AI's games take an infinite amount of time. Aborted Game.");
        }
        return false;
    }


    private void notifyPlayersWonCard(Color color, Symbol symbol) {
        for (Map.Entry<Color, Player> entry : players.entrySet()) {
            entry.getValue().notifyWonCards(color, symbol);
        }
    }

    private void distributeGameStateToPlayers() {
        for (Map.Entry<Color, Player> entry : players.entrySet()) {
            entry.getValue().updateGameState(gameState);
        }
        if (guiController != null) {
            // https://stackoverflow.com/questions/17850191/why-am-i-getting-java-lang-illegalstateexception-not-on-fx-application-thread
            Platform.runLater(
                    () -> {
                        guiController.repaint(false);
                    }
            );
            try {
                if (!players.get(gameState.getActiveColor()).getClass().getName().contains("PlayerPhysical")) {
                    sleep(guiController.getDelay());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the GUIController for this instance of Game_Impl.
     * @param guiController the GUIController object
     */
    public void setGUIController(GUIController guiController) {
        this.guiController = guiController;
    }

    /**
     * Sets the DatabaseAccess object for this instance of Game_Impl.
     * @param dbAccess the DatabaseAccess object
     */
    public void setDBAccess(DatabaseAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    /**
     * Writes players of this game into database if they do not exist already, writes a game with its base information into the database, writes the participants of this game into the database.<br>
     * All this happens only if it is not a simulation game.
     */
    private void writeGameToDB() {
        // Error messages if no connection can be established
        if (!dbAccess.openConn()) {
            // error no dbCon for GUI and CLI{
            dbError();
        }

        // TABLE PLAYERS
        // only for GUI games, in simulation players are checked and inserted beforehand
        if (isSimulationGame == false) {
            Map<Color, Player> players = getPlayers();
            for (Map.Entry<Color, Player> entry : players.entrySet()) {
                Player player = entry.getValue();
                // standard value for type is 1 (AI) - if human: set to 0
                int type = 1;
                if (player.getClass().getName().contains("PlayerPhysical"))
                    type = 0;
                List<Map<String, Object>> dbPlayers = dbAccess.selectSQL("SELECT * FROM players WHERE name = '" + player.getName() + "'");
                if (dbPlayers == null) {
                    dbError();
                } else {
                    if (dbPlayers.isEmpty()) {
                        //System.out.println("player " + player.getName() + " NOT found");
                        if (!dbAccess.insertSQL("INSERT INTO players (name, type, description, is_active, create_date) " +
                                "VALUES ('" + player.getName() + "', " + type + ", " + null + ", " + 1 + ", '" + dbAccess.createTimestamp() + "')")) {
                            dbError();
                        }
                    }
                }
            }
        }

        // TABLE GAMES
        // store games id after insert (for moves, participants and later updates) with last_inser_ID()
        // this is save in a multi-thread/user environment as stated by this link:
        // https://stackoverflow.com/questions/30959678/thread-safety-of-mysqls-select-last-insert-id
        // https://dev.mysql.com/doc/refman/8.0/en/information-functions.html#function_last-insert-id
        // since each thread has its on DatabaseAccess object and therefore its own connection it applies to our app

        // AI game or not?
        boolean aiGame = true;
        for (Map.Entry<Color, Player> entry : players.entrySet()) {
            Player player = entry.getValue();
            if (player.getClass().getName().contains("PlayerPhysical"))
                aiGame = false;
        }

        // build String for initialGameState
        String strInitGameState = createInitialGameState();

        if (!dbAccess.insertSQL("INSERT INTO games (start_time, end_time, initial_board, ai_only) VALUES ('" + dbAccess.createTimestamp() + "', " + null + ", '" + strInitGameState + "', " + aiGame + ")")) {
            dbError();
        }

        // TABLE PARTICIPANTS
        // save last auto_increment FOR THIS CONNECTION in dbID
        List<Map<String, Object>> returnList = dbAccess.selectSQL("SELECT LAST_INSERT_ID();");
        if (returnList == null || returnList.isEmpty()) {
            dbError();
        } else {
            dbGameID = (BigInteger) returnList.get(0).get("LAST_INSERT_ID()");
        }

        // insert participant for every player in this game
        for (Map.Entry<Color, Player> entry : players.entrySet()) {
            Player player = entry.getValue();
            Integer playerID = 0;
            Integer colorID = 0;
            // get player ID
            returnList = dbAccess.selectSQL("SELECT id FROM players WHERE name = '" + player.getName() + "'");
            if (returnList == null || returnList.isEmpty()) {
                // error messages
                dbError();
            } else {
                playerID = (int) returnList.get(0).get("id");
            }
            // get color ID
            returnList = dbAccess.selectSQL("SELECT id FROM colors WHERE color = '" + player.getColor() + "'");
            if (returnList == null || returnList.isEmpty()) {
                // error messages
                dbError();
            } else {
                colorID = (int) returnList.get(0).get("id");
            }
            if (!dbAccess.insertSQL("INSERT INTO participants (player, game, color) VALUES (" + playerID + ", " + dbGameID + ", '" + colorID + "')")) {
                // error messages
                dbError();
            }
        }

        dbAccess.closeConn();
    }

    /**
     * Saves a insert query for every move/turn a participant takes as a String in a List (sqlStringsList).
     * @param wonCard the card the participant won in this move/turn, null if no card was won
     */
    private void saveMovesForDB(Symbol wonCard) {
        int turn = infinityCounter;

        //new_tile_pos  - nextcloud 1-12 bzw 0-11
        int oldTile50Pos = calcOldTile50Pos(gameState.getTile50Position());

        //get orientation of oldTile50
        int orientation = getOldTile50Orientation(oldTile50Pos);

        //symbol
        String wonCardString;
        if (wonCard.equals(Symbol.None)) {
            wonCardString = null;
        } else {
            wonCardString = wonCard.toString();
        }

        //create SQL-String and add to sqlStrings
        String sqlString = "INSERT INTO moves (game, turn, participant, orientation, new_tile_pos, new_player_pos, symbol) " +
                "VALUES (" +
                // game
                dbGameID + ", " +
                // turn
                turn + ", " +
                // participant
                "(SELECT participants.id FROM participants " +
                "INNER JOIN colors ON participants.color = colors.id " +
                "INNER JOIN games ON participants.game = games.id " +
                "WHERE colors.color = '" + getFixedColorString(gameState.getActiveColor()) + "' AND games.id = " + dbGameID + "), " +
                // orientation
                orientation + ", " +
                // new_tile_pos
                oldTile50Pos + ", '" +
                // new_player_pos
                getPlayerPositionsAsString() + "', " +
                // symbol
                "(SELECT symbols.id FROM symbols WHERE symbols.name ='" + wonCardString + "')" + ");";
        sqlStringsList.add(sqlString);

    }

    /**
     * Writes the stored moves in sqlStringsList into the database.<br>
     * Finishes up the game by setting a winner in participants and an end_time in games tables.
     * @param winnerColor the color of the participant that won
     */
    private void writeMovesAndEndGameToDB(Color winnerColor) {
        if (!dbAccess.openConn()) {
            // error messages
            dbError();
        } else {
            // write moves
            if (sqlStringsList == null) {
                //System.out.println("Nothing to write");
            } else {
                for (String sql : sqlStringsList) {
                    if (!dbAccess.insertSQL(sql)) {
                        // error messages
                        dbError();
                    }
                }
            }
            // update table games with end_time
            if (!dbAccess.updateSQL("UPDATE games SET end_time='" + dbAccess.createTimestamp() + "' WHERE id=" + dbGameID + ";")) {
                // error messages
                dbError();
            }
            // update table participants with winner
            if (winnerColor != null) {
                if (!dbAccess.updateSQL("UPDATE participants INNER JOIN colors " +
                        "ON participants.color = colors.id " +
                        "SET participants.winner=" + true + " " +
                        "WHERE participants.game=" + dbGameID + " " +
                        "AND colors.color='" + winnerColor + "';")) {
                    // error messages
                    dbError();
                }
            }
            dbAccess.closeConn();
        }
    }

    /**
     * Specifies if this game is a GUI or a simulation game.
     *
     * @param isSimulationGame sets isSimulationGame to this boolean value
     */
    public void setIsSimulationGame(boolean isSimulationGame) {
        this.isSimulationGame = isSimulationGame;
    }

    /**
     * Since insert statements for moves are not executed immediately the active player/color would change
     * until the statement gets executed.<br> Therefore a fixed String gets returned through this method.
     *
     * @param activeColor the currently active color
     * @return the color of the parameter as a String
     */
    private String getFixedColorString(Color activeColor) {
        for (Map.Entry<Color, Player> entry : players.entrySet()) {
            if (activeColor.equals(entry.getKey()))
                return entry.getKey().toString();
        }
        return null;
    }

    /**
     * Returns the position of all players in the following schema:<br>
     * {{Y: posX, posY}, {R: posX, posY}, {B: posX, posY}, {G: posX, posY}}<br><br>
     * Where R=Red, Y=Yellow, G=Green, B=Blue<br>
     * If there are less than 4 players the number of entries shrinks.
     *
     * @return the concatenation of the Strings of each player
     */
    private String getPlayerPositionsAsString() {
        String outputString = "{";
        int counter = 0;
        for (Map.Entry<Color, Player> entry : players.entrySet()) {
            if (counter != 0)
                outputString += ", ";
            outputString += "{" + entry.getKey().toString().substring(0, 1) + ": " + gameState.getPositionX(Color.Blue) + ", " + gameState.getPositionY(Color.Blue) + "}";
            counter++;
        }
        outputString += "}";
        return outputString;
    }

    /**
     * Calculates the position where the last tile was inserted from the new position of tile50 (the tile that is free to insert for the player).
     *
     * @param newTile50Pos the position of the current Tile50, upon which the old position gets calculated
     * @return the position where the last tile was inserted
     */
    private int calcOldTile50Pos(int newTile50Pos) {
        switch (newTile50Pos) {
            case 0:
                return 8;
            case 1:
                return 7;
            case 2:
                return 6;
            case 3:
                return 11;
            case 4:
                return 10;
            case 5:
                return 9;
            case 6:
                return 2;
            case 7:
                return 1;
            case 8:
                return 0;
            case 9:
                return 5;
            case 10:
                return 4;
            case 11:
                return 3;
            default:
                return 77;
        }
    }

    /**
     * Takes the old inserting position and looks up the board position where the old tile50 is now.
     *
     * @param oldTile50Pos the position where Tile50 was inserted last turn.
     * @return the orientation of the tile that was inserted last
     */
    private int getOldTile50Orientation(int oldTile50Pos) {
        Tile[][] board = gameState.getBoard();
        switch (oldTile50Pos) {
            case 0:
                return board[1][0].getOrientation();
            case 1:
                return board[3][0].getOrientation();
            case 2:
                return board[5][0].getOrientation();
            case 3:
                return board[6][1].getOrientation();
            case 4:
                return board[6][3].getOrientation();
            case 5:
                return board[6][5].getOrientation();
            case 6:
                return board[5][6].getOrientation();
            case 7:
                return board[3][6].getOrientation();
            case 8:
                return board[1][6].getOrientation();
            case 9:
                return board[0][5].getOrientation();
            case 10:
                return board[0][3].getOrientation();
            case 11:
                return board[0][1].getOrientation();
            default:
                return 77;
        }
    }

    /**
     * Shows database error messages in cli or gui depending on whether it is a simulation game or not.
     */
    private void dbError() {
        if (isSimulationGame) {
            // CLI
            System.out.println("\nERROR! Could not write data into database.");
            System.exit(-1);
        } else {
            // GUI
            if (!suppressDBWarnings) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database error");
                alert.setHeaderText(null);
                alert.setContentText("Could not write data to database. The game may finish but no data will be stored.");
                alert.showAndWait();
                suppressDBWarnings = true;
            }
        }
    }

    /**
     * Sets suppressDBWarnings.
     * @param suppressDBWarnings sets suppressDBWarnings to this boolean value
     */
    public void setSuppressDBWarnings(boolean suppressDBWarnings) {
        this.suppressDBWarnings = suppressDBWarnings;
    }

    /** Builds a String of the initial board with he following schema:<br><br>
     {{{t1 r1}, {t2 r1}, ...   },<br>
     {        ...             },<br>
     {        ...             }},<br>
     T50={[type], [symbol], [orientation]}<br><br>
     with every entry like t1 r1 consisting of '[type], [symbol], [orientation]'
     and T50 as the free tile.
     *
     * @return the initial GameState as a String
     */
    private String createInitialGameState(){
        String strInitGameState = "{{";
        Tile[][] board = gameState.getBoard();
        for (int i = 0; i < board.length - 1; i++) {
            if (i != 0)
                strInitGameState += ", ";
            strInitGameState += "{";
            for (int j = 0; j < board[i].length - 1; j++) {
                if (j != 0)
                    strInitGameState += ", ";
                strInitGameState += "{" + board[i][j].getType() + ", " + board[j][i].getSymbol() + ", " + board[j][i].getOrientation() + "}";
            }
            strInitGameState += "}";
        }
        strInitGameState += "}, ";
        strInitGameState += "T50={" + gameState.getTile50().getType() + ", " + gameState.getTile50().getSymbol() + ", " + gameState.getTile50().getOrientation() + "}}";
        return strInitGameState;
    }
}
