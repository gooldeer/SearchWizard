package com.moysa.searchwizard.exceptions;

/**
 * Excpetion for words, that aren't in word's database
 */
public class NonDatabaseWordException extends Exception {

    private static final String MESSAGE = "This word is not in the database";

    public NonDatabaseWordException() {
        super(MESSAGE);
    }
}
