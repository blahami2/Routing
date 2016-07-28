/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class RouteNotFoundException extends Exception {

    public RouteNotFoundException() {
    }

    public RouteNotFoundException( String message ) {
        super( message );
    }

    public RouteNotFoundException( String message, Throwable cause ) {
        super( message, cause );
    }

    public RouteNotFoundException( Throwable cause ) {
        super( cause );
    }

    public RouteNotFoundException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
        super( message, cause, enableSuppression, writableStackTrace );
    }
}
