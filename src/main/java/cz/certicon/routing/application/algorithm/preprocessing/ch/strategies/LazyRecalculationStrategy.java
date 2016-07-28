/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.preprocessing.ch.strategies;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.preprocessing.ch.ContractionHierarchiesPreprocessor;
import cz.certicon.routing.application.algorithm.preprocessing.ch.EdgeDifferenceCalculator;
import cz.certicon.routing.application.algorithm.preprocessing.ch.NodeRecalculationStrategy;
import cz.certicon.routing.application.algorithm.preprocessing.ch.calculators.BasicEdgeDifferenceCalculator;
import cz.certicon.routing.model.entity.Graph;
import gnu.trove.iterator.TIntIterator;

/**
 * Lazy update implementation of the {@link NodeRecalculationStrategy}
 * interface. The node is recalculated on demand, then the evaluation repeats.
 * 
 * @deprecated not working yet
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class LazyRecalculationStrategy implements NodeRecalculationStrategy, TIntIterator {
    // DEBUG
    public ContractionHierarchiesPreprocessor preprocessor;

    private EdgeDifferenceCalculator edgeDifferenceCalculator = new BasicEdgeDifferenceCalculator();

    private NodeDataStructure<Integer> priorityQueue;
    private ContractionHierarchiesPreprocessor.ProcessingData data;
    private boolean calculate;

    @Override
    public TIntIterator recalculationIterator( Graph graph, ContractionHierarchiesPreprocessor.ProcessingData data, int contractedNode, NodeDataStructure<Integer> priorityQueue ) {
        this.priorityQueue = priorityQueue;
        this.data = data;
        this.calculate = true;
//        return this;
        throw new UnsupportedOperationException( "Not supported yet. Does not work with the current concept." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onShortcutsCalculated( Graph graph, int[] nodeDegrees, int node, NodeDataStructure<Integer> priorityQueue, int shortcuts, int contractedNode ) {
        int ed = edgeDifferenceCalculator.calculate( contractedNode, nodeDegrees, node, shortcuts );
        calculate = ( ed > priorityQueue.minValue() );
        priorityQueue.add( node, ed );
    }

    @Override
    public void setEdgeDifferenceCalculator( EdgeDifferenceCalculator edgeDifferenceCalculator ) {
        this.edgeDifferenceCalculator = edgeDifferenceCalculator;
    }

    @Override
    public EdgeDifferenceCalculator getEdgeDifferenceCalculator() {
        return edgeDifferenceCalculator;
    }

    @Override
    public int next() {
        return priorityQueue.extractMin();
    }

    @Override
    public boolean hasNext() {
        return !priorityQueue.isEmpty() && calculate;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
}
