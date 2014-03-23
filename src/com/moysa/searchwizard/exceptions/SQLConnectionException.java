package com.moysa.searchwizard.exceptions;

/**
 * Exception when expander can't connect to server
 */
public class SQLConnectionException extends Exception {

    public SQLConnectionException(String message) {
        super(message);
    }
}
