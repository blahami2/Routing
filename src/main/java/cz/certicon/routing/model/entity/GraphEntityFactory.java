/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.application.algorithm.Distance;

/**
 * The root interface for graph-related entity factories
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface GraphEntityFactory {

    /**
     * Creates a node for the given coordinates
     *
     * @param id an instance of {@link Node.Id} as an identifier of this node
     * @param latitude double representation of latitude as a part of node's
     * coordinates
     * @param longitude double representation of longitude as a part of node's
     * coordinates
     * @return an instance of {@link Node}
     */
    public Node createNode( Node.Id id, double latitude, double longitude );

    /**
     * Creates an edge between the given source and target nodes
     *
     * @param id an instance of {@link Edge.Id} as an identifier of this edge
     * @param sourceNode an instance of {@link Node} as a source of the edge
     * @param targetNode an instance of {@link Node} as a target of the edge
     * @param length abstract length of the edged represented by an instance of
     * {@link Distance}
     * @return an instance of {@link Edge}
     */
    public Edge createEdge( Edge.Id id, Node sourceNode, Node targetNode, Distance length );

    /**
     * Creates an empty path with a source node
     *
     * @param graph an instance of {@link Graph} as a base for the new path
     * @param sourceNode an instance of {@link Node} representing a starting
     * point of this path
     * @return an instance of {@link Path}
     */
    public Path createPathWithSource( Graph graph, Node sourceNode );

    /**
     * Creates an empty path with a target node
     *
     * @param graph
     * @param targetNode an instance of {@link Node} representing an end point
     * of this path
     * @return
     */
    public Path createPathWithTarget( Graph graph, Node targetNode );

    /**
     * Creates an empty graph
     *
     * @return an instance of {@link Graph}
     */
    public Graph createGraph();
}
