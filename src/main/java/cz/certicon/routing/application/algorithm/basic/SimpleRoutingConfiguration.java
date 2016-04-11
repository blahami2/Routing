/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.basic;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceEvaluator;
import cz.certicon.routing.application.algorithm.EdgeValidator;
import cz.certicon.routing.application.algorithm.RoutingConfiguration;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Node;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleRoutingConfiguration implements RoutingConfiguration {

    private DistanceEvaluator distanceEvaluator = new DistanceEvaluator() {
        @Override
        public Distance evaluate( Node sourceNode, Edge edgeFromSourceToTarget, Node targetNode ) {
            return sourceNode.getDistance().add( edgeFromSourceToTarget.getDistance() );
        }
    };
    private EdgeValidator edgeValidator = new EdgeValidator() {
        @Override
        public boolean validate( Edge edge ) {
            return true;
        }
    };

    @Override
    public DistanceEvaluator getDistanceEvaluator() {
        return distanceEvaluator;
    }

    @Override
    public void setDistanceEvaluator( DistanceEvaluator distanceEvaluator ) {
        this.distanceEvaluator = distanceEvaluator;
    }

    @Override
    public EdgeValidator getEdgeValidator() {
        return edgeValidator;
    }

    @Override
    public void setEdgeValidator( EdgeValidator edgeValidator ) {
        this.edgeValidator = edgeValidator;
    }

}
