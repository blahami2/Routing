/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class MockupDataSource implements MapDataSource {

    @Override
    public void loadGraph( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, GraphLoadListener graphLoadListener ) throws IOException {
        Graph graph = graphEntityFactory.createGraph();
        Node a = graphEntityFactory.createNode( Node.Id.generateId(), 50.1001831, 14.3856114 );
        Node b = graphEntityFactory.createNode( Node.Id.generateId(), 50.1002725, 14.3872906 );
        Node c = graphEntityFactory.createNode( Node.Id.generateId(), 50.1018347, 14.3857995 );
        Node d = graphEntityFactory.createNode( Node.Id.generateId(), 50.1017039, 14.3871028 );
        Node e = graphEntityFactory.createNode( Node.Id.generateId(), 50.1002828, 14.3878056 );
        Node f = graphEntityFactory.createNode( Node.Id.generateId(), 50.1016489, 14.3876339 );
        Edge ab = createEdge( graphEntityFactory, distanceFactory, a, b );
        Edge ba = createEdge( graphEntityFactory, distanceFactory, b, a );
        Edge ac = createEdge( graphEntityFactory, distanceFactory, a, c );
        Edge ca = createEdge( graphEntityFactory, distanceFactory, c, a );
        Edge db = createEdge( graphEntityFactory, distanceFactory, d, b );
        Edge cd = createEdge( graphEntityFactory, distanceFactory, c, d );
        Edge dc = createEdge( graphEntityFactory, distanceFactory, d, c );
        Edge be = createEdge( graphEntityFactory, distanceFactory, b, e );
        Edge eb = createEdge( graphEntityFactory, distanceFactory, e, b );
        Edge df = createEdge( graphEntityFactory, distanceFactory, d, f );
        Edge fd = createEdge( graphEntityFactory, distanceFactory, f, d );
        Edge ef = createEdge( graphEntityFactory, distanceFactory, e, f );
        graph.addNode( a ).addNode( b ).addNode( c ).addNode( d ).addNode( e ).addNode( f );
        graph.addEdge( a, b, ab )
                .addEdge( b, a, ba )
                .addEdge( a, c, ac )
                .addEdge( c, a, ca )
                .addEdge( d, b, db )
                .addEdge( c, d, cd )
                .addEdge( c, d, dc )
                .addEdge( b, e, be )
                .addEdge( e, b, eb )
                .addEdge( d, f, df )
                .addEdge( f, d, fd )
                .addEdge( e, f, ef );
        graphLoadListener.onGraphLoaded( graph );
    }

    private static Edge createEdge( GraphEntityFactory entityFactory, DistanceFactory distanceFactory, Node sourceNode, Node targetNode ) {
        return entityFactory.createEdge( Edge.Id.generateId(), sourceNode, targetNode, distanceFactory.createFromDouble( CoordinateUtils.calculateDistance( sourceNode.getCoordinates(), targetNode.getCoordinates() ) ) );
    }
}
