/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import java.util.List;

/**
 * Builder for paths
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> path type
 * @param <G> graph type
 */
public interface PathBuilder<T, G> {

    /**
     * Adds start edge, which is usually found independently of routing - it is
     * the closest edge to the original source location.
     *
     * @param graph related graph
     * @param edgeId global edge id
     * @param isForward whether this edge is the forward one in relation to data
     * (for example, if it is not forward, the geometry (coordinates) should be
     * reversed
     * @param coordinates edge's coordinates (geometry)
     * @param length edge's length in meters
     * @param time time to travel pass the edge in seconds
     */
    public void addStartEdge( G graph, long edgeId, boolean isForward, List<Coordinate> coordinates, double length, double time );

    /**
     * Adds end edge, which is usually found independently of routing - it is
     * the closest edge to the original target location.
     *
     * @param graph related graph
     * @param edgeId global edge id
     * @param isForward whether this edge is the forward one in relation to data
     * (for example, if it is not forward, the geometry (coordinates) should be
     * reversed
     * @param coordinates edge's coordinates (geometry)
     * @param length edge's length in meters
     * @param time time to travel pass the edge in seconds
     */
    public void addEndEdge( G graph, long edgeId, boolean isForward, List<Coordinate> coordinates, double length, double time );

    /**
     * Adds an edge (to the end of list)
     *
     * @param graph related graph
     * @param edgeId global edge id
     * @param isForward whether this edge is the forward one in relation to data
     * (for example, if it is not forward, the geometry (coordinates) should be
     * reversed
     * @param coordinates edge's coordinates (geometry)
     * @param length edge's length in meters
     * @param time time to travel pass the edge in seconds
     */
    public void addEdge( G graph, long edgeId, boolean isForward, List<Coordinate> coordinates, double length, double time );

    /**
     * Adds coordinate (single pair of latitude and longitude - a point)
     *
     * @param coordinate given coordinate
     */
    public void addCoordinates( Coordinate coordinate );

    /**
     * Adds length to the current length (additional length)
     *
     * @param length given length in meters
     */
    public void addLength( double length );

    /**
     * Adds time to the current time (additional time)
     *
     * @param time given time in seconds
     */
    public void addTime( double time );

    /**
     * Clears this builder
     */
    public void clear();

    /**
     * Creates and returns the path from the given data
     *
     * @param graph related graph
     * @param sourceCoordinate original location of the source
     * @param targetCoordinate original location of the target
     * @return path based on the accumulated data
     */
    public T build( Graph graph, Coordinate sourceCoordinate, Coordinate targetCoordinate );
}
