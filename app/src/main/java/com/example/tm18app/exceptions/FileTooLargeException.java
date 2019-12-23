package com.example.tm18app.exceptions;

/**
 * Custom {@link Exception} subclass. Used for when a File, image or video, is attempted to be
 * uploaded but it's size is too big.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 05.12.2019
 */
public class FileTooLargeException extends Exception {

    public FileTooLargeException(String msg) {
        super(msg);
    }

}
