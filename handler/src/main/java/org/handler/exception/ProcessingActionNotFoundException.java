package org.handler.exception;

public class ProcessingActionNotFoundException extends RuntimeException {
    public ProcessingActionNotFoundException(String message) {
      super(message);
    }
}
