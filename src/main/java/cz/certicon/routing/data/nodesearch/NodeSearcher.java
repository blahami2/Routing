/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.nodesearch;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Node;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface NodeSearcher {

    /**
     * Based on given coordinates find the closest nodes and actual distances (in kilometers) to them
     * 
     * @param coordinates a geographical point specifying the approximate location
     * @param distanceFactory factory for distance creation
     * @param searchfor determines what to search for
     * @return a {@link Map} of node id's and distances (in kilometers) representing the set of closest nodes, from which the correct one cannot easily be determined
     * @throws java.io.IOException thrown when an error occurs while searching
     */
    public Map<Node.Id, Distance> findClosestNodes( Coordinates coordinates, DistanceFactory distanceFactory, SearchFor searchfor ) throws IOException;
    
    public static enum SearchFor {
        SOURCE, TARGET;
    }
}
