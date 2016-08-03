/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.preprocessing.ch;

/**
 * Edge difference is an indicator used as a part of priority queue key. It
 * helps to determine the next contracted node. This interface offers
 * calculation edge difference function for a given node.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface EdgeDifferenceCalculator {

    /**
     * Calculates the ED (edge difference) for the given node N based on the
     * node degrees, number of shortcuts and also previously contracted node.
     *
     * @param contractedNode the node that was just contracted, input -1 if none
     * was
     * @param nodeDegrees array of node degrees
     * @param node the node N for which the edge difference is calculated
     * @param numberOfShortcuts number of shortcuts for the node N
     * @return edge difference of the node N
     */
    public int calculate( int contractedNode, int[] nodeDegrees, int node, int numberOfShortcuts );

}
