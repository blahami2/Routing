/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.osm;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.utils.CoordinateUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openstreetmap.osmosis.osmbinary.BinaryParser;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.file.BlockInputStream;
import cz.certicon.routing.data.MapDataSource;
import cz.certicon.routing.data.Restriction;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.EdgeAttributes;
import cz.certicon.routing.model.entity.common.SimpleEdgeData;
import cz.certicon.routing.utils.DoubleComparator;
import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * @deprecated java osm parsing not supported anymore, use database or other external application
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class OsmPbfDataSource implements MapDataSource {

    private static final double SPEED_EPS = 10E-2;

    private final DataSource source;
    private Restriction restriction;
    private final List<JoinCondition> joinConditions;

    public OsmPbfDataSource( DataSource source ) throws IOException {
        this.source = source;
        this.restriction = Restriction.getDefault();
        this.joinConditions = new LinkedList<>();

        joinConditions.add( new JoinCondition() {
            @Override
            public boolean shouldJoin( Node node, List<Edge> edges ) {
                return edges.size() == 2;
            }
        } );
        joinConditions.add( new JoinCondition() {
            @Override
            public boolean shouldJoin( Node node, List<Edge> edges ) {
                JoinCondition.EdgePair edgePair = JoinCondition.EdgePair.getSortedPair( node, edges );
                return edgePair.first.getAttributes().isPaid() == edgePair.second.getAttributes().isPaid();
            }
        } );
        joinConditions.add( new JoinCondition() {
            @Override
            public boolean shouldJoin( Node node, List<Edge> edges ) {
                JoinCondition.EdgePair edgePair = JoinCondition.EdgePair.getSortedPair( node, edges );
                return edgePair.first.getTargetNode().equals( node ) && edgePair.second.getSourceNode().equals( node );
            }
        } );
    }

    public OsmPbfDataSource addJoinCondition( JoinCondition joinCondition ) {
        this.joinConditions.add( joinCondition );
        return this;
    }

    @Override
    public void loadGraph( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, GraphLoadListener graphLoadListener ) throws IOException {
        OsmBinaryParser brad = new OsmBinaryParser( graphEntityFactory, distanceFactory, graphLoadListener );
        BlockInputStream blockInputStream = new BlockInputStream( new BufferedInputStream( source.getInputStream() ), brad );
        blockInputStream.process();
    }

    @Override
    public MapDataSource setRestrictions( Restriction restriction ) {
        this.restriction = restriction;
        return this;
    }

    private class OsmBinaryParser extends BinaryParser {

        private static final int PRINT_FREQUENCY = 1000000;

        private final GraphEntityFactory graphEntityFactory;
        private final DistanceFactory distanceFactory;
        private final GraphLoadListener graphLoadListener;
        private int nodeCounter = 0;
        private int edgeCounter = 0;

        private Map<Long, Node> nodeMap = new HashMap<>();
        private Graph graph;

        private final Map<Node, List<Edge>> nodeEdgeMap;

        public OsmBinaryParser( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, GraphLoadListener graphLoadListener ) {
            this.graphEntityFactory = graphEntityFactory;
            this.distanceFactory = distanceFactory;
            this.graphLoadListener = graphLoadListener;
            this.graph = graphEntityFactory.createGraph();
            this.nodeEdgeMap = new HashMap<>();
        }

        @Override
        protected void parseRelations( List<Osmformat.Relation> list ) {
//            for ( Osmformat.Relation relation : list ) {
//                printRelation( relation );
//            }
//            System.out.println( "relations: " + list.size() );
        }

        @Override
        protected void parseDense( Osmformat.DenseNodes nodes ) {
            long lastId = 0;
            long lastLat = 0;
            long lastLon = 0;
//            System.out.println( "nodes count = " + nodes.getIdCount() );
//            System.out.println( "keyvals count = " + nodes.getKeysValsCount() );
//            for ( int i = 0; i < nodes.getKeysValsCount(); i++ ) {
//                System.out.println( "'" + getStringById( nodes.getKeysVals( i ) ) + "'" );
//
//            }
            for ( int i = 0; i < nodes.getIdCount(); i++ ) {
                lastId += nodes.getId( i );
                lastLat += nodes.getLat( i );
                lastLon += nodes.getLon( i );
                Node n = graphEntityFactory.createNode( Node.Id.createId( lastId ), parseLat( lastLat ), parseLon( lastLon ) );
                n.setLabel( Long.toString( lastId ) );
                if ( ++nodeCounter % PRINT_FREQUENCY == 0 ) {
                    System.out.println( "loaded nodes: " + nodeCounter );
                }
//                System.out.println( "keyvals size = " + nodes.getKeysValsCount() + ", id = " + lastId );
//                for ( int j = 0; j < nodes.getKeysValsCount(); j++ ) {
//                    System.out.println( "'" + getStringById( nodes.getKeysVals( j ) ) + "'" );
//                }
                nodeMap.put( lastId, n );
            }
        }

        @Override
        protected void parseNodes( List<Osmformat.Node> nodes ) {
            for ( Osmformat.Node node : nodes ) {
                Node n = graphEntityFactory.createNode( Node.Id.createId( node.getId() ), parseLat( node.getLat() ), parseLon( node.getLon() ) );
                n.setLabel( Long.toString( node.getId() ) );
                if ( ++nodeCounter % PRINT_FREQUENCY == 0 ) {
                    System.out.println( "loaded nodes: " + nodeCounter );
                }
                nodeMap.put( node.getId(), n );
            }
        }

        @Override
        protected void parseWays( List<Osmformat.Way> ways ) {
            WayAttributeParser wayAttributeParser = new WayAttributeParser();
            List<Osmformat.Way> filteredWays = new LinkedList<>();
            for ( Osmformat.Way way : ways ) {
                List<Restriction.Pair> pairs = new LinkedList<>();
                for ( int i = 0; i < way.getKeysCount(); i++ ) {
                    String key = getStringById( way.getKeys( i ) );
                    String value = getStringById( way.getVals( i ) );
                    pairs.add( new Restriction.Pair( key, value ) );
                }
                if ( restriction.isAllowed( pairs ) ) {
                    filteredWays.add( way );
                }
            }
            for ( Osmformat.Way way : filteredWays ) {
                long lastRef = 0;
                List<WayAttributeParser.Pair> pairs = new LinkedList<>();
                for ( int i = 0; i < way.getKeysCount(); i++ ) {
                    String key = getStringById( way.getKeys( i ) );
                    String value = getStringById( way.getVals( i ) );
                    pairs.add( new WayAttributeParser.Pair( key, value ) );
                }
                for ( Long ref : way.getRefsList() ) {
                    Node sourceNode = null;
                    Node targetNode = null;
                    if ( lastRef != 0 ) {
                        sourceNode = nodeMap.get( lastRef );
                    }
                    lastRef += ref;

                    if ( sourceNode != null ) {
                        targetNode = nodeMap.get( lastRef );

                        // country code and inside city, solve it!
                        EdgeAttributes edgeAttributes = wayAttributeParser.parse( "CZ", true, pairs, CoordinateUtils.calculateDistance( sourceNode.getCoordinates(), targetNode.getCoordinates() ) );

//                                if ( printThisWay ) {
//                                    System.out.println( "source = " + sourceNode );
//                                    System.out.println( "target = " + targetNode );
//                                    System.out.println( edgeAttributes );
//                                }
                        String speedStr = edgeAttributes.getAdditionalAttribute( "speed" );
                        EdgeData edgeData = new SimpleEdgeData( ( speedStr != null ) ? Integer.parseInt( speedStr ) : 50, edgeAttributes.isPaid(), edgeAttributes.getLength() );
                        Edge edge = graphEntityFactory.createEdge( Edge.Id.generateId(), sourceNode, targetNode,
                                distanceFactory.createInfiniteDistance() );
                        edge.setDistance( distanceFactory.createFromEdgeData( edgeData ) );
                        edge.setAttributes( edgeAttributes );

//                                StringBuilder sb = new StringBuilder();
//                                pairs.forEach( ( pair ) -> {
//                                    sb.append( pair.key ).append( "=" ).append( pair.value ).append( "\n" );
//                                } );
//                                edge.setLabel( edge.getAttributes().toString() + "\n" + sb.toString() );
                        edge.setCoordinates( Arrays.asList( sourceNode.getCoordinates(), targetNode.getCoordinates() ) );
//                                edge.setLabel( value );
                        getFromMap( sourceNode ).add( edge );
                        getFromMap( targetNode ).add( edge );
                        if ( ++edgeCounter % PRINT_FREQUENCY == 0 ) {
                            System.out.println( "loaded edges: " + edgeCounter );
                        }
                    }
                }
            }
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

        private boolean join( Node node, List<Edge> edges ) {
            for ( JoinCondition joinCondition : joinConditions ) {
                if ( !joinCondition.shouldJoin( node, edges ) ) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void complete() {
            System.out.println( "Complete loading! Starting processing." );
            nodeMap = null;
            for ( Map.Entry<Node, List<Edge>> entry : nodeEdgeMap.entrySet() ) {
                Node node = entry.getKey();
                List<Edge> list = entry.getValue();
                if ( !join( node, list ) ) {
//                    node.setLabel( node.getId() + "[" + list.size() + "]" ); 
                    graph.addNode( node );
                }
            }
            for ( Map.Entry<Node, List<Edge>> entry : nodeEdgeMap.entrySet() ) {
                Node node = entry.getKey();
                List<Edge> list = entry.getValue();
                if ( join( node, list ) ) {
                    if ( list.size() != 2 ) {
                        throw new AssertionError();
                    }
                    Edge a;
                    Edge b;
                    if ( list.get( 0 ).getTargetNode().equals( node ) ) {
                        a = list.get( 0 );
                        b = list.get( 1 );
                    } else {
                        a = list.get( 1 );
                        b = list.get( 0 );
                    }

                    Node nodeA = a.getOtherNode( node );
                    Node nodeB = b.getOtherNode( node );
                    // inherits ID of the first edge
                    Edge newEdge = graphEntityFactory.createEdge( a.getId(), nodeA, nodeB, a.getDistance().add( b.getDistance() ) );
                    newEdge.setAttributes( a.getAttributes().copyWithNewLength( a.getAttributes().getLength() + b.getAttributes().getLength() ) );

//                    if ( node.getLabel().equals( Long.toString( 352744338L ) )
//                            || node.getLabel().equals( Long.toString( 8810008L ) )
//                            || node.getLabel().equals( Long.toString( 271021678L ) )
//                            || node.getLabel().equals( Long.toString( 271021161L ) )
//                            || node.getLabel().equals( Long.toString( 2266700742L ) )
//                            || node.getLabel().equals( Long.toString( 8810009L ) )
//                            || node.getLabel().equals( Long.toString( 25936035L ) ) ) {
//                        System.out.println( "==================================================" );
//                        System.out.println( "found: " + node.getLabel() );
//                        System.out.println( "edge a: " + a );
//                        System.out.println( "edge b: " + b );
//                        System.out.println( "new attributes: " + newEdge.getAttributes() );
//                        System.out.println( "new nodes: from " + nodeA.getLabel() + " to " + nodeB.getLabel() );
//                    }
//                    System.out.println( "distance a = " + a.getDistance() );
//                    System.out.println( "distance b = " + b.getDistance() );
//                    System.out.println( "distance new = " + newEdge.getDistance() );
//                    newEdge.setLabel( nodeA.getLabel() + ":" + nodeB.getLabel() );
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
                    newEdge.setLabel( a.getLabel() );
//                    System.out.println( "new edge has " + newEdge.getCoordinates().size() + " coordinates" );
//                    System.out.println( "orig edge: " + nodeA.getLabel() + ":" + node.getLabel() + ":" + nodeB.getLabel() );
//                    System.out.println( "new edge: " + newEdge.getLabel() );
                    List<Edge> aList = getFromMap( nodeA );
                    if ( !aList.remove( a ) ) {
                        throw new AssertionError( "Not removed!" );
                    }
                    aList.add( newEdge );
                    List<Edge> bList = getFromMap( nodeB );
                    if ( !bList.remove( b ) ) {
                        throw new AssertionError( "Not removed!" );
                    }
                    bList.add( newEdge );
                }
            }
            for ( Node node : graph.getNodes() ) {
                for ( Edge edge : getFromMap( node ) ) {
                    if ( node.equals( edge.getSourceNode() ) ) {
//                        edge.setLabel( edge.getId() + "|" + edge.getSourceNode().getLabel() + ":" + edge.getTargetNode().getLabel() );
//                        System.out.println( "added edge has " + edge.getCoordinates().size() + " coordinates" );
                        graph.addEdge( edge );
                    }
                }
            }

            // map ids to sequence
//            Graph g = graph;
//            graph = graphEntityFactory.createGraph();
//
//            Map<Node, Node> oldToNewMap = new HashMap<>();
//            int nodeCounter = 0;
//            for ( Node node : g.getNodes() ) {
//                Node newNode = node.createCopyWithNewId( Node.Id.createId( nodeCounter++ ) );
//                newNode.setLabel( newNode.getId() + "[" + getFromMap( node ).size() + "]" );
//                oldToNewMap.put( node, newNode ); // old node edge set is copied to the new node
//                graph.addNode( newNode );
//            }
//            int edgeCounter = 0;
//            for ( Edge edge : g.getEdges() ) {
//                edge = edge.newNodes( oldToNewMap.get( edge.getSourceNode() ), oldToNewMap.get( edge.getTargetNode() ) );
//                Edge newEdge = edge.createCopyWithNewId( Edge.Id.createId( edgeCounter++ ) );
//                newEdge.setLabel( newEdge.getAttributes().toString() );
//                graph.addEdge( newEdge );
//            }
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
