/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import java.util.Map;

/**
 * The root interface for routing algorithms. It's purpose is to find the
 * shortest path between two points. The distance between two points is abstract
 * (geographical distance, time, etc.).
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public interface RoutingAlgorithm {

    /**
     * Find shortest path between two points.
     * 
     * @param from source point
     * @param to target point
     * @return instance of {@link Path} representing sequence of edges (ways) or null when no path has been found between the two points.
     */
    public Path route( Node.Id from, Node.Id to );
    
    /**
     * Find shortest path between a set of starting points and a target point
     * @param from map of source points (with distances)
     * @param to map of target points (with distances)
     * @return instance of {@link Path} representing sequence of edges (ways) or null when no path has been found between the source set and the target set.
     */
    public Path route(Map<Node.Id, Distance> from, Map<Node.Id, Distance> to);
    
    public RoutingConfiguration getRoutingConfiguration();
}
