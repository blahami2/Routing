/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.common;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface DataContainer {

    int getPredecessor( int node );

    double getDistance( int node );

    void setDistance( int node, double distance );

    void setPredecessor( int node, int edge );
    
}
