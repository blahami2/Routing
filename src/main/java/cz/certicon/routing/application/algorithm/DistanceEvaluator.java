/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Node;

/**
 * An interface for distance evaluators. A distance evaluator calculates the distance of
 * a certain node using a previous node and an edge connecting them.
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public interface DistanceEvaluator {

    /**
     * Evaluates distance of the target node.
     *
     * @param sourceNode node to start the calculation from
     * @param edgeFromSourceToTarget edge used for calculation
     * @param targetNode target node
     * @return distance of the target node
     */
    public Distance evaluate( Node sourceNode, Edge edgeFromSourceToTarget, Node targetNode );
    
        /**
     * Evaluates distance of the target node.
     *
     * @param sourceNode node to start the calculation from
     * @param edgeFromSourceToTarget edge used for calculation
     * @param targetNode target node
     * @param targetNodeDistanceToTarget distance of the target node to the actual target (precomputed)
     * @return distance of the target node
     */
    public Distance evaluate( Node sourceNode, Edge edgeFromSourceToTarget, Node targetNode, Distance targetNodeDistanceToTarget );
}
