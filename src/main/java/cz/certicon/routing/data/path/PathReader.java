/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.path;

import cz.certicon.routing.application.Route;
import cz.certicon.routing.model.entity.PathBuilder;
import cz.certicon.routing.model.entity.Coordinate;
import java.io.IOException;

/**
 * An interface defining a functionality for reading path data based on given
 * route or other data constructed by the algorithm.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <G> graph type
 */
public interface PathReader<G> {

    /**
     * Reads all the necessary data for displaying the route (geometry) and its
     * length or time or other metrics in a form of path object T.
     *
     * @param <T> path type
     * @param pathBuilder path builder of T
     * @param graph graph
     * @param route result of the routing algorithm
     * @param origSource original coordinates of the source point
     * @param origTarget original coordinates of the target point
     * @return path containing all the required data
     * @throws IOException thrown when an IO exception occurs
     */
    public <T> T readPath( PathBuilder<T, G> pathBuilder, G graph, Route route, Coordinate origSource, Coordinate origTarget ) throws IOException;

    /**
     * Reads all the necessary data for displaying the route (geometry) and its
     * length or time or other metrics in a form of path object T.
     *
     * @param <T> path type
     * @param pathBuilder path builder of T
     * @param graph graph
     * @param edgeId id of the edge which both the source and target map to (the
     * closest to both)
     * @param origSource original coordinates of the source point
     * @param origTarget original coordinates of the target point
     * @return path containing all the required data
     * @throws IOException thrown when an IO exception occurs
     */
    public <T> T readPath( PathBuilder<T, G> pathBuilder, G graph, long edgeId, Coordinate origSource, Coordinate origTarget ) throws IOException;
}
