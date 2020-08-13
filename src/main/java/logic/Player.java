package logic;

import model.*;


/**
 * Beschreibt einen Spielteilnehmer. Entweder eine KI oder der vom physischen Spieler gesteuerte Spieler.
 * Im Konstruktor wird die Color festgelegt.
 * Dieses Interface ist mit allen Methoden zu implementieren, wenn eine eigene KI implementiert werden soll.
 */

public interface Player {
    /**
     * has to be implemented
     * wird von update() in Game aufgerufen.
     * @return eine der Actions aus Rules.getActionsTileMove()
     */
    GameState requestActionTile();
    /**
     * has to be implemented
     * wird von update() in Game aufgerufen.
     * @return eine der Actions aus Rules.getActionsGamePieceMove()
     */
    GameState requestActionGamePiece();

    /**
     * has to be implemented
     * wird von update() aufgerufen.
     * @param gameState aktueller GameState
     */
    void updateGameState(GameState gameState);

    GameState getGameState();

    /**
     * has to be implemented
     * @return the color of the Player
     */
    Color getColor();

    void setRules(Rules rules);
    Rules getRules();

    void setName(String name);
    String getName();

    void setThread(ThreadUpdate thread);
    void setSynchronizer(Object synchronizer);

    /**
     * wird von update aufgerufen, sobald ein anderer Spieler eine Karte gewinnt.
     * @param color Farbe des Spielers, der eine Karte gewonnen hat
     * @param symbol auf dieser Karte
     */
    void notifyWonCards(Color color, Symbol symbol);

    /**
     * legt die Karte ganz oben auf dem Stapel, die der Spieler als nächstes gewinnen muss fest.
     * Wird von Game_Impl aufgerufen um dem Player seine Karte mitzuteilen
     * @param symbol neue zu erreichende Karte/Symbol
     */
    void setActiveCard(Symbol symbol);

    /**
     * has to be implemented
     * @return the activeCard, that was set by Game_Impl
     */
    Symbol getActiveCard();


    /**
     * creates new instance of chosen player
     *
     * Correct place to add new and more KI players!
     *
     * @param className     name of the class to create an instance of
     * @param color         chosen color for a player
     * @return  new instance of playerclass
     */
    static Player createNewPlayer(String className, Color color) throws IllegalArgumentException {
        switch (className) {
            case "PlayerKI":
                return new PlayerKI(color);
            case "PlayerKI2":
                return new PlayerKI2(color);
            case "PlayerKI3":
                return new PlayerKI3(color);
            case "PlayerKI4":
                return new PlayerKI4(color);
            case "PlayerHuman":
                return new PlayerPhysical(color);
            default:
                throw new IllegalArgumentException("createNewPlayer in Player.java doesn't know how to handle this!");
        }
    }
}
