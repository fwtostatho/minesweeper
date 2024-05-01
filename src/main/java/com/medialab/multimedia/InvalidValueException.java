package com.medialab.multimedia;

public class InvalidValueException extends Exception {
    public InvalidValueException() {
        super("The file contains invalid values");
    }
}
