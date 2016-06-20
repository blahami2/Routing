/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.calculators;

import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.ContractionHierarchiesPreprocessor;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.EdgeDifferenceCalculator;
import cz.certicon.routing.utils.EffectiveUtils;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SpatialHeuristicEdgeDifferenceCalculator implements EdgeDifferenceCalculator {

    private final int[] contractedNeighboursCountArray;
    private final int[] lastNodeContractedArray;

    // DEBUG
    public ContractionHierarchiesPreprocessor preprocessor;

    public SpatialHeuristicEdgeDifferenceCalculator( int nodeCount ) {
        contractedNeighboursCountArray = new int[nodeCount];
        lastNodeContractedArray = new int[nodeCount];
//        EffectiveUtils.fillArray( contractedNeighboursCountArray, -1 );
        EffectiveUtils.fillArray( lastNodeContractedArray, -1 );
    }

    @Override
    public int calculate( int contractedNode, int[] nodeDegrees, int node, int numberOfShortcuts ) {
        if ( contractedNode != -1 ) {
            if ( contractedNode != lastNodeContractedArray[node] ) {
                contractedNeighboursCountArray[node]++;
                lastNodeContractedArray[node] = contractedNode;
            }
        }
        if ( preprocessor != null && preprocessor.graph.getNodeOrigId( node ) == preprocessor.nodeOfInterest || preprocessor.nodeOfInterest < 0 ) {
            preprocessor.out.println( "#" + preprocessor.graph.getNodeOrigId( node ) + " - calculate: " + contractedNeighboursCountArray[node] + " + " + numberOfShortcuts + " - " + nodeDegrees[node] );
        }
        return contractedNeighboursCountArray[node] + numberOfShortcuts - nodeDegrees[node];
    }

}
