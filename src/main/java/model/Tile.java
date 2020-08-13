package model;

/**
 * beschreibt die einzelnen Schiebekärtchen auf dem Spielbrett.
 */
public interface Tile {

    /**
     *
     * @return Orientierung des model.Tile 0, 1, 2 oder 3
     * Standardorientierung, d.h. orientierung == 0:
     *                           ________
     * ___|  |___   ________    |   _____
     * __________   ________    |  |
     *                          |  |
     * sonst im Uhrzeigersinn drehen = orientierung + 1
     */
    int getOrientation();

    void setOrientation(int orientation);

    /**
     *
     * @return model.Symbol auf dem model.Tile inklusive der Farbe für die Ecken und None, wenn kein model.Symbol darauf ist
     */
    Symbol getSymbol();

    /**
     *
     * @return welcher der 3 Arten von model.Tile er ist
     *                           ________
     * ___|  |___   ________    |   _____
     * __________   ________    |  |
     *                          |  |
     *  Crossway     Way         Edge
     *
     */
    TileType getType();

    /**
     * dreht Tile nach rechts, d.h. orientierung = (orientierung + 1) % 4
     */
    void turnRight();
    /**
     * dreht Tile nach links
     */
    void turnLeft();


}
