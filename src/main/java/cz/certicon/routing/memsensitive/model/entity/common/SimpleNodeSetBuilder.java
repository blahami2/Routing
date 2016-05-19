/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.model.entity.NodeSetBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleNodeSetBuilder implements NodeSetBuilder<Map<Integer, Double>> {

    private final DistanceType distanceType;
    private final Graph graph;
    private final Map<Integer, Double> nodeDistanceMap;

    public SimpleNodeSetBuilder( Graph graph, DistanceType distanceType ) {
        this.distanceType = distanceType;
        this.graph = graph;
        this.nodeDistanceMap = new HashMap<>();
    }

    @Override
    public void addNode( long nodeId, long edgeId, double length, double speed ) {
        double dist = distanceType.calculateDistance( length, speed );
        nodeDistanceMap.put( graph.getNodeByOrigId( nodeId ), dist );
    }

    @Override
    public void addCrossroad( long nodeId ) {
        nodeDistanceMap.put( graph.getNodeByOrigId( nodeId ), 0.0 );
    }

    @Override
    public Map<Integer, Double> build() {
        return nodeDistanceMap;
    }

}
