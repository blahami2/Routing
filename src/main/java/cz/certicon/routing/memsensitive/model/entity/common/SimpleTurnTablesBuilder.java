/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.TurnTablesBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleTurnTablesBuilder implements TurnTablesBuilder<SimpleTurnTablesBuilder.TurnTablesContainer, Graph> {

    private ArrayList<ArrayList<Integer>>[] tr;

    @Override
    public void addRestriction( Graph graph, long[] from, long via, long to ) {
        if ( tr == null ) {
            tr = (ArrayList<ArrayList<Integer>>[]) new ArrayList[graph.getNodeCount()];
        }
        int node = graph.getNodeByOrigId( via );
        if ( tr[node] == null ) {
            tr[node] = new ArrayList<>();
        }
        ArrayList<Integer> edges = new ArrayList<>();
        for ( long e : from ) {
            edges.add( graph.getEdgeByOrigId( e ) );
        }
        edges.add( graph.getEdgeByOrigId( to ) );
        tr[node].add( edges );
    }

    @Override
    public TurnTablesContainer build( Graph graph ) {
        int[][][] arr = new int[graph.getNodeCount()][][];
        for ( int i = 0; i < tr.length; i++ ) {
            if ( tr[i] != null ) {
                ArrayList<ArrayList<Integer>> trs = tr[i];
                arr[i] = new int[trs.size()][];
                for ( int j = 0; j < trs.size(); j++ ) {
                    ArrayList<Integer> edges = trs.get( j );
                    arr[i][j] = new int[edges.size()];
                    for ( int k = 0; k < edges.size(); k++ ) {
                        arr[i][j][k] = edges.get( k );
                    }
                }
            }
        }
        return new TurnTablesContainer( arr );
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
