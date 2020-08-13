package model;

/**
 * beschreibt die Spielfigur der einzelnen Spieler auf dem Spielbrett
 */
public interface GamePiece {
    /**
     *
     * @return x-Position des GamePieces auf dem board
     */
    int getX();
    /**
     *
     * @return y-Position des GamePieces auf dem board
     */
    int getY();

    /**
     *
     * @param x neue x-Position des GamePieces
     * @param y neue y-Position des GamePieces
     */
    void setPosition(int x, int y);

    /**
     *
     * @return Color des GamePieces
     */
    Color getColor();

}
