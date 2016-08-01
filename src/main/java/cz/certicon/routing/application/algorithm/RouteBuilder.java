/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

/**
 * An interface defining the functionality of a route builder - where route and
 * graphs can be defined later in the implementation.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <R> the route class to be built
 * @param <G> the graph to be built upon
 */
public interface RouteBuilder<R, G> {

    /**
     * Sets the source node of this route. It is recommended to set source or
     * target before adding edges, so that the other one can be determined
     * automatically and the edges can be set appropriately.
     *
     * @param graph a graph to be built upon
     * @param nodeId an id of the source node
     */
    public void setSourceNode( G graph, long nodeId );

    /**
     * Sets the target node of this route. It is recommended to set source or
     * target before adding edges, so that the other one can be determined
     * automatically and the edges can be set appropriately.
     *
     * @param graph a graph to be built upon
     * @param nodeId an id of the target node
     */
    public void setTargetNode( G graph, long nodeId );

    /**
     * Adds edge to the beginning of this sequence. Moves the source to the
     * other end of added edge. Checks the connection (one node of this edge
     * must match the current source node).
     *
     * @param graph a graph to be built upon
     * @param edgeId an id of the added edge
     */
    public void addEdgeAsFirst( G graph, long edgeId );

    /**
     * Adds edge to the end of this sequence. Moves the target to the other end
     * of added edge. Checks the connection (one node of this edge must match
     * the current target node).
     *
     * @param graph a graph to be built upon
     * @param edgeId an id of the added edge
     */
    public void addEdgeAsLast( G graph, long edgeId );

    /**
     * Clears the builder for later use
     */
    public void clear();

    /**
     * Builds the route
     *
     * @return the built route
     */
    public R build();
}
