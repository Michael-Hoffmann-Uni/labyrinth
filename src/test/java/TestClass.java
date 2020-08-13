
import static org.junit.Assert.*;

import logic.*;
import model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


public class TestClass {

    private ArrayList<Tile> getAllTiles(){
        ArrayList<Tile> allTilesExpected = new ArrayList<>(Arrays.asList(
                //fixed Tiles
                new Tile_Impl(TileType.Edge, Symbol.StartingPointRed, 0),
                new Tile_Impl(TileType.Crossway, Symbol.Book, 2),
                new Tile_Impl(TileType.Crossway, Symbol.Coin, 2),
                new Tile_Impl(TileType.Edge, Symbol.StartingPointYellow, 1),

                new Tile_Impl(TileType.Crossway, Symbol.Map, 1),
                new Tile_Impl(TileType.Crossway, Symbol.Crown, 1),
                new Tile_Impl(TileType.Crossway, Symbol.Keys, 2),
                new Tile_Impl(TileType.Crossway, Symbol.Skull, 3),

                new Tile_Impl(TileType.Crossway, Symbol.Ring, 1),
                new Tile_Impl(TileType.Crossway, Symbol.Treasure, 0),
                new Tile_Impl(TileType.Crossway, Symbol.Emerald, 3),
                new Tile_Impl(TileType.Crossway, Symbol.Sword, 3),

                new Tile_Impl(TileType.Edge, Symbol.StartingPointGreen, 3),
                new Tile_Impl(TileType.Crossway, Symbol.Candleholder, 0),
                new Tile_Impl(TileType.Crossway, Symbol.Helmet, 0),
                new Tile_Impl(TileType.Edge, Symbol.StartingPointBlue, 2)
        ));

        //unfixed Tiles with Symbols
        allTilesExpected.add(new Tile_Impl(TileType.Crossway, Symbol.Bat, 0));
        allTilesExpected.add(new Tile_Impl(TileType.Crossway, Symbol.Goblin, 0));
        allTilesExpected.add(new Tile_Impl(TileType.Crossway, Symbol.Fairy, 0));
        allTilesExpected.add(new Tile_Impl(TileType.Crossway, Symbol.Genie, 0));
        allTilesExpected.add(new Tile_Impl(TileType.Crossway, Symbol.Ghost, 0));
        allTilesExpected.add(new Tile_Impl(TileType.Crossway, Symbol.Dragon, 0));

        allTilesExpected.add(new Tile_Impl(TileType.Edge, Symbol.Beetle, 0));
        allTilesExpected.add(new Tile_Impl(TileType.Edge, Symbol.Spider, 0));
        allTilesExpected.add(new Tile_Impl(TileType.Edge, Symbol.Moth, 0));
        allTilesExpected.add(new Tile_Impl(TileType.Edge, Symbol.Salamander, 0));
        allTilesExpected.add(new Tile_Impl(TileType.Edge, Symbol.Mouse, 0));
        allTilesExpected.add(new Tile_Impl(TileType.Edge, Symbol.Owl, 0));

        //unfixed Tiles without Symbols
        for(int i = 0; i < 9; i++){
            allTilesExpected.add(new Tile_Impl(TileType.Edge, Symbol.None,0));
        }
        for(int i = 0; i < 13; i++){
            allTilesExpected.add(new Tile_Impl(TileType.Way, Symbol.None, 0));
        }
        return allTilesExpected;
    }

    private ArrayList<Symbol> getAllSymbolsWithoutStarts(){
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

        return deckOfCards;
    }

    /**
     * tests if the method getInitialGameStateRandom in Rules_Impl contains all Tiles
     */
    @Test
    public void getInitialGameStateRandomTest(){
        ArrayList<Tile> allTilesExpected = getAllTiles();


        ArrayList<Tile> allTilesActual = new ArrayList<>();

        Rules_Impl rules = new Rules_Impl(new ArrayList<Color>(Arrays.asList(Color.Yellow, Color.Red, Color.Blue, Color.Green)));
        GameState initialRandomGameState = rules.getInitialGameStateRandom();
        allTilesActual.add(initialRandomGameState.getTile50());
        Tile[][] board = initialRandomGameState.getBoard();

        for (Tile[] tiles : board) {
            allTilesActual.addAll(Arrays.asList(tiles));
        }

        for(Tile t : allTilesActual){
            t.setOrientation(0);
        }
        for(Tile t : allTilesExpected){
            t.setOrientation(0);
        }

        HashSet<Tile> allTilesExpectedSet = new HashSet<>(allTilesExpected);
        HashSet<Tile> allTilesActualSet = new HashSet<>(allTilesActual);

        assertEquals("initialGameStateRandom does not contain all Tiles. The size is different.", allTilesExpected.size(), allTilesActual.size());
        assertEquals("initialGameStateRandom does not contain all Tiles. Different Elements", allTilesExpectedSet, allTilesActualSet);
    }

    @Test
    public void testDrawCardContainsAllSymbols(){
        HashSet<Symbol> expectedSymbols = new HashSet<>(getAllSymbolsWithoutStarts());
        Rules_Impl rules = new Rules_Impl(new ArrayList<Color>(Arrays.asList(Color.Yellow, Color.Red, Color.Blue, Color.Green)));
        HashSet<Symbol> actualSymbols = drawAllCards(rules);
        assertEquals("Rules does not contain all Symbols for four Players.", expectedSymbols, actualSymbols);

        rules = new Rules_Impl(new ArrayList<Color>(Arrays.asList(Color.Yellow, Color.Red, Color.Blue)));
        actualSymbols = drawAllCards(rules);
        assertEquals("Rules does not contain all Symbols for three Players.", expectedSymbols, actualSymbols);

        rules = new Rules_Impl(new ArrayList<Color>(Arrays.asList(Color.Yellow, Color.Red)));
        actualSymbols = drawAllCards(rules);
        assertEquals("Rules does not contain all Symbols for two Players.", expectedSymbols, actualSymbols);

        rules = new Rules_Impl(new ArrayList<Color>(Arrays.asList(Color.Blue, Color.Green)));
        actualSymbols = drawAllCards(rules);
        assertEquals("Rules does not contain all Symbols for two Players.", expectedSymbols, actualSymbols);

    }

    private HashSet<Symbol> drawAllCards(Rules_Impl rules){
        HashSet<Symbol> actualSymbols = new HashSet<>();
        for(Color color : rules.getPlayedColors()){
            int numberOfSymbols = 24/rules.getPlayedColors().size();
            for(int i = 0; i < numberOfSymbols ; i++) {
                actualSymbols.add(rules.drawCard(color));
            }
        }
        return actualSymbols;
    }

    /**
     * tests if the constructor Rules_Impl throws IllegalArgumentException for multiple Colors
     */
    @Test(expected = IllegalArgumentException.class)
    public void testForIllegalArgumentExceptionRulesColorTwoTimes(){
        Rules_Impl rules = new Rules_Impl((new ArrayList<Color>(Arrays.asList(Color.Yellow, Color.Yellow))));
    }

    /**
     * tests if the constructor Rules_Impl throws IllegalArgumentException for less than two Colors
     */
    @Test(expected = IllegalArgumentException.class)
    public void testForIllegalArgumentExceptionRulesLessThanTwoColors(){
        Rules_Impl rules = new Rules_Impl((new ArrayList<Color>(Arrays.asList(Color.Yellow))));
    }

    /**
     * tests if start in Game_Impl throws Exception when started without Players
     */
    @Test(expected = IllegalArgumentException.class)
    public void testStartGameWithoutPlayers(){
        Game_Impl game = new Game_Impl();
        Rules_Impl rules = new Rules_Impl((new ArrayList<Color>(Arrays.asList(Color.Yellow, Color.Red))));
        game.start(rules.getInitialGameStateRandom());
    }

    /**
     * tests if start in Game_Impl throws Exception when started without 1 Player
     */
    @Test(expected = IllegalArgumentException.class)
    public void testStartGameWithOnePlayer(){
        Game_Impl game = new Game_Impl();
        Rules_Impl rules = new Rules_Impl((new ArrayList<Color>(Arrays.asList(Color.Yellow, Color.Red))));
        game.join(new PlayerPhysical(Color.Red), Color.Red);
        game.start(rules.getInitialGameStateRandom());
    }

    /**
     * tests the insertion of a Tile in GameState
     */
    @Test
    public void testChangeBoard(){
        Rules_Impl rules = new Rules_Impl((new ArrayList<Color>(Arrays.asList(Color.Yellow, Color.Red))));
        GameState gameState = rules.getTestGameStateFixed();
        gameState = gameState.changeBoard(9,2);
        assertEquals("changeBoard in GameState_Impl does not work properly", "Tile50: |Edg Non 3| Position: 5  Orientation: 3 \n" +
                " | Edg Sta 0 |  | Way Non 1 |  | Cro Boo 2 |  | Way Non 2 |  | Cro Coi 2 |  | Edg Mot 3 |  | Edg Sta 1 |  \n" +
                " | Edg Non 1 |  | Cro Gob 2 |  | Cro Bat 3 |  | Edg Non 2 |  | Way Non 0 |  | Cro Fai 1 |  | Edg Non 3 |  \n" +
                " | Cro Map 1 |  | Edg Sal 2 |  | Cro Cro 1 |  | Way Non 2 |  | Cro Key 2 |  | Way Non 1 |  | Cro Sku 3 |  \n" +
                " | Way Non 2 |  | Edg Non 0 |  | Way Non 1 |  | Edg Bee 1 |  | Way Non 0 |  | Way Non 3 |  | Cro Dra 2 |  \n" +
                " | Cro Rin 1 |  | Way Non 3 |  | Cro Tre 0 |  | Way Non 2 |  | Cro Eme 3 |  | Way Non 0 |  | Cro Swo 3 |  \n" +
                " | Edg Non 2 |  | Edg Mou 1 |  | Cro Gho 3 |  | Edg Owl 0 |  | Cro Gen 0 |  | Way Non 3 |  | Edg Spi 0 |  \n" +
                " | Edg Sta 3 |  | Edg Non 0 |  | Cro Can 0 |  | Edg Non 3 |  | Cro Hel 0 |  | Edg Non 2 |  | Edg Sta 2 | ",
                ((GameState_Impl) gameState).toString()
                );
        gameState = gameState.changeBoard(2,3);
        assertEquals("changeBoard in GameState_Impl does not work properly", "Tile50: |Edg Non 2| Position: 6  Orientation: 2 \n" +
                        " | Edg Sta 0 |  | Way Non 1 |  | Cro Boo 2 |  | Way Non 2 |  | Cro Coi 2 |  | Edg Non 3 |  | Edg Sta 1 |  \n" +
                        " | Edg Non 1 |  | Cro Gob 2 |  | Cro Bat 3 |  | Edg Non 2 |  | Way Non 0 |  | Edg Mot 3 |  | Edg Non 3 |  \n" +
                        " | Cro Map 1 |  | Edg Sal 2 |  | Cro Cro 1 |  | Way Non 2 |  | Cro Key 2 |  | Cro Fai 1 |  | Cro Sku 3 |  \n" +
                        " | Way Non 2 |  | Edg Non 0 |  | Way Non 1 |  | Edg Bee 1 |  | Way Non 0 |  | Way Non 1 |  | Cro Dra 2 |  \n" +
                        " | Cro Rin 1 |  | Way Non 3 |  | Cro Tre 0 |  | Way Non 2 |  | Cro Eme 3 |  | Way Non 3 |  | Cro Swo 3 |  \n" +
                        " | Edg Non 2 |  | Edg Mou 1 |  | Cro Gho 3 |  | Edg Owl 0 |  | Cro Gen 0 |  | Way Non 0 |  | Edg Spi 0 |  \n" +
                        " | Edg Sta 3 |  | Edg Non 0 |  | Cro Can 0 |  | Edg Non 3 |  | Cro Hel 0 |  | Way Non 3 |  | Edg Sta 2 | ",
                ((GameState_Impl) gameState).toString()
                );
        gameState = gameState.changeBoard(3,0);
        assertEquals("changeBoard in GameState_Impl does not work properly", "Tile50: |Edg Non 1| Position: 11  Orientation: 1 \n" +
                        " | Edg Sta 0 |  | Way Non 1 |  | Cro Boo 2 |  | Way Non 2 |  | Cro Coi 2 |  | Edg Non 3 |  | Edg Sta 1 |  \n" +
                        " | Cro Gob 2 |  | Cro Bat 3 |  | Edg Non 2 |  | Way Non 0 |  | Edg Mot 3 |  | Edg Non 3 |  | Edg Non 0 |  \n" +
                        " | Cro Map 1 |  | Edg Sal 2 |  | Cro Cro 1 |  | Way Non 2 |  | Cro Key 2 |  | Cro Fai 1 |  | Cro Sku 3 |  \n" +
                        " | Way Non 2 |  | Edg Non 0 |  | Way Non 1 |  | Edg Bee 1 |  | Way Non 0 |  | Way Non 1 |  | Cro Dra 2 |  \n" +
                        " | Cro Rin 1 |  | Way Non 3 |  | Cro Tre 0 |  | Way Non 2 |  | Cro Eme 3 |  | Way Non 3 |  | Cro Swo 3 |  \n" +
                        " | Edg Non 2 |  | Edg Mou 1 |  | Cro Gho 3 |  | Edg Owl 0 |  | Cro Gen 0 |  | Way Non 0 |  | Edg Spi 0 |  \n" +
                        " | Edg Sta 3 |  | Edg Non 0 |  | Cro Can 0 |  | Edg Non 3 |  | Cro Hel 0 |  | Way Non 3 |  | Edg Sta 2 | ",
                ((GameState_Impl) gameState).toString()
        );
        gameState = gameState.changeBoard(6,1);
        assertEquals("changeBoard in GameState_Impl does not work properly", "Tile50: |Edg Non 3| Position: 2  Orientation: 3 \n" +
                        " | Edg Sta 0 |  | Way Non 1 |  | Cro Boo 2 |  | Way Non 2 |  | Cro Coi 2 |  | Edg Non 3 |  | Edg Sta 1 |  \n" +
                        " | Cro Gob 2 |  | Cro Bat 3 |  | Edg Non 2 |  | Way Non 0 |  | Edg Mot 3 |  | Cro Fai 1 |  | Edg Non 0 |  \n" +
                        " | Cro Map 1 |  | Edg Sal 2 |  | Cro Cro 1 |  | Way Non 2 |  | Cro Key 2 |  | Way Non 1 |  | Cro Sku 3 |  \n" +
                        " | Way Non 2 |  | Edg Non 0 |  | Way Non 1 |  | Edg Bee 1 |  | Way Non 0 |  | Way Non 3 |  | Cro Dra 2 |  \n" +
                        " | Cro Rin 1 |  | Way Non 3 |  | Cro Tre 0 |  | Way Non 2 |  | Cro Eme 3 |  | Way Non 0 |  | Cro Swo 3 |  \n" +
                        " | Edg Non 2 |  | Edg Mou 1 |  | Cro Gho 3 |  | Edg Owl 0 |  | Cro Gen 0 |  | Way Non 3 |  | Edg Spi 0 |  \n" +
                        " | Edg Sta 3 |  | Edg Non 0 |  | Cro Can 0 |  | Edg Non 3 |  | Cro Hel 0 |  | Edg Non 1 |  | Edg Sta 2 | ",
                ((GameState_Impl) gameState).toString()
        );


    }

    /**
     * tests if the PlayerKI's actions are valid
     * if a new AI Player is implemented the class in this test can be changed accordingly
     */
    @Test
    public void testKIValidActions(){
        ArrayList<Color> playedColors = new ArrayList<>(Arrays.asList(Color.Yellow, Color.Red, Color.Blue, Color.Green));
        Rules_Impl rules = new Rules_Impl(playedColors);
        GameState gameState = rules.getInitialGameStateRandom();
        Player playerKI;
        for(Color color : playedColors) {
            playerKI = new PlayerKI(color); //put your new Class here
            playerKI.setRules(rules);
            playerKI.updateGameState(gameState);
            assertTrue("requestActionTile() in PlayerKI is not working properly.", rules.getActionsTileMove(color, gameState).contains(playerKI.requestActionTile()));
            gameState = playerKI.requestActionTile();
            playerKI.updateGameState(gameState);
            assertTrue("requestActionTile() in PlayerKI is not working properly.", rules.getActionsGamePieceMove(color, gameState).contains(playerKI.requestActionGamePiece()));
        }
    }

    @Test
    public void testCoordinatesOfTilesInRange(){
        ArrayList<Color> playedColors = new ArrayList<>(Arrays.asList(Color.Yellow, Color.Red, Color.Blue, Color.Green));
        Rules_Impl rules = new Rules_Impl(playedColors);
        HashSet<int[]> expected = new HashSet<>();
        expected.add(new int[]{4, 2});
        expected.add(new int[]{3, 6});
        expected.add(new int[]{3, 4});
        expected.add(new int[]{1, 2});
        expected.add(new int[]{4, 5});
        expected.add(new int[]{4, 4});
        expected.add(new int[]{2, 3});
        expected.add(new int[]{4, 6});
        expected.add(new int[]{0, 1});
        expected.add(new int[]{2, 4});
        expected.add(new int[]{5, 5});
        expected.add(new int[]{1, 1});
        expected.add(new int[]{2, 1});
        expected.add(new int[]{3, 0});
        expected.add(new int[]{5, 6});
        expected.add(new int[]{4, 0});
        expected.add(new int[]{2, 2});
        expected.add(new int[]{3, 2});
        expected.add(new int[]{2, 0});
        expected.add(new int[]{0, 2});

        assertTrue("coordinatesOfTilesInRange in Rules_Impl does not work.", setsOfArraysEqual(expected,rules.coordinatesOfTilesInRange(2,1,rules.getTestGameStateFixed().getBoard())));
    }

    private boolean setsOfArraysEqual(HashSet<int[]> expected, HashSet<int[]> actual){
        HashSet<String> expectedString = new HashSet<>();
        HashSet<String> actualString = new HashSet<>();
        for(int[] a : expected){
            expectedString.add(""+a[0] +a[1]);
        }
        for(int[] a : actual){
            actualString.add(""+a[0] +a[1]);
        }
        return expectedString.equals(actualString);
    }

}
