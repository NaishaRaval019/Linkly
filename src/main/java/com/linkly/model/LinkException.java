package com.linkly.model;

/** Custom exception that carries the HTTP status to show on the error page. */
public class LinkException extends RuntimeException {
    private final int status;

    public LinkException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() { return status; }
}
