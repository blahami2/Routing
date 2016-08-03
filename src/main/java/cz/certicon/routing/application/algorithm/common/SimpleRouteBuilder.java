/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.common;

import cz.certicon.routing.application.algorithm.Route;
import cz.certicon.routing.application.algorithm.RouteBuilder;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.basic.Pair;
import java.util.LinkedList;

/**
 * Simple implementation of the {@link RouteBuilder} interface.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleRouteBuilder implements RouteBuilder<Route, Graph> {

    private long source;
    private long target;
    private LinkedList<Pair<Long, Boolean>> edges;

    public SimpleRouteBuilder() {
        clear();
    }

    @Override
    public void setSourceNode( Graph graph, long nodeId ) {
        source = nodeId;
        target = nodeId;
    }

    @Override
    public void setTargetNode( Graph graph,  long nodeId ) {
//        System.out.println( "target node = " + nodeId );
        target = nodeId;
        source = nodeId;
    }

    @Override
    public void addEdgeAsFirst( Graph graph, long edgeId ) {
        int edge = graph.getEdgeByOrigId( edgeId );
        long sourceNode = graph.getNodeOrigId( graph.getSource( edge ) );
        long targetNode = graph.getNodeOrigId( graph.getTarget( edge ) );
        if ( edges.isEmpty() && source == -1 && target == -1 ) {
            edges.add( new Pair<>( edgeId, true ) );
            source = sourceNode;
            target = targetNode;
        } else if ( source == targetNode ) {
            edges.addFirst( new Pair<>( edgeId, true ) );
            source = sourceNode;
        } else if ( source == sourceNode ) {
            edges.addFirst( new Pair<>( edgeId, false ) );
            source = targetNode;
        } else {
            throw new IllegalArgumentException( "Cannot connect edge: " + edgeId
                    + " with source: " + sourceNode
                    + " and target: " + targetNode
                    + " to: " + source );
        }
    }

    @Override
    public void addEdgeAsLast( Graph graph, long edgeId ) {
        int edge = graph.getEdgeByOrigId( edgeId );
        long sourceNode = graph.getNodeOrigId( graph.getSource( edge ) );
        long targetNode = graph.getNodeOrigId( graph.getTarget( edge ) );
        if ( edges.isEmpty() && source == -1 && target == -1 ) {
            edges.add( new Pair<>( edgeId, true ) );
            source = sourceNode;
            target = targetNode;
        } else if ( target == sourceNode ) {
            edges.addLast( new Pair<>( edgeId, true ) );
            target = targetNode;
        } else if ( target == targetNode ) {
            edges.addLast( new Pair<>( edgeId, false ) );
            target = sourceNode;
        } else {
            throw new IllegalArgumentException( "Cannot connect edge: " + edgeId
                    + " with source: " + sourceNode
                    + " and target: " + targetNode
                    + " to: " + target );
        }
    }

    @Override
    public Route build() {
        return new SimpleRoute( edges, source, target );
    }

    @Override
    public final void clear() {
        source = -1;
        target = -1;
        edges = new LinkedList<>();
    }

}
