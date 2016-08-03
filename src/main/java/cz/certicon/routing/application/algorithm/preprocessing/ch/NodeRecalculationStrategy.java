/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.preprocessing.ch;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.preprocessing.ch.strategies.LazyRecalculationStrategy;
import cz.certicon.routing.model.entity.Graph;
import gnu.trove.iterator.TIntIterator;

/**
 * Interface offering iterator of the nodes to be recalculated as well as
 * methods for the recalculation. Principle: once the node is contracted, it is
 * necessary to determine the next node to be contracted. This is done via a
 * recalculation phase, where all the relevant nodes (based on the
 * implementation) are recalculated. At first, these nodes are chosen via
 * iterator. While iterating the nodes, each should have its shortcuts
 * calculated and after calculation, the onShortcutsCalculated method should be
 * called. This is important to perform BEFORE the iterators next operation,
 * because in can change the following node (see
 * {@link LazyRecalculationStrategy} for example)
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface NodeRecalculationStrategy {

    /**
     * Iterator supplying the caller with nodes requiring recalculation (based
     * on the implementation)
     *
     * @param graph an input graph
     * @param data processing data (graph wrapper)
     * @param contractedNode recently/last contracted node or -1 if none such
     * exists
     * @param priorityQueue simple integer priority queue
     * @return node iterator
     */
    public TIntIterator recalculationIterator( Graph graph, ContractionHierarchiesPreprocessor.ProcessingData data, int contractedNode, NodeDataStructure<Integer> priorityQueue );

    /**
     * Call when shortcuts get calculated for the node N
     *
     * @param graph an input graph
     * @param nodeDegrees an array of node degrees
     * @param node the node N to be recalculated
     * @param priorityQueue simple integer priority queue
     * @param shortcuts number of shortcuts for this node N
     * @param contractedNode recently/last contracted node or -1 if none such
     * exists
     */
    public void onShortcutsCalculated( Graph graph, int[] nodeDegrees, int node, NodeDataStructure<Integer> priorityQueue, int shortcuts, int contractedNode );

    /**
     * Setter
     *
     * @param edgeDifferenceCalculator {@link EdgeDifferenceCalculator}
     */
    public void setEdgeDifferenceCalculator( EdgeDifferenceCalculator edgeDifferenceCalculator );

    /**
     * Getter
     *
     * @return {@link EdgeDifferenceCalculator}
     */
    public EdgeDifferenceCalculator getEdgeDifferenceCalculator();

}
