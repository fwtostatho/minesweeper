package com.medialab.multimedia;

public class InvalidDescriptionException extends Exception {
    public InvalidDescriptionException() {
        super("The file must contain 4 lines");
    }

}
