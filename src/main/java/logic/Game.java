package logic;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Die Hauptklasse des Spiels. Hier laufen alle Informationen über den GameState, die Spieler und die Rules zusammen und die von den Spielern angeforderten Züge werden aufgeführt.
 */

public interface Game {

    /**
     *
     * @return Hashmap der beteiligten Spieler mit Key deren Color
     */
    HashMap<Color, Player> getPlayers();

    /**
     *
     * @return eine ArrayList der gespielten Colors, die für den Konstruktor der Rules_Impl benötigt wird
     */
    ArrayList<Color> getPlayedColors();

    /**
     * gibt alle gewonnen Symbole des Spielers der Farbe color zurück.
     * @param color Farbe des angefragten Spielers.
     * @return alle gewonnen Symbole des Spielers der Farbe color
     */
    ArrayList<Symbol> getWonCards(Color color);

    GameState getGameState();

    /**
     *
     * muss vor start() aufgerufen werden.
     * @param rules rules nach denen das Spiel gespielt werden soll
     */
    void setRules(Rules rules);


    /**
     * Startet das Spiel. Das Festlegen der Spieler muss vorher geschehen.
     * Verteilt die Karten an die Spieler.
     * @param initialGameState der GameState mit dem das Spiel beginnt. Erhält man in rules.getInitialGameStateRandom()
     */
    void start(GameState initialGameState);

    Rules getRules();

    /**
     * Ein
     * @param player Spieler mit der Farbe
     * @param color wird in die Liste der Spieler hinzugefügt.
     * ein GamePiece wird in GameState angelegt
     * @return false, wenn der Spieler nicht hinzugefügt werden konnte, weil die Farbe bereits gespielt wird oder weil schon 4 Spieler teilnehmen
     *         sonst true.
     */
    boolean join(Player player, Color color);

    /**
     *
     * @param player wird aus der Spielerliste entfernt.
     * @return false, falls der Spieler nicht in der Liste vorhanden war
     *         sonst true
     */
    boolean leave(Player player);

    /**
     *
     * Spieler mit der Farbe
     * @param color wird aus der Spielerliste entfernt.
     * @return false, falls der Spieler nicht in der Liste vorhanden war
     *         sonst true
     */
    boolean leave(Color color);


    boolean isRunning();

    /**
    1. Verteilt aktuellen GameState an alle Spieler
    2. Fordert den aktuellen Spieler auf TileMove zu machen (requestAction)
    3. Wendet Zug auf Spielfeld an
    4. GameState verteilen
       (ActionsPlayerMove erst hier berechnen)
    5. fordert den aktuellen Spieler auf GamePieceMove zu machen
    6. nochmal GameState verteilen
     7.   7.1 überprüft ob activePlayer auf dem Tile steht mit Symbol == Player.getActiveCard()
     7.2 wenn ja:
       notifyPlayers über gewonnene Karte des activePlayer
       Player.setActiveCard(rules.drawCard(activeColor))
    8. Überprüft auf Spielende oder Endlosspiel (!) und beendet falls ja
    9. ruft updateActiveColor in GameState auf
     @return true, falls das Spiel weiter läuft und false sonst
     */
    boolean update();
    //Abbruchbedingung der Endlosspiele: Zähler Anzahl der update-Aufrufe (Bei Abbruchbedingung mit einbeziehen, wie viele Spieler mitspielen)
}
