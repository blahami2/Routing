/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.ch;

import cz.certicon.routing.memsensitive.data.turntables.TurnTablesReader;
import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.ch.ChDataExtractor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
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

    @Override
    public Iterator<Trinity<List<Long>, Long, Long>> getTurnTableIterator() {
        return new TurnTableIterator();
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
            long startEdge = data.getEdgeOrigId( data.getStartEdge( counter ), graph );
            long endEdge = data.getEdgeOrigId( data.getEndEdge( counter ), graph );
            return new Trinity<>( data.getStartId() + counter, startEdge, endEdge );
        }

    }

    private class TurnTableIterator implements Iterator<Trinity<List<Long>, Long, Long>> {

        private int counter = -1;
        private int secondaryCounter = -1;
        private final int lastValidIndex;

        public TurnTableIterator() {
            int last = -2;
            int[][][] turnRestrictions = data.getTurnRestrictions();
            // TTDEBUG
//            if ( turnRestrictions != null ) {
//                int maxLen = 0;
//                for ( int i = 0; i < turnRestrictions.length; i++ ) {
//                    if ( turnRestrictions[i] != null ) {
//                        maxLen = Math.max( maxLen, turnRestrictions[i].length );
//                    }
//                }
//                System.out.println( getClass().getSimpleName() + "-maxlen = " + maxLen );
//            } else {
//                System.out.println( getClass().getSimpleName() + "-null " );
//            }
            for ( int i = turnRestrictions.length - 1; i >= 0; i-- ) {
                if ( turnRestrictions[i] != null && turnRestrictions[i].length > 0 ) {
//                    System.out.println( "last valid index = " + i + " with " + turnRestrictions[i].length + " turn restrictions" );
                    last = i;
                    break;
                }
            }
            lastValidIndex = last;
        }

        @Override
        public boolean hasNext() {
            return counter < lastValidIndex || ( counter == lastValidIndex && secondaryCounter < data.getTurnRestrictions()[counter].length - 1 );
        }

        @Override
        public Trinity<List<Long>, Long, Long> next() {
            int[][][] turnRestrictions = data.getTurnRestrictions();
            if ( secondaryCounter >= 0 ) {
                if ( turnRestrictions[counter].length > ++secondaryCounter ) {
                    return toTrinity( turnRestrictions[counter][secondaryCounter], counter );
                } else {
                    secondaryCounter = -1;
                }
            }
            while ( turnRestrictions[++counter] == null || turnRestrictions[counter].length == 0 ) {
                // do nothing
            }
            return toTrinity( turnRestrictions[counter][++secondaryCounter], counter );
        }

        public Trinity<List<Long>, Long, Long> toTrinity( int[] sequence, int node ) {
//            System.out.println( "returning for: " + node );
            List<Long> seq = new ArrayList<>();
            for ( int i = 0; i < sequence.length - 1; i++ ) { // ommit the last edge
                seq.add( data.getEdgeOrigId( sequence[i], graph ) );
            }
            return new Trinity<>( seq, graph.getNodeOrigId( node ), data.getEdgeOrigId( sequence[sequence.length - 1], graph ) );
        }

    }

}
