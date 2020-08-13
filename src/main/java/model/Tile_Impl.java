package model;

import java.util.Objects;

public class Tile_Impl implements Tile {
    private TileType tileType;
    private int orientation;
    private Symbol symbol;

    public Tile_Impl(TileType tileType, Symbol symbol, int orientation){
        this.orientation = orientation;
        this.symbol = symbol;
        this.tileType = tileType;
    }

    @Override
    public int getOrientation() {
        return orientation;
    }

    @Override
    public void setOrientation(int orientation) {
        if(orientation < 0 || orientation > 3) throw new IllegalArgumentException("Bitte nur Orientations zwischen 0 und 3 eingeben!");
        this.orientation = orientation;
    }

    @Override
    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public TileType getType() {
        return tileType;
    }

    @Override
    public void turnRight() {
        orientation = (orientation + 1) % 4;
    }

    @Override
    public void turnLeft() {
        orientation = (orientation + 3) % 4;
    }

    @Override
    public String toString() {
        /*return "Tile_Impl{" +
                "tileType=" + tileType +
                ", orientation=" + orientation +
                ", symbol=" + symbol +
                '}';

         */
        return "" + tileType.toString().substring(0,3) +
                " " + symbol.toString().substring(0,3) +
                " " + orientation;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile_Impl tile_ = (Tile_Impl) o;
        return orientation == tile_.orientation &&
                tileType == tile_.tileType &&
                symbol == tile_.symbol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tileType, orientation, symbol);
    }
}
