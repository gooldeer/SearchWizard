package com.moysa.searchwizard.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gets words and synonyms from MySQL Db, running on localhost
 */
public class WordsSQLHelper {

    private static final String USER = "user";
    private static final String PASS = "password";

    private static final String SQL_QUERY_FOR_WORD = "SELECT synonym "
            + "FROM words.synonyms T1 "
            + "INNER JOIN words.words T2 ON T1.word_id = T2.id "
            + "WHERE T2.word = " ;

    public static final String SYNONYM_COLUMN = "synonym";

    private static WordsSQLHelper instance;

    private java.sql.Connection connect = null;
    private Statement statement = null;
    private ResultSet resultSet = null;

    public static WordsSQLHelper newInstance() {
        instance = new WordsSQLHelper();
        return instance;
    }

    public static WordsSQLHelper getInstance() {
        return instance;
    }

    public List<String> getSynonymsForWord(String word) throws SQLException {

        // resultSet gets the result of the SQL query
        resultSet = statement
                .executeQuery(SQL_QUERY_FOR_WORD + "'" + word + "'" + ";");

        return writeResultSet(resultSet);
    }

    public boolean connect(String url) {
        // this will load the MySQL driver, each DB has its own driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // setup the connection with the DB.
            connect = DriverManager
                    .getConnection(url
                            + "user=" + USER + "&password=" + PASS);

            // statements allow to issue SQL queries to the database
            statement = connect.createStatement();

            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<String> writeResultSet(ResultSet resultSet) throws SQLException {
        List<String> result = new ArrayList<>();

        while (resultSet.next()) {

            result.add(resultSet.getString(SYNONYM_COLUMN));
        }

        return result;
    }
}
