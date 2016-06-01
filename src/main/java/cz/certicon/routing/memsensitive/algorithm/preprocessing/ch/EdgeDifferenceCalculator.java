/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.preprocessing.ch;


/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface EdgeDifferenceCalculator {

    public int calculate( int contractedNode, int[] nodeDegrees, int node, int numberOfShortcuts );

}
