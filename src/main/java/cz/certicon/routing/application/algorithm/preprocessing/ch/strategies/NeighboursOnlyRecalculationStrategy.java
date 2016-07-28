/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.preprocessing.ch.strategies;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.preprocessing.ch.ContractionHierarchiesPreprocessor;
import cz.certicon.routing.application.algorithm.preprocessing.ch.ContractionHierarchiesPreprocessor.ProcessingData;
import cz.certicon.routing.application.algorithm.preprocessing.ch.EdgeDifferenceCalculator;
import cz.certicon.routing.application.algorithm.preprocessing.ch.NodeRecalculationStrategy;
import cz.certicon.routing.application.algorithm.preprocessing.ch.calculators.BasicEdgeDifferenceCalculator;
import cz.certicon.routing.model.entity.Graph;
import gnu.trove.iterator.TIntIterator;

/**
 * Neighbors update implementation of the {@link NodeRecalculationStrategy}
 * interface. The neighbor nodes are recalculated on the node contraction event.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class NeighboursOnlyRecalculationStrategy implements NodeRecalculationStrategy {

    // DEBUG
    public ContractionHierarchiesPreprocessor preprocessor;

    private EdgeDifferenceCalculator edgeDifferenceCalculator = new BasicEdgeDifferenceCalculator();

    @Override
    public TIntIterator recalculationIterator( Graph graph, ProcessingData data, int contractedNode, NodeDataStructure<Integer> priorityQueue ) {
//        System.out.println( "#" + contractedNode + " - returning recalculation iterator " );
        return new NodeIterator( data, contractedNode );
    }

    @Override
    public void onShortcutsCalculated( Graph graph, int[] nodeDegrees, int node, NodeDataStructure<Integer> priorityQueue, int shortcuts, int contractedNode ) {
        if ( priorityQueue.contains( node ) ) {
            int ed = edgeDifferenceCalculator.calculate( contractedNode, nodeDegrees, node, shortcuts );
            // DEBUG
//            if ( graph.getNodeOrigId( node ) == preprocessor.nodeOfInterest || preprocessor.nodeOfInterest < 0 ) {
//                preprocessor.out.println( "ED for #" + graph.getNodeOrigId( node ) + " = " + ed );
//            }

//            System.out.println( "#" + node + " = " + ed );
            priorityQueue.notifyDataChange( node, ed );
        }
    }

    //DEBUG
    public void setPreprocessor( ContractionHierarchiesPreprocessor preprocessor ) {
        this.preprocessor = preprocessor;
    }

    @Override
    public void setEdgeDifferenceCalculator( EdgeDifferenceCalculator edgeDifferenceCalculator ) {
        this.edgeDifferenceCalculator = edgeDifferenceCalculator;
    }

    @Override
    public EdgeDifferenceCalculator getEdgeDifferenceCalculator() {
        return edgeDifferenceCalculator;
    }

    private static class NodeIterator implements TIntIterator {

        private final ProcessingData data;
        private final int node;
        private final TIntIterator out;
        private final TIntIterator in;
        private boolean outHasNext;

        public NodeIterator( ProcessingData data, int node ) {
//            System.out.println( "#" + node + " - new node iterator for neighbours" );
            this.data = data;
            this.node = node;
            this.out = data.getOutgoingEdgesIterator( node );
            this.in = data.getIncomingEdgesIterator( node );
        }

        @Override
        public int next() {
            if ( outHasNext ) {
                return data.getOtherNode( out.next(), node );
            } else {
                return data.getOtherNode( in.next(), node );
            }
        }

        @Override
        public boolean hasNext() {
            return ( outHasNext = out.hasNext() ) || in.hasNext();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
