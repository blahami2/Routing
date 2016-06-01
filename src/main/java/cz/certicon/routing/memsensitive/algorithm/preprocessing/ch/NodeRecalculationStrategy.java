/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.preprocessing.ch;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import gnu.trove.iterator.TIntIterator;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface NodeRecalculationStrategy {

    public TIntIterator recalculationIterator( Graph graph,ContractionHierarchiesPreprocessor.ProcessingData data,  int contractedNode, NodeDataStructure<Integer> priorityQueue );

    public void onShortcutsCalculated( Graph graph, int[] nodeDegrees, int node, NodeDataStructure<Integer> priorityQueue, int shortcuts, int contractedNode );
    
    public void setEdgeDifferenceCalculator(EdgeDifferenceCalculator edgeDifferenceCalculator);
    
    public EdgeDifferenceCalculator getEdgeDifferenceCalculator();

}
