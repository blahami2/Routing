/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.ch;

import cz.certicon.routing.model.entity.DistanceType;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import java.util.Iterator;

/**
 * Simple implementation of the {@link ChDataExtractor} using the
 * {@link PreprocessedData} as a representation of CH data
 *
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleChDataExtractor implements ChDataExtractor<PreprocessedData> {

    private final Graph graph;
    private final DistanceType distanceType;
    private final PreprocessedData data;

    public SimpleChDataExtractor( Graph graph, DistanceType distanceType, PreprocessedData data ) {
        this.graph = graph;
        this.distanceType = distanceType;
        this.data = data;
    }

    @Override
    public Iterator<Pair<Long, Integer>> getRankIterator() {
        return new RankIterator();
    }

    @Override
    public Iterator<Trinity<Long, Long, Long>> getShortcutIterator() {
        return new ShortcutIterator();
    }

    @Override
    public DistanceType getDistanceType() {
        return distanceType;
    }

    private class RankIterator implements Iterator<Pair<Long, Integer>> {

        private int counter = -1;

        @Override
        public boolean hasNext() {
            return counter + 1 < data.getRanks().length;
        }

        @Override
        public Pair<Long, Integer> next() {
            counter++;
            return new Pair<>( graph.getNodeOrigId( counter ), data.getRank( counter ) );
        }

    }

    private class ShortcutIterator implements Iterator<Trinity<Long, Long, Long>> {

        private int counter = -1;

        @Override
        public boolean hasNext() {
            return counter + 1 < data.getShortcutCount();
        }

        @Override
        public Trinity<Long, Long, Long> next() {
            counter++;
            long startEdge;
            if ( data.getStartEdge( counter ) < graph.getEdgeCount() ) {
                startEdge = graph.getEdgeOrigId( data.getStartEdge( counter ) );
            } else {
                startEdge = data.getStartId() + data.getStartEdge( counter ) - graph.getEdgeCount();
            }
            long endEdge;
            if ( data.getEndEdge( counter ) < graph.getEdgeCount() ) {
                endEdge = graph.getEdgeOrigId( data.getEndEdge( counter ) );
            } else {
                endEdge = data.getStartId() + data.getEndEdge( counter ) - graph.getEdgeCount();
            }
//            if ( data.getStartId() + counter == 127945 ) {
//                System.out.println( "writing: " + ( data.getStartId() + counter ) + " = " + startEdge + " -> " + endEdge );
//                System.out.println( "from: " + counter + " = " + data.getStartEdge( counter ) + " -> " + data.getEndEdge( counter ) );
//                System.out.println( "data: startId = " + data.getStartId() + ", counter = " + counter + ", edgeCount = " + graph.getEdgeCount() );
//            }
            return new Trinity<>( data.getStartId() + counter, startEdge, endEdge );
        }

    }

}
