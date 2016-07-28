/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.data.nodesearch.EvaluableOnlyException;
import cz.certicon.routing.model.entity.DistanceType;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.NodeSet;
import cz.certicon.routing.model.entity.NodeSet.NodeCategory;
import cz.certicon.routing.model.entity.NodeSet.NodeEntry;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.NodeSetBuilder;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.utils.DoubleComparator;
import java.util.Iterator;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleNodeSetBuilder implements NodeSetBuilder<NodeSet<Graph>> {

    private final DistanceType distanceType;
    private final Graph graph;
    private final NodeSet<Graph> nodeSet;

    public SimpleNodeSetBuilder( Graph graph, DistanceType distanceType ) {
        this.distanceType = distanceType;
        this.graph = graph;
        this.nodeSet = new SimpleNodeSet();
    }

    @Override
    public NodeSet<Graph> build() throws EvaluableOnlyException {
        Iterator<NodeEntry> itSource = nodeSet.iterator( NodeCategory.SOURCE );
        while ( itSource.hasNext() ) {
            Iterator<NodeEntry> itTarget = nodeSet.iterator( NodeCategory.TARGET );
            NodeEntry sourceEntry = itSource.next();
            while ( itTarget.hasNext() ) {
                NodeEntry targetEntry = itTarget.next();
                if ( sourceEntry.getEdgeId() == targetEntry.getEdgeId() ) {
                    float edgeLength = graph.getLength( graph.getEdgeByOrigId( sourceEntry.getEdgeId() ) );
                    float sourceDist = edgeLength - sourceEntry.getDistance();
                    float targetDist = targetEntry.getDistance();
                    System.out.println( "comparing: " + sourceDist + " vs " + targetDist );
                    if ( DoubleComparator.isLowerOrEqualTo( sourceDist, targetDist, CoordinateUtils.DISTANCE_PRECISION_METERS ) ) {
                        int srcNode = graph.getNodeByOrigId( sourceEntry.getNodeId() );
                        int tarNode = graph.getNodeByOrigId( targetEntry.getNodeId() );
                        Coordinate src = new Coordinate( graph.getLatitude( srcNode ), graph.getLongitude( srcNode ) );
                        Coordinate tar = new Coordinate( graph.getLatitude( tarNode ), graph.getLongitude( tarNode ) );
                        throw new EvaluableOnlyException( sourceEntry.getEdgeId(), src, tar );
                    }
                }
            }
        }
        return nodeSet;
    }

    @Override
    public void addNode( NodeCategory nodeCategory, long nodeId, long edgeId, float length, float speed ) {
        float dist = (float) distanceType.calculateDistance( length, speed );
        nodeSet.put( nodeCategory, edgeId, nodeId, dist );
    }

    @Override
    public void addCrossroad( NodeCategory nodeCategory, long nodeId ) {
        nodeSet.put( nodeCategory, -1, nodeId, 0 );
    }

}
