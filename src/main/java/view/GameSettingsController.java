package view;

import database.DatabaseAccess;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import logic.Game_Impl;
import logic.Player;
import logic.PlayerPhysical;
import logic.Rules_Impl;
import model.Color;

import java.util.HashMap;
import java.util.Optional;

/**
 * Controller to gameSettings.fxml
 * Allows to initGames by selecting players, colors, AIs and names
 */
public class GameSettingsController {

    @FXML
    private Button cancelButton;
    @FXML
    private Button initGameButton;
    @FXML
    private Circle circleBlue;
    @FXML
    private Circle circleGreen;
    @FXML
    private Circle circleYellow;
    @FXML
    private Circle circleRed;
    @FXML
    private TextField nameBlue;
    @FXML
    private TextField nameGreen;
    @FXML
    private TextField nameYellow;
    @FXML
    private TextField nameRed;
    @FXML
    private ToggleButton toggleBlue;
    @FXML
    private ToggleButton toggleGreen;
    @FXML
    private ToggleButton toggleYellow;
    @FXML
    private ToggleButton toggleRed;
    @FXML
    private ToggleGroup humanPlayerToggleGroup;
    @FXML
    private ChoiceBox<String> choiceBlue;
    @FXML
    private ChoiceBox<String> choiceGreen;
    @FXML
    private ChoiceBox<String> choiceYellow;
    @FXML
    private ChoiceBox<String> choiceRed;

    private Game_Impl game;
    private GUIController guiController;
    private final HashMap<String, ToggleButton> toggleButtons = new HashMap<>();
    private DatabaseAccess dbAccess;
    private boolean suppressDBWarnings = false;

    /**
     * method run after starting GameSettings
     * Correct place to add new and more KI players to dropdown list!
     * <p>
     * insert new case in switch-statement in Player.createNewPlayer(Color color)
     */
    public void initialize() {
        ObservableList<String> list = FXCollections.observableArrayList();
        list.add("PlayerKI");
        list.add("PlayerKI2");
        list.add("PlayerKI3");
        list.add("PlayerKI4");
        //list.add("PlayerNewKI");


        choiceBlue.setItems(list);
        choiceBlue.getSelectionModel().selectFirst();
        choiceGreen.setItems(list);
        choiceGreen.getSelectionModel().selectFirst();
        choiceYellow.setItems(list);
        choiceYellow.getSelectionModel().selectFirst();
        choiceRed.setItems(list);
        choiceRed.getSelectionModel().selectFirst();

        toggleBlue.setOnAction(actionEvent -> {
            if (toggleBlue.isSelected()) {
                setSelectionKIOnly();
                choiceBlue.setVisible(false);
                nameBlue.setVisible(true);
                toggleBlue.setText("X");
            } else {
                setSelectionKIOnly();
            }
        });
        toggleGreen.setOnAction(actionEvent -> {
            if (toggleGreen.isSelected()) {
                setSelectionKIOnly();
                choiceGreen.setVisible(false);
                nameGreen.setVisible(true);
                toggleGreen.setText("X");
            } else {
                setSelectionKIOnly();
            }
        });
        toggleYellow.setOnAction(actionEvent -> {
            if (toggleYellow.isSelected()) {
                setSelectionKIOnly();
                choiceYellow.setVisible(false);
                nameYellow.setVisible(true);
                toggleYellow.setText("X");
            } else {
                setSelectionKIOnly();
            }
        });
        toggleRed.setOnAction(actionEvent -> {
            if (toggleRed.isSelected()) {
                setSelectionKIOnly();
                choiceRed.setVisible(false);
                nameRed.setVisible(true);
                toggleRed.setText("X");
            } else {
                setSelectionKIOnly();
            }
        });
        toggleButtons.put("blue", toggleBlue);
        toggleButtons.put("green", toggleGreen);
        toggleButtons.put("yellow", toggleYellow);
        toggleButtons.put("red", toggleRed);
    }

    public void setDatabaseAccess(DatabaseAccess dbAccess){
        this.dbAccess = dbAccess;
    }

    private void setSelectionKIOnly() {
        choiceBlue.setVisible(circleBlue.getRadius()==25);
        nameBlue.setVisible(false);
        toggleBlue.setText("");
        choiceGreen.setVisible(circleGreen.getRadius()==25);
        nameGreen.setVisible(false);
        toggleGreen.setText("");
        choiceYellow.setVisible(circleYellow.getRadius()==25);
        nameYellow.setVisible(false);
        toggleYellow.setText("");
        choiceRed.setVisible(circleRed.getRadius()==25);
        nameRed.setVisible(false);
        toggleRed.setText("");
    }

    /**
     * User clicked 'Close'-Button in GameSettings Dialog - close stage
     *
     */
    @FXML
    public void closeSettings() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * User clicked 'Start Game!'-Button in GameSettings Dialog
     * 1 check for valid GameSettings
     * 1.1 FALSE: throw away current information and let's try again with new dialog!
     * 1.2 TRUE: go on
     * 2 check for currently running game
     * 2.1 FALSE: go on
     * 2.2 TRUE: ask for aborting currently running game
     * 2.2.1 ABORT: stop currently running game without winner, go on
     * 2.2.2 ELSE: Go back to game
     * 3 init new game with new settings
     * 4 close settings windows and start playing!
     *
     */
    public void initGame() {
        // check database connection before starting a game
        if((!dbAccess.testCon()) && suppressDBWarnings == false){
            // open dialog that asks if user wants to continue, if it returns false end method (start no game)
            if(!dbNoConnectionConfirmation()) {
                return;
            }else{
                // after one warning and the user accepting it no more warnings will be shown
                suppressDBWarnings = true;
            }
        }
        // count Players to add - if under 2: recall GameSettings!
        int countPlayers = 0;
        for (ToggleButton tb : toggleButtons.values()) {
            if (tb.isVisible()) {
                countPlayers++;
            }
        }
        if (countPlayers < 2) {
            this.closeSettings();
            guiController.callMenuGameSettings();
        }

        // get game from guiController or create a new Game_Impl()
        game = guiController.getGame();
        // Ask for confirmation to abort a currently running game
        if (game != null && game.isRunning()) {
            ChoiceDialog<Boolean> gameAbort = new ChoiceDialog<>(true);
            gameAbort.setTitle("About");
            DialogPane dialogPane = new DialogPane();
            dialogPane.setHeaderText("Game is runnig!");
            dialogPane.setContentText("Would you like to abort currently running game?");
            gameAbort.setDialogPane(dialogPane);
            // Add YES and NO Button to dialog
            dialogPane.getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
            Button abortButton = (Button) dialogPane.lookupButton(ButtonType.YES);
            abortButton.setText("Abort Game!");
            gameAbort.setResultConverter((ButtonType type) -> {
                        ButtonBar.ButtonData data = type == null ? null : type.getButtonData();
                        return data == ButtonBar.ButtonData.YES;
                    }
            );
            // Show dialog and wait for user action
            Optional<Boolean> userAbortGame = gameAbort.showAndWait();


            if (userAbortGame.isPresent()) {
                if (!userAbortGame.get()) {
                    closeSettings();
                    return;
                }
                else {
                    // ToDo: maybe find a way to exit thread in a cleaner way - call method game.stop() (to be written)
                    // stop thread to avoid the user-aborted game is played to finish in background
                    game.getThread().stop();

                    // get a new game and set in guiController: new game, no players, set dbAccess in new game
                    game = new Game_Impl();
                    game.setSuppressDBWarnings(suppressDBWarnings);
                    // remove players from GUI
                    guiController.setPlayers(game.getPlayers());
                    guiController.setGame(game);
                    // start new game and distribute it to GUI!
                    game.setDBAccess(dbAccess);
                    game.setGUIController(guiController);
                }
            }
        }
        // there is an already finished game, so create a new one
        else {
            game = new Game_Impl();
            game.setSuppressDBWarnings(suppressDBWarnings);
            game.setDBAccess(dbAccess);
            guiController.setGame(game);
            game.setGUIController(guiController);
        }

        // try getting color of chosen human player
        // if it's not possible: watch a game of KIs
        String colorOfHumanPlayer = "";
        try {
            colorOfHumanPlayer = ((ToggleButton) humanPlayerToggleGroup.getSelectedToggle()).getId();
        } catch (NullPointerException npe) {
            // no human player in game - watch a game of computer players
        }

        // add blue player if present
        if (toggleBlue.isVisible()) {
            Player player;
            if (colorOfHumanPlayer.equals("toggleBlue")) {
                player = Player.createNewPlayer("PlayerHuman", Color.Blue);
                player.setName(!nameBlue.getText().equals("")?nameBlue.getText():"John Blue");
                ((PlayerPhysical) player).setGuiController(guiController);
            } else {
                player = Player.createNewPlayer(choiceBlue.getValue(), Color.Blue);
                player.setName(choiceBlue.getValue());
            }
            addPlayerToGame(game, player, Color.Blue);
        }
        // add green player if present
        if (toggleGreen.isVisible()) {
            Player player;
            if (colorOfHumanPlayer.equals("toggleGreen")) {
                player = Player.createNewPlayer("PlayerHuman", Color.Green);
                player.setName(!nameGreen.getText().equals("")?nameGreen.getText():"John Green");
                ((PlayerPhysical) player).setGuiController(guiController);
            } else {
                player = Player.createNewPlayer(choiceGreen.getValue(), Color.Green);
                player.setName(choiceGreen.getValue());
            }
            addPlayerToGame(game, player, Color.Green);
        }
        // add yellow player if present
        if (toggleYellow.isVisible()) {
            Player player;
            if (colorOfHumanPlayer.equals("toggleYellow")) {
                player = Player.createNewPlayer("PlayerHuman", Color.Yellow);
                player.setName(!nameYellow.getText().equals("")?nameYellow.getText():"John Yellow");
                ((PlayerPhysical) player).setGuiController(guiController);
            } else {
                player = Player.createNewPlayer(choiceYellow.getValue(), Color.Yellow);
                player.setName(choiceYellow.getValue());
            }
            addPlayerToGame(game, player, Color.Yellow);
        }
        // add red player if present
        if (toggleRed.isVisible()) {
            Player player;
            if (colorOfHumanPlayer.equals("toggleRed")) {
                player = Player.createNewPlayer("PlayerHuman", Color.Red);
                player.setName(!nameRed.getText().equals("")?nameRed.getText():"John Red");
                ((PlayerPhysical) player).setGuiController(guiController);
            } else {
                player = Player.createNewPlayer(choiceRed.getValue(), Color.Red);
                player.setName(choiceRed.getValue());
            }
            addPlayerToGame(game, player, Color.Red);
        }


        // rules must be called AFTER joining players
        Rules_Impl rules = new Rules_Impl(game.getPlayedColors());
        game.setRules(rules);

        //distribute players in game to GUIController
        guiController.setPlayers(game.getPlayers());
        game.start(rules.getInitialGameStateRandom());

        Stage stage = (Stage) initGameButton.getScene().getWindow();
        stage.close();
    }

    private void addPlayerToGame(Game_Impl game, Player player, Color color) {
        game.join(player, color);
    }

    /**
     * switch Color BLUE between ACTIVE and INACTIVE
     */
    public void switchStatusBlue() {
        changeCircleState(circleBlue);
        changeHumanPlayer(circleBlue, toggleBlue);
        changeTextfieldName(circleBlue, nameBlue, toggleBlue);
        changeChoiceState(circleBlue, choiceBlue, toggleBlue);
    }

    /**
     * switch Color GREEN between ACTIVE and INACTIVE
     */
    public void switchStatusGreen() {
        changeCircleState(circleGreen);
        changeHumanPlayer(circleGreen, toggleGreen);
        changeTextfieldName(circleGreen, nameGreen, toggleGreen);
        changeChoiceState(circleGreen, choiceGreen, toggleGreen);
    }

    /**
     * switch Color YELLOW between ACTIVE and INACTIVE
     */
    public void switchStatusYellow() {
        changeCircleState(circleYellow);
        changeHumanPlayer(circleYellow, toggleYellow);
        changeTextfieldName(circleYellow, nameYellow, toggleYellow);
        changeChoiceState(circleYellow, choiceYellow, toggleYellow);
    }

    /**
     * switch Color RED between ACTIVE and INACTIVE
     */
    public void switchStatusRed() {
        changeCircleState(circleRed);
        changeHumanPlayer(circleRed, toggleRed);
        changeTextfieldName(circleRed, nameRed, toggleRed);
        changeChoiceState(circleRed, choiceRed, toggleRed);
    }

    /**
     * Change from ACTIVE to INACTIVE Color in GameSettings
     *
     * @param circle JavaFX Circle Object to change
     */
    private void changeCircleState(Circle circle) {
        if (circle.getRadius() == 25) {
            circle.setRadius(22.5);
            circle.setOpacity(0.5);
        } else {
            circle.setRadius(25);
            circle.setOpacity(1);
        }
    }

    /**
     * Change visibility of a ChoiceBox according to visibility of circle
     *
     * @param choiceBox JavaFx ChoiceBox to change
     */
    private void changeChoiceState(Circle circle, ChoiceBox<String> choiceBox, ToggleButton toggle) {
        choiceBox.setVisible(!(circle.getRadius() != 25) && !toggle.isSelected());
    }

    /**
     * Change visibility of a toggleButton according to visibility of circle
     *
     * @param toggleButton JavaFX toggleButton to change
     */
    private void changeHumanPlayer(Circle circle, ToggleButton toggleButton) {
        toggleButton.setVisible(!(circle.getRadius() != 25));
    }

    /**
     * Change visibility of a TextField according to visibility of circle
     *
     * @param textField JavaFX TextField to change
     */
    private void changeTextfieldName(Circle circle, TextField textField, ToggleButton toggle) {
        textField.setVisible(!(circle.getRadius() != 25) && toggle.isSelected());
    }

    public Game_Impl getGame() {
        return game;
    }

    public void setGUIController(GUIController guiController) {
        this.guiController = guiController;
    }

    public boolean dbNoConnectionConfirmation(){
        Alert resetConfirmationAlert = new Alert(Alert.AlertType.WARNING,
                "There is currently no database connection. No data will be written into the database.\n\nDo you still wish to start the game?",
                ButtonType.OK,
                ButtonType.CANCEL);
        resetConfirmationAlert.setTitle("Database Error");
        Optional<ButtonType> result = resetConfirmationAlert.showAndWait();

        if (result.get() == ButtonType.OK) {
            return true;
        }else{
            return false;
        }
    }
}
