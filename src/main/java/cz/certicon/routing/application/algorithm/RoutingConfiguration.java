/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

/**
 * The root interface for routing configurations. The routing configuration
 * serves as additional query criteria.
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public interface RoutingConfiguration {

    /**
     * Serves simply as a supplier of {@link NodeEvaluator}.
     *
     * @return evaluator for calculating the node distance.
     */
    public NodeEvaluator getNodeEvaluator();
}
