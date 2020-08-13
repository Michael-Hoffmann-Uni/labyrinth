package model;

/**
 * Zählt die drei Arten von Tiles auf.
 */
public enum TileType {
    Crossway("../images/tile-crossway.png"),
    Way("../images/tile-way.png"),
    Edge("../images/tile-edge.png");

    TileType(String imagePath) {
        this.imagePath = imagePath;
    }

    private String imagePath;

    public String getImagePath() {
        return imagePath;
    }
}
