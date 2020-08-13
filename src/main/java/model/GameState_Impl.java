package model;

import java.util.*;

public class GameState_Impl implements GameState {
    private HashMap<Color, GamePiece_Impl> gamePieces;
    private Color activeColor;
    private Tile_Impl tile50;
    private int tile50Position = 77;
    private Tile_Impl[][] board;


    public GameState_Impl(Color activeColor, Tile_Impl[][] board, Tile_Impl tile50, ArrayList<Color> playedColors){
        //GamePieces in der Hashmap anlegen
        gamePieces = new HashMap<>();
        for(Color color : playedColors){
            gamePieces.put(color, new GamePiece_Impl(color));
        }

        this.activeColor = activeColor;
        this.board = board;
        this.tile50 = tile50;
    }


    //only for deep-copying an existing GameState_Impl
    public GameState_Impl(HashMap<Color, GamePiece_Impl> gamePiecesNew, Color activeColor, Tile_Impl tile50, int tile50Position, Tile_Impl[][] boardNew){
        this.tile50Position = tile50Position;
        this.activeColor = activeColor;
        this.tile50 = new Tile_Impl(tile50.getType(), tile50.getSymbol(), tile50.getOrientation());
        //deep copy HashMap gamepieces
        this.gamePieces = new HashMap<>();
        for(Map.Entry<Color, GamePiece_Impl> entry : gamePiecesNew.entrySet()){
            GamePiece_Impl gamePiece = new GamePiece_Impl(entry.getKey());
            //copy positions of the gamePieces
            gamePiece.setPosition(entry.getValue().getX(), entry.getValue().getY());
            gamePieces.put(entry.getKey(), gamePiece);
        }

        //deep copy board
        this.board = new Tile_Impl[7][7];
        for(int x = 0; x < 7; x++){
            for(int y = 0; y < 7; y++){
                this.board[x][y] = new Tile_Impl(boardNew[x][y].getType(), boardNew[x][y].getSymbol(), boardNew[x][y].getOrientation());
            }
        }


    }


    @Override
    public GamePiece getGamePiece(Color color) {
        return gamePieces.get(color);
    }

    @Override
    public int getPositionX(Color color) {
        return gamePieces.get(color).getX();
    }

    @Override
    public int getPositionY(Color color) {
        return gamePieces.get(color).getY();
    }

    @Override
    public void setPosition(Color color, int x, int y) {
        gamePieces.get(color).setPosition(x, y);
    }

    @Override
    public Color getActiveColor() {
        return activeColor;
    }

    /**
     * aktualisiert die ActiveColor, d.h.:
     * Red -&gt; Yellow -&gt; Blue -&gt; Green -&gt; Red
     * (nicht teilnehmende Farben werden übersprungen)
     */
    @Override
    public void updateActiveColor() {
        switch(activeColor){
            case Red:
                if(gamePieces.containsKey(Color.Yellow)){
                    activeColor = Color.Yellow;
                    break;
                }
            case Yellow:
                if(gamePieces.containsKey(Color.Blue)){
                activeColor = Color.Blue;
                break;
                }
            case Blue:
                if(gamePieces.containsKey(Color.Green)){
                    activeColor = Color.Green;
                    break;
                }
            case Green:
                if(gamePieces.containsKey(Color.Red)){
                    activeColor = Color.Red;
                }
                else if(gamePieces.containsKey(Color.Yellow)){
                    activeColor = Color.Yellow;
                }
                else{
                    activeColor = Color.Blue;
                }
                break;
        }

    }

    @Override
    public Tile[][] getBoard() {
        return board;
    }

    @Override
    public Tile getTile50() {
        return tile50;
    }

    @Override
    public int getTile50Position() {
        //System.out.println(tile50Position);
        return tile50Position;
    }

    @Override
    public GameState changeBoard(int position, int tile50Orientation) {
        if(position==getTile50Position()) throw new IllegalArgumentException("Das Tile50 kann nicht dort hineingeschoben werden, wo es hinausgeschoben wurde.");
        GameState_Impl gameStateClone = clone();
        gameStateClone.tile50.setOrientation(tile50Orientation);
        Tile_Impl tile50temp = gameStateClone.tile50;
        switch(position){
            case 0:
                //das neue Tile50 wird gespeichert
                gameStateClone.tile50 = board[1][6];
                //die Tiles werden in der entsprechenden Spalte um 1 verschoben
                for(int i = 1; i < 7; i++){
                    gameStateClone.board[1][7-i] = board[1][7-i-1];
                }
                //das alte tile50 wird an der gewünschten Einschubstelle eingeschoben
                gameStateClone.board[1][0] = tile50temp;
                gameStateClone.tile50Position = 8;
                break;
                //andere cases analog
            case 1:
                gameStateClone.tile50 = board[3][6];
                for(int i = 1; i < 7; i++){
                    gameStateClone.board[3][7-i] = board[3][7-i-1];
                }
                gameStateClone.board[3][0] = tile50temp;
                gameStateClone.tile50Position = 7;
                break;
            case 2:
                gameStateClone.tile50 = board[5][6];
                for(int i = 1; i < 7; i++){
                    gameStateClone.board[5][7-i] = board[5][7-i-1];
                }
                gameStateClone.board[5][0] = tile50temp;
                gameStateClone.tile50Position = 6;
                break;

            case 3:
                gameStateClone.tile50 = board[0][1];
                for(int i = 0; i < 6; i++){
                    gameStateClone.board[i][1] = board[i+1][1];
                }
                gameStateClone.board[6][1] = tile50temp;
                gameStateClone.tile50Position = 11;
                break;
            case 4:
                gameStateClone.tile50 = board[0][3];
                for(int i = 0; i < 6; i++){
                    gameStateClone.board[i][3] = board[i+1][3];
                }
                gameStateClone.board[6][3] = tile50temp;
                gameStateClone.tile50Position = 10;
                break;
            case 5:
                gameStateClone.tile50 = board[0][5];
                for(int i = 0; i < 6; i++){
                    gameStateClone.board[i][5] = board[i+1][5];
                }
                gameStateClone.board[6][5] = tile50temp;
                gameStateClone.tile50Position = 9;
                break;

            case 6:
                gameStateClone.tile50 = board[5][0];
                for(int i = 0; i < 6; i++){
                    gameStateClone.board[5][i] = board[5][i+1];
                }
                gameStateClone.board[5][6] = tile50temp;
                gameStateClone.tile50Position = 2;
                break;
            case 7:
                gameStateClone.tile50 = board[3][0];
                for(int i = 0; i < 6; i++){
                    gameStateClone.board[3][i] = board[3][i+1];
                }
                gameStateClone.board[3][6] = tile50temp;
                gameStateClone.tile50Position = 1;
                break;
            case 8:
                gameStateClone.tile50 = board[1][0];
                for(int i = 0; i < 6; i++){
                    gameStateClone.board[1][i] = board[1][i+1];
                }
                gameStateClone.board[1][6] = tile50temp;
                gameStateClone.tile50Position = 0;
                break;

            case 9:
                gameStateClone.tile50 = board[6][5];
                for(int i = 1; i < 7; i++){
                    gameStateClone.board[7-i][5] = board[7-i-1][5];
                }
                gameStateClone.board[0][5] = tile50temp;
                gameStateClone.tile50Position = 5;
                break;
            case 10:
                gameStateClone.tile50 = board[6][3];
                for(int i = 1; i < 7; i++){
                    gameStateClone.board[7-i][3] = board[7-i-1][3];
                }
                gameStateClone.board[0][3] = tile50temp;
                gameStateClone.tile50Position = 4;
                break;
            case 11:
                gameStateClone.tile50 = board[6][1];
                for(int i = 1; i < 7; i++){
                    gameStateClone.board[7-i][1] = board[7-i-1][1];
                }
                gameStateClone.board[0][1] = tile50temp;
                gameStateClone.tile50Position = 3;
                break;
            default: throw new IllegalArgumentException("nur Zahlen zwischen 0 und 11 eingeben. changeBoard()");
        }
        //position ist hier die Einschubposition
        updatePositionGamePieceWithBoard(position, gameStateClone); //Positions der GamePieces aktualisieren
        return gameStateClone;
    }

    private void updatePositionGamePieceWithBoard(int position, GameState_Impl gameStateClone){
        //geht durch alle gamPieces durch und überprüft je nach gewünschter Einschiebeposition ob dieses GamePiece auf der entsprechenden Zeile bzw. Spalte steht und verschiebt es dann um 1
        for(Map.Entry<Color, GamePiece_Impl> entry : gamePieces.entrySet()) {
            GamePiece_Impl gamePiece = entry.getValue();
            GamePiece_Impl gamePieceClone = gameStateClone.gamePieces.get(entry.getKey());
            switch (position) {
                case 0:
                    if(gamePiece.getX() == 1) gamePieceClone.setPosition(gamePiece.getX(), (gamePiece.getY() + 1) % 7);
                    // % 7, für den Fall, dass das GamePiece sonst am Rand des Boards rausfallen würde
                    break;
                case 1:
                    if(gamePiece.getX() == 3) gamePieceClone.setPosition(gamePiece.getX(), (gamePiece.getY() + 1) % 7);
                    break;
                case 2:
                    if(gamePiece.getX() == 5) gamePieceClone.setPosition(gamePiece.getX(), (gamePiece.getY() + 1) % 7);
                    break;
                case 3:
                    if(gamePiece.getY() == 1) gamePieceClone.setPosition((gamePiece.getX() + 6) % 7, gamePiece.getY()); //+6 % 7 entspricht -1
                    break;
                case 4:
                    if(gamePiece.getY() == 3) gamePieceClone.setPosition((gamePiece.getX() + 6) % 7, gamePiece.getY());
                    break;
                case 5:
                    if(gamePiece.getY() == 5) gamePieceClone.setPosition((gamePiece.getX() + 6) % 7, gamePiece.getY());
                    break;
                case 6:
                    if(gamePiece.getX() == 5) gamePieceClone.setPosition(gamePiece.getX(), (gamePiece.getY() + 6) % 7);
                    break;
                case 7:
                    if(gamePiece.getX() == 3) gamePieceClone.setPosition(gamePiece.getX(), (gamePiece.getY() + 6) % 7);
                    break;
                case 8:
                    if(gamePiece.getX() == 1) gamePieceClone.setPosition(gamePiece.getX(), (gamePiece.getY() + 6) % 7);
                    break;
                case 9:
                    if(gamePiece.getY() == 5) gamePieceClone.setPosition((gamePiece.getX() + 1) % 7, gamePiece.getY());
                    break;
                case 10:
                    if(gamePiece.getY() == 3) gamePieceClone.setPosition((gamePiece.getX() + 1) % 7, gamePiece.getY());
                    break;
                case 11:
                    if(gamePiece.getY() == 1) gamePieceClone.setPosition((gamePiece.getX() + 1) % 7, gamePiece.getY());
                    break;
            }
        }

    }

    @Override
    public GameState changeBoardGamePiece(Color color, int x, int y) {
        GameState_Impl gameStateClone = clone();
        gameStateClone.setPosition(color, x, y);
        return gameStateClone;
    }

    @Override
    public int[] getPositionOfSymbol(Symbol symbol) {
        int[] coordinates = new int[2];
        // nötig, falls das gesuchte Symbol beim Aufrufen tile50 ist
        // sonst wäre x == 0 und y == 0 und man könnte das tile50-Symbol beim (0,0) einsammeln
        coordinates[0] = 77;
        coordinates[1] = 77;
        for(int x = 0; x < 7; x++){
            for(int y = 0; y < 7; y++){
                if(board[x][y].getSymbol() == symbol){
                    coordinates[0] = x;
                    coordinates[1] = y;
                }
            }
        }
        return coordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameState_Impl that = (GameState_Impl) o;
        return  tile50Position == that.tile50Position &&
                gamePieces.equals(that.gamePieces) &&
                activeColor == that.activeColor &&
                Objects.equals(tile50, that.tile50) &&
                Arrays.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(gamePieces, activeColor, tile50, tile50Position);
        result = 31 * result + Arrays.deepHashCode(board);
        return result;
    }


    @Override
    public String toString(){
        String s = "";
        s = s + "Tile50: |" + tile50 + "| Position: " + tile50Position + "  Orientation: " + tile50.getOrientation() ;
        for(int y = 0; y < 7; y++){
            s = s + (" ") + "\n";
            for(int x = 0; x < 7; x++){
                s = s + (" | " + getBoard()[x][y]  + " | ");
            }
        }
        return s;
    }

    public GameState_Impl clone(){
        return new GameState_Impl(gamePieces, activeColor, tile50, tile50Position, board);
    }
}
