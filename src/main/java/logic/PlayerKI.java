package logic;

import model.Color;
import model.GameState;
import model.Symbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Eine mittelstarke KI. Die genauere Umsetzung ist in den Methoden requestActionTile() und requestActionGamePiece beschrieben.
 */

public class PlayerKI implements Player {
    private Color color;
    private GameState gameState;
    private Rules rules;
    private Symbol activeCard;
    private String name;


    public PlayerKI(Color color){
        this.color = color;
    }

    @Override
    public void setThread(ThreadUpdate thread) {
    }

    @Override
    public void setSynchronizer(Object synchronizer) {
    }

    @Override
    public void notifyWonCards(Color color, Symbol symbol) {

    }

    @Override
    public GameState requestActionTile() {
        ArrayList<GameState> actionsTile = rules.getActionsTileMove(color, gameState);
        int tilesInRangeCounter = 0;
        GameState gameStateMaxTilesInRange = actionsTile.get(0);

        /* überprüft ob das gesuchte Symbol durch einen der möglichen TileMoves erreicht werden kann. Wenn ja, wird dieser zurückgegeben (d.h. ausgeführt). */
        for(GameState gs : actionsTile){
            HashSet<int[]> tilesInRange = rules.coordinatesOfTilesInRange(gs.getGamePiece(color).getX(), gs.getGamePiece(color).getY(), gs.getBoard());
            int[] positionOfActiveCard = gs.getPositionOfSymbol(activeCard);
            for(int[] coord : tilesInRange){
                if(Arrays.equals(coord, positionOfActiveCard)) {
                    return gs;
                }
            }
            /* Ansonsten wird der Zug zurückgegeben, der die meisten Tiles erreichbar macht */
            if(tilesInRangeCounter < tilesInRange.size()){
                tilesInRangeCounter = tilesInRange.size();
                gameStateMaxTilesInRange = gs;
            }
        }

        return gameStateMaxTilesInRange;
    }

    @Override
    public GameState requestActionGamePiece() {
        /* Wenn das gesuchte Symbol eingesammelt werden kann wird dies getan. Ansonsten wird ein zufälliger Zug ausgeführt */
        ArrayList<GameState> listActions = rules.getActionsGamePieceMove(color, gameState);
        int[] positionOfActiveCard = gameState.getPositionOfSymbol(activeCard);
        HashSet<int[]> tilesInRange = rules.coordinatesOfTilesInRange(gameState.getGamePiece(color).getX(), gameState.getGamePiece(color).getY(), gameState.getBoard());
        for(int[] coord : tilesInRange){
            if(Arrays.equals(coord, positionOfActiveCard)) return gameState.changeBoardGamePiece(color, coord[0], coord[1]);
        }


        /* zufälliger erlaubter Zug wird ausgeführt */
        int rnd = (int) (Math.random()*listActions.size());
        return listActions.get(rnd);
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
        this.activeCard = symbol;
    }

    @Override
    public Symbol getActiveCard() {
        return activeCard;
    }

}
