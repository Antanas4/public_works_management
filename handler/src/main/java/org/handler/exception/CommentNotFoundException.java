package org.handler.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String message) {
      super(message);
    }
}
