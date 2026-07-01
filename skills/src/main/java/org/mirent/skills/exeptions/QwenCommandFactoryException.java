package org.mirent.skills.exeptions;

public class QwenCommandFactoryException extends RuntimeException {

    public QwenCommandFactoryException(String message) {
        super(message);
    }

    public QwenCommandFactoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
