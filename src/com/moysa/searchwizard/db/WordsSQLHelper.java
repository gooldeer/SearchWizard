package com.moysa.searchwizard.db;

import com.moysa.searchwizard.exceptions.NonDatabaseWordException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gets words and synonyms from MySQL Db, running on localhost
 */
public class WordsSQLHelper {

    /**
     * User and pass name for db connection
     */
    private static final String USER = "user";
    private static final String PASS = "password";

    /**
     * Query for getting synonyms list
     */
    private static final String SQL_QUERY_FOR_SYNONYMS = "SELECT morpheme, ending "
            + "FROM words.words T1 "
            + "INNER JOIN words.synonyms T2 ON T1.id = T2.synonym_id "
            + "WHERE T2.word_id = " ;

    /**
     * Query for getting word from db if exists
     */
    private static final String SQL_QUERY_WORD_BEGINNING = "SELECT id, morpheme, ending "
            + "FROM words.words Words "
            + "WHERE INSTR('";

    private static final String SQL_QUERY_WORD_ENDING = "', Words.morpheme) > 0;";

    /**
     * Words table column names
     */
    public static final String MORPHEME_COLUMN = "morpheme";
    public static final String ENDING_COLUMN = "ending";
    public static final String ID_COLUMN = "id";

    private static WordsSQLHelper instance;

    private Statement statement = null;

    public static WordsSQLHelper newInstance() {
        instance = new WordsSQLHelper();
        return instance;
    }

    public static WordsSQLHelper getInstance() {
        return instance;
    }

    /**
     * Returns synonyms from db for word
     * @param word word for searching
     * @return list of synonyms
     * @throws SQLException some connection exception or query exception
     */
    public List<String> getSynonymsForWord(String word) throws SQLException {

        // resultSet gets the result of the SQL query
        ResultSet resultSet = statement
                .executeQuery(SQL_QUERY_FOR_SYNONYMS + queryForId(word) + ";");

        return writeResultSet(resultSet);
    }

    /**
     * Returns id of word in database
     * @param word word for searching
     * @return id in String
     * @throws SQLException some connection exception or query exception
     */
    private String queryForId(String word) throws SQLException {

        int result = 0;

        ResultSet resultSet = statement.executeQuery(SQL_QUERY_WORD_BEGINNING + word
                + SQL_QUERY_WORD_ENDING);

        if (resultSet.next())
            result = resultSet.getInt(ID_COLUMN);

        return String.valueOf(result);
    }

    /**
     * Retunrs standard db word implementation
     * @param word word for standardization
     * @return standard word, like in db
     * @throws SQLException some connection exception or query exception
     * @throws NonDatabaseWordException word is not in database
     */
    public String queryForWordInBase(String word) throws SQLException, NonDatabaseWordException {

        String result = null;

        ResultSet resultSet = statement.executeQuery(SQL_QUERY_WORD_BEGINNING + word
                + SQL_QUERY_WORD_ENDING);

        if (resultSet.next())
            result = resultSet.getString(MORPHEME_COLUMN)
                    + resultSet.getString(ENDING_COLUMN);

        if (result == null) {
            throw new NonDatabaseWordException();
        }

        return result;
    }

    /**
     * Connects to database
     * @param url url for connection
     * @return true if connected
     */
    public boolean connect(String url) {
        // this will load the MySQL driver, each DB has its own driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // setup the connection with the DB.
            Connection connect = DriverManager
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

    /**
     * Writes result if query on synonyms to List
     * @param resultSet query result
     * @return list of results
     * @throws SQLException some connection exception or query exception
     */
    private List<String> writeResultSet(ResultSet resultSet) throws SQLException {
        List<String> result = new ArrayList<>();

        while (resultSet.next()) {

            result.add(resultSet.getString(MORPHEME_COLUMN) + resultSet.getString(ENDING_COLUMN));
        }

        return result;
    }
}
