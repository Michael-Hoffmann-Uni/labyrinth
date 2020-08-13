package model;

/**
 * Zählt die vier spielbaren Farben auf.
 */
public enum Color {
    Red(javafx.scene.paint.Color.RED),
    Yellow(javafx.scene.paint.Color.YELLOW),
    Green(javafx.scene.paint.Color.LIMEGREEN),
    Blue(javafx.scene.paint.Color.DODGERBLUE);

    private javafx.scene.paint.Color color;

    Color (javafx.scene.paint.Color color) {
        this.color = color;
    }

    public javafx.scene.paint.Color getColor() {
        return color;
    }
}
