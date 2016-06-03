/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.NodeSet;
import cz.certicon.routing.model.entity.NodeSetBuilder;
import cz.certicon.routing.model.entity.NodeSetBuilderFactory;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleNodeSetBuilderFactory implements NodeSetBuilderFactory<NodeSet<Graph>> {

    private final DistanceType distanceType;
    private final Graph graph;

    public SimpleNodeSetBuilderFactory( Graph graph, DistanceType distanceType ) {
        this.graph = graph;
        this.distanceType = distanceType;
    }

    @Override
    public NodeSetBuilder<NodeSet<Graph>> createNodeSetBuilder() {
        return new SimpleNodeSetBuilder( graph, distanceType );
    }

}
