package com.moysa.searchwizard.exceptions;

/**
 * Exception when expander can't connect to server
 */
public class SQLConnectionException extends Exception {

    private static final String CONNECTION_EXCEPTION_MESSAGE = "Can't connect to server";

    public SQLConnectionException() {
        super(CONNECTION_EXCEPTION_MESSAGE);
    }
}
