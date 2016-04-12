/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.application.algorithm.Distance;
import java.util.List;

/**
 * The root interface for representation of path (as a sequence of edges)
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Path extends Iterable<Edge> {

    /**
     * Adds edge into the path (does not guarantee order)
     *
     * @param edge an instance of {@link Edge} to be added
     * @return this instance
     */
    public Path addEdge( Edge edge );

    /**
     * Adds edge to the beginning of this path
     *
     * @param edge an instance of {@link Edge} to be added
     * @return this instance
     */
    public Path addEdgeAsFirst( Edge edge );

    /**
     * Adds edge to the end of this path
     *
     * @param edge an instance of {@link Edge} to be added
     * @return this instance
     */
    public Path addEdgeAsLast( Edge edge );

    /**
     * Getter for the first node in this path (source)
     *
     * @return an instance of {@link Node}
     */
    public Node getSourceNode();

    /**
     * Getter for the last node in this path (target)
     *
     * @return an instance of {@link Node}
     */
    public Node getTargetNode();

    /**
     * Getter for the graph which is this path based on
     *
     * @return an instance of {@link Graph}
     */
    public Graph getGraph();

    /**
     * Getter for the abstract length of this path
     *
     * @return an instance of {@link Distance}
     */
    public Distance getDistance();

    /**
     * Getter for the overall length of this path
     *
     * @return double length in meters
     */
    public double getLength();
    
    /**
     * Getter for the overall time of this path (estimated)
     * @return double time in seconds
     */
    public double getTime();

    /**
     * Connects other path to the end of this path
     *
     * @param otherPath an instance of {@link Path} to be connected
     * @return this instance
     */
    public Path connectWith( Path otherPath );

    /**
     * Retrieves an aggregation of string representations from all the edges in
     * this path
     *
     * @return an instance of {@link String} representing connected edge labels
     */
    public String toStringLabelPath();

    /**
     * Retrieves number of edges in this path.
     *
     * @return integer amount of edges
     */
    public int size();
    
    public List<Edge> getEdges();
    
    public List<Node> getNodes();
    
    public List<Coordinates> getCoordinates();
}
