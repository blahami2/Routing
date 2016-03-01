/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.application.algorithm.Distance;
import java.util.List;

/**
 * The root interface for graph edge
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Edge {

    /**
     * Getter for length represented by an instance of {@link Distance}
     *
     * @return {@link Distance}
     */
    public Distance getDistance();

    /**
     * Getter for attributes
     *
     * @return an instance of {@link EdgeAttributes}
     */
    public EdgeAttributes getAttributes();

    /**
     * Setter for attributes
     *
     * @param attributes an instance of {@link EdgeAttributes}
     * @return this instance
     */
    public Edge setAttributes( EdgeAttributes attributes );

    /**
     * Getter for label represented by an instance of {@link String}
     *
     * @return {@link String}
     */
    public String getLabel();

    /**
     * Setter for label
     *
     * @param label string
     * @return this instance
     */
    public Edge setLabel( String label );

    /**
     * Setter for distance
     *
     * @param newDistance instance of {@link Distance}
     * @return this instance
     */
    public Edge setDistance( Distance newDistance );

    /**
     * Getter for coordinates of the path
     *
     * @param graph graph to optionally load coordinates from
     * @return path coordinates
     */
    public List<Coordinates> getCoordinates( Graph graph );

    /**
     * Setter for coordinates of the path
     *
     * @param coordinates list of coordinates representing the path
     * @return this instance
     */
    public Edge setCoordinates( List<Coordinates> coordinates );

    /**
     * Getter for the source point of this edge
     *
     * @return an instance of {@link Node}
     */
    public Node getSourceNode();

    /**
     * Getter for the target point of this edge
     *
     * @return an instance of {@link Node}
     */
    public Node getTargetNode();

    /**
     * Getter for the other node than the given node
     *
     * @param node an instance of {@link Node} connected to this edge
     * @return the other node connected to this edge
     * @throws IllegalArgumentException thrown when a node is not connected to
     * this edge
     */
    public Node getOtherNode( Node node );

    /**
     * Setter for a new source node. IMPORTANT: this method creates a new
     * instance of the {@link Edge}! In context of nodes, the edge is immutable.
     *
     * @param sourceNode new source {@link Node}
     * @return new instance of {@link Edge} with a given {@link Node} as a
     * source node
     */
    public Edge newSourceNode( Node sourceNode );

    /**
     * Setter for a new target node. IMPORTANT: this method creates a new
     * instance of the {@link Edge}! In context of nodes, the edge is immutable.
     *
     * @param targetNode new target {@link Node}
     * @return new instance of {@link Edge} with a given {@link Node} as a
     * target node
     */
    public Edge newTargetNode( Node targetNode );

    /**
     * Setter for a new source and target nodes. IMPORTANT: this method creates
     * a new instance of the {@link Edge}! In context of nodes, the edge is
     * immutable.
     *
     * @param sourceNode new source {@link Node}
     * @param targetNode new target {@link Node}
     * @return new instance of {@link Edge} with a given {@link Node} as a
     * source node and a target node
     */
    public Edge newNodes( Node sourceNode, Node targetNode );
}
