/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphBuilder;
import cz.certicon.routing.model.entity.GraphBuilderFactory;
import cz.certicon.routing.model.entity.GraphEntityFactory;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleGraphBuilderFactory implements GraphBuilderFactory<Graph> {

    private final GraphEntityFactory graphEntityFactory;
    private final DistanceFactory distanceFactory;

    public SimpleGraphBuilderFactory( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory ) {
        this.graphEntityFactory = graphEntityFactory;
        this.distanceFactory = distanceFactory;
    }

    @Override
    public GraphBuilder<Graph> createGraphBuilder( int nodeCount, int edgeCount ) {
        return new SimpleGraphBuilder( graphEntityFactory, distanceFactory );
    }

}
