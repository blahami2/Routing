/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.model.entity.DistanceType;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphBuilder;
import cz.certicon.routing.model.entity.GraphBuilderFactory;

/**
 * Simple implementation of the {@link GraphBuilderFactory} interface. Uses maps
 * internally.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleGraphBuilderFactory implements GraphBuilderFactory<Graph> {

    private final DistanceType distanceType;

    /**
     * Constructor of {@link SimpleGraphBuilderFactory}
     *
     * @param distanceType metric
     */
    public SimpleGraphBuilderFactory( DistanceType distanceType ) {
        this.distanceType = distanceType;
    }

    @Override
    public GraphBuilder<Graph> createGraphBuilder( int nodeCount, int edgeCount ) {
        return new SimpleGraphBuilder( nodeCount, edgeCount, distanceType );
    }

}
