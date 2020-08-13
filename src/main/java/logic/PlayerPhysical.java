package logic;

import model.Color;
import model.GameState;
import model.Symbol;
import view.GUIController;

import java.util.*;

/**
 * Die Klasse, die für den menschlichen Spieler und dessen Interaktion mit der GUI verantwortlich ist.
 */

public class PlayerPhysical implements Player{
    private GameState gameState;
    private Color color;
    private Rules rules;
    private Symbol activeCard;
    private String name;
    private GUIController guiController;
    private ThreadUpdate thread;
    private Object synchronizer;

    public PlayerPhysical(Color color){
        this.color = color;
    }

    @Override
    public void notifyWonCards(Color color, Symbol symbol){
    }

    public void setGuiController(GUIController guiController) {
        this.guiController = guiController;
    }

    @Override
    public void setThread(ThreadUpdate thread) {
        this.thread = thread;
    }

    @Override
    public void setSynchronizer(Object synchronizer) {
        this.synchronizer = synchronizer;
    }

    @Override
    public GameState requestActionTile() {
        guiController.setNewTile50Orientation(gameState.getTile50().getOrientation());
        guiController.paintTileAction();
        synchronized (synchronizer) {
            thread.waitForInput();
        }
        int position = guiController.getTileActionToPerform()[0];
        int orientation = guiController.getTileActionToPerform()[1];
        return gameState.changeBoard(position, orientation);
    }

    @Override
    public GameState requestActionGamePiece() {
        guiController.setStatusMoveGamePiece(true);
        guiController.showHelpingBorders();
        synchronized (synchronizer) {
            thread.waitForInput();
        }
        return gameState.changeBoardGamePiece(color, guiController.getNewGamePieceCoordinates()[0], guiController.getNewGamePieceCoordinates()[1]);
    }

    @Override
    public void updateGameState(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setRules(Rules rules) {
        this.rules = rules;
    }

    @Override
    public Rules getRules() {
        return rules;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setActiveCard(Symbol symbol) {
        activeCard = symbol;
    }

    @Override
    public Symbol getActiveCard() {
        return activeCard;
    }

}
