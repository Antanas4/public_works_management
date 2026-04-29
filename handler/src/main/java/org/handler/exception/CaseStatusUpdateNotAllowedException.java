package org.handler.exception;

public class CaseStatusUpdateNotAllowedException extends RuntimeException {
    public CaseStatusUpdateNotAllowedException(String message) {
        super(message);
    }
}
