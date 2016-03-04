/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.algorithms;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.application.algorithm.RoutingAlgorithm;
import cz.certicon.routing.application.algorithm.RoutingConfiguration;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;

/**
 * Base abstract class for routing algorithms.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public abstract class AbstractRoutingAlgorithm implements RoutingAlgorithm {

    private final Graph graph;
    private final GraphEntityFactory entityAbstractFactory;
    private final DistanceFactory distanceFactory;
    private RoutingConfiguration routingConfiguration;

    /**
     * Constructor
     *
     * @param graph an instance of {@link Graph} representing the current map
     * topology
     * @param entityAbstractFactory an instance of {@link GraphEntityFactory}
     * for generating the graph-related entities (must be consistent with the
     * given graph)
     * @param distanceFactory an instance of {@link DistanceFactory} for
     * creating the distances (must be consistent with the given graph)
     */
    public AbstractRoutingAlgorithm( Graph graph, GraphEntityFactory entityAbstractFactory, DistanceFactory distanceFactory ) {
        this.graph = graph;
        this.entityAbstractFactory = entityAbstractFactory;
        this.distanceFactory = distanceFactory;
        this.routingConfiguration = () -> ( Node sourceNode, Edge edgeFromSourceToTarget, Node targetNode ) -> sourceNode.getDistance().add( edgeFromSourceToTarget.getDistance() );
    }

    /**
     * Getter for {@link Graph}
     *
     * @return instance of {@link Graph} representing the current map topology
     */
    protected Graph getGraph() {
        return graph;
    }

    /**
     * Getter for {@link GraphEntityFactory}
     *
     * @return instance if {@link GraphEntityFactory} used for graph-related
     * objects creation
     */
    protected GraphEntityFactory getEntityAbstractFactory() {
        return entityAbstractFactory;
    }

    protected RoutingConfiguration getRoutingConfiguration() {
        return routingConfiguration;
    }

    protected DistanceFactory getDistanceFactory() {
        return distanceFactory;
    }

    public AbstractRoutingAlgorithm setRoutingConfiguration( RoutingConfiguration routingConfiguration ) {
        this.routingConfiguration = routingConfiguration;
        return this;
    }

}
