package database;

import logic.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles connections, statements and queries and also holds
 * methods to reset database and automatically create tables.
 */
public class DatabaseAccess {

    /**
     * The connection of this DatabaseAccess object.
     */
    private Connection conn;
    /**
     * A object that stores data at a location dependant on the users OS to open a connection based on the users preferences.
     */
    private static Preferences prefs;
    /**
     * A boolean that holds information about whether the last connection test was successful or not.
     */
    private boolean lastConTest;

//    public static void main(String[] args) {
//        //create DatabaseAccess object
//        DatabaseAccess dbAccess = new DatabaseAccess();
//
//        //open connection
//        System.out.println(dbAccess.openConn());
//
//        //close connection
//        System.out.println(dbAccess.closeConn());
//
//        /* ZEITSTEMPEL
//        System.out.println("#### " + this.getClass() + ": method " + new Object(){}.getClass().getEnclosingMethod().getName() + "() is running now at line XXXX. Time: " + java.time.Clock.systemUTC().instant());
//         */

//        // Example: set preferences
//        System.out.println("setPreferences Test: " + dbAccess.setServerPrefs("mysql",
//                "localhost",
//                "3306",
//                "laby",
//                "root",
//                "root123"));
//        //read .ini file
//        dbAccess.readINI();
        //test create table if not exist
//        dbAccess.insertSQL("CREATE TABLE IF NOT EXISTS TEST " +
//                            "( id INTEGER not NULL AUTO_INCREMENT, " +
//                            " name VARCHAR(255), "+
//                            " PRIMARY KEY ( id ))");

//        //Example: insert new user into database (table players)
//        dbAccess.insertSQL("INSERT INTO players (name, description) VALUES ('Joni', 'our customer')");
//
//        //Example: send query (automatically closes statement)
//        ArrayList<Map<String, Object>> resultList = dbAccess.selectSQL("Select * from players");
//
//        // Example: looping through all datasets
//        for(Map<String, Object> row : resultList){
//            int id = (int) row.get("id");
//            String name = (String) row.get("name");
//            String description = (String) row.get("description");
//            System.out.println("ID: " + id);
//            System.out.println("Name: " + name);
//            System.out.println("Description: " + description);
//        }
//        //Example: access single information (dataset: 0; field: name)
//        String justName = (String) resultList.get(0).get("name");
//        System.out.println("\nGoodbye - your " + justName);
//    }

    /**
     * The constructor that sets timezone and enables allowMultiQueries for the database, also sets OS dependent location to save db-connection data.
     */
    public DatabaseAccess() {
        // when initialized there was no connection test before -> lastConTest = false
        lastConTest = false;
        conn = null; //initialize connection with null
        prefs = Preferences.userRoot().node((this.getClass().getName())); //define a node in which the preferences can be stored
        //set timezone
        prefs.put("timezone", "useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Berlin");
        prefs.put("allowMultiQueries", "allowMultiQueries=true");
    }

    /**
     * Opens a connection with the information stored in preferences and handles exceptions.
     *
     * @return true if connection can be established, otherwise false.
     */
    public boolean openConn() {
        conn = null;
        try {
            String conString = "jdbc:" + prefs.get("protocol", "") + ":" +
                    "//" + prefs.get("db_url", "") +
                    ":" + prefs.get("port", "") +
                    "/" + prefs.get("db_name", "") +
                    "?" + prefs.get("timezone", "") +
                    "&" + prefs.get("allowMultiQueries", "");
            conn = DriverManager.getConnection(conString, prefs.get("user", ""), prefs.get("pass", ""));
            return true;
        } catch (SQLException se) {
            //handle JDBC errors
            //se.printStackTrace();
            return false;
        }
    }

    /**
     * Opens a connection with the information given by its parameters and handles exceptions.
     *
     * @param protocol  the protocol used for the database connection, e.g. mysql
     * @param db_url    the ip-address of the database server
     * @param port      the port used, e.g. 3306
     * @param db_name   the name of the database
     * @param user      the username
     * @param pass      the password
     * @return true if connection can be established, otherwise false.
     */
    public boolean openConn(String protocol, String db_url, String port, String db_name, String user, String pass) {
        conn = null;
        try {
            String conString = "jdbc:" + protocol + ":" +
                    "//" + db_url +
                    ":" + port +
                    "/" + db_name +
                    "?" + prefs.get("timezone", "") +
                    "&" + prefs.get("allowMultiQueries", "");
            conn = DriverManager.getConnection(conString, user, pass);
            return true;
        } catch (SQLException se) {
            //handle JDBC errors
            //se.printStackTrace();
            return false;
        }
    }

    /**
     * Closes the connection and handles exceptions.
     *
     * @return  true if connection can be closed, otherwise false
     */
    public boolean closeConn() {
        //close connection
        try {
            if (conn != null)
                conn.close();
            return true;
        } catch (SQLException se) {
            //se.printStackTrace();
            return false;
        }
    }

    /**
     * Provides a comfortable way to send select-queries. Gets results as ArrayList.
     * Handles exceptions and closes the statement.
     *
     * @param sql the query for the statement
     * @return null if exceptions get catched, otherwise a ArrayList with HashMaps as elements for each
     * dataset received from the database
     */
    public ArrayList<Map<String, Object>> selectSQL(String sql) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<Map<String, Object>> resultList = resultSetToArrayList(rs);
            if (stmt != null)
                stmt.close();
            return (ArrayList<Map<String, Object>>) resultList;
        } catch (SQLException stmtE) {
            //stmtE.printStackTrace();
            return null;
        } catch (NullPointerException nE) {
            return null;
        }
    }

    /**
     * This is the method to use for executing statements (e.g. drop table, create table etc.).
     * Handles exceptions and closes the statement.
     *
     * @param sql the query for the statement
     * @return false if exceptions get catched, otherwise true
     */
    public boolean executeSQL(String sql) {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            if (stmt != null)
                stmt.close();
            return true;
        } catch (SQLException stmtE) {
            //stmtE.printStackTrace();
            return false;
        } catch (NullPointerException nE) {
            return false;
        }
    }

    /**
     * Does the same as executeSQL, this method just exists for readability when executing statements.
     *
     * @param sql the query for the statement
     * @return false if exceptions get catched, otherwise true
     */
    public boolean insertSQL(String sql) {
        return executeSQL(sql);
    }

    /**
     * Does the same as executeSQL, this method just exists for readability when executing statements
     *
     * @param sql the query for the statement
     * @return false if exceptions get catched, otherwise true
     */
    public boolean updateSQL(String sql) {
        return (insertSQL(sql));
    }

    /**
     * Does the same as executeSQL, this method just exists for readability when executing statements
     *
     * @param sql the query for the statement
     * @return false if exceptions get catched, otherwise true
     */
    public boolean deleteSQL(String sql) {
        return (insertSQL(sql));
    }


    /**
     * Converts a ResultSet to an ArrayList.
     * Each entry in ArrayList resembles a row of the database resultSet (as a Map).
     * Each Map holds the column name as a key (String) and the database entry as a value (Object).
     *
     * @param rs the resultSet that a select-statement delivers
     * @return an ArrayList that contains a map for each entry in the resultSet
     * @throws SQLException since the resultSet directly accesses the db when getting called to pull data
     * every call can throw a SQLException if there is no connection (anymore)
     */
    public List<Map<String, Object>> resultSetToArrayList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>(columns);
            for (int i = 1; i <= columns; i++) {
                row.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(row);
        }
        return list;
    }

    /**
     * Stores information for the database connection in preferences and handles OS storage automatically.
     *
     * @param protocol  the protocol used for the database connection, e.g. mysql
     * @param db_url    the ip-address of the database server
     * @param port      the port used, e.g. 3306
     * @param db_name   the name of the database
     * @param user      the username
     * @param pass      the password
     * @return true if the connection test at the end of the method is successful, otherwise false
     */
    public boolean setServerPrefs(String protocol, String db_url, String port, String db_name, String user, String pass) {
        prefs.put("protocol", protocol);
        prefs.put("db_url", db_url);
        prefs.put("port", port);
        prefs.put("db_name", db_name);
        prefs.put("user", user);
        prefs.put("pass", pass);

        //test connection
        return this.testCon();
    }

    /**
     * Delivers the current server preferences.
     *
     * @return the Preferences stored at the location determined in the constructor
     */
    public Preferences getServerPrefs() {
        return prefs;
    }

    /**
     * Returns true if connection can be established with the information stored in preferences.
     *
     * @return true if a connection can be opened, otherwise false
     */
    public boolean testCon() {
        // try to open connection
        boolean conTest = this.openConn();
        // close connection
        this.closeConn();
        lastConTest = conTest;
        return conTest;
    }

    /**
     * Returns true if connection can be established with the information given by parameters.
     *
     * @param protocol  The protocol used for the database connection, e.g. mysql
     * @param db_url    The ip-address of the database server
     * @param port      The port used, e.g. 3306
     * @param db_name   The name of the database
     * @param user      The username
     * @param pass      The password
     * @return true if a connection with given parameters can be opened, otherwise false
     */
    public boolean testCon(String protocol, String db_url, String port, String db_name, String user, String pass) {
        // try to open connection
        boolean conTest = this.openConn(protocol, db_url, port, db_name, user, pass);
        // close connection
        this.closeConn();
        lastConTest = conTest;
        return conTest;
    }

    /**
     * Returns the value of the last connection test (true if successful).
     *
     * @return Last result of a connection test
     */
    public boolean getLastConTest() {
        return lastConTest;
    }

    /**
     * Gets the names of all existing tables in the current database.
     * Calls resultSetToArrayList to store them in an ArrayList
     *
     * @return an ArrayList in the format described at resultSetToArrayList() containing the names of all tables in the database
     */
    public List<Map<String, Object>> getTableNames() {
        DatabaseMetaData md = null;
        try {
            md = conn.getMetaData();
            ResultSet rs = md.getTables(prefs.get("db_name", ""), null, "%", null);
            return resultSetToArrayList(rs);
        } catch (SQLException sqlException) {
            //sqlException.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if all necessary tables exist and creates them if not.
     *
     * @return true if all tables are there or if all necessary tables can be created
     */
    public boolean checkTables() {
        // reference list of all tables that should be there
        List<String> referenceTables = Arrays.asList("colors", "games", "moves", "participants", "symbols", "players", "types");
        // get db name from prefs
        String dbName = prefs.get("db_name", "");
        if (dbName.equals("")) {
            return false;
        } else {
            // get names of tables
            List<Map<String, Object>> tableNames = getTableNames();
            if (tableNames == null) {
                return false;
            } else {
                // check for each reference table if the table name exists in the DB
                // if not break out of loop and set allTablesThere to false
                boolean allTablesThere = false;
                for (String table : referenceTables) {
                    allTablesThere = false;
                    for (Map<String, Object> row : tableNames) {
                        if (row.get("TABLE_NAME").equals(table)) {
                            allTablesThere = true;
                            break;
                        }
                    }
                    if (allTablesThere == false) {
                        break;
                    }
                }

                //create tables and alter foreign keys if at least one of the tables was missing
                if (!allTablesThere) {
                    String createTables;
                    createTables = "CREATE TABLE IF NOT EXISTS `players` (" +
                            "  `id` int PRIMARY KEY AUTO_INCREMENT," +
                            "  `name` varchar(255)," +
                            "  `type` int," +
                            "  `description` varchar(255)," +
                            "  `is_active` boolean," +
                            "  `create_date` timestamp" +
                            ");" +
                            "CREATE TABLE IF NOT EXISTS `types` (" +
                            "  `id` int PRIMARY KEY," +
                            "  `name` varchar(255)" +
                            ");" +
                            "" +
                            "CREATE TABLE IF NOT EXISTS `games` (" +
                            "  `id` int PRIMARY KEY AUTO_INCREMENT," +
                            "  `start_time` timestamp DEFAULT (now())," +
                            "  `end_time` timestamp DEFAULT null," +
                            "  `initial_board` longtext," +
                            "  `ai_only` boolean" +
                            ");" +
                            "" +
                            "CREATE TABLE IF NOT EXISTS `participants` (" +
                            "  `id` int PRIMARY KEY AUTO_INCREMENT," +
                            "  `player` int," +
                            "  `game` int," +
                            "  `color` int," +
                            "  `winner` boolean" +
                            ");" +
                            "" +
                            "CREATE TABLE IF NOT EXISTS `colors` (" +
                            "  `id` int PRIMARY KEY," +
                            "  `color` varchar(255)" +
                            ");" +
                            "" +
                            "CREATE TABLE IF NOT EXISTS `moves` (" +
                            "  `game` int," +
                            "  `turn` int," +
                            "  `participant` int," +
                            "  `orientation` int," +
                            "  `new_tile_pos` varchar(255)," +
                            "  `new_player_pos` varchar(255)," +
                            "  `symbol` int," +
                            "  PRIMARY KEY (`game`, `turn`)" +
                            ");" +
                            "" +
                            "CREATE TABLE IF NOT EXISTS `symbols` (" +
                            "  `id` int PRIMARY KEY," +
                            "  `name` varchar(255)" +
                            ");" +
                            "ALTER TABLE `players` ADD FOREIGN KEY (`type`) REFERENCES `types` (`id`);" +
                            "ALTER TABLE `participants` ADD FOREIGN KEY (`player`) REFERENCES `players` (`id`);" +
                            "ALTER TABLE `participants` ADD FOREIGN KEY (`game`) REFERENCES `games` (`id`);" +
                            "ALTER TABLE `participants` ADD FOREIGN KEY (`color`) REFERENCES `colors` (`id`);" +
                            "ALTER TABLE `moves` ADD FOREIGN KEY (`game`) REFERENCES `games` (`id`);" +
                            "ALTER TABLE `moves` ADD FOREIGN KEY (`participant`) REFERENCES `participants` (`id`);" +
                            "ALTER TABLE `moves` ADD FOREIGN KEY (`symbol`) REFERENCES `symbols` (`id`);";

                    // execute SQL-Statements
                    if (!executeSQL(createTables))
                        return false;
                    // fill database with initial data
                    if (!createInitialData())
                        return false;
                }
                // create views
                if(!createViews())
                    return false;
                return true;
            }
        }
    }

    /**
     * Fills the database with initial data, e.g. colors: Green, Red, Blue, Yellow.
     * Cleanses the tables from old data to avoid primary key conflicts before inserting.
     *
     * @return true if all insert statements can be executed without returning false, otherwise false
     */
    public boolean createInitialData() {
        String fillColors;
        String fillTypes;
        String fillSymbols;

        fillColors = "INSERT INTO colors (`id`, `color`) VALUES (0, 'Red');" +
                "INSERT INTO colors (`id`, `color`) VALUES (1, 'Yellow');" +
                "INSERT INTO colors (`id`, `color`) VALUES (2, 'Green');" +
                "INSERT INTO colors (`id`, `color`) VALUES (3, 'Blue');";

        fillTypes = "INSERT INTO types (`id`, `name`) VALUES (0, 'Human');" +
                "INSERT INTO types (`id`, `name`) VALUES (1, 'AI');";

        fillSymbols = "INSERT INTO symbols (`id`, `name`) VALUES (0, 'Owl');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (1, 'Mouse');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (2, 'Salamander');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (3, 'Moth');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (4, 'Spider');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (5, 'Beetle');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (6, 'Book');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (7, 'Coin');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (8, 'Map');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (9, 'Crown');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (10, 'Keys');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (11, 'Skull');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (12, 'Ring');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (13, 'Treasure');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (14, 'Emerald');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (15, 'Sword');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (16, 'Candleholder');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (17, 'Helmet');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (18, 'Bat');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (19, 'Goblin');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (20, 'Fairy');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (21, 'Genie');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (22, 'Ghost');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (23, 'Dragon');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (24, 'StartingPointRed');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (25, 'StartingPointYellow');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (26, 'StartingPointGreen');" +
                "INSERT INTO symbols (`id`, `name`) VALUES (27, 'StartingPointBlue');";

        //clear data from tables before filling them
        if (!deleteSQL("DELETE FROM colors;"))
            return false;
        if (!deleteSQL("DELETE FROM types;"))
            return false;
        if (!deleteSQL("DELETE FROM symbols;"))
            return false;

        if (!insertSQL(fillColors))
            return false;
        if (!insertSQL(fillTypes))
            return false;
        if (!insertSQL(fillSymbols))
            return false;
        //System.out.println("Initial data created.");
        return true;
    }

    /**
     * Completely drops the database and sets up a fresh one with all tables, views and initial data.
     *
     * @return true if all steps to a complete reset were successful, otherwise false
     */
    public boolean completeReset() {
        if (!openConn()) {
            return false;
        } else {
            String dbName = prefs.get("db_name", "");

            // drop database with name from prefs
            if (!executeSQL("DROP DATABASE " + dbName))
                return false;
            //System.out.println("Database " + dbName + " dropped.");

            // create database with name from prefs
            if (!executeSQL("CREATE DATABASE " + dbName))
                return false;
            //System.out.println("Database " + dbName + "  created.");

            // use database
            executeSQL("USE " + prefs.get("db_name", ""));
            //System.out.println("Using database " + dbName);

            // if user had rights to drop and create database then creating tables and foreign keys is not problem
            // checkTables also fills DB with initial data because it calls createInitialData in the end
            checkTables();
            //System.out.println("Tables created.");

            closeConn();
            return true;
        }
    }

    /**
     * Creates a timestamp of the current system time and
     * converts it to a String that fits the mysql timestamp format.
     *
     * @return the converted timestamp as a String
     */
    public String createTimestamp() {
        Timestamp timeStamp = new java.sql.Timestamp(new java.util.Date().getTime());
        String timeStampString = timeStamp.toString().substring(0, timeStamp.toString().length() - 4);
        //System.out.println("TimeString-uncut:" + timeStamp.toString());
        //System.out.println("TimeString-cut: " + timeStampString);
        return timeStampString;
    }

    /**
     * Creates a view that makes selecting data in the statistics gui easier.
     *
     * @return true if the view can be created, otherwise false
     */
    private boolean createViews(){
        String createViewsString = "";
        createViewsString += "CREATE OR REPLACE VIEW `max_turn` AS select `t`.`game` AS `game`,`t`.`turn` AS `turn` from " +
                "((select `moves`.`game` AS `game`,max(`moves`.`turn`) AS `max_turn` from `moves` " +
                "group by `moves`.`game`) `m` " +
                "join `moves` `t` on(((`t`.`game` = `m`.`game`) and (`t`.`turn` = `m`.`max_turn`))));";
        return executeSQL(createViewsString);
    }
}
