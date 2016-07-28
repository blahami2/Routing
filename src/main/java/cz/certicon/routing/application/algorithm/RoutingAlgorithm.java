/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

import java.util.Map;

/**
 * The root interface for routing algorithms. It's purpose is to find the
 * shortest path between two points. The distance between two points is abstract
 * (geographical distance, time, etc.).
 *
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface RoutingAlgorithm<G> {

    /**
     * Find the shortest route (R) between a set of source points to a set of
     * target points
     *
     * @param <R> route return type, see {@link RouteBuilder}
     * @param routeBuilder builder for the result route
     * @param from a set of source points (and their initial distances)
     * @param to a set of target points (and their initial distances)
     * @return the shortest route of type R
     * @throws RouteNotFoundException thrown when no route was found between the
     * two points
     */
    public <R> R route( RouteBuilder<R, G> routeBuilder, Map<Integer, Float> from, Map<Integer, Float> to ) throws RouteNotFoundException;
}
