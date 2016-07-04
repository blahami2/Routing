/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.TurnTablesBuilder;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import gnu.trove.TIntCollection;
import gnu.trove.function.TIntFunction;
import gnu.trove.function.TObjectFunction;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TLongObjectProcedure;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleTurnTablesBuilder implements TurnTablesBuilder<SimpleTurnTablesBuilder.TurnTablesContainer, Graph> {

    private Map<Trinity<Integer, Integer, Integer>, ArrayList<EdgePair>> map = new HashMap<>(); // <array_id, to> -> <list<from>,via,to>
    private TIntObjectMap<Set<Trinity<Integer, Integer, Integer>>> nodeToArrayIdMap = new TIntObjectHashMap<>(); // node -> set<array_id, to>

    @Override
    public void addRestriction( Graph graph, int arrayId, long from, int fromPosition, long via, long to ) {
//        System.out.println( "adding: arrayId = " + arrayId + ", from = " + from + ", pos = " + fromPosition + ", via = " + via + ", to = " + to );
        int viaInt = graph.getNodeByOrigId( via );
        int toInt = graph.getEdgeByOrigId( to );
        int fromInt = graph.getEdgeByOrigId( from );
        ArrayList<EdgePair> fromList;
        Trinity<Integer, Integer, Integer> key = new Trinity<>( arrayId, viaInt, toInt );
        if ( map.containsKey( key ) ) {
            fromList = map.get( key );
        } else {
            fromList = new ArrayList<>();
            map.put( key, fromList );
        }
        while ( fromList.size() < fromPosition + 1 ) {
            fromList.add( null );
        }
        EdgePair edgePair = fromList.get( fromPosition );
        if ( edgePair == null ) {
            edgePair = new EdgePair();
            edgePair.a = fromInt;
            fromList.set( fromPosition, edgePair );
        } else {
            edgePair.b = fromInt;
        }
        Set<Trinity<Integer, Integer, Integer>> arrayIdSet;
        if ( nodeToArrayIdMap.containsKey( viaInt ) ) {
            arrayIdSet = nodeToArrayIdMap.get( viaInt );
        } else {
            arrayIdSet = new HashSet<>();
            nodeToArrayIdMap.put( viaInt, arrayIdSet );
        }
        arrayIdSet.add( key );
    }

    @Override
    public void addRestriction( Graph graph, PreprocessedData chData, int arrayId, long from, int fromPosition, long via, long to ) {
//        if ( via == 11647 ) {
//            System.out.println( "ORIG from: " + from + ", via = " + via + " , to = " + to );
//        }
        int viaInt = graph.getNodeByOrigId( via );
        int toInt = chData.getEdgeByOrigId( to, graph );
        int fromInt = chData.getEdgeByOrigId( from, graph );
//        if ( via == 11647 ) {
//            System.out.println( "from: " + fromInt + ", via = " + viaInt + " , to = " + toInt );
//        }
        ArrayList<EdgePair> fromList;
        Trinity<Integer, Integer, Integer> key = new Trinity<>( arrayId, viaInt, toInt );
        if ( map.containsKey( key ) ) {
            fromList = map.get( key );
        } else {
            fromList = new ArrayList<>();
            map.put( key, fromList );
        }
        while ( fromList.size() < fromPosition + 1 ) {
            fromList.add( null );
        }
//        if ( via == 11647 ) {
//            System.out.println( "key: " + key );
//        }
        EdgePair edgePair = fromList.get( fromPosition );
        if ( edgePair == null ) {
            edgePair = new EdgePair();
            edgePair.a = fromInt;
            fromList.set( fromPosition, edgePair );
        } else {
            edgePair.b = fromInt;
        }
//        if ( via == 11647 ) {
//            System.out.println( "pair: " + edgePair );
//        }
        Set<Trinity<Integer, Integer, Integer>> arrayIdSet;
        if ( nodeToArrayIdMap.containsKey( viaInt ) ) {
            arrayIdSet = nodeToArrayIdMap.get( viaInt );
        } else {
            arrayIdSet = new HashSet<>();
            nodeToArrayIdMap.put( viaInt, arrayIdSet );
        }
        arrayIdSet.add( key );
    }

    @Override
    public TurnTablesContainer build( Graph graph ) {
//        System.out.println( "map" );
//        for ( Map.Entry<Trinity<Integer, Integer, Integer>, ArrayList<EdgePair>> entry : map.entrySet() ) {
//            System.out.println( entry.getKey() + " -> " + entry.getValue() );
//        }
//        System.out.println( "nodeToArrayIdMap" );
//        for ( int key : nodeToArrayIdMap.keys() ) {
//            System.out.println( key + " -> " + nodeToArrayIdMap.get( key ) );
//        }

        int[][][] arr = new int[graph.getNodeCount()][][];
        for ( int i = 0; i < arr.length; i++ ) {
            if ( nodeToArrayIdMap.containsKey( i ) ) {
                Set<Trinity<Integer, Integer, Integer>> set = nodeToArrayIdMap.get( i );
                arr[i] = new int[set.size()][];
                int j = 0;
                for ( Trinity<Integer, Integer, Integer> trinity : set ) {
                    ArrayList<EdgePair> edgePairList = map.get( trinity );
                    arr[i][j] = new int[edgePairList.size() + 1];
                    for ( int k = arr[i][j].length - 2; k >= 0; k-- ) {
                        EdgePair edgePair = edgePairList.get( k );
                        arr[i][j][k] = ( i == graph.getTarget( edgePair.a ) ) ? edgePair.a : edgePair.b;
//                        System.out.println( "#" + i + " -> " + ( edgePair.a != -1 ? graph.getTarget( edgePair.a ) : -1 ) + " vs " + ( edgePair.b != -1 ? graph.getTarget( edgePair.b ) : -1 ) );
                    }
                    arr[i][j][arr[i][j].length - 1] = trinity.c;

//                    System.out.print( "node#" + graph.getNodeOrigId( i ) + "-sequence#" + j + ": " );
//                    for ( int k = 0; k < arr[i][j].length; k++ ) {
//                        int l = arr[i][j][k];
//                        System.out.print( graph.getEdgeByOrigId( l ) + " " );
//                    }
//                    System.out.println( "" );
                    j++;
                }
            }
        }
        return new TurnTablesContainer( arr );
    }

    @Override
    public TurnTablesContainer build( Graph graph, PreprocessedData chData ) {
        int[][][] arr = new int[graph.getNodeCount()][][];
        for ( int i = 0; i < arr.length; i++ ) {
            if ( nodeToArrayIdMap.containsKey( i ) ) {
                Set<Trinity<Integer, Integer, Integer>> set = nodeToArrayIdMap.get( i );
                arr[i] = new int[set.size()][];
                int j = 0;
                for ( Trinity<Integer, Integer, Integer> trinity : set ) {
                    ArrayList<EdgePair> edgePairList = map.get( trinity );
                    arr[i][j] = new int[edgePairList.size() + 1];
                    for ( int k = arr[i][j].length - 2; k >= 0; k-- ) {
                        EdgePair edgePair = edgePairList.get( k );
                        arr[i][j][k] = ( i == chData.getTarget( edgePair.a, graph ) ) ? edgePair.a : edgePair.b;
//                        System.out.println( "#" + i + " -> " + ( edgePair.a != -1 ? chData.getTarget( edgePair.a, graph ) : -1 ) + " vs " + ( edgePair.b != -1 ? chData.getTarget( edgePair.b, graph ) : -1 ) );
                    }
                    arr[i][j][arr[i][j].length - 1] = trinity.c;
                    j++;
                }
            }
        }
        return new TurnTablesContainer( arr );
    }

    private static class EdgePair {

        int a;
        int b;

        public EdgePair() {
            this.a = -1;
            this.b = -1;
        }

        public EdgePair( int a, int b ) {
            this.a = a;
            this.b = b;
        }

        @Override
        public String toString() {
            return "EdgePair{" + "a=" + a + ", b=" + b + '}';
        }

    }

    public static class TurnTablesContainer {

        private final int[][][] tr;

        public TurnTablesContainer( int[][][] tr ) {
            this.tr = tr;
        }

        public int[][][] getTr() {
            return tr;
        }
    }
}
