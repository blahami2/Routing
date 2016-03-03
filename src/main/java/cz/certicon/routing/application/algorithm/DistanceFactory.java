/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.EdgeAttributes;

/**
 * The root interface for distance factories - classes for creating
 * {@link Distance} objects.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface DistanceFactory {

    /**
     * Creates instance with the value of zero.
     *
     * @return created {@link Distance} instance
     */
    public Distance createZeroDistance();

    /**
     * Creates instance with the value of infinity.
     *
     * @return created {@link Distance} instance
     */
    public Distance createInfiniteDistance();

//    /**
//     * Creates instance with the value of a given double number.
//     *
//     * @param distance abstract distance represented in double
//     * @return created {@link Distance} instance
//     */
//    public Distance createFromDouble( double distance );
    
    public Distance createFromEdgeAttributes(EdgeAttributes edgeAttributes);
}
