/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.model.entity.DistanceType;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.NodeSet;
import cz.certicon.routing.model.entity.NodeSet.NodeCategory;
import cz.certicon.routing.model.entity.NodeSet.NodeEntry;
import cz.certicon.routing.model.entity.NodeSetBuilder;
import java.util.Iterator;

/**
 * Simple implementation of the {@link NodeSetBuilder} interface. Uses maps
 * internally.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleNodeSetBuilder implements NodeSetBuilder<NodeSet<Graph>> {

    private final DistanceType distanceType;
    private final Graph graph;
    private final NodeSet<Graph> nodeSet;

    /**
     * Constructor of {@link NodeSetBuilder}
     *
     * @param graph graph
     * @param distanceType metric
     */
    public SimpleNodeSetBuilder( Graph graph, DistanceType distanceType ) {
        this.distanceType = distanceType;
        this.graph = graph;
        this.nodeSet = new SimpleNodeSet();
    }

    @Override
    public NodeSet<Graph> build() {
        Iterator<NodeEntry> itSource = nodeSet.iterator( NodeCategory.SOURCE );
        while ( itSource.hasNext() ) {
            Iterator<NodeEntry> itTarget = nodeSet.iterator( NodeCategory.TARGET );
            NodeEntry sourceEntry = itSource.next();
            while ( itTarget.hasNext() ) {
                NodeEntry targetEntry = itTarget.next();
                if ( sourceEntry.getEdgeId() != -1 && sourceEntry.getEdgeId() == targetEntry.getEdgeId() && sourceEntry.getDistance() + targetEntry.getDistance() - graph.getLength( graph.getEdgeByOrigId( sourceEntry.getEdgeId() ) ) > 0 ) {
                    nodeSet.putUpperBound( sourceEntry, targetEntry, sourceEntry.getDistance() + targetEntry.getDistance() - graph.getLength( graph.getEdgeByOrigId( sourceEntry.getEdgeId() ) ) );
                }
            }
        }
        return nodeSet;
    }

    @Override
    public void addNode( NodeCategory nodeCategory, long nodeId, long edgeId, float length, float speed ) {
        float dist = (float) distanceType.calculateDistance( length, speed );
        nodeSet.put( graph, nodeCategory, edgeId, nodeId, dist );
    }

    @Override
    public void addCrossroad( NodeCategory nodeCategory, long nodeId ) {
        nodeSet.put( graph, nodeCategory, -1, nodeId, 0 );
    }

}
