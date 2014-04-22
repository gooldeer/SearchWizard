package com.moysa.searchwizard.core;

import com.moysa.searchwizard.db.WordsSQLHelper;
import com.moysa.searchwizard.exceptions.NonDatabaseWordException;
import com.moysa.searchwizard.exceptions.SQLConnectionException;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.*;

/**
 * Expands one request with a list of similar
 */
public class Expander {

    private static final String SERVER_ADDRESS = "jdbc:mysql://localhost/words?";
    public static final String ORIGINAL_REQUEST = "originalRequest";
    public static final String EQUIVALENT_REQUESTS = "equivalentRequests";
    public static final String JDBC_MYSQL = "jdbc:mysql://";


    /**
     * Original request from user. Just saved.
     */
    private String originalRequest;

    /**
     * Request saved to remove after end.
     */
    private String originalTranslatedRequest;

    /**
     * Current request for work
     */
    private String request;

    /**
     * All of similar requests. Recommended to use HashSet
     */
    private Set<String> similarRequests;

    public Expander(String request) throws SQLConnectionException {

        if (WordsSQLHelper.newInstance().connect(SERVER_ADDRESS)) {
            setRequest(request);
            this.similarRequests = new HashSet<>();
        } else {
            throw new SQLConnectionException();
        }

    }

    /**
     * Constructor, which connects to db in current address
     * @param request user request
     * @param mySQLAddress address without 'jdbc:mysql://' and '?' ending
     * @throws SQLConnectionException
     */
    public Expander(String request, String mySQLAddress) throws SQLConnectionException {
        if (WordsSQLHelper.newInstance().connect(JDBC_MYSQL + mySQLAddress + "?")) {
            setRequest(request);
            this.similarRequests = new HashSet<>();
        } else {
            throw new SQLConnectionException();
        }
    }

    /**
     * Expands request with similar requests
     * @return set of similar requests
     */
    public Set<String> expand() {

        Map<String, List<String>> similarWords = getSimilarWordsFromRequest(request);

        return createRequestsFromWords(similarWords);
    }

    /**
     * Places words in right order (does similar requests)
     * @param similarWords Map with word and his synonyms
     * @return Set of similar requests
     */
    private Set<String> createRequestsFromWords(Map<String, List<String>> similarWords) {

        List<String> replacedWithSynonyms;
        final Iterator<String> iterator = similarWords.keySet().iterator();

        //go to word on which we are now
        do {

            if (!iterator.hasNext()) {
                return removeOriginalRequestFromSet();
            }

            String word = iterator.next();
            List<String> similars = similarWords.get(word);
            //replace word with his synonyms
            replacedWithSynonyms = replaceWordWithSynonyms(request, word, similars);

        } while (isContains(replacedWithSynonyms));

        similarRequests.addAll(replacedWithSynonyms);
        //for each similar request replace word one more time
        replacedWithSynonyms.forEach(similar -> {
            this.request = similar;
            createRequestsFromWords(getSimilarWordsFromRequest(similar));
        });

        return removeOriginalRequestFromSet();
    }

    /**
     * In algorithm original request is doubled, we need to remove on of them
     * @return Set with removed original request
     */
    private Set<String> removeOriginalRequestFromSet() {
        similarRequests.remove(originalTranslatedRequest);
        return similarRequests;
    }

    /**
     * Check if replaced requests are in similar requests
     * @param replacedWithSynonyms requests to check
     * @return true if contains, false either
     */
    private boolean isContains(List<String> replacedWithSynonyms) {

        boolean result = false;

        for (String request : replacedWithSynonyms) {
            result = similarRequests.contains(request);
        }

        return result;
    }

    /**
     * Creates list of requests with switched word on his synonyms
     * @param request request to switch words
     * @param word word, which we need to switch
     * @param synonyms his synonyms
     * @return List of requests with switched word
     */
    private List<String> replaceWordWithSynonyms(String request, String word, List<String> synonyms) {

        List<String> requests = new ArrayList<>();
        requests.add(request);

        List<String> words = Arrays.asList(request.split(" "));
        int index = words.indexOf(word);

        if (synonyms == null) {
            return requests;
        }

        synonyms.forEach(similar -> {

            words.set(index, similar);

            String newRequest = listToString(words);
            requests.add(newRequest);
        });

        return requests;
    }

    /**
     * From List with words to string
     * @param words list of words
     * @return String, concatenated by space
     */
    private String listToString(List<String> words) {
        StringBuilder builder = new StringBuilder();

        words.forEach(word -> builder.append(word).append(" "));

        return builder.toString();
    }

    /**
     * Returns map of synonyms of each word in request
     * @param request request
     * @return map of synonyms per word
     */
    private Map<String, List<String>> getSimilarWordsFromRequest(String request) {

        Map<String, List<String>> map = new HashMap<>();
        //I don't think that it's a god way to split...
        List<String> words = Arrays.asList(request.split(" "));

        words.forEach(word -> map.put(word, getSimilarWords(word)));

        return map;
    }

    /**
     * Gets synonyms for word from database
     * @param word word
     * @return list of synonyms
     */
    private List<String> getSimilarWords(String word) {

        List<String> result = null;

        try {
            result = WordsSQLHelper.getInstance().getSynonymsForWord(word);
        } catch (SQLException e) {
            //TODO catch nicely
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Pack into json
     * @return JSONObject with ORIGINAL_REQUEST
     * and SIMILAR_REQUESTS
     * @throws JSONException
     */
    public JSONObject getJSON() throws JSONException {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(ORIGINAL_REQUEST, originalRequest);
        jsonObject.put(EQUIVALENT_REQUESTS, similarRequests);

        return jsonObject;
    }

    /**
     * Removes word from request
     * @param request request to remove words from
     * @param word word to remove
     * @return request
     */
    private String removeWordFromRequest(String request, String word) {

        String regex = word + "\\b\\s*";
        request = request.replaceAll(regex, "");

        return request;
    }

    /**
     * Prepares request (removes all non-char symbols)
     * @param request request to set
     */
    public void setRequest(String request) {
        this.originalRequest = request;
        originalTranslatedRequest = this.request = transformRequest(request);
    }

    /**
     * Transforms request for using in expander
     * Replaces words with it's standards
     * @param request request to transform
     * @return transformed request
     */
    private String transformRequest(String request) {

        String result = request.toLowerCase().replaceAll("[^\\p{L}\\p{Nd}\\s]", "");

        List<String> words = Arrays.asList(request.split(" "));

        for (String word : words) {

            try {
                result = result.replace(word, WordsSQLHelper.getInstance().queryForWordInBase(word));
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NonDatabaseWordException e) {
                //simply remove word if it's not in database
//                result = removeWordFromRequest(request, word);
            }
        }

        return result;
    }
}
