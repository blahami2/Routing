/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

import cz.certicon.routing.application.algorithm.EdgeValidator;

/**
 * The root interface for routing configurations. The routing configuration
 * serves as additional query criteria.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface RoutingConfiguration {

    /**
     * Serves simply as a supplier of {@link DistanceEvaluator}.
     *
     * @return evaluator for calculating the node distance.
     */
    public DistanceEvaluator getDistanceEvaluator();

    public void setDistanceEvaluator( DistanceEvaluator distanceEvaluator );

    /**
     * Serves simply as a supplier of {@link EdgeValidator}.
     *
     * @return validator for determining whether the edge is valid
     */
    public EdgeValidator getEdgeValidator();

    public void setEdgeValidator( EdgeValidator edgeValidator );
}
