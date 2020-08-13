package model;

import java.util.Objects;

public class GamePiece_Impl implements GamePiece{
    private Color color;
    private int x;
    private int y;

    public GamePiece_Impl(Color color){
        this.color = color;
        switch(color){
            case Red:
                x = 0;
                y = 0;
                break;
            case Yellow:
                x = 6;
                y = 0;
                break;
            case Green:
                x = 0;
                y = 6;
                break;
            case Blue:
                x = 6;
                y = 6;
                break;
            default:
                break;
        }
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GamePiece_Impl that = (GamePiece_Impl) o;
        return x == that.x &&
                y == that.y &&
                color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, x, y);
    }
}
