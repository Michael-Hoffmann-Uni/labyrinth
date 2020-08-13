package logic;

import model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Stack;

/**
 * Die Implementierung der Regeln gemäß der offiziellen "Das verrückte Labyrint"-Regeln.
 */
public class Rules_Impl implements Rules {
    private ArrayList<Color> playedColors;
    private Stack<Symbol> deckOfCardsRed;
    private Stack<Symbol> deckOfCardsBlue;
    private Stack<Symbol> deckOfCardsYellow;
    private Stack<Symbol> deckOfCardsGreen;

    public Rules_Impl(ArrayList<Color> playedColors){
        //überprüfen, ob eine Farbe mehrfach vorkommt (beeinhaltet mehr als 4 Spieler übergeben)
        if(new HashSet<>(playedColors).size() != playedColors.size()) throw new IllegalArgumentException("Rules_Impl called with a list containing at least one Color more than once.");
        //überprüfen, ob weniger als zwei Spieler übergeben wurden
        if(playedColors.size()<2) throw new IllegalArgumentException("Rules_Impl called with a list containing less than two Players");


        this.playedColors = playedColors;

        //Erzeugt Liste mit allen Karten
        ArrayList<Symbol> deckOfCards = new ArrayList<>();
        deckOfCards.add(Symbol.Owl);
        deckOfCards.add(Symbol.Mouse);
        deckOfCards.add(Symbol.Salamander);
        deckOfCards.add(Symbol.Moth);
        deckOfCards.add(Symbol.Spider);
        deckOfCards.add(Symbol.Beetle);
        deckOfCards.add(Symbol.Book);
        deckOfCards.add(Symbol.Coin);
        deckOfCards.add(Symbol.Map);
        deckOfCards.add(Symbol.Crown);
        deckOfCards.add(Symbol.Keys);
        deckOfCards.add(Symbol.Skull);
        deckOfCards.add(Symbol.Ring);
        deckOfCards.add(Symbol.Treasure);
        deckOfCards.add(Symbol.Emerald);
        deckOfCards.add(Symbol.Sword);
        deckOfCards.add(Symbol.Candleholder);
        deckOfCards.add(Symbol.Helmet);
        deckOfCards.add(Symbol.Bat);
        deckOfCards.add(Symbol.Goblin);
        deckOfCards.add(Symbol.Fairy);
        deckOfCards.add(Symbol.Genie);
        deckOfCards.add(Symbol.Ghost);
        deckOfCards.add(Symbol.Dragon);

        //Verteilt diese Karten zufällig auf die Stapel der teilnehmenden Farben
        for(Color color : playedColors){
            Stack<Symbol> stacky = new Stack<>();
            for(int i = 0; i < 24/playedColors.size(); i++) {
                int zufallszahl = (int) (Math.random() * deckOfCards.size()); //Zahl zwischen 0 und size-1
                stacky.add(deckOfCards.remove(zufallszahl));
            }
            switch(color){
                case Red:
                    deckOfCardsRed = new Stack<>();
                    deckOfCardsRed.add(Symbol.StartingPointRed);
                    deckOfCardsRed.addAll(stacky);
                    break;
                case Green:
                    deckOfCardsGreen = new Stack<>();
                    deckOfCardsGreen.add(Symbol.StartingPointGreen);
                    deckOfCardsGreen.addAll(stacky);
                    break;
                case Yellow:
                    deckOfCardsYellow = new Stack<>();
                    deckOfCardsYellow.add(Symbol.StartingPointYellow);
                    deckOfCardsYellow.addAll(stacky);
                    break;
                case Blue:
                    deckOfCardsBlue = new Stack<>();
                    deckOfCardsBlue.add(Symbol.StartingPointBlue);
                    deckOfCardsBlue.addAll(stacky);
                    break;
            }
        }

    }

    @Override
    public int getNumberOfPlayers() {
        return playedColors.size();
    }

    @Override
    public ArrayList<Color> getPlayedColors() {
        return playedColors;
    }

    @Override
    public ArrayList<GameState> getActionsTileMove(Color color, GameState gameState) {
        ArrayList<GameState> listActions = new ArrayList<>();
        for(int orientation = 0; orientation < 4; orientation++) {
            for (int i = 0; i < 12; i++) {
                if (i != gameState.getTile50Position()) {
                    listActions.add(gameState.changeBoard(i, orientation));
                }
            }
        }
        return listActions;
    }

    @Override
    public ArrayList<GameState> getActionsGamePieceMove(Color color, GameState gameState) {
        ArrayList<GameState> listActions = new ArrayList<>();
        HashSet<int[]> setOfCoordinatesInRange = coordinatesOfTilesInRange(gameState.getGamePiece(color).getX(), gameState.getGamePiece(color).getY(), gameState.getBoard());
        for(int[] coord : setOfCoordinatesInRange){
            listActions.add(gameState.changeBoardGamePiece(color, coord[0], coord[1]));
        }
        return listActions;
    }

    @Override
    public boolean isEndState(GameState gameState) {
        for(Color color : playedColors){
            switch(color) {
                case Red:
                    if (deckOfCardsRed.size() == 0 && gameState.getGamePiece(Color.Red).getX() == 0 && gameState.getGamePiece(Color.Red).getY() == 0)
                        return true;
                    break;
                case Green:
                    if (deckOfCardsGreen.size() == 0 && gameState.getGamePiece(Color.Green).getX() == 0 && gameState.getGamePiece(Color.Green).getY() == 6)
                        return true;
                    break;
                case Yellow:
                    if (deckOfCardsYellow.size() == 0 && gameState.getGamePiece(Color.Yellow).getX() == 6 && gameState.getGamePiece(Color.Yellow).getY() == 0)
                        return true;
                    break;
                case Blue:
                    if (deckOfCardsBlue.size() == 0 && gameState.getGamePiece(Color.Blue).getX() == 6 && gameState.getGamePiece(Color.Blue).getY() == 6)
                        return true;
                    break;
            }
        }
        return false;
    }


    @Override
    public Color getWinner(GameState gameState) {
        for(Color color : playedColors){
            switch(color) {
                case Red:
                    if (deckOfCardsRed.size() == 0 && gameState.getGamePiece(Color.Red).getX() == 0 && gameState.getGamePiece(Color.Red).getY() == 0)
                        return Color.Red;
                    break;
                case Green:
                    if (deckOfCardsGreen.size() == 0 && gameState.getGamePiece(Color.Green).getX() == 0 && gameState.getGamePiece(Color.Green).getY() == 6)
                        return Color.Green;
                    break;
                case Yellow:
                    if (deckOfCardsYellow.size() == 0 && gameState.getGamePiece(Color.Yellow).getX() == 6 && gameState.getGamePiece(Color.Yellow).getY() == 0)
                        return Color.Yellow;
                    break;
                case Blue:
                    if (deckOfCardsBlue.size() == 0 && gameState.getGamePiece(Color.Blue).getX() == 6 && gameState.getGamePiece(Color.Blue).getY() == 6)
                        return Color.Blue;
                    break;
            }
        }
        return null;
    }

    @Override
    public Symbol drawCard(Color color) {
        if(!playedColors.contains(color)) throw new NullPointerException("die Farbe mit der drawCard(Color) in Rules aufgerufen wurde ist nicht in playedColors");
        switch(color){
            case Red:
                if(deckOfCardsRed.size() == 0) return null;
                return deckOfCardsRed.pop();
            case Green:
                if(deckOfCardsGreen.size() == 0) return null;
                return deckOfCardsGreen.pop();
            case Yellow:
                if(deckOfCardsYellow.size() == 0) return null;
                return deckOfCardsYellow.pop();
            case Blue:
                if(deckOfCardsBlue.size() == 0) return null;
                return deckOfCardsBlue.pop();
        }
        return null;
    }

    private Tile_Impl[][] getFixedBoard(){
        Tile_Impl[][] board = new Tile_Impl[7][7];
        board[0][0] = new Tile_Impl(TileType.Edge, Symbol.StartingPointRed, 0);
        board[2][0] = new Tile_Impl(TileType.Crossway, Symbol.Book, 2);
        board[4][0] = new Tile_Impl(TileType.Crossway, Symbol.Coin, 2);
        board[6][0] = new Tile_Impl(TileType.Edge, Symbol.StartingPointYellow, 1);

        board[0][2] = new Tile_Impl(TileType.Crossway, Symbol.Map, 1);
        board[2][2] = new Tile_Impl(TileType.Crossway, Symbol.Crown, 1);
        board[4][2] = new Tile_Impl(TileType.Crossway, Symbol.Keys, 2);
        board[6][2] = new Tile_Impl(TileType.Crossway, Symbol.Skull, 3);

        board[0][4] = new Tile_Impl(TileType.Crossway, Symbol.Ring, 1);
        board[2][4] = new Tile_Impl(TileType.Crossway, Symbol.Treasure, 0);
        board[4][4] = new Tile_Impl(TileType.Crossway, Symbol.Emerald, 3);
        board[6][4] = new Tile_Impl(TileType.Crossway, Symbol.Sword, 3);

        board[0][6] = new Tile_Impl(TileType.Edge, Symbol.StartingPointGreen, 3);
        board[2][6] = new Tile_Impl(TileType.Crossway, Symbol.Candleholder, 0);
        board[4][6] = new Tile_Impl(TileType.Crossway, Symbol.Helmet, 0);
        board[6][6] = new Tile_Impl(TileType.Edge, Symbol.StartingPointBlue, 2);
        return board;
    }

    private ArrayList<Tile_Impl> getListOfUnfixedTiles(){
        ArrayList<Tile_Impl> list = new ArrayList<>();
        list.add(new Tile_Impl(TileType.Crossway, Symbol.Bat, 0));
        list.add(new Tile_Impl(TileType.Crossway, Symbol.Goblin, 0));
        list.add(new Tile_Impl(TileType.Crossway, Symbol.Fairy, 0));
        list.add(new Tile_Impl(TileType.Crossway, Symbol.Genie, 0));
        list.add(new Tile_Impl(TileType.Crossway, Symbol.Ghost, 0));
        list.add(new Tile_Impl(TileType.Crossway, Symbol.Dragon, 0));

        list.add(new Tile_Impl(TileType.Edge, Symbol.Beetle, 0));
        list.add(new Tile_Impl(TileType.Edge, Symbol.Spider, 0));
        list.add(new Tile_Impl(TileType.Edge, Symbol.Moth, 0));
        list.add(new Tile_Impl(TileType.Edge, Symbol.Salamander, 0));
        list.add(new Tile_Impl(TileType.Edge, Symbol.Mouse, 0));
        list.add(new Tile_Impl(TileType.Edge, Symbol.Owl, 0));

        for(int i = 0; i < 9; i++){
            list.add(new Tile_Impl(TileType.Edge, Symbol.None,0));
        }
        for(int i = 0; i < 13; i++){
            list.add(new Tile_Impl(TileType.Way, Symbol.None, 0));
        }

        return list;
    }

    @Override
    public GameState getInitialGameStateRandom() {
        Color randomStartColor = playedColors.get((int) (Math.random()*playedColors.size()));
        Tile_Impl[][] board = getFixedBoard();
        ArrayList<Tile_Impl> unfixedTiles = getListOfUnfixedTiles();

        //setzt die Orientation aller unfixed Tiles auf random
        for(Tile_Impl tile : unfixedTiles){
            int randInt = (int) (Math.random() * 4);
            for(int i = 0; i < randInt; i++){
                tile.turnRight();
            }
        }

        //verteilt die unfixed Tiles auf allen Feldern des boards, die nicht fixed sind (d.h. es ist nicht x und y ein vielfaches von 2)
        for(int x = 0; x < 7; x++){
            for(int y = 0; y < 7; y++){
                if(!(x%2 == 0 && y%2 == 0)){
                    int randomListInt = (int) (Math.random() * unfixedTiles.size());
                    board[x][y] = unfixedTiles.remove(randomListInt);
                }
            }
        }

        return new GameState_Impl(randomStartColor, board, unfixedTiles.get(0), playedColors);
    }

    /* Ein festgelegter GameState für Testzwecke. Do not remove! */
    public GameState getTestGameStateFixed(){
        Tile_Impl[][] board = getFixedBoard();
        ArrayList<Tile_Impl> unfixedTiles = getListOfUnfixedTiles();
        
        int randInt = 0;
        for(Tile_Impl tile : unfixedTiles){
            randInt = (randInt+7)%4;
            for(int i = 0; i < randInt; i++){
                tile.turnRight();
            }
        }

        //verteilt die unfixed Tiles auf allen Feldern des boards, die nicht fixed sind (d.h. es ist nicht x und y ein vielfaches von 2)
        int k = 0;
        for(int x = 0; x < 7; x++){
            for(int y = 0; y < 7; y++){
                k = (k+7)% unfixedTiles.size();
                if(!(x%2 == 0 && y%2 == 0)){
                    board[x][y] = unfixedTiles.remove(k);
                }
            }
        }

        return new GameState_Impl(Color.Red, board, unfixedTiles.get(0), playedColors);
    }

    /*  Erstellt ein Set mit allen von den Koordinaten x,y aus erreichbaren Koordinaten. */
    @Override
    public HashSet<int[]> coordinatesOfTilesInRange(int x, int y, Tile[][] board){
        HashSet<int[]> hashy = new HashSet<>();
        int[] c = {x,y};
        hashy.add(c);
        return findTiles(x, y, board, hashy);
    }

    /*

    Idee: Sammle rekursiv in einem Set alle jeweils erreichbaren Nachbartiles ein

     */                                         //deine x und y
    public HashSet<int[]> findTiles(int x, int y, Tile[][] board, HashSet<int[]> tilePositions){
        TileType tileType = board[x][y].getType();
        int tileOrientation = board[x][y].getOrientation();

        switch(tileType){
            case Crossway:
                switch(tileOrientation){
                    case 0:
                        //nach oben
                        //sind wir in der obersten Zeile oder hat das Tile über diesem nach unten eine Wand oder wurde das Tile über diesem schon angeschaut?
                        if(y!=0 && !neighbourHasWall(2, board[x][y-1]) && !tilePositionsContainsXY(x,y-1, tilePositions) ) {
                            tilePositions.add(arrayOfXY(x,y-1));
                            tilePositions.addAll(findTiles(x, y - 1, board, tilePositions));
                        }
                        //nach links
                        if(x!=0 && !neighbourHasWall(1, board[x-1][y]) && !tilePositionsContainsXY(x-1,y, tilePositions)) {
                            tilePositions.add(arrayOfXY(x - 1, y));
                            tilePositions.addAll(findTiles(x - 1, y, board, tilePositions));
                        }
                        //nach rechts
                        if(x!=6 && !neighbourHasWall(3, board[x+1][y]) && !tilePositionsContainsXY(x+1,y,tilePositions)) {
                            tilePositions.add(arrayOfXY(x + 1, y));
                            tilePositions.addAll(findTiles(x + 1, y, board, tilePositions));
                        }
                        break;
                    case 1:
                        //nach oben
                        if(y!=0 && !neighbourHasWall(2, board[x][y-1]) && !tilePositionsContainsXY(x,y-1, tilePositions)) {
                            tilePositions.add(arrayOfXY(x, y-1));
                            tilePositions.addAll(findTiles(x, y - 1, board, tilePositions));
                        }
                        //nach unten
                        if(y!=6 && !neighbourHasWall(0, board[x][y+1]) && !tilePositionsContainsXY(x,y+1, tilePositions)) {
                            tilePositions.add(arrayOfXY(x, y+1));
                            tilePositions.addAll(findTiles(x, y + 1, board, tilePositions));
                        }
                        //nach rechts
                        if(x!=6 && !neighbourHasWall(3, board[x+1][y]) && !tilePositionsContainsXY(x+1,y,tilePositions)) {
                            tilePositions.add(arrayOfXY(x+1, y));
                            tilePositions.addAll(findTiles(x + 1, y, board, tilePositions));
                        }
                        break;
                    case 2:
                        //nach links
                        if(x!=0 && !neighbourHasWall(1, board[x-1][y]) && !tilePositionsContainsXY(x-1, y, tilePositions)) {
                            tilePositions.add(arrayOfXY(x-1, y));
                            tilePositions.addAll(findTiles(x - 1, y, board, tilePositions));
                        }
                        //nach unten
                        if(y!=6 && !neighbourHasWall(0, board[x][y+1]) && !tilePositionsContainsXY(x,y+1, tilePositions)) {
                            tilePositions.add(arrayOfXY(x, y+1));
                            tilePositions.addAll(findTiles(x, y + 1, board, tilePositions));
                        }
                        //nach rechts
                        if(x!=6 && !neighbourHasWall(3, board[x+1][y]) && !tilePositionsContainsXY(x+1,y,tilePositions)) {
                            tilePositions.add(arrayOfXY(x+1, y));
                            tilePositions.addAll(findTiles(x + 1, y, board, tilePositions));
                        }
                        break;
                    case 3:
                        //nach links
                        if(x!=0 && !neighbourHasWall(1, board[x-1][y]) && !tilePositionsContainsXY(x-1, y, tilePositions)) {
                            tilePositions.add(arrayOfXY(x-1, y));
                            tilePositions.addAll(findTiles(x - 1, y, board, tilePositions));
                        }
                        //nach unten
                        if(y!=6 && !neighbourHasWall(0, board[x][y+1]) && !tilePositionsContainsXY(x,y+1, tilePositions)) {
                            tilePositions.add(arrayOfXY(x, y+1));
                            tilePositions.addAll(findTiles(x, y + 1, board, tilePositions));
                        }
                        //nach oben
                        if(y!=0 && !neighbourHasWall(2, board[x][y-1]) && !tilePositionsContainsXY(x,y-1,tilePositions)) {
                            tilePositions.add(arrayOfXY(x, y-1));
                            tilePositions.addAll(findTiles(x, y - 1, board, tilePositions));
                        }
                        break;
                }
                break;
            case Way:
                if(tileOrientation == 0 || tileOrientation == 2){
                    //nach links
                    if(x!=0 && !neighbourHasWall(1, board[x-1][y]) && !tilePositionsContainsXY(x-1,y, tilePositions)) {
                        tilePositions.add(arrayOfXY(x-1, y));
                        tilePositions.addAll(findTiles(x - 1, y, board, tilePositions));
                    }
                    //nach rechts
                    if(x!=6 && !neighbourHasWall(3, board[x+1][y]) && !tilePositionsContainsXY(x+1,y,tilePositions)) {
                        tilePositions.add(arrayOfXY(x+1, y));
                        tilePositions.addAll(findTiles(x + 1, y, board, tilePositions));
                    }
                }
                else{
                    //nach unten
                    if(y!=6 && !neighbourHasWall(0, board[x][y+1]) && !tilePositionsContainsXY(x,y+1, tilePositions)) {
                        tilePositions.add(arrayOfXY(x, y+1));
                        tilePositions.addAll(findTiles(x, y + 1, board, tilePositions));
                    }
                    //nach oben
                    if(y!=0 && !neighbourHasWall(2, board[x][y-1]) && !tilePositionsContainsXY(x,y-1,tilePositions)) {
                        tilePositions.add(arrayOfXY(x, y-1));
                        tilePositions.addAll(findTiles(x, y - 1, board, tilePositions));
                    }
                }
                break;
            case Edge:
                switch(tileOrientation){
                    case 0:
                        //nach unten
                        if(y!=6 && !neighbourHasWall(0, board[x][y+1]) && !tilePositionsContainsXY(x,y+1, tilePositions)) {
                            tilePositions.add(arrayOfXY(x, y+1));
                            tilePositions.addAll(findTiles(x, y + 1, board, tilePositions));
                        }
                        //nach rechts
                        if(x!=6 && !neighbourHasWall(3, board[x+1][y]) && !tilePositionsContainsXY(x+1,y,tilePositions)) {
                            tilePositions.add(arrayOfXY(x+1, y));
                            tilePositions.addAll(findTiles(x + 1, y, board, tilePositions));
                        }
                        break;
                    case 1:
                        //nach links
                        if(x!=0 && !neighbourHasWall(1, board[x-1][y]) && !tilePositionsContainsXY(x-1, y, tilePositions)) {
                            tilePositions.add(arrayOfXY(x-1, y));
                            tilePositions.addAll(findTiles(x - 1, y, board, tilePositions));
                        }
                        //nach unten
                        if(y!=6 && !neighbourHasWall(0, board[x][y+1]) && !tilePositionsContainsXY(x,y+1, tilePositions)) {
                            tilePositions.add(arrayOfXY(x, y+1));
                            tilePositions.addAll(findTiles(x, y + 1, board, tilePositions));
                        }
                        break;
                    case 2:
                        //nach oben
                        if(y!=0 && !neighbourHasWall(2, board[x][y-1]) && !tilePositionsContainsXY(x,y-1, tilePositions)) {
                            tilePositions.add(arrayOfXY(x, y-1));
                            tilePositions.addAll(findTiles(x, y - 1, board, tilePositions));
                        }
                        //nach links
                        if(x!=0 && !neighbourHasWall(1, board[x-1][y]) && !tilePositionsContainsXY(x-1,y, tilePositions)) {
                            tilePositions.add(arrayOfXY(x-1, y));
                            tilePositions.addAll(findTiles(x - 1, y, board, tilePositions));
                        }
                        break;
                    case 3:
                        //nach oben
                        if(y!=0 && !neighbourHasWall(2, board[x][y-1]) && !tilePositionsContainsXY(x,y-1, tilePositions)) {
                            tilePositions.add(arrayOfXY(x, y-1));
                            tilePositions.addAll(findTiles(x, y - 1, board, tilePositions));
                        }
                        //nach rechts
                        if(x!=6 && !neighbourHasWall(3, board[x+1][y]) && !tilePositionsContainsXY(x+1, y, tilePositions)) {
                            tilePositions.add(arrayOfXY(x+1, y));
                            tilePositions.addAll(findTiles(x + 1, y, board, tilePositions));
                        }
                }
                break;
        }
        return tilePositions;
    }

    public int[] arrayOfXY(int x, int y){
        return new int[]{x,y};
    }

    public boolean tilePositionsContainsXY(int x, int y, HashSet<int[]> tilePositions){
        for(int[] coord : tilePositions){
            if(coord[0]==x && coord[1]==y) return true;
        }

        return false;
    }

    /* hat das Tile eine Wand in die übergebene direction vom Nachbar aus
     *   z.B. hat das Tile eine Wand nach unten neighbourHasWall(2, Tile) == true*/
    public boolean neighbourHasWall(int direction, Tile neighbourTile){
        TileType tileType = neighbourTile.getType();
        int orientationNeighbourTile = neighbourTile.getOrientation();
        switch (tileType){
            case Crossway:
                return (orientationNeighbourTile + 2) % 4 == direction;
            case Way:
                return (orientationNeighbourTile == direction || (orientationNeighbourTile + 2) % 4 == direction);
            case Edge:
                switch (orientationNeighbourTile){
                    case 0:
                        return direction == 0 || direction == 3;
                    case 1:
                        return direction == 0 || direction == 1;
                    case 2:
                        return direction == 1 || direction == 2;
                    case 3:
                        return direction == 2 || direction == 3;
                }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rules_Impl rules_ = (Rules_Impl) o;
        return playedColors.equals(rules_.playedColors) &&
                deckOfCardsRed.equals(rules_.deckOfCardsRed) &&
                deckOfCardsBlue.equals(rules_.deckOfCardsBlue) &&
                deckOfCardsYellow.equals(rules_.deckOfCardsYellow) &&
                deckOfCardsGreen.equals(rules_.deckOfCardsGreen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playedColors, deckOfCardsRed, deckOfCardsBlue, deckOfCardsYellow, deckOfCardsGreen);
    }
}
