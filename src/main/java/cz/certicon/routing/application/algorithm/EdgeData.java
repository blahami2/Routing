/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

import cz.certicon.routing.model.entity.Edge;

/**
 * A container for {@link Edge} attributes and additional data.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface EdgeData {
    
    /**
     * Returns source node of the edge
     * 
     * @return source {@link Node}
     */
    //public Node getSource();
    
    /**
     * Returns target node of the edge
     * 
     * @return target {@link Node}
     */
    //public Node getTarget();
    
    /**
     * Returns maximal speed of the edge.
     * 
     * @return speed in kmph
     */
    public int getSpeed();

    /**
     * Returns true or false based on whether is the edge paid or not
     * 
     * @return true if the edge is paid, false otherwise
     */
    public boolean isPaid();

    /**
     * Returns actual length of the edge
     * 
     * @return edge length in kilometers
     */
    public double getLength();
    
    // todo road type
}
