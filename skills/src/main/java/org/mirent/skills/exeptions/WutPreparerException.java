package org.mirent.skills.exeptions;

public class WutPreparerException extends RuntimeException {

    public WutPreparerException(String message) {
        super(message);
    }

    public WutPreparerException(String message, Throwable cause) {
        super(message, cause);
    }
}
