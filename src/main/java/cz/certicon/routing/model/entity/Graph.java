/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import java.util.Set;

/**
 * The root interface for graph representation of a map topology
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public interface Graph {

    /**
     * Adds node to the graph
     *
     * @param node an instance of {@link Node} to be added
     * @return this instance
     */
    public Graph addNode( Node node );

    /**
     * Removes node from the graph. Also removes all connected edges.
     *
     * @param node an instance of {@link Node} to be removed
     * @return this instance
     */
    public Graph removeNode( Node node );

    /**
     * Adds edge to the graph
     *
     * @param edge an instance of {@link Edge} to be added
     * @return this instance
     */
    public Graph addEdge( Edge edge );

    /**
     * Adds edge to the graph, sets source and target node to the edge if necessary
     *
     * @param sourceNode source {@link Node} of the given {@link Edge}
     * @param targetNode target {@link Node} of the given {@link Edge}
     * @param edge an instance of {@link Edge} to be added
     * @return this instance
     */
    public Graph addEdge( Node sourceNode, Node targetNode, Edge edge );

    /**
     * Removes edge from the graph
     *
     * @param edge an instance of {@link Edge} to be removed
     * @return this instance
     */
    public Graph removeEdge( Edge edge );

    /**
     * Getter for the source node of the given edge
     *
     * @param edge an instance of {@link Edge} to determine the node
     * @return source {@link Node}
     */
    public Node getSourceNodeOf( Edge edge );

    /**
     * Getter for the target node of the given edge
     *
     * @param edge an instance of {@link Edge} to determine the node
     * @return target {@link Node}
     */
    public Node getTargetNodeOf( Edge edge );
    
/**
     * Getter for the other node connected to the edge than the given node
     *
     * @param edge an instance of {@link Edge} connected to the two nodes
     * @param node an instance of {@link Node} connected to this edge
     * @return the other node connected to the edge
     * @throws IllegalArgumentException thrown when a node is not connected to
     * this edge
     */
    public Node getOtherNodeOf(Edge edge, Node node);

    /**
     * Getter for all the edges of the given node
     *
     * @param node an instance of {@link Node} to determine the edges
     * @return {@link Set} of {@link Edge}s
     */
    public Set<Edge> getEdgesOf( Node node );

    /**
     * Getter for the incoming edges to the given node
     *
     * @param node an instance of {@link Node} to determine the edges
     * @return {@link Set} of {@link Edge}s
     */
    public Set<Edge> getIncomingEdgesOf( Node node );

    /**
     * Getter for the outgoing edges of the given node
     *
     * @param node an instance of {@link Node} to determine the edges
     * @return {@link Set} of {@link Edge}s
     */
    public Set<Edge> getOutgoingEdgesOf( Node node );

    /**
     * Getter for the degree of the given node (amount of all the edges somehow directly connected to this node)
     *
     * @param node an instance of {@link Node}
     * @return integer number of connected edges
     */
    public int getDegreeOf( Node node );

    /**
     * Getter for the in degree of the given node (amount of the incoming edges directly connected to this node)
     *
     * @param node an instance of {@link Node}
     * @return integer number of incoming edges
     */
    public int getInDegreeOf( Node node );

    /**
     * Getter for the out degree of the given node (amount of the outgoing edges directly connected to this node)
     *
     * @param node an instance of {@link Node}
     * @return integer number of outgoing edges
     */
    public int getOutDegreeOf( Node node );

    /**
     * Getter for all the nodes in this graph
     *
     * @return {@link Set} of {@link Node}s
     */
    public Set<Node> getNodes();

    /**
     * Getter for all the edges in this graph
     *
     * @return {@link Set} of {@link Node}s
     */
    public Set<Edge> getEdges();
}
