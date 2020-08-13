package view;

/**
 * Launcher-Class.
 * Needed for use as shaded-jar, otherwise errors occur due to non available dependencies
 * main-class must not extend javafx.Application!
 */
public class Launcher {
    public static void main(String[] args) {
        GUI.main(args);
    }
}
