package com.moysa.searchwizard.core;

import com.moysa.searchwizard.db.WordsSQLHelper;
import com.moysa.searchwizard.exceptions.NonDatabaseWordException;
import com.moysa.searchwizard.exceptions.SQLConnectionException;

import java.sql.SQLException;
import java.util.*;

/**
 * Expands one request with a list of similar
 */
public class Expander {

    private static final String SERVER_ADDRESS = "jdbc:mysql://localhost/words?";

    /**
     * Request from user
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

            if (!iterator.hasNext())
                return similarRequests;

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

        List<String> words = Arrays.asList(request.split(" "));
        int index = words.indexOf(word);

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
        //TODO split by words from db instead of space
        Map<String, List<String>> map = new HashMap<>();

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
        } catch (NonDatabaseWordException e) {
            //simply remove word if it's not in database
            removeWordFromRequest(word);
        }

        return result;
    }

    /**
     * Removes word from request
     * @param word word to remove
     */
    private void removeWordFromRequest(String word) {

        String regex = "\\s*\\b" + word + "\\b\\s*";
        request = request.replaceAll(regex, "");
    }

    /**
     * Prepares request (removes all non-char symbols)
     * @param request request to set
     */
    public void setRequest(String request) {
        //TODO Check request words on similarity

        //Removes all non-charachters from string
        this.request = request.toLowerCase().replaceAll("[^\\p{L}\\p{Nd}\\s]", "");
    }
}
