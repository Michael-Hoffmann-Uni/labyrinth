package model;

/**
 * Zählt die Symbole, die einzusammeln sind und sich auf den einzelnen Tiles befinden auf.
 */
public enum Symbol {
    None(false, ""),

    StartingPointRed(true, TileType.Edge, ""),
    StartingPointYellow(true, TileType.Edge, ""),
    StartingPointBlue(true, TileType.Edge,""),
    StartingPointGreen(true, TileType.Edge,""),

    Owl(false, TileType.Edge,"../images/symbol-owl.png"),
    Mouse(false, TileType.Edge,"../images/symbol-mouse.png"),
    Salamander(false, TileType.Edge,"../images/symbol-salamander.png"),
    Moth(false, TileType.Edge,"../images/symbol-moth.png"),
    Spider(false, TileType.Edge,"../images/symbol-spider.png"),
    Beetle(false, TileType.Edge,"../images/symbol-beetle.png"),

    Book(true, TileType.Crossway,"../images/symbol-book.png"),
    Coin(true, TileType.Crossway,"../images/symbol-coin.png"),
    Map(true, TileType.Crossway,"../images/symbol-map.png"),
    Crown(true, TileType.Crossway,"../images/symbol-crown.png"),
    Keys(true, TileType.Crossway,"../images/symbol-keys.png"),
    Skull(true, TileType.Crossway,"../images/symbol-skull.png"),
    Ring(true, TileType.Crossway,"../images/symbol-ring.png"),
    Treasure(true, TileType.Crossway,"../images/symbol-treasure.png"),
    Emerald(true, TileType.Crossway,"../images/symbol-emerald.png"),
    Sword(true, TileType.Crossway,"../images/symbol-sword.png"),
    Candleholder(true, TileType.Crossway,"../images/symbol-candleholder.png"),
    Helmet(true, TileType.Crossway,"../images/symbol-helmet.png"),

    Bat(false, TileType.Crossway,"../images/symbol-bat.png"),
    Goblin(false, TileType.Crossway,"../images/symbol-goblin.png"),
    Fairy(false, TileType.Crossway,"../images/symbol-fairy.png"),
    Genie(false, TileType.Crossway,"../images/symbol-genie.png"),
    Ghost(false, TileType.Crossway,"../images/symbol-ghost.png"),
    Dragon(false, TileType.Crossway,"../images/symbol-dragon.png");


    private boolean fixed;
    private TileType tileType;
    private String imagePath;

    Symbol(boolean fixed, String imagePath){
        this.fixed = fixed;
        this.imagePath = imagePath;
    }

    Symbol(boolean fixed, TileType tileType, String imagePath){
        this.fixed = fixed;
        this.tileType = tileType;
        this.imagePath = imagePath;
    }

    public boolean isFixed(){
        return fixed;
    }

    public TileType getTileType(){
        return tileType;
    }

    public String getImagePath() {
        return imagePath;
    }
}