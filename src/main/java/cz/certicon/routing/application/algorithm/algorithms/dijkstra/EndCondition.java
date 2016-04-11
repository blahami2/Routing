/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.algorithms.dijkstra;

import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;

/**
 * Algorithm determination  class, enables various settings of the same algorithm.
 * 
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface EndCondition {

    /**
     * Determines, whether the computation is done (the algorithm has reached a result).
     * 
     * @param graph graph upon which is the computation performed
     * @param sourceNode source node of the route
     * @param targetNode target node of the route
     * @param currentNode currently examined node
     * @return true if the algorithm is supposed to end, false otherwise
     */
    public boolean isFinished( Graph graph, Node sourceNode, Node targetNode, Node currentNode );

    /**
     * Creates required result based on the algorithm progress
     * 
     * @param graph graph upon which is the computation performed
     * @param graphEntityFactory factory for graph entity creation
     * @param sourceNode source node of the route
     * @param targetNode target node of the route
     * @return result of the algorithm
     */
    public Path getResult( Graph graph, GraphEntityFactory graphEntityFactory, Node sourceNode, Node targetNode );
}
