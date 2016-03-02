/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.osm;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.GraphLoadListener;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.utils.CoordinateUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openstreetmap.osmosis.osmbinary.BinaryParser;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.file.BlockInputStream;
import cz.certicon.routing.data.MapDataSource;
import cz.certicon.routing.model.entity.Coordinate;
import java.util.Arrays;
import cz.certicon.routing.data.coordinates.CoordinateWriter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class AlternativeOsmPbfDataSource implements MapDataSource {

    private final DataSource source;

    public AlternativeOsmPbfDataSource( DataSource source ) throws IOException {
        this.source = source;
    }

    @Override
    public void loadGraph( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, GraphLoadListener graphLoadListener ) throws IOException {
        OsmBinaryParser brad = new OsmBinaryParser( graphEntityFactory, distanceFactory, graphLoadListener );
        BlockInputStream blockInputStream = new BlockInputStream( new BufferedInputStream( source.getInputStream() ), brad );
        blockInputStream.process();
    }

    private class OsmBinaryParser extends BinaryParser {

        private final GraphEntityFactory graphEntityFactory;
        private final DistanceFactory distanceFactory;
        private final GraphLoadListener graphLoadListener;

        private Map<Long, Node> nodeMap = new HashMap<>();
        private Graph graph;

        private Map<Node, List<Edge>> nodeEdgeMap;

        public OsmBinaryParser( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, GraphLoadListener graphLoadListener ) {
            this.graphEntityFactory = graphEntityFactory;
            this.distanceFactory = distanceFactory;
            this.graphLoadListener = graphLoadListener;
            this.graph = graphEntityFactory.createGraph();
            this.nodeEdgeMap = new HashMap<>();
        }

        @Override
        protected void parseRelations( List<Osmformat.Relation> list ) {
//            System.out.println( "relations: " + list.size() );
        }

        @Override
        protected void parseDense( Osmformat.DenseNodes nodes ) {
            long lastId = 0;
            long lastLat = 0;
            long lastLon = 0;
            for ( int i = 0; i < nodes.getIdCount(); i++ ) {
                lastId += nodes.getId( i );
                lastLat += nodes.getLat( i );
                lastLon += nodes.getLon( i );
                Node n = graphEntityFactory.createNode( Node.Id.generateId(), parseLat( lastLat ), parseLon( lastLon ) );
                nodeMap.put( lastId, n );
            }
        }

        @Override
        protected void parseNodes( List<Osmformat.Node> nodes ) {
            nodes.stream().forEach( ( node ) -> {
                Node n = graphEntityFactory.createNode( Node.Id.generateId(), parseLat( node.getLat() ), parseLon( node.getLon() ) );
                nodeMap.put( node.getId(), n );
            } );
        }

        @Override
        protected void parseWays( List<Osmformat.Way> ways ) {
            ways.stream()
                    .filter( ( w ) -> {
                        for ( int i = 0; i < w.getKeysCount(); i++ ) {
                            if ( TagKey.HIGHWAY.equals( TagKey.parse( getStringById( w.getKeys( i ) ) ) ) ) {
                                return true;
                            }
                        }
                        return false;
                    } )
                    .forEach( ( w ) -> {

                        long lastRef = 0;
                        for ( Long ref : w.getRefsList() ) {
                            Node sourceNode = null;
                            Node targetNode = null;
                            if ( lastRef != 0 ) {
                                sourceNode = nodeMap.get( lastRef );
                            }
                            lastRef += ref;
                            if ( sourceNode != null ) {
                                targetNode = nodeMap.get( lastRef );
                                Edge edge = graphEntityFactory.createEdge( Edge.Id.generateId(), sourceNode, targetNode,
                                        distanceFactory.createFromDouble( CoordinateUtils.calculateDistance( sourceNode.getCoordinates(), targetNode.getCoordinates() ) ) );
                                edge.setCoordinates( Arrays.asList( sourceNode.getCoordinates(), targetNode.getCoordinates() ) );
                                getFromMap( sourceNode ).add( edge );
                                getFromMap( targetNode ).add( edge );
                            }
                        }
                    } );
        }

        private List<Edge> getFromMap( Node node ) {
            List<Edge> list = nodeEdgeMap.get( node );
            if ( list == null ) {
                list = new ArrayList<>();
                nodeEdgeMap.put( node, list );
            }
            return list;
        }

        @Override
        protected void parse( Osmformat.HeaderBlock hb ) {
//            System.out.println( "Got header block." );
        }

        @Override
        public void complete() {
            System.out.println( "Complete loading! Starting processing." );
            nodeMap = null;
            for ( Map.Entry<Node, List<Edge>> entry : nodeEdgeMap.entrySet() ) {
                Node node = entry.getKey();
                List<Edge> list = entry.getValue();
                if ( list.size() != 2 ) {
                    node.setLabel( node.getId() + "[" + list.size() + "]" );
                    graph.addNode( node );
                } else {
                }
            }
            for ( Map.Entry<Node, List<Edge>> entry : nodeEdgeMap.entrySet() ) {
                Node node = entry.getKey();
                List<Edge> list = entry.getValue();
                if ( list.size() != 2 ) {
                } else {
                    Edge a = list.get( 0 );
                    Edge b = list.get( 1 );
                    Node nodeA = a.getOtherNode( node );
                    Node nodeB = b.getOtherNode( node );
                    Edge newEdge = graphEntityFactory.createEdge( Edge.Id.generateId(), nodeA, nodeB, a.getDistance().add( b.getDistance() ) );
                    newEdge.setLabel( nodeA.getLabel() + ":" + nodeB.getLabel() );
                    List<Coordinate> coords = new ArrayList<>();
                    // connect coordinates
                    List<Coordinate> aCoords = a.getCoordinates();
                    List<Coordinate> bCoords = b.getCoordinates();
                    if ( node.equals( a.getSourceNode() ) ) {
                        for ( int i = aCoords.size() - 1; i >= 0; i-- ) {
                            coords.add( aCoords.get( i ) );
                        }
                    } else {
                        for ( int i = 0; i < aCoords.size(); i++ ) {
                            coords.add( aCoords.get( i ) );
                        }
                    }
                    if ( node.equals( b.getSourceNode() ) ) {
                        for ( int i = 1; i < bCoords.size(); i++ ) {
                            coords.add( bCoords.get( i ) );
                        }
                    } else {
                        for ( int i = bCoords.size() - 2; i >= 0; i-- ) {
                            coords.add( bCoords.get( i ) );
                        }
                    }
                    newEdge.setCoordinates( coords );
//                    System.out.println( "new edge has " + newEdge.getCoordinates().size() + " coordinates" );
//                    System.out.println( "orig edge: " + nodeA.getLabel() + ":" + node.getLabel() + ":" + nodeB.getLabel() );
//                    System.out.println( "new edge: " + newEdge.getLabel() );
                    List<Edge> aList = getFromMap( nodeA );
                    aList.remove( a );
                    aList.add( newEdge );
                    List<Edge> bList = getFromMap( nodeB );
                    bList.remove( b );
                    bList.add( newEdge );
                }
            }
            for ( Node node : graph.getNodes() ) {
                for ( Edge edge : getFromMap( node ) ) {
                    if ( node.equals( edge.getSourceNode() ) ) {
                        edge.setLabel( edge.getId() + "|" + edge.getSourceNode().getLabel() + ":" + edge.getTargetNode().getLabel() );
//                        System.out.println( "added edge has " + edge.getCoordinates().size() + " coordinates" );
                        graph.addEdge( edge );
                    }
                }
            }

            // map ids to sequence
            Graph g = graph;
            graph = graphEntityFactory.createGraph();

            Map<Node, Node> oldToNewMap = new HashMap<>();
            int nodeCounter = 0;
            for ( Node node : g.getNodes() ) {
                Node newNode = node.createCopyWithNewId( Node.Id.createId( nodeCounter++ ) );
                newNode.setLabel( newNode.getId() + "[" + getFromMap( node ).size() + "]" );
                oldToNewMap.put( node, newNode ); // old node edge set is copied to the new node
                graph.addNode( newNode );
            }
            int edgeCounter = 0;
            for ( Edge edge : g.getEdges() ) {
//                System.out.println( "old old edge has " + edge.getCoordinates().size() + " coordinates" );
                edge = edge.newNodes( oldToNewMap.get( edge.getSourceNode() ), oldToNewMap.get( edge.getTargetNode() ) );
//                System.out.println( "old edge has " + edge.getCoordinates().size() + " coordinates" );
                Edge newEdge = edge.createCopyWithNewId( Edge.Id.createId( edgeCounter++ ) );
                newEdge.setLabel( newEdge.getId() + "|" + newEdge.getSourceNode().getLabel() + ":" + newEdge.getTargetNode().getLabel() );
//                System.out.println( "new edge has " + newEdge.getCoordinates().size() + " coordinates" );
                graph.addEdge( newEdge );
            }
            System.out.println( "Processing done!" );

            graphLoadListener.onGraphLoaded( graph );
//            try {
//                onLoadFinish( graphEntityFactory, distanceFactory, graphLoadListener, graph );
//            } catch ( IOException ex ) {
//                Logger.getLogger( OsmPbfDataSource.class.getName() ).log( Level.SEVERE, null, ex );
//            }
        }

    }
}
