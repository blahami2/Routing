/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.model.entity.DistanceType;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.NodeSet;
import cz.certicon.routing.model.entity.NodeSetBuilder;
import cz.certicon.routing.model.entity.NodeSetBuilderFactory;

/**
 * Simple implementation of the {@link NodeSetBuilderFactory} interface. Uses
 * maps internally.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleNodeSetBuilderFactory implements NodeSetBuilderFactory<NodeSet<Graph>> {

    private final DistanceType distanceType;
    private final Graph graph;

    /**
     * Constructor of {@link SimpleNodeSetBuilderFactory}
     *
     * @param graph graph
     * @param distanceType metric
     */
    public SimpleNodeSetBuilderFactory( Graph graph, DistanceType distanceType ) {
        this.graph = graph;
        this.distanceType = distanceType;
    }

    @Override
    public NodeSetBuilder<NodeSet<Graph>> createNodeSetBuilder() {
        return new SimpleNodeSetBuilder( graph, distanceType );
    }

}
