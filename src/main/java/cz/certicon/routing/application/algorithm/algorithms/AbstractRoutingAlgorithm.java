/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.algorithms;

import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.application.algorithm.RoutingAlgorithm;
import cz.certicon.routing.model.entity.GraphEntityFactory;

/**
 * Base abstract class for routing algorithms.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public abstract class AbstractRoutingAlgorithm implements RoutingAlgorithm {

    private final Graph graph;
    private final GraphEntityFactory entityAbstractFactory;

    /**
     * Constructor
     *
     * @param graph an instance of {@link Graph} representing the current map topology
     * @param entityAbstractFactory an instance of {@link GraphEntityFactory} for generating the graph-related entities
     */
    public AbstractRoutingAlgorithm( Graph graph, GraphEntityFactory entityAbstractFactory ) {
        this.graph = graph;
        this.entityAbstractFactory = entityAbstractFactory;
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

}
