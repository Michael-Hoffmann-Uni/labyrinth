package view;

import database.DatabaseAccess;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

/**
 * This is the controller for dbSettings.fxml,
 * it is used to change database settings (host, user, pwd, ...) in a form
 * and save the information fitting to the user OS.
 */
public class DBSettingsController {
    // isAutoCall gets set to true in constructor if db-settings got called automatically by system, otherwise false
    boolean isAutoCall;
    DatabaseAccess dbAccess;
    // db-settings that are stored in dbAccess
    Preferences prefs = null;

    @FXML
    private VBox vBoxWarning;
    @FXML
    private Label lblWarning1;
    @FXML
    private Label lblWarning2;
    @FXML
    private Label lblConSuccess;
    @FXML
    private TextField txtProtocol, txtIPAddress, txtPort, txtDBName, txtUser;
    @FXML
    private PasswordField pwdPass;
    @FXML
    private Button btnCancel, btnOK, btnTestCon;

    /**
     * The constructor which receives a DatabaseAccess object and information about whether it got called by a user or the system.
     *
     * @param dbAccess the DatabaseAccess object for this instance of DBSettingsController
     * @param isAutoCall this specifies if the DBSettings dialog got called by system or user
     *                   (isAutoCall == true when called by system)
     */
    public DBSettingsController(DatabaseAccess dbAccess, boolean isAutoCall) {
        // need dbAccess in constructor because we need it already in initialize
        this.dbAccess = dbAccess;
        // isAutoCall = true means not a user but the system opened the window
        this.isAutoCall = isAutoCall;
    }

    /**
     * Prepares the GUI on startup.
     */
    public void initialize() {
        // set labels for status message according to last connection test
        // since we test right when the app starts and before this window
        // automatically opens we don't have to test the connection again
        // not checking db connection twice SAVES A LOT of startup time
        if (isAutoCall) {
            setLabelsRed();
        } else {
            setLabelsInvis();
        }
        // get server preferences
        prefs = dbAccess.getServerPrefs();

        // fill mask with preferences from dbAccess
        txtProtocol.setText(prefs.get("protocol", ""));
        txtIPAddress.setText(prefs.get("db_url", ""));
        txtPort.setText(prefs.get("port", ""));
        txtDBName.setText(prefs.get("db_name", ""));
        txtUser.setText(prefs.get("user", ""));
        pwdPass.setText(prefs.get("pass", ""));

        // set focus on btnTestCon
        // runLater because the scene graph has to be constructed completely first
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                btnTestCon.requestFocus();
            }
        });
    }

    /**
     * Tests the database connection and sets warning/information labels accordingly.
     */
    public void clickTestCon() {
        // test connection and set labels for status message according to connection test
        // if test is successful: check and create tables
        if (dbAccess.testCon(txtProtocol.getText(), txtIPAddress.getText(),
                txtPort.getText(), txtDBName.getText(),
                txtUser.getText(), pwdPass.getText())) {
            dbAccess.openConn();
            if (!dbAccess.checkTables()) {
                showAlertCheckTablesFail();
            }
            dbAccess.closeConn();
            setLabelsGreen();
        } else {
            setLabelsRed();
        }
    }

    /**
     * Tries to set the value from textBoxes as new server preferences.
     * Also automatically tests the connection with those information.
     * Sets waring/information labels accordingly.
     */
    public void clickOK() {
        // tries to set preferences (which includes a connection test)
        // on success: save prefs and close. on fail: show error labels
        if (dbAccess.setServerPrefs(txtProtocol.getText(), txtIPAddress.getText(),
                txtPort.getText(), txtDBName.getText(),
                txtUser.getText(), pwdPass.getText())) {
            Stage stage = (Stage) btnOK.getScene().getWindow();
            stage.close();
        } else {
            setLabelsRed();
        }
    }

    /**
     * Closes the stage without saving any information.
     */
    public void clickCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /**
     * An alert that shows up when checkTables() fails (is false).
     */
    private void showAlertCheckTablesFail() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database error");

        // Header Text: null
        alert.setHeaderText("Database schema test failed.");
        alert.setContentText("The schema of the current database seems to be corrupted and could not be fixed automatically.\n\n" +
                "To completely reset the database go to: \nStatistics -> Completely reset database.");
        alert.showAndWait();
    }

    /**
     * Sets only warning labels for unsuccessful db-connection visible.
     */
    public void setLabelsRed() {
        vBoxWarning.setStyle("-fx-border-color: #c61717;");
        lblWarning1.setVisible(true);
        lblWarning2.setVisible(true);
        lblConSuccess.setVisible(false);
    }

    /**
     * Sets only warning labels for successful db-connection visible.
     */
    public void setLabelsGreen() {
        vBoxWarning.setStyle("-fx-border-style: none");
        lblWarning1.setVisible(false);
        lblWarning2.setVisible(false);
        lblConSuccess.setVisible(true);
    }

    /**
     * Sets all labels invisible - e.g. when user opens db-settings manually.
     */
    public void setLabelsInvis() {
        vBoxWarning.setStyle("-fx-border-style: none");
        lblWarning1.setVisible(false);
        lblWarning2.setVisible(false);
        lblConSuccess.setVisible(false);
    }
}
