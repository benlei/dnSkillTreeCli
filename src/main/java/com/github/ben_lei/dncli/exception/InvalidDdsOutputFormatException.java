package com.github.ben_lei.dncli.exception;

/**
 * Created by blei on 6/16/16.
 */
public class InvalidDdsOutputFormatException extends IllegalArgumentException {
    public InvalidDdsOutputFormatException() {
        super();
    }

    public InvalidDdsOutputFormatException(String s) {
        super(s);
    }

    public InvalidDdsOutputFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDdsOutputFormatException(Throwable cause) {
        super(cause);
    }
}
