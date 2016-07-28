/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.preprocessing.ch.calculators;

import cz.certicon.routing.application.algorithm.preprocessing.ch.EdgeDifferenceCalculator;

/**
 * Basic implementation of the {@link EdgeDifferenceCalculator} interface. Uses
 * difference of the number of shortcuts and the degree of node.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class BasicEdgeDifferenceCalculator implements EdgeDifferenceCalculator {

    @Override
    public int calculate( int contractedNode, int[] nodeDegrees, int node, int numberOfShortcuts ) {
        return numberOfShortcuts - nodeDegrees[node];
    }

}
