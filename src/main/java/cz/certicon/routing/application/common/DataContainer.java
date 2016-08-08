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

    /**
     * Returns predecessor of the given node
     *
     * @param node given node
     * @return edge as a predecessor of the given node
     */
    int getPredecessor( int node );

    /**
     * Returns distance of the given node
     *
     * @param node given node
     * @return distance
     */
    double getDistance( int node );

    /**
     * Sets distance to the given node
     *
     * @param node given node
     * @param distance distance
     */
    void setDistance( int node, double distance );

    /**
     * Sets predecessor to the given node
     *
     * @param node given node
     * @param edge predecessor (edge)
     */
    void setPredecessor( int node, int edge );

}
