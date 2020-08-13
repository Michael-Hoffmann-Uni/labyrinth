package model;

/**
 * Der GameState speichert alles, was auf und neben dem Spielbrett passiert. Also alle Informationen über die Schiebekarten (Tiles) und Spielfiguren (GamePieces) der Spieler.
 *
 */
public interface GameState {
    /**
     * @param color die Farbe, dessen GamePiece angefordert wird
     * @return das GamePiece zu jeder Farbe
     */
    GamePiece getGamePiece(Color color);

    /**
     *
     * @param color die Farbe eines GamePiece
     * @return die Position des entsprechenden GamePiece
     * Tile des StartpunktGelb wäre x = 6 und y = 0
     */
    int getPositionX(Color color);
    int getPositionY(Color color);
    void setPosition(Color color, int x, int y);


    /**
     *
     * @return die Farbe, die aktuell am Zug ist
     */
    Color getActiveColor();

    /**
     * aktualisiert die ActiveColor d.h.:
     * Red -&gt; Yellow -&gt; Blue -&gt; Green -&gt; Red
     * (nicht teilnehmende Farben werden übersprungen)
     */
    void updateActiveColor();

    /**
     *
     * @return das Spielbrett bestehend aus den 49 Tiles. Als 7x7-Array mit den Tiles mit folgenden Symbolen
     * [0][0] = StartpunktRot      [6][0] = StartpunktGelb
     * [0][6] = StartpunktGruen    [6][6] = StartpunktBlau
     */
    Tile[][] getBoard();


    /**
     *
     * @return Das Tile, das nicht auf dem Spielfeld liegt, sondern im letzten Zug heraus- und nun wieder hineingeschoben wird
     */
    Tile getTile50();

    /**
     *
     * @return Position des Tile50 von 0-11 siehe Dokument in NextCloud
     *    0 1 2
     * 11       3
     * 10       4
     *  9       5
     *    8 7 6
     *
     *    im Startzustand ist Tile50Position == 77
     */
    int getTile50Position();

    /**
     * Schiebt Tile50 in das board ein.
     * @param position Zahl zwischen 0-11 siehe Dokument in Nextcloud. Wo wird das Tile50 eingeschoben.
     *                 darf nach Regeln nicht die aktuelle Tile50Position sein.
     * @param tile50Orientation Zahl zwisch 0-3. Mit welcher Orientation wird Tile50 eingeschoben.
     * @return ein neues GameState-Objekt mit dem gewünschten Zug angewandt. Das ursprüngliche GameState-Objekt bleibt unverändert.
     */
    GameState changeBoard(int position, int tile50Orientation);

    /**
     * verändert die Position des GamePieces der Farbe
     * @param color Farbe des GamePiece
     * @param x neue x-Position
     * @param y neue y-Position
     * @return ein neues GameState-Objekt mit dem gewünschten Zug angewandt. Das ursprüngliche GameState-Objekt bleibt unverändert.
     */
    GameState changeBoardGamePiece(Color color, int x, int y);

    /**
     *
     * @param symbol gesuchtes Symbol
     * @return  Array der Länge 2 mit a[0] == x-Koordinate des gesuchten Symbols auf dem board
     *          und a[1] == y-Koordinate des gesuchten Symbols
     */
    int[] getPositionOfSymbol(Symbol symbol);



}
