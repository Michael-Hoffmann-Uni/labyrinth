package logic;

import model.*;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Beschreibt den austauschbaren Teil des Spieles. Die Regeln, wie das Spielbrett verändert werden kann, mit welchen Karten gespielt wird, wie diese verteilt werden und die Siegbedingung.
 * Im Konstruktor wird eine Liste der gespielten Farben übergeben, da diese benötigt wird um die Kartenstapel für die Spieler anzulegen.
 * Die 2-4 Kartenstapel der einzelnen Spieler werden hier gespeichert.
 * Zum Ändern der Regeln muss dieses Interface implementiert werden.
 */
public interface Rules {
    int getNumberOfPlayers();

    /**
     * gibt alle möglichen Züge für das Einschieben von Tiles für den Spieler
     * @param color übergebene Farbe
     * @param gameState übergebener aktueller GameState
     * @return Eine Liste der möglichen GameStates nach dem jeweiligen Zug.
     */
    ArrayList<GameState> getActionsTileMove(Color color, GameState gameState);

    /**
     * gibt alle möglichen Züge für das Ziehen der Spielfigur (GamePiece) für den Spieler der Farbe zurück.
     * @param color übergebene Farbe
     * @param gameState übergebener aktueller GameState
     * @return Eine Liste der möglichen GameStates nach dem jeweiligen Zug.
     */
    ArrayList<GameState> getActionsGamePieceMove(Color color, GameState gameState);

    /**
     *
     * @param gameState der GameState, der auf Endbedingung überprüft werden soll
     * @return true, falls im übergebenen GameState das Spiel zu Ende ist, also ein Spieler gewonnen hat.
     */
    boolean isEndState(GameState gameState);

    /**
     *
     * @return die Farbe des Spielers, der den übergebenen
     * @param gameState gewonnen hat.
     *                  gibt null zurück, falls das Spiel noch läuft.
     */
    Color getWinner(GameState gameState);

    /**
     *
     * @param color von welchem der Kartenstapel gezogen werden soll. Da jeder der Spieler seinen eigenen Kartenstapel besitzt.
     * @return die nächste zu gewinnene Karte des jeweiligen Spielers
     */
    Symbol drawCard(Color color);

    /**
     *
     * @return einen (zufälligen) Startzustand. Mit den fixed Tiles immer an deren Stelle und den GamePieces der entsprechenden Farben auf ihren Startpositionen.
     */
    GameState getInitialGameStateRandom();

    /**
     * gibt die Koordinaten der vom Tile (x,y) erreichbaren Tiles zurück. Nützlich für KI.
     *
     * @param x - Koordinate
     * @param y - Koordinate
     * @param board board, auf das der Algorithmus angewandt werden soll
     * @return Die Menge der erreichbaren Koordinaten auf dem board. Ein Hashset mit Arrays der Länge 2. mit a[0] = x-Koordinate des erreichbaren Tiles und a[1] = y-Koordinate des erreichbaren Tiles
     */
    HashSet<int[]> coordinatesOfTilesInRange(int x, int y, Tile[][] board);

    ArrayList<Color> getPlayedColors();

}
