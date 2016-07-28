/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.ch;

/**
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
