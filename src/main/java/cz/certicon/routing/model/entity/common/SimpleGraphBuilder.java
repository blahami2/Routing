/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphBuilder;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleGraphBuilder implements GraphBuilder<Graph> {

    private final GraphEntityFactory graphEntityFactory;
    private final DistanceFactory distanceFactory;
    private final Graph graph;

    public SimpleGraphBuilder( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory ) {
        this.distanceFactory = distanceFactory;
        this.graphEntityFactory = graphEntityFactory;
        this.graph = this.graphEntityFactory.createGraph();
    }

    @Override
    public void addNode( long id, long dataId, long osmId, double latitude, double longitude ) {
        graph.addNode( graphEntityFactory.createNode( Node.Id.createId( id ), latitude, longitude ) );
    }

    @Override
    public void addEdge( long id, long dataId, long osmId, long sourceId, long targetId, double length, double speed, boolean isPaid ) {
        Edge edge = graphEntityFactory.createEdge(
                Edge.Id.createId( id ),
                graph.getNode( Node.Id.createId( sourceId ) ),
                graph.getNode( Node.Id.createId( targetId ) ),
                distanceFactory.createFromEdgeData( new SimpleEdgeData( (int) ( speed + 0.0001 ), isPaid, length ) )
        );
        edge.setAttributes( SimpleEdgeAttributes.builder().setLength( length ).setPaid( isPaid ).build() );
        graph.addEdge( edge );
    }

    @Override
    public Graph build() {
        return graph;
    }

}
