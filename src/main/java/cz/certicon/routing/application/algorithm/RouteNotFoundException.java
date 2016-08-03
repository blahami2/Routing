/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

/**
 * Thrown when a route has not been found by the algorithm. Route should always
 * be found, therefore this means an implementation or data error. Solution for
 * nodes, which are not connected by any path: create maximal connected
 * components from the graph. When searching for nodes, make sure they are in
 * the same component - select the first/bigger/closer and then map the second
 * node inside that component (to the closest node). This ensures the route
 * existence.
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
