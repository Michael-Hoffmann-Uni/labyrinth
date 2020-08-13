package view;

import database.DatabaseAccess;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.Game_Impl;
import logic.Player;
import logic.Rules_Impl;
import model.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Controller for main-windows (GUI)
 * repaints board at distributeGameState in Game_Impl
 */
public class GUIController {
    DatabaseAccess dbAccess = null;
    Stage gameSettingsStage;
    GameSettingsController gameSettingsController;
    private Game_Impl game;
    @FXML
    GridPane tilesPane;
    @FXML
    GridPane leftInfoPane;
    @FXML
    GridPane rightInfoPane;
    @FXML
    BorderPane borderPaneMain;
    @FXML
    GridPane centering;

    // image hashmaps
    private final HashMap<Symbol, Image> symbolImageHashMap = new HashMap<>();
    private final HashMap<TileType, Image> tileTypeImageHashMap = new HashMap<>();
    private Image symbolUnknownImage;

    // sizes for image width & height
    private double tilesPaneColumnWidth = 70;
    private double tilesPaneRowHeight = 70;
    private double symbolWidth = 35;
    private double symbolHeight = 35;
    private double gamePieceRadius = 10;
    private Insets insets = new Insets(5);
    private Object synchronizer;
    private boolean statusMoveTile = false;
    private boolean statusMoveGamePiece = false;
    private int newTile50Orientation;
    private int newTile50Position;
    private final int[] newGamePieceCoordinates = new int[]{-1, -1};
    private HashMap<Color, Player> players;
    private Player humanPlayer = null;
    private boolean showHelp = false;
    private boolean showWayOfGamePiece;
    private boolean saveLastPos;
    private HashMap<Color, int[]> lastPos = new HashMap<>();
    private double maxSymbolHeight;
    private double maxSymbolWidth;
    private boolean resizeTileAction = false;
    private long delay;
    private boolean initialDBHandlingFinished = false;

    public void setShowWayOfGamePiece(boolean showWayOfGamePiece) {
        this.showWayOfGamePiece = showWayOfGamePiece;
    }

    public void setSaveLastPos(boolean saveLastPos) {
        this.saveLastPos = saveLastPos;
    }

    public void setSynchronizer(Object synchronizer) {
        this.synchronizer = synchronizer;
    }

    private final EventHandler<MouseEvent> mouseEventEventHandler = new EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            int clickedColumn = (int) (mouseEvent.getX() / tilesPaneColumnWidth);
            int clickedRow = (int) (mouseEvent.getY() / tilesPaneRowHeight);
            if (game != null && game.isRunning()) {
                synchronized (synchronizer) {
                    if (statusMoveTile) {
                        // check for valid tile50Move and set newTile50Position and newTile50Orientation
                        newTile50Position = convertFromCoordsToPosition(new int[]{clickedColumn, clickedRow});
                        if (newTile50Position < 12 && newTile50Position >= 0 && newTile50Position != game.getGameState().getTile50Position()) {
                            statusMoveTile = false;
                            synchronizer.notifyAll();
                        }
                    }
                    if (statusMoveGamePiece) {
                        newGamePieceCoordinates[0] = clickedColumn - 1;
                        newGamePieceCoordinates[1] = clickedRow - 1;
                        HashSet<int[]> tilesInRange = game.getRules().coordinatesOfTilesInRange(
                                game.getGameState().getGamePiece(game.getGameState().getActiveColor()).getX(),
                                game.getGameState().getGamePiece(game.getGameState().getActiveColor()).getY(),
                                game.getGameState().getBoard()
                        );
                        for (int[] a : tilesInRange) {
                            if (Arrays.equals(a, newGamePieceCoordinates)) {
                                statusMoveGamePiece = false;
                                synchronizer.notifyAll();
                                break;
                            }
                        }
                    }
                }
            }
        }
    };
    private final ChangeListener<Number> windowResizer = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number oldVal, Number newVal) {
            Platform.runLater(() -> recalcValues());
            if (game == null || !game.isRunning()) {
                Platform.runLater(() -> repaintEmptyBoard());
            } else {
                Platform.runLater(() -> {
                    repaint(true);
                    resizePlayerSymbols();
                    if (statusMoveTile) {
                        resizeTileAction();
                    }

                });
            }
        }
    };

    void recalcValues() {
        double tmp;
        if (centering.getWidth() < centering.getHeight()) {
            tmp = (centering.getWidth() - 30) / 9;
        } else {
            tmp = (centering.getHeight() - 30) / 9;
        }
        if (tmp < 0) {
            tmp = 70;
        }
        tilesPaneColumnWidth = tmp;
        tilesPaneRowHeight = tmp;
        symbolHeight = 0.45 * tmp;
        symbolWidth = 0.45 * tmp;
        maxSymbolHeight = Math.min(symbolHeight, 290. / 7);
        maxSymbolWidth = Math.min(symbolWidth, 290. / 7);
        gamePieceRadius = tmp / 7;
        insets = new Insets(tmp / 14);
        tilesPane.setPrefWidth(9 * tmp);
        tilesPane.setPrefHeight(9 * tmp);
    }

    public int[] getTileActionToPerform() {
        return new int[]{newTile50Position, newTile50Orientation};
    }

    public void setStatusMoveGamePiece(boolean statusMoveGamePiece) {
        this.statusMoveGamePiece = statusMoveGamePiece;
    }

    public void setNewTile50Orientation(int newTile50Orientation) {
        this.newTile50Orientation = newTile50Orientation;
    }

    public int[] getNewGamePieceCoordinates() {
        return newGamePieceCoordinates;
    }

    public void initialize() {
        // set default delay
        delay = 2500;
        // set EventHandler
        tilesPane.setOnMouseClicked(mouseEventEventHandler);
        borderPaneMain.widthProperty().addListener(windowResizer);
        borderPaneMain.heightProperty().addListener(windowResizer);

        // fill symbolImageHashmap with images
        for (Symbol s : Symbol.values()) {
            if (!(s.getImagePath().equals(""))) {
                symbolImageHashMap.put(s, new Image(String.valueOf(this.getClass().getClassLoader().getResource(s.getImagePath().substring(3)))));
            }
        }
        symbolUnknownImage = new Image(String.valueOf(this.getClass().getClassLoader().getResource("../images/symbol-unknown.png".substring(3))));

        // fill tileTypeImageHashMap with Images
        for (TileType t : TileType.values()) {
            tileTypeImageHashMap.put(t, new Image(String.valueOf(getClass().getClassLoader().getResource(t.getImagePath().substring(3)))));
        }

        repaintEmptyBoard();
    }

    /**
     * Paint Tile50 in current rotation on all possible positions (0 - 11, but getTile50Position)
     */
    public void paintTileAction() {
        statusMoveTile = true;
        GameState gameState = game.getGameState();
        int tile50Position = game.getGameState().getTile50Position();
        int[] tile50Coords = convertFromPositionToCoords(tile50Position);
        Image tile50ActionImage = tileTypeImageHashMap.get(gameState.getTile50().getType());
        for (int i = 0; i < 12; i++) {
            if (i != tile50Position) {
                int[] tile50ActionCoords = convertFromPositionToCoords(i);
                ImageView tile50TileImage = new ImageView();
                tile50TileImage.setId("tile50" + i);
                tile50TileImage.setFitWidth(tilesPaneColumnWidth);
                tile50TileImage.setFitHeight(tilesPaneRowHeight);
                tile50TileImage.setRotate(newTile50Orientation * 90);
                tile50TileImage.setImage(tile50ActionImage);
                tile50TileImage.setOpacity(0.3);
                ImageView tile50SymbolImage = new ImageView();
                tile50SymbolImage.setId("tile50symbol" + i);
                if (gameState.getTile50().getSymbol() != Symbol.None) {
                    tile50SymbolImage.setImage(symbolImageHashMap.get(gameState.getTile50().getSymbol()));
                    tile50SymbolImage.setFitWidth(symbolWidth);
                    tile50SymbolImage.setFitHeight(symbolHeight);
                }
                tile50SymbolImage.setOpacity(0.3);
                if (resizeTileAction) {
                    tilesPane.add(tile50TileImage, tile50ActionCoords[0], tile50ActionCoords[1]);
                    tilesPane.add(tile50SymbolImage, tile50ActionCoords[0], tile50ActionCoords[1]);
                } else {
                    Platform.runLater(
                            () -> {
                                tilesPane.add(tile50TileImage, tile50ActionCoords[0], tile50ActionCoords[1]);
                                tilesPane.add(tile50SymbolImage, tile50ActionCoords[0], tile50ActionCoords[1]);
                            }
                    );
                }
            }
        }
        // add Buttons to rotate tile50
        Button rotateLeft = new Button("↶");
        rotateLeft.setStyle("-fx-font-size:14");
        rotateLeft.setPadding(new Insets(2));
        rotateLeft.setPrefWidth(tilesPaneColumnWidth / 3);
        rotateLeft.setPrefHeight(tilesPaneRowHeight / 3);
        rotateLeft.setOnAction(actionEvent -> {
            newTile50Orientation = (newTile50Orientation + 3) % 4;
            repaintActionTiles();
        });
        Button rotateRight = new Button("↷");
        rotateRight.setStyle("-fx-font-size:14");
        rotateRight.setPadding(new Insets(2));
        rotateRight.setPrefWidth(tilesPaneColumnWidth / 3);
        rotateRight.setPrefHeight(tilesPaneRowHeight / 3);
        rotateRight.setOnAction(actionEvent -> {
            newTile50Orientation = (newTile50Orientation + 1) % 4;
            repaintActionTiles();
        });
        if (resizeTileAction) {
            tilesPane.add(rotateLeft, tile50Coords[0], tile50Coords[1]);
            GridPane.setValignment(rotateLeft, VPos.BOTTOM);
            GridPane.setHalignment(rotateLeft, HPos.LEFT);
            tilesPane.add(rotateRight, tile50Coords[0], tile50Coords[1]);
            GridPane.setValignment(rotateRight, VPos.BOTTOM);
            GridPane.setHalignment(rotateRight, HPos.RIGHT);
        } else {
            Platform.runLater(
                    () -> {
                        tilesPane.add(rotateLeft, tile50Coords[0], tile50Coords[1]);
                        GridPane.setValignment(rotateLeft, VPos.BOTTOM);
                        GridPane.setHalignment(rotateLeft, HPos.LEFT);
                        tilesPane.add(rotateRight, tile50Coords[0], tile50Coords[1]);
                        GridPane.setValignment(rotateRight, VPos.BOTTOM);
                        GridPane.setHalignment(rotateRight, HPos.RIGHT);
                    }
            );
        }
    }

    /**
     * helper-method for windows resize: call paintActionTiles with resizeTileAction = true
     * will repaint ActionTiles immediately, not on a Platform.runLater
     * the call with Platform.runLater is necessary for calling paintActionTiles from requestActionTile from player
     */
    private void resizeTileAction() {
        resizeTileAction = true;
        paintTileAction();
        resizeTileAction = false;
    }

    /**
     * Helper-method for paintTileAction() -> perform tile50-updates after changing orientation
     */
    private void repaintActionTiles() {
        GameState gamestate = game.getGameState();
        for (int i = 0; i < 12; i++) {
            if (i != gamestate.getTile50Position()) {
                ImageView imageView = (ImageView) tilesPane.lookup("#tile50" + i);
                if (imageView != null) {
                    imageView.setRotate(newTile50Orientation * 90);
                }
            }
        }
    }

    /**
     * Converts possible tile50-Positions to coords in GridPane tilesPane
     * (board [7x7] + surrounding +1 in all directions -> tilesPane 9x9-matrix  )
     *
     * @param i position of pushing tile50 into (valid values: {0..11, 77})
     * @return return int[] with two coords [0]: x-coord (column), [1]: y-coord (row)
     */
    private int[] convertFromPositionToCoords(int i) {
        switch (i) {
            case 0:
                return new int[]{2, 0};
            case 1:
                return new int[]{4, 0};
            case 2:
                return new int[]{6, 0};
            case 3:
                return new int[]{8, 2};
            case 4:
                return new int[]{8, 4};
            case 5:
                return new int[]{8, 6};
            case 6:
                return new int[]{6, 8};
            case 7:
                return new int[]{4, 8};
            case 8:
                return new int[]{2, 8};
            case 9:
                return new int[]{0, 6};
            case 10:
                return new int[]{0, 4};
            case 11:
                return new int[]{0, 2};
            case 77:
                return new int[]{0, 0};
            default:
                return new int[]{-1, -1};
        }
    }

    /**
     * Converts tile50 coords of GrinPane tilesPane [9x9] to possible tile50-Positions
     *
     * @param coords coords[0]: x-coord (column), coords[1]: y-coord (row)
     * @return tile50Position: 0..11, clockwise, starting on the top left, -1 for invalid coords
     */
    private int convertFromCoordsToPosition(int[] coords) {
        int x = coords[0];
        int y = coords[1];

        if (x == 2 && y == 0) {
            return 0;
        }
        if (x == 4 && y == 0) {
            return 1;
        }
        if (x == 6 && y == 0) {
            return 2;
        }
        if (x == 8 && y == 2) {
            return 3;
        }
        if (x == 8 && y == 4) {
            return 4;
        }
        if (x == 8 && y == 6) {
            return 5;
        }
        if (x == 6 && y == 8) {
            return 6;
        }
        if (x == 4 && y == 8) {
            return 7;
        }
        if (x == 2 && y == 8) {
            return 8;
        }
        if (x == 0 && y == 6) {
            return 9;
        }
        if (x == 0 && y == 4) {
            return 10;
        }
        if (x == 0 && y == 2) {
            return 11;
        }
        return -1;
    }

    public void setGame(Game_Impl game) {
        this.game = game;
        if (lastPos.size() != 0) {
            lastPos = new HashMap<>();
        }
    }

    public Game_Impl getGame() {
        return game;
    }

    /**
     * get new gamestate and repaint board and some user information
     *
     * @param calledIntern  boolean, is this method called from this class or from outer class?
     */
    public void repaint(boolean calledIntern) {
        // repaint player information just for active players
        for (Color color : game.getPlayedColors()) {
            repaintPlayerInformation(color);
        }

        if (!game.isRunning()) {
            paintGamePieces(game.getPlayedColors());
            paintWinnerInformation();
            return;
        }

        // clean up tilesPane
        tilesPane.getChildren().clear();

        GameState gameState = game.getGameState();

        // set start position if no lastPos is set yet
        if (lastPos.size() == 0) {
            for (Color color : this.game.getPlayedColors()) {
                lastPos.put(color, new int[]{gameState.getPositionX(color), gameState.getPositionY(color)});
            }
        }
        // save new lastPos (after moving tiles)
        if (saveLastPos) {
            for (Color color : game.getPlayedColors()) {
                lastPos.put(color, new int[]{gameState.getPositionX(color), gameState.getPositionY(color)});
            }
            saveLastPos = false;
        }
        Color activeColor = gameState.getActiveColor();
        // highlight possible way of active gamepiece
        if (showWayOfGamePiece) {
            if (lastPos.containsKey(activeColor) && (humanPlayer == null || humanPlayer.getColor() != activeColor)) {
                int[] activePos = new int[]{gameState.getPositionX(activeColor), gameState.getPositionY(activeColor)};
                for (int[] pos : game.getRules().coordinatesOfTilesInRange(lastPos.get(activeColor)[0], lastPos.get(activeColor)[1], gameState.getBoard())) {
                    if (pos != lastPos.get(activeColor) && pos != activePos) {
                        paintRectangle(pos[0], pos[1], activeColor, "way", calledIntern);
                    }
                }
                paintRectangle(lastPos.get(activeColor)[0], lastPos.get(activeColor)[1], activeColor, "start", calledIntern);
                paintRectangle(gameState.getPositionX(activeColor), gameState.getPositionY(activeColor), activeColor, "stop", calledIntern);
            }
        }

        paintBoard(gameState.getBoard());

        // set gamePieces home as small rectangle
        paintHomeRectangles();

        // set gamePieces as small circles in correct color
        paintGamePieces(game.getPlayedColors());

        // set tile50 tileTypeImage
        ImageView tile50TileImage = new ImageView();
        tile50TileImage.setFitWidth(tilesPaneColumnWidth);
        tile50TileImage.setFitHeight(tilesPaneRowHeight);
        tile50TileImage.setRotate(gameState.getTile50().getOrientation() * 90);
        tile50TileImage.setImage(tileTypeImageHashMap.get(gameState.getTile50().getType()));
        int[] tile50Position = convertFromPositionToCoords(gameState.getTile50Position());
        tilesPane.add(tile50TileImage, tile50Position[0], tile50Position[1]);
        // add tile50 Symbol Image if present
        ImageView tile50SymbolImage = new ImageView();
        if (gameState.getTile50().getSymbol() != Symbol.None) {
            tile50SymbolImage.setImage(symbolImageHashMap.get(gameState.getTile50().getSymbol()));
            tile50SymbolImage.setFitWidth(symbolWidth);
            tile50SymbolImage.setFitHeight(symbolHeight);
        }
        tilesPane.add(tile50SymbolImage, tile50Position[0], tile50Position[1]);
    }

    /**
     * helper-method for repaint() and repaintEmptyBoard()
     * adds ImageViews for Tiles and Symbols to GridPane tilesPane
     *
     * @param board the board to be painted
     */
    private void paintBoard(Tile[][] board) {
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                ImageView tileImage = new ImageView();
                tileImage.setFitWidth(tilesPaneColumnWidth);
                tileImage.setFitHeight(tilesPaneRowHeight);
                tileImage.setPreserveRatio(true);
                switch (board[x][y].getOrientation()) {
                    case 1:
                        tileImage.setRotate(90);
                        break;
                    case 2:
                        tileImage.setRotate(180);
                        break;
                    case 3:
                        tileImage.setRotate(270);
                        break;
                    default:
                        tileImage.setRotate(0);
                }
                tileImage.setImage(tileTypeImageHashMap.get(board[x][y].getType()));
                board[x][y].getSymbol();
                tilesPane.add(tileImage, x + 1, y + 1);

                if (board[x][y].getSymbol() != null) {
                    ImageView symbolImage = new ImageView();
                    symbolImage.setImage(symbolImageHashMap.get(board[x][y].getSymbol()));
                    symbolImage.setFitHeight(symbolHeight);
                    symbolImage.setFitWidth(symbolWidth);

                    tilesPane.add(symbolImage, x + 1, y + 1);
                }
            }
        }
    }

    /**
     * helper-method for repaint()
     * adds Rectangle (as background) and Text (as information)
     * to show who has won the game
     */
    private void paintWinnerInformation() {
        Rectangle winnerBackground = new Rectangle();
        winnerBackground.setWidth(tilesPaneColumnWidth * 5.5);
        winnerBackground.setHeight(tilesPaneRowHeight * 2.5);
        winnerBackground.setArcWidth(symbolWidth);
        winnerBackground.setArcHeight(symbolHeight);
        winnerBackground.setFill(javafx.scene.paint.Color.BLACK);
        winnerBackground.setOpacity(0.7);
        tilesPane.add(winnerBackground, 0, 0, 9, 9);

        Text winnerInformation = new Text();
        winnerInformation.setText(game.getRules().getWinner(game.getGameState()) + " has won!\n Congratulations to\n"
                + players.get(game.getRules().getWinner(game.getGameState())).getName());
        winnerInformation.setFont(Font.font(null, FontWeight.BLACK, symbolHeight));
        winnerInformation.setTextAlignment(TextAlignment.CENTER);
        winnerInformation.setFill(game.getRules().getWinner(game.getGameState()).getColor());
        GridPane.setValignment(winnerInformation, VPos.CENTER);
        GridPane.setHalignment(winnerInformation, HPos.CENTER);
        tilesPane.add(winnerInformation, 0, 0, 9, 9);
    }

    /**
     * helper-method for repaint() and repaintEmptyBoard()
     * adds Rectangles as StartingPoint for colors
     */
    private void paintHomeRectangles() {
        double tmp = 2 * gamePieceRadius;

        Rectangle blueHome = new Rectangle(tmp, tmp);
        blueHome.setFill(Color.Blue.getColor());
        tilesPane.add(blueHome, 7, 7);
        GridPane.setMargin(blueHome, insets);
        GridPane.setHalignment(blueHome, HPos.RIGHT);
        GridPane.setValignment(blueHome, VPos.BOTTOM);

        Rectangle greenHome = new Rectangle(tmp, tmp);
        greenHome.setFill(Color.Green.getColor());
        tilesPane.add(greenHome, 1, 7);
        GridPane.setMargin(greenHome, insets);
        GridPane.setHalignment(greenHome, HPos.LEFT);
        GridPane.setValignment(greenHome, VPos.BOTTOM);

        Rectangle yellowHome = new Rectangle(tmp, tmp);
        yellowHome.setFill(Color.Yellow.getColor());
        tilesPane.add(yellowHome, 7, 1);
        GridPane.setMargin(yellowHome, insets);
        GridPane.setHalignment(yellowHome, HPos.RIGHT);
        GridPane.setValignment(yellowHome, VPos.TOP);

        Rectangle redHome = new Rectangle(tmp, tmp);
        redHome.setFill(Color.Red.getColor());
        tilesPane.add(redHome, 1, 1);
        GridPane.setMargin(redHome, insets);
        GridPane.setHalignment(redHome, HPos.LEFT);
        GridPane.setValignment(redHome, VPos.TOP);
    }

    /**
     * helper-method for repaint()
     * adds Circles for players in game
     *
     * @param players currently available players
     */
    private void paintGamePieces(ArrayList<Color> players) {
        for (Color player : players) {
            Circle circleGamePiece = new Circle(gamePieceRadius);
            circleGamePiece.setFill(player.getColor());
            circleGamePiece.setStroke(javafx.scene.paint.Color.BLACK);
            tilesPane.add(circleGamePiece, game.getGameState().getGamePiece(player).getX() + 1, game.getGameState().getGamePiece(player).getY() + 1);
            switch (player) {
                case Blue:
                    GridPane.setValignment(circleGamePiece, VPos.BOTTOM);
                    GridPane.setHalignment(circleGamePiece, HPos.RIGHT);
                    break;
                case Green:
                    GridPane.setValignment(circleGamePiece, VPos.BOTTOM);
                    GridPane.setHalignment(circleGamePiece, HPos.LEFT);
                    break;
                case Yellow:
                    GridPane.setValignment(circleGamePiece, VPos.TOP);
                    GridPane.setHalignment(circleGamePiece, HPos.RIGHT);
                    break;
                case Red:
                    GridPane.setValignment(circleGamePiece, VPos.TOP);
                    GridPane.setHalignment(circleGamePiece, HPos.LEFT);
                    break;
                default:
            }
            GridPane.setMargin(circleGamePiece, insets);
        }
    }

    /**
     * repaint empty board in case of window resizing
     */
    void repaintEmptyBoard() {
        GameState gameState = new Rules_Impl(new ArrayList<>(Arrays.asList(Color.Blue, Color.Green))).getTestGameStateFixed();
        tilesPane.getChildren().clear();
        paintBoard(gameState.getBoard());
        paintHomeRectangles();
    }

    /**
     * Helper-method for repaint() -> add player information in GUI, according to number of players in game
     *
     * @param color Color of player to repaint information
     */
    private void repaintPlayerInformation(Color color) {
        int cardsCount = (24 / players.size());
        int i = 1;
        for (Symbol symbol : game.getWonCards(color)) {
            if (i <= cardsCount) {
                ImageView imageView = (ImageView) borderPaneMain.getScene().lookup("#playerCard" + color.toString() + i);
                imageView.setImage(symbolImageHashMap.get(symbol));
                crossOutNode(imageView);
                i++;
            } else {
                Node node = borderPaneMain.getScene().lookup("#endPoint" + color.toString());
                crossOutNode(node);
            }
        }
        if (humanPlayer != null) {
            int index = game.getWonCards(humanPlayer.getColor()).size() + 1;
            ImageView activeCard = (ImageView) borderPaneMain.getScene().lookup("#activeCard");
            if (index <= cardsCount) {
                activeCard.setImage(symbolImageHashMap.get(humanPlayer.getActiveCard()));
                ImageView activeCardIndex = (ImageView) borderPaneMain.getScene().lookup("#playerCard" + humanPlayer.getColor().toString() + index);
                activeCardIndex.setImage(symbolImageHashMap.get(humanPlayer.getActiveCard()));
            } else {
                Rectangle activeCardEndPoint = new Rectangle(60, 60, humanPlayer.getColor().getColor());
                GridPane.setHalignment(activeCardEndPoint, HPos.CENTER);
                GridPane.setValignment(activeCardEndPoint, VPos.CENTER);
                activeCard.setImage(null);
                GridPane gridPane = (GridPane) borderPaneMain.getScene().lookup("#playerInfo" + humanPlayer.getColor().toString());
                gridPane.add(activeCardEndPoint, GridPane.getColumnIndex(activeCard), GridPane.getRowIndex(activeCard), 7, 1);
            }
        } else {
            for (Player player : players.values()) {
                int index = game.getWonCards(player.getColor()).size() + 1;
                if (index <= cardsCount) {
                    ImageView imageViewIndex = (ImageView) borderPaneMain.getScene().lookup("#playerCard" + player.getColor().toString() + index);
                    imageViewIndex.setImage(symbolImageHashMap.get(player.getActiveCard()));
                }
            }
        }
    }

    /**
     * helper-method for repaintPlayerInformation to cross out found symbols
     *
     * @param node the node to be crossed out
     */
    private void crossOutNode(Node node) {
        int rowIndexEndPoint = GridPane.getRowIndex(node);
        int columnIndexEndPoint = GridPane.getColumnIndex(node);
        GridPane gridPane = (GridPane) node.getParent();
        Line line1 = new Line(0, 0, maxSymbolHeight - 5, maxSymbolWidth - 5);
        line1.setStroke(javafx.scene.paint.Color.BLACK);
        line1.setStrokeWidth(2.5);
        GridPane.setValignment(line1, VPos.CENTER);
        GridPane.setHalignment(line1, HPos.CENTER);
        gridPane.add(line1, columnIndexEndPoint, rowIndexEndPoint);
        Line line2 = new Line(0, maxSymbolHeight - 5, maxSymbolWidth - 5, 0);
        line2.setStroke(javafx.scene.paint.Color.BLACK);
        line2.setStrokeWidth(2.5);
        GridPane.setValignment(line2, VPos.CENTER);
        GridPane.setHalignment(line2, HPos.CENTER);
        gridPane.add(line2, columnIndexEndPoint, rowIndexEndPoint);
    }

    /**
     * menu-call 'Help' &gt; 'About'
     * Show some information about authors and why this Application exists
     */
    public void callMenuShowAbout() {
        Dialog<Boolean> about = new Dialog<>();
        about.setTitle("About");
        // load about-pane.fxml for showing data
        try {
            DialogPane dialogPane = FXMLLoader.load(getClass().getResource("about-pane.fxml"));
            about.setDialogPane(dialogPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Add closing Button to dialog
        about.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        // Show dialog and wait for user action
        about.showAndWait();
    }

    /**
     * menu-call 'Help' &gt; 'How to play?'
     * Gives users some information on how to play the game!
     */
    public void callMenuShowHowTo() {
        ChoiceDialog<Boolean> howToPlay = new ChoiceDialog<>();
        howToPlay.setTitle("How to play?");
        DialogPane dialogPane = new DialogPane();
        dialogPane.setHeaderText("You don't know how to play this game?");
        GridPane pane = new GridPane();
        pane.setMinSize(50, 50);
        pane.setMaxSize(50, 50);
        Circle circleOuter = new Circle(20);
        circleOuter.setStroke(javafx.scene.paint.Color.WHITE);
        circleOuter.setStrokeWidth(5);
        circleOuter.setFill(javafx.scene.paint.Color.DODGERBLUE);
        DropShadow ds = new DropShadow();
        ds.setOffsetX(5);
        ds.setOffsetY(5);
        ds.setColor(javafx.scene.paint.Color.LIGHTGRAY);
        circleOuter.setEffect(ds);
        GridPane.setValignment(circleOuter, VPos.CENTER);
        GridPane.setHalignment(circleOuter, HPos.CENTER);
        Text text = new Text("?");
        text.setFill(javafx.scene.paint.Color.WHITE);
        text.setFont(Font.font(null, FontWeight.BLACK, 30));
        GridPane.setHalignment(text, HPos.CENTER);
        GridPane.setValignment(text, VPos.BASELINE);
        pane.add(circleOuter, 1, 1);
        pane.add(text, 1, 1);
        Light.Distant light = new Light.Distant();
        light.setAzimuth(-135);
        Lighting l = new Lighting();
        l.setLight(light);
        l.setSurfaceScale(5);
        pane.setEffect(l);
        dialogPane.setGraphic(pane);
        dialogPane.setContentText("You can just download manual as PDF from manufacturers website!");
        dialogPane.setMaxSize(600, 200);
        dialogPane.setMinSize(600, 200);
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        howToPlay.setDialogPane(dialogPane);
        Button downloadButton = (Button) dialogPane.lookupButton(ButtonType.APPLY);
        downloadButton.setDefaultButton(true);
        downloadButton.setText("Download!");
        howToPlay.setResultConverter((ButtonType type) -> {
            ButtonBar.ButtonData data = type == null ? null : type.getButtonData();
            if (data == ButtonBar.ButtonData.APPLY) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save manual (Download as PDF)");
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
                fileChooser.setInitialFileName("Labyrinth_26446-anl-201637944.pdf");
                String userHome = System.getProperty("user.home");
                fileChooser.setInitialDirectory(new File(userHome + "/Downloads"));
                File file = fileChooser.showSaveDialog(null);
                if (file != null) {
                    try {
                        FileUtils.copyURLToFile(new URL("https://www.ravensburger.de/spielanleitungen/ecm/Spielanleitungen/26446%20anl%201637944.pdf"), file);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Download successful!");
                        alert.setHeaderText("Manual is now available on local system as PDF!");
                        alert.setContentText("Go to your specified directory and read your PDF!");
                        alert.getDialogPane().setMaxSize(600, 200);
                        alert.getDialogPane().setMinSize(600, 200);
                        alert.showAndWait();
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Oh no!");
                        alert.setHeaderText("Something went wrong!");
                        alert.setContentText("Try again later please!");
                        alert.getDialogPane().setMaxSize(600, 200);
                        alert.getDialogPane().setMinSize(600, 200);
                        alert.showAndWait();
                    }
                }
                return true;
            } else {
                return false;
            }
        });
        howToPlay.showAndWait();
    }

    /**
     * menu-call 'Statistics' &gt; 'Statistics'
     * Show all available statistic information in a dialog
     *
     * @throws IOException throws, if fxml file is not readable
     */
    public void callMenuShowStats() throws IOException {
        Stage statisticsStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("statistics.fxml"));
        // create controller manually to pass dbAccess to controller constructor
        StatisticsController controller = new StatisticsController(dbAccess);
        // set controller manually
        loader.setController(controller);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        statisticsStage.setScene(scene);
        // preposition the wait window to center because later GUI might be blocked
        controller.prePositionWaitAlert(statisticsStage.getScene().getWindow());
        // lock main window and force settings first
        statisticsStage.initModality(Modality.APPLICATION_MODAL);
        statisticsStage.setTitle("Statistics");
        statisticsStage.showAndWait();
    }

    /**
     * menu-call 'Game' &gt; 'Close'
     * Closes Application by menu-call.
     * possible hook to save some important information before closing!
     */
    public void callMenuExitAction() {
        System.exit(0);
        Platform.exit();
    }

    /**
     * menu-call 'Game' &gt; 'Game Settings'
     * Configure your game! Set Players, Colors, Names and switch KIs
     */
    public void callMenuGameSettings() {
        gameSettingsStage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gameSettings.fxml"));
            Parent gameSettings = loader.load();
            gameSettingsController = loader.getController();
            gameSettingsController.setDatabaseAccess(dbAccess);
            gameSettingsController.setGUIController(this);
            gameSettingsStage.setScene(new Scene(gameSettings, 600, 400));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // lock main window and force settings first
        gameSettingsStage.initModality(Modality.APPLICATION_MODAL);
        gameSettingsStage.setTitle("Edit Game Settings");
        gameSettingsStage.showAndWait();

        if (game != null) {
            game.setGUIController(this);
        }
    }

    /**
     * menu-call 'Game' &gt; 'Database Settings'
     * <p>
     * Scene to configure database connection. Accepted DB must work like mySQL-DB
     *
     * @throws IOException throws, if fxml file is not readable
     */
    // gets called by user and not the system - hence isAutoCall = false
    public void callMenuDatabaseSettings() throws IOException {
        if (initialDBHandlingFinished)
            autoCallMenuDatabaseSettings(false);
    }

    /**
     * Gets called directly by system - hence isAutoCall = true (if not called by callMenuDatabaseSettings)
     *
     * @param isAutoCall This specifies if the method got called by system or user
     *                   (isAutoCall == true when called by system)
     * @throws IOException throws, if fxml file is not readable
     */
    public void autoCallMenuDatabaseSettings(boolean isAutoCall) throws IOException {
        Stage dbSettingsStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dbSettings.fxml"));
        // create controller manually to pass dbAccess to controller constructor
        DBSettingsController controller = new DBSettingsController(dbAccess, isAutoCall);
        // set controller manually
        loader.setController(controller);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        dbSettingsStage.setScene(scene);
        // lock main window and force settings first
        dbSettingsStage.initModality(Modality.APPLICATION_MODAL);
        dbSettingsStage.setTitle("Database Settings");

        // make sure any other closing operation than pressing OK gets handled as cancel to make sure the rollback happens
        dbSettingsStage.setOnCloseRequest(event -> controller.clickCancel());
        dbSettingsStage.showAndWait();
    }

    /**
     * Sets the Database Access at program launch that will be used for the entire time
     *
     * @param dbAccess The DatabaseAccess object for this instance of GUIController
     */
    public void setDatabaseAccess(DatabaseAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    /**
     * Set field players and call firstPaintPlayerInformation.
     *
     * @param players players of game
     */
    public void setPlayers(HashMap<Color, Player> players) {
        this.players = players;
        firstPaintPlayerInformation();
    }

    /**
     * generate placeholders for ingame-information of players (color, name, won cards, ...)
     */
    private void firstPaintPlayerInformation() {
        // remove previous information
        leftInfoPane.getChildren().clear();
        leftInfoPane.getColumnConstraints().clear();
        leftInfoPane.getRowConstraints().clear();
        leftInfoPane.setAlignment(Pos.CENTER);
        rightInfoPane.getChildren().clear();
        rightInfoPane.getColumnConstraints().clear();
        rightInfoPane.getRowConstraints().clear();
        rightInfoPane.setAlignment(Pos.CENTER);


        // check for human player in game
        humanPlayer = null;
        for (Player player : players.values()) {
            // ToDo: configure at this place, if PlayerPhysical changes name!
            // maybe set this string as a CONSTANT STRING at beginning of this file?
            if (player.getClass().getName().contains("PlayerPhysical")) {
                humanPlayer = player;
            }
        }

        // define player information layout for game without human player
        // place one information on the left, one on the right (for 2 players)
        // place a third on the left, a fourth on the right
        if (humanPlayer == null) {
            boolean left = true;
            int leftRow = 1;
            int rightRow = 1;
            for (Player player : players.values()) {
                if (left) {
                    leftInfoPane.add(playerInformationPane(player.getColor()), 1, ++leftRow);
                } else {
                    rightInfoPane.add(playerInformationPane(player.getColor()), 1, ++rightRow);
                }
                left = !left;
            }
        }
        // define player information for game with human player
        // place AI-players information on the left and human player information on the right
        else {
            int leftRow = 1;
            for (Player player : players.values()) {
                if (player != humanPlayer) {
                    leftInfoPane.add(playerInformationPane(player.getColor()), 1, ++leftRow);
                } else {
                    rightInfoPane.add(humanPlayerInformationPane(player.getColor()), 1, 1);
                }
            }
        }
    }

    /**
     * Generate GridPane with ID for given color (just for human player!)
     *
     * @param color the playes color to get the GridPane for
     * @return returns a GridPane o be added to rightInfoPane
     */
    private GridPane humanPlayerInformationPane(Color color) {
        GridPane gridPane = new GridPane();
        gridPane.setId("playerInfo" + color.toString());
        gridPane.setPadding(insets);

        gridPane.add(getPlayerDisplayName(color), 1, 0, 7, 1);

        Rectangle cardOuter = new Rectangle(135, 210, javafx.scene.paint.Color.web("#1065b5"));
        cardOuter.setArcWidth(15);
        cardOuter.setArcHeight(15);
        cardOuter.setStroke(javafx.scene.paint.Color.WHITE);
        cardOuter.setStrokeType(StrokeType.OUTSIDE);
        GridPane.setHalignment(cardOuter, HPos.CENTER);
        GridPane.setValignment(cardOuter, VPos.CENTER);
        GridPane.setMargin(cardOuter, new Insets(15));
        gridPane.add(cardOuter, 1, 2, 7, 1);

        Rectangle cardInner = new Rectangle(125, 200, color.getColor());
        cardInner.setArcWidth(10);
        cardInner.setArcHeight(10);
        cardInner.setStrokeType(StrokeType.INSIDE);
        GridPane.setValignment(cardInner, VPos.CENTER);
        GridPane.setHalignment(cardInner, HPos.CENTER);
        gridPane.add(cardInner, 1, 2, 7, 1);

        Circle cardInnerCircle = new Circle(62, javafx.scene.paint.Color.WHITE);
        cardInnerCircle.setStroke(javafx.scene.paint.Color.web("#1065b5"));
        cardInnerCircle.setStrokeWidth(5);
        cardInnerCircle.setStrokeType(StrokeType.OUTSIDE);
        GridPane.setHalignment(cardInnerCircle, HPos.CENTER);
        GridPane.setValignment(cardInnerCircle, VPos.CENTER);
        gridPane.add(cardInnerCircle, 1, 2, 7, 1);

        ImageView activeCardSymbol = new ImageView();
        activeCardSymbol.setId("activeCard");
        activeCardSymbol.setFitHeight(80);
        activeCardSymbol.setFitWidth(80);
        GridPane.setValignment(activeCardSymbol, VPos.CENTER);
        GridPane.setHalignment(activeCardSymbol, HPos.CENTER);
        activeCardSymbol.setImage(symbolUnknownImage);
        gridPane.add(activeCardSymbol, 1, 2, 7, 1);

        paintInitialPlayerSymbols(gridPane, color);
        return gridPane;
    }

    /**
     * Generate GridPane with ID for given color (just for AI players)
     *
     * @param color the played color to get a GridPane for
     * @return returns a GridPane to be added to left/rightInfoPane
     */
    private GridPane playerInformationPane(Color color) {
        GridPane gridPane = new GridPane();
        gridPane.setId("playerInfo" + color.toString());
        gridPane.setPadding(insets);
        gridPane.add(getPlayerDisplayName(color), 1, 1, 7, 1);
        gridPane.add(getCircle(color), 1, 2, 7, 1);
        paintInitialPlayerSymbols(gridPane, color);
        return gridPane;
    }

    /**
     * helper-method for paintInitialPlayerSymbols(GridPane, Color) to generate new ImageViews with correct ID
     * for Color and Index i
     *
     * @param color Color to paint the symbols for
     * @param i     Index of needed ImageView
     * @return ImageView to add to list of symbols
     */
    private ImageView getSymbolUnknownImageView(Color color, int i) {
        ImageView imageViewCard = new ImageView();
        imageViewCard.setId("playerCard" + color.toString() + i);
        imageViewCard.setFitWidth(symbolWidth);
        imageViewCard.setFitHeight(symbolHeight);
        imageViewCard.setImage(symbolUnknownImage);
        return imageViewCard;
    }

    /**
     * helper-method for (human)playerInformationPane(Color) to get a JavaFX.Circle for chosen color
     *
     * @param color Color to get a circle for
     * @return Circle with the correct PAINT.color for color
     */
    private Circle getCircle(Color color) {
        Circle circle = new Circle();
        circle.setFill(color.getColor());
        circle.setRadius(symbolWidth);
        circle.setStroke(javafx.scene.paint.Color.BLACK);
        GridPane.setHalignment(circle, HPos.CENTER);
        GridPane.setValignment(circle, VPos.CENTER);
        GridPane.setMargin(circle, new Insets(symbolWidth));
        return circle;
    }

    /**
     * helper-method for (human)playerInformationPane(Color) to get a JavaFX.Text with the name of player
     *
     * @param color Color of the player to get a name for
     * @return Text with name of player set
     */
    private Text getPlayerDisplayName(Color color) {
        Text playerDisplayName = new Text();
        playerDisplayName.setText(color.toString() + ": " + players.get(color).getName());
        playerDisplayName.setFont(Font.font(null, FontWeight.BOLD, 18));
        GridPane.setHalignment(playerDisplayName, HPos.LEFT);
        GridPane.setMargin(playerDisplayName, insets);
        return playerDisplayName;
    }

    /**
     * helper-method for paintInitialPlayerSymbols to generate the last symbol (home) of a player
     *
     * @param color Color of the player to get and Endpoint for
     * @return Rectangle as symbol for the last symbol to find
     */
    private Rectangle getEndPointRectangle(Color color) {
        Rectangle endPoint = new Rectangle(symbolWidth, symbolHeight);
        endPoint.setFill(color.getColor());
        endPoint.setId("endPoint" + color.toString());
        return endPoint;
    }

    /**
     * helper-method for (human)playerInformationPane(Color) to add little symbols
     *
     * @param gridPane GridPane for PlayerInformation of color
     * @param color    Color of player to paint symbols for
     */
    private void paintInitialPlayerSymbols(GridPane gridPane, Color color) {
        int cardsCount = 24 / game.getPlayers().size();
        int lastUsedColumn = 0;
        for (int i = 1; i <= cardsCount; i++) {
            ImageView imageViewCard = getSymbolUnknownImageView(color, i);
            if (i <= 7) {
                gridPane.add(imageViewCard, i, 3);
                lastUsedColumn = i;
            } else {
                gridPane.add(imageViewCard, i - 7, 4);
                lastUsedColumn = i - 7;
            }
        }
        gridPane.add(getEndPointRectangle(color), lastUsedColumn + 1, cardsCount < 7 ? 3 : 4);
    }

    /**
     * helper-method for window-resize-event: adjust symbols to look for / found earlier
     */
    private void resizePlayerSymbols() {
        int cardsCount = 24 / game.getPlayers().size();
        for (Color color : game.getPlayedColors()) {
            for (int i = 1; i <= cardsCount; i++) {
                ImageView imageView = (ImageView) borderPaneMain.getScene().lookup("#playerCard" + color.toString() + i);
                imageView.setFitWidth(maxSymbolWidth);
                imageView.setFitHeight(maxSymbolHeight);
            }
        }
    }

    /**
     * menu-call 'Help' &gt; 'Show visual help'
     * <p>
     * Changes boolean showHelp.
     * If game is running and showHelp is set true: calls showHelpingBorders() instantly!
     * If game is running and showHelp is set false: calls repaint() to remove highlighting!
     */
    public void callMenuVisualHelp() {
        showHelp = !showHelp;
        if (game != null) {
            if (showHelp && statusMoveGamePiece) {
                showHelpingBorders();
            } else {
                if (!statusMoveTile) {
                    repaint(true);
                }
            }
        }
    }

    /**
     * Generates highlighting for currently reachable tiles in following conditions:
     * <ol>
     * <li>Showing Help is active (set by menu)
     * <li>There is a humanPlayer in game
     * <li>humanPlayer is ActiveColor in current gameState
     * </ol><p>
     * Method called by requestActionGamePiece() of PlayerPhysical
     */
    public void showHelpingBorders() {
        // show helping borders
        GameState gameState = game.getGameState();
        if (showHelp && humanPlayer != null && gameState.getActiveColor() == humanPlayer.getColor()) {
            HashSet<int[]> allowedCoords = game.getRules().coordinatesOfTilesInRange(gameState.getPositionX(humanPlayer.getColor()), gameState.getPositionY(humanPlayer.getColor()), gameState.getBoard());
            for (int[] i : allowedCoords) {
                paintRectangle(i[0], i[1], humanPlayer.getColor(), "way", false);
            }
        }
    }

    /**
     * helper-method for repaint() - used to highlight possible ways of last moved gamepiece
     *
     * @param x            x-coordinate of tile to highlight
     * @param y            y-coordinate of tile to highlight
     * @param color        the paint to use for highlight
     * @param type         'way', 'start', 'stop' - set opacity in a different way for highlight start/stop
     * @param calledIntern is it called intern from GUIController or by other thread?
     */
    private void paintRectangle(int x, int y, Color color, String type, boolean calledIntern) {
        Rectangle highlightRectangle = new Rectangle(tilesPaneColumnWidth, tilesPaneRowHeight);
        highlightRectangle.setArcHeight(symbolHeight);
        highlightRectangle.setArcWidth(symbolWidth);
        highlightRectangle.setFill(color.getColor());
        highlightRectangle.setStroke(color.getColor());
        switch (type) {
            case "way":
                highlightRectangle.setOpacity(0.2);
                highlightRectangle.setStrokeWidth(2);
                break;
            case "start":
            case "stop":
                highlightRectangle.setOpacity(0.3);
                highlightRectangle.setStrokeWidth(4);
                break;
        }
        GridPane.setHalignment(highlightRectangle, HPos.CENTER);
        GridPane.setValignment(highlightRectangle, VPos.CENTER);
        if (calledIntern) {
            tilesPane.add(highlightRectangle, x + 1, y + 1);
        } else {
            Platform.runLater(
                    () -> tilesPane.add(highlightRectangle, x + 1, y + 1)
            );
        }
    }

    public long getDelay() {
        return delay;
    }

    /**
     * method to change private member 'delay' to value of RadioMenuItem.userData
     *
     * @param actionEvent switched RadioMenuItem
     */
    public void changeDelay(ActionEvent actionEvent) {
        String userData = (String) ((RadioMenuItem) actionEvent.getSource()).getUserData();
        delay = Integer.parseInt(userData);
    }

    public void setInitialDBHandlingFinished(boolean initialDBHandlingFinished) {
        this.initialDBHandlingFinished = initialDBHandlingFinished;
    }
}
