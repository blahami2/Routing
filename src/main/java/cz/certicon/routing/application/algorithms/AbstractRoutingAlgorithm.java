/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithms;

import cz.certicon.routing.application.RouteBuilder;
import cz.certicon.routing.application.RouteNotFoundException;
import cz.certicon.routing.application.RoutingAlgorithm;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.NodeSet;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public abstract class AbstractRoutingAlgorithm implements RoutingAlgorithm<Graph> {

    protected final Graph graph;

    public AbstractRoutingAlgorithm( Graph graph ) {
        this.graph = graph;
    }

    @Override
    public <R> R route( RouteBuilder<R, Graph> routeBuilder, NodeSet<Graph> nodeSet ) throws RouteNotFoundException {
        routeBuilder.clear();
        return route( routeBuilder, nodeSet, nodeSet.getMap( graph, NodeSet.NodeCategory.SOURCE ), nodeSet.getMap( graph, NodeSet.NodeCategory.TARGET ),
                nodeSet.hasUpperBound() ? nodeSet.getUpperBound() : Float.MAX_VALUE );
    }

    /**
     * Find the shortest route (R) between a set of source points to a set of
     * target points
     *
     * @param <R> route return type, see {@link RouteBuilder}
     * @param routeBuilder builder for the result route
     * @param nodeSet initial data container
     * @param from a set of source points (and their initial distances)
     * @param to a set of target points (and their initial distances)
     * @param upperBound upper bound of existing path
     * @return the shortest route of type R
     * @throws RouteNotFoundException thrown when no route was found between the
     * two points, see the {@link RouteNotFoundException} for more information
     */
    abstract public <R> R route( RouteBuilder<R, Graph> routeBuilder, NodeSet<Graph> nodeSet, Map<Integer, NodeSet.NodeEntry> from, Map<Integer, NodeSet.NodeEntry> to, float upperBound ) throws RouteNotFoundException;

    public <R> void updateRouteBySingleEdge( RouteBuilder<R, Graph> routeBuilder, NodeSet<Graph> nodeSet ) {
        Pair<NodeSet.NodeEntry, NodeSet.NodeEntry> upperBoundEntries = nodeSet.getUpperBoundEntries();
        NodeSet.NodeEntry from = upperBoundEntries.a;
        NodeSet.NodeEntry to = upperBoundEntries.b;
        if ( from.getEdgeId() != to.getEdgeId() ) {
            throw new AssertionError( "Incompatible single-edge: from = " + from + ", to = " + to );
        }
        routeBuilder.setSingleEdged( from.getEdgeId() );
    }
}
