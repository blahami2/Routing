/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.ch;

/**
 * This exception is thrown when a read on CH is performed, but the data are not
 * present and the preprocessor is not provided. Please, use the preprocessor if
 * you want to prepare the data. Do not use the preprocessor, if the data should
 * already be present - then this is a valid error.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class NotPreprocessedException extends Exception {

    public NotPreprocessedException() {
    }

    public NotPreprocessedException( String message ) {
        super( message );
    }

    public NotPreprocessedException( String message, Throwable cause ) {
        super( message, cause );
    }

    public NotPreprocessedException( Throwable cause ) {
        super( cause );
    }

    public NotPreprocessedException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
        super( message, cause, enableSuppression, writableStackTrace );
    }
}
