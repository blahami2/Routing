/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.osm;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.GraphLoadListener;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.utils.CoordinateUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.osmosis.osmbinary.BinaryParser;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.file.BlockInputStream;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class OsmPbfDataSource implements DataSource {

    private final InputStream input;

    public OsmPbfDataSource( File input ) throws FileNotFoundException {
        this.input = new BufferedInputStream( new FileInputStream( input ) );

    }

    @Override
    public void loadGraph( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, GraphLoadListener graphLoadListener ) throws IOException {
        OsmBinaryParser brad = new OsmBinaryParser( graphEntityFactory, distanceFactory, graphLoadListener );
        BlockInputStream blockInputStream = new BlockInputStream( input, brad );
        blockInputStream.process();
    }

    private void onLoadFinish( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, GraphLoadListener graphLoadListener, Graph graph ) throws IOException {
        OsmBinaryProcessor brad = new OsmBinaryProcessor( graphEntityFactory, distanceFactory, graphLoadListener, graph );
        BlockInputStream blockInputStream = new BlockInputStream( input, brad );
        blockInputStream.process();
    }

    private class OsmBinaryParser extends BinaryParser {

        private final GraphEntityFactory graphEntityFactory;
        private final DistanceFactory distanceFactory;
        private final GraphLoadListener graphLoadListener;

        private final Map<Long, Node> nodeMap = new HashMap<>();
        private final Graph graph;

        public OsmBinaryParser( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, GraphLoadListener graphLoadListener ) {
            this.graphEntityFactory = graphEntityFactory;
            this.distanceFactory = distanceFactory;
            this.graphLoadListener = graphLoadListener;
            this.graph = graphEntityFactory.createGraph();
        }

        @Override
        protected void parseRelations( List<Osmformat.Relation> list ) {
//            System.out.println( "relations: " + list.size() );
        }

        @Override
        protected void parseDense( Osmformat.DenseNodes nodes ) {
//            System.out.println( "dense nodes: " + nodes.getIdCount() );
            long lastId = 0;
            long lastLat = 0;
            long lastLon = 0;

            for ( int i = 0; i < nodes.getIdCount(); i++ ) {
                lastId += nodes.getId( i );
                lastLat += nodes.getLat( i );
                lastLon += nodes.getLon( i );
//                System.out.printf( "Dense node, ID %d @ %.6f,%.6f\n",
//                        lastId, parseLat( lastLat ), parseLon( lastLon ) );
                Node n = graphEntityFactory.createNode( Node.Id.generateId(), parseLat( lastLat ), parseLon( lastLon ) );
                nodeMap.put( lastId, n );
                graph.addNode( n );

            }
        }

        @Override
        protected void parseNodes( List<Osmformat.Node> nodes ) {
//            System.out.println( "nodes: " + nodes.size() );
            nodes.stream().map( ( node ) -> {
                //                System.out.printf( "Regular node, ID %d @ %.6f,%.6f\n",
//                        node.getId(), parseLat( node.getLat() ), parseLon( node.getLon() ) );
                Node n = graphEntityFactory.createNode( Node.Id.generateId(), parseLat( node.getLat() ), parseLon( node.getLon() ) );
                nodeMap.put( node.getId(), n );
                return n;
            } ).forEach( ( n ) -> {
                graph.addNode( n );
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
                       /* 
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
                        Distance distance = distanceFactory.createFromDouble( CoordinateUtils.calculateDistance( sourceNode.getCoordinates(), targetNode.getCoordinates() ) );
                        Edge edge = graphEntityFactory.createEdge( sourceNode, targetNode, distance );
                        StringBuilder sb = new StringBuilder();
                        for ( int i = 0; i < w.getKeysCount(); i++ ) {
                            sb.append( getStringById( w.getKeys( i ) ) ).append( "=" )
                                    .append( getStringById( w.getVals( i ) ) ).append( " " )
                                    .append( "\n" );
                        }
                        edge.setLabel( sb.toString() );
                        graph.addEdge( edge );
                    }
                }

                         */
                        long lastRef = 0;
                        List<Coordinates> edgeCoords = new LinkedList<>();
                        Node sourceNode = null;
                        Node targetNode = null;
                        Node tmpSource = null;
                        Node tmpTarget = null;
                        Distance edgeLength = distanceFactory.createZeroDistance();
                        for ( Long ref : w.getRefsList() ) {
                            if ( lastRef != 0 ) {
                                tmpSource = nodeMap.get( lastRef );
                            }
                            lastRef += ref;
                            if ( sourceNode == null ) {
                                sourceNode = nodeMap.get( lastRef );
                            }
                            tmpTarget = nodeMap.get( lastRef );
                            if ( tmpSource != null ) {
                                Distance distance = distanceFactory.createFromDouble( CoordinateUtils.calculateDistance( tmpSource.getCoordinates(), tmpTarget.getCoordinates() ) );
                                edgeLength = edgeLength.add( distance );
                            }
                            targetNode = tmpTarget;
                            edgeCoords.add( targetNode.getCoordinates() );
                        }
                        Edge edge = graphEntityFactory.createEdge( Edge.Id.generateId(), sourceNode, targetNode, edgeLength );
                        StringBuilder sb = new StringBuilder();
                        for ( int i = 0; i < w.getKeysCount(); i++ ) {
                            sb.append( getStringById( w.getKeys( i ) ) ).append( "=" )
                                    .append( getStringById( w.getVals( i ) ) ).append( " " )
                                    .append( "\n" );
                        }
                        edge.setLabel( sb.toString() );
                        edge.setCoordinates( edgeCoords );
                        graph.addEdge( edge );

//                sb.append( "\n  Key=value pairs: " );
//                for ( int i = 0; i < w.getKeysCount(); i++ ) {
//                    sb.append( getStringById( w.getKeys( i ) ) ).append( "=" )
//                            .append( getStringById( w.getVals( i ) ) ).append( " " );
//                }
//                System.out.println( sb.toString() );
                    } );
        }

        @Override
        protected void parse( Osmformat.HeaderBlock hb ) {
//            System.out.println( "Got header block." );
        }

        @Override
        public void complete() {
            System.out.println( "Complete loading! Starting processing." );
            graphLoadListener.onGraphLoaded( graph );
//            try {
//                onLoadFinish( graphEntityFactory, distanceFactory, graphLoadListener, graph );
//            } catch ( IOException ex ) {
//                Logger.getLogger( OsmPbfDataSource.class.getName() ).log( Level.SEVERE, null, ex );
//            }
        }

        private Graph preprocess( Graph graph ) {
            List<Node> nodes = new ArrayList<>( graph.getNodes() );
            int ptCounter = 0;
            for ( int i = 0; i < nodes.size(); i++ ) {
                if ( ( 100 * i ) / nodes.size() > ptCounter ) {
                    System.out.println( ++ptCounter + "%" );
                }
                Node node = nodes.get( i );
                if ( graph.getInDegreeOf( node ) == graph.getOutDegreeOf( node ) && graph.getOutDegreeOf( node ) == 1 ) {
                    Node first = node;
                    Node tmp;
                    while ( ( tmp = getPrevious( graph, first ) ) != null ) {
                        first = tmp;
                    }
                    List<Coordinates> coords = new LinkedList<>();
                    Distance length = distanceFactory.createZeroDistance();
                    coords.add( first.getCoordinates() );
                    Node prev = first;
                    while ( ( tmp = getNext( graph, prev ) ) != null ) {
                        if ( !prev.equals( first ) ) {
                            graph.removeNode( prev );
                        }
                        length = length.add( distanceFactory.createFromDouble( CoordinateUtils.calculateDistance( prev.getCoordinates(), tmp.getCoordinates() ) ) );
                        prev = tmp;
                        coords.add( tmp.getCoordinates() );
                    }
                    Edge edge = graphEntityFactory.createEdge( Edge.Id.generateId(), first, prev, length );
                    edge.setCoordinates( coords );
                    graph.addEdge( edge );
                }
            }
            return graph;
        }

        private Node getPrevious( Graph graph, Node node ) {
//            System.out.println( "node = " + node );
            Set<Edge> edges = graph.getIncomingEdgesOf( node );
            if ( edges.size() != 1 ) {
                return null;
            }
            return edges.stream().findFirst().get().getSourceNode();
        }

        private Node getNext( Graph graph, Node node ) {
            Set<Edge> edges = graph.getOutgoingEdgesOf( node );
            if ( edges.size() != 1 ) {
                return null;
            }
            return edges.stream().findFirst().get().getTargetNode();
        }

    }

    private class OsmBinaryProcessor extends BinaryParser {

        private final GraphEntityFactory graphEntityFactory;
        private final DistanceFactory distanceFactory;
        private final GraphLoadListener graphLoadListener;

        private final Map<Long, Node> nodeMap = new HashMap<>();
        private final Graph graph;

        public OsmBinaryProcessor( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, GraphLoadListener graphLoadListener, Graph graph ) {
            this.graphEntityFactory = graphEntityFactory;
            this.distanceFactory = distanceFactory;
            this.graphLoadListener = graphLoadListener;
            this.graph = graph;
        }

        @Override
        protected void parseRelations( List<Osmformat.Relation> list ) {
        }

        @Override
        protected void parseDense( Osmformat.DenseNodes nodes ) {
        }

        @Override
        protected void parseNodes( List<Osmformat.Node> nodes ) {
//            System.out.println( "nodes: " + nodes.size() );
            nodes.stream().map( ( node ) -> {
                //                System.out.printf( "Regular node, ID %d @ %.6f,%.6f\n",
//                        node.getId(), parseLat( node.getLat() ), parseLon( node.getLon() ) );
                Node n = graphEntityFactory.createNode( Node.Id.generateId(), parseLat( node.getLat() ), parseLon( node.getLon() ) );
                nodeMap.put( node.getId(), n );
                return n;
            } ).forEach( ( n ) -> {
                graph.addNode( n );
            } );
        }

        @Override
        protected void parseWays( List<Osmformat.Way> ways ) {
            ways.stream().forEach( ( w ) -> {
                List<Node> redundantNodes = new ArrayList<>();
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

                        if ( graph.getDegreeOf( targetNode ) == 2 && graph.getInDegreeOf( targetNode ) == 1 ) {
                            redundantNodes.add( targetNode );
                        } else if ( !redundantNodes.isEmpty() ) {
                            String label = graph.getIncomingEdgesOf( redundantNodes.get( 0 ) ).stream().findFirst().get().getLabel();
                            Node s = graph.getIncomingEdgesOf( redundantNodes.get( 0 ) ).stream().findFirst().get().getSourceNode();
                            Node t = graph.getOutgoingEdgesOf( redundantNodes.get( redundantNodes.size() - 1 ) ).stream().findFirst().get().getTargetNode();
                            Distance length = distanceFactory.createZeroDistance();
                            List<Coordinates> coords = new LinkedList<>();
                            coords.add( s.getCoordinates() );
                            Node prev = s;
                            for ( Node rn : redundantNodes ) {
                                coords.add( rn.getCoordinates() );
                                length = length.add( distanceFactory.createFromDouble( CoordinateUtils.calculateDistance( prev.getCoordinates(), rn.getCoordinates() ) ) );
                                prev = rn;
                                graph.removeNode( rn );
                            }
                            coords.add( t.getCoordinates() );
                            Edge edge = graphEntityFactory.createEdge(Edge.Id.generateId(), s, t, length );
                            edge.setCoordinates( coords );
                            edge.setLabel( label );
                            graph.addEdge( edge );
                        }
                    }
                }
            } );
        }

        @Override
        protected void parse( Osmformat.HeaderBlock hb ) {
        }

        @Override
        public void complete() {
            System.out.println( "Complete processing!" );
            graphLoadListener.onGraphLoaded( graph );
        }

        private Graph preprocess( Graph graph ) {
            List<Node> nodes = new ArrayList<>( graph.getNodes() );
            int ptCounter = 0;
            for ( int i = 0; i < nodes.size(); i++ ) {
                if ( ( 100 * i ) / nodes.size() > ptCounter ) {
                    System.out.println( ++ptCounter + "%" );
                }
                Node node = nodes.get( i );
                if ( graph.getInDegreeOf( node ) == graph.getOutDegreeOf( node ) && graph.getOutDegreeOf( node ) == 1 ) {
                    Node first = node;
                    Node tmp;
                    while ( ( tmp = getPrevious( graph, first ) ) != null ) {
                        first = tmp;
                    }
                    List<Coordinates> coords = new LinkedList<>();
                    Distance length = distanceFactory.createZeroDistance();
                    coords.add( first.getCoordinates() );
                    Node prev = first;
                    while ( ( tmp = getNext( graph, prev ) ) != null ) {
                        if ( !prev.equals( first ) ) {
                            graph.removeNode( prev );
                        }
                        length = length.add( distanceFactory.createFromDouble( CoordinateUtils.calculateDistance( prev.getCoordinates(), tmp.getCoordinates() ) ) );
                        prev = tmp;
                        coords.add( tmp.getCoordinates() );
                    }
                    Edge edge = graphEntityFactory.createEdge( Edge.Id.generateId(),first, prev, length );
                    edge.setCoordinates( coords );
                    graph.addEdge( edge );
                }
            }
            return graph;
        }

        private Node getPrevious( Graph graph, Node node ) {
//            System.out.println( "node = " + node );
            Set<Edge> edges = graph.getIncomingEdgesOf( node );
            if ( edges.size() != 1 ) {
                return null;
            }
            return edges.stream().findFirst().get().getSourceNode();
        }

        private Node getNext( Graph graph, Node node ) {
            Set<Edge> edges = graph.getOutgoingEdgesOf( node );
            if ( edges.size() != 1 ) {
                return null;
            }
            return edges.stream().findFirst().get().getTargetNode();
        }

    }

}
