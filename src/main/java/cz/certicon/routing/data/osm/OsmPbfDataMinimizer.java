/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.osm;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.MapDataMinimizer;
import cz.certicon.routing.data.Restriction;
import cz.certicon.routing.data.TemporaryMemory;
import cz.certicon.routing.data.basic.xml.AbstractXmlWriter;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.openstreetmap.osmosis.osmbinary.BinaryParser;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.file.BlockInputStream;
import org.openstreetmap.osmosis.osmbinary.BinarySerializer;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;
import static cz.certicon.routing.data.graph.xml.Tag.*;
import cz.certicon.routing.model.basic.Quaternion;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.EdgeAttributes;
import cz.certicon.routing.utils.CoordinateUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class OsmPbfDataMinimizer implements MapDataMinimizer {

    private static final boolean PRINT = false;
    private static final double SPEED_EPS = 10E-2;

    private static final int PRINT_FREQUENCY = 1000000;

    private final DataSource dataSource;
    private final DataDestination dataDestination;
    private final TemporaryMemory temporaryMemory;
    private final TemporaryWriter temporaryWriter;
    private Restriction restriction;

    public OsmPbfDataMinimizer( DataSource dataSource, DataDestination dataDestination, TemporaryMemory temporaryMemory ) {
        this.dataSource = dataSource;
        this.dataDestination = dataDestination;
        this.temporaryMemory = temporaryMemory;
        this.restriction = Restriction.getDefault();
        this.temporaryWriter = new TemporaryWriter( temporaryMemory.getMemoryAsDestination() );
    }

    @Override
    public MapDataMinimizer setRestrictions( Restriction restriction ) {
        this.restriction = restriction;
        return this;
    }

    @Override
    public void loadGraph( GraphMinimizeListener graphMinimizeListener ) throws IOException {
        NodeParser brad = new NodeParser( graphMinimizeListener );
        BlockInputStream blockInputStream = new BlockInputStream( new BufferedInputStream( dataSource.getInputStream() ), brad );
        blockInputStream.process();

    }

    private class OsmBinarySerializer extends BinarySerializer {

        public OsmBinarySerializer( BlockOutputStream output ) {
            super( output );
        }

    }

    private static abstract class AbstractBinaryParser extends BinaryParser {

        @Override
        protected void parseRelations( List<Osmformat.Relation> list ) {
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
                parseNode( lastId, lastLat, lastLon );
            }
        }

        @Override
        protected void parseNodes( List<Osmformat.Node> nodes ) {
            for ( Osmformat.Node node : nodes ) {
                parseNode( node.getId(), parseLat( node.getLat() ), parseLon( node.getLon() ) );
            }
        }

        @Override
        protected void parseWays( List<Osmformat.Way> list ) {
        }

        @Override
        protected void parse( Osmformat.HeaderBlock hb ) {
        }

        @Override
        public void complete() {
        }

        protected void parseNode( long id, double latitude, double longitude ) {

        }
    }

    /*
    *************************************** NODE PARSER ********************************************
     */
    private class NodeParser extends AbstractBinaryParser {

        private final GraphMinimizeListener graphMinimizeListener;
        private final DataDestination temporaryDestination;
        private DataSource temporarySource;
        private final Map<Long, SmallNode> nodeMap = new HashMap<>();
        private int nodeCounter = 0;
        private int edgeCounter = 0;

        public NodeParser( GraphMinimizeListener graphMinimizeListener ) {
            this.graphMinimizeListener = graphMinimizeListener;
            this.temporaryDestination = temporaryMemory.getMemoryAsDestination();
        }

        @Override
        protected void parseNode( long id, double latitude, double longitude ) {
            SmallNode n = new SmallNode();
            nodeMap.put( id, n );
            if ( PRINT && ++nodeCounter % PRINT_FREQUENCY == 0 ) {
                System.out.println( "loaded nodes: " + nodeCounter );
                printMemory();
            }
        }

        @Override
        protected void parseWays( List<Osmformat.Way> ways ) {
            for ( Osmformat.Way way : ways ) {
                List<Restriction.Pair> pairs = new LinkedList<>();
                for ( int i = 0; i < way.getKeysCount(); i++ ) {
                    String key = getStringById( way.getKeys( i ) );
                    String value = getStringById( way.getVals( i ) );
                    pairs.add( new Restriction.Pair( key, value ) );
                }
                if ( restriction.isAllowed( pairs ) ) {
                    final int refsCount = way.getRefsCount();
                    long lastRef = 0;
                    for ( int i = 0; i < refsCount; i++ ) {
                        long ref = way.getRefs( i );
                        lastRef += ref;
                        SmallNode node = nodeMap.get( lastRef );
                        if ( i == 0 || i == refsCount - 1 ) {
                            node.edgeCount++;
                            if ( node.edgeCount == 2 ) {
                                node.wayChanged = true;
                            }
                        } else {
                            node.edgeCount += 2;
                        }
                    }
                    if ( PRINT && ++edgeCounter % PRINT_FREQUENCY == 0 ) {
                        System.out.println( "loaded ways: " + edgeCounter );
                        printMemory();
                    }
                }
            }
        }

        @Override
        public void complete() {
            System.out.println( "complete, nodeMap size = " + nodeMap.size() );
            printMemory();
            for ( Iterator<Map.Entry<Long, SmallNode>> it = nodeMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Long, SmallNode> entry = it.next();
                if ( !isValidNode( entry.getValue() ) ) {
                    it.remove();
                }
            }
            try {
                NodeEnhanceParser brad = new NodeEnhanceParser( temporaryDestination, nodeMap );
                BlockInputStream blockInputStream = new BlockInputStream( new BufferedInputStream( dataSource.getInputStream() ), brad );
                blockInputStream.process();
            } catch ( IOException ex ) {
                Logger.getLogger( OsmPbfDataMinimizer.class.getName() ).log( Level.SEVERE, null, ex );
            }
        }

    }

    /*
    *************************************** NODE ENHANCER ********************************************
     */
    private class NodeEnhanceParser extends AbstractBinaryParser {

        private final DataDestination destination;
        private final Map<Long, SmallNode> nodeMap;
        private int nodeCounter = 0;

        public NodeEnhanceParser( DataDestination destination, Map<Long, SmallNode> nodeMap ) {
            this.destination = destination;
            this.nodeMap = nodeMap;
            temporaryWriter.addWriter( Trinity.class, new TemporaryWriter.Writer<Trinity<Long, Double, Double>>() {
                @Override
                public void write( XMLStreamWriter streamWriter, Trinity<Long, Double, Double> node ) throws XMLStreamException {
                    streamWriter.writeStartElement( NODE.name().toLowerCase() );
                    streamWriter.writeAttribute( ID.name().toLowerCase(), Long.toString( node.a ) );
                    streamWriter.writeAttribute( LATITUDE.name().toLowerCase(), Double.toString( node.b ) );
                    streamWriter.writeAttribute( LONGITUDE.name().toLowerCase(), Double.toString( node.c ) );
                    streamWriter.writeEndElement();
                }
            } );
        }

        @Override
        protected void parseNode( long id, double latitude, double longitude ) {
            if ( nodeMap.containsKey( id ) ) {
                try {
                    temporaryWriter.write( new Trinity<>( id, latitude, longitude ) );
                } catch ( IOException ex ) {
                    Logger.getLogger( OsmPbfDataMinimizer.class.getName() ).log( Level.SEVERE, null, ex );
                }
                if ( PRINT && ++nodeCounter % PRINT_FREQUENCY == 0 ) {
                    System.out.println( "loaded nodes: " + nodeCounter );
                    printMemory();
                }
            }
        }

        @Override
        public void complete() {
            System.out.println( "complete, nodeMap size = " + nodeMap.size() );
            try {
                EdgeParser brad = new EdgeParser( destination, nodeMap );
                BlockInputStream blockInputStream = new BlockInputStream( new BufferedInputStream( dataSource.getInputStream() ), brad );
                blockInputStream.process();
            } catch ( IOException ex ) {
                Logger.getLogger( OsmPbfDataMinimizer.class.getName() ).log( Level.SEVERE, null, ex );
            }
        }
    }

    /*
    *************************************** EDGE PARSER ********************************************
     */
    private class EdgeParser extends AbstractBinaryParser {

        private final DataDestination destination;
        private final Map<Long, SmallNode> nodeMap;
        private final Map<Long, List<SmallEdge>> edgeMap;

        public EdgeParser( DataDestination destination, Map<Long, SmallNode> nodeMap ) {
            this.nodeMap = nodeMap;
            this.destination = destination;
            temporaryWriter.addWriter( SmallEdge.class, new TemporaryWriter.Writer<SmallEdge>() {
                @Override
                public void write( XMLStreamWriter streamWriter, SmallEdge edge ) throws XMLStreamException {
                    streamWriter.writeStartElement( NODE.name().toLowerCase() );
                    streamWriter.writeAttribute( ID.name().toLowerCase(), Long.toString( edge.id ) );
                    streamWriter.writeAttribute( WAY_ID.name().toLowerCase(), Long.toString( edge.wayId ) );
                    streamWriter.writeAttribute( SOURCE.name().toLowerCase(), Long.toString( edge.sourceId ) );
                    streamWriter.writeAttribute( TARGET.name().toLowerCase(), Long.toString( edge.targetId ) );
                    streamWriter.writeAttribute( LENGTH.shortLowerName(), Double.toString( edge.edgeAttributes.getLength() ) );
                    streamWriter.writeAttribute( PAID.shortLowerName(), Boolean.toString( edge.edgeAttributes.isPaid() ) );
                    streamWriter.writeEndElement();
                }
            } );
            edgeMap = new HashMap<>();
        }

        @Override
        protected void parseWays( List<Osmformat.Way> ways ) {
            WayAttributeParser wayAttributeParser = new WayAttributeParser();
            for ( Osmformat.Way way : ways ) {
                List<Restriction.Pair> pairs = new LinkedList<>();
                for ( int i = 0; i < way.getKeysCount(); i++ ) {
                    String key = getStringById( way.getKeys( i ) );
                    String value = getStringById( way.getVals( i ) );
                    pairs.add( new Restriction.Pair( key, value ) );
                }
                if ( restriction.isAllowed( pairs ) ) {

                    List<WayAttributeParser.Pair> waPairs = new LinkedList<>();
                    for ( int i = 0; i < way.getKeysCount(); i++ ) {
                        String key = getStringById( way.getKeys( i ) );
                        String value = getStringById( way.getVals( i ) );
                        waPairs.add( new WayAttributeParser.Pair( key, value ) );
                    }

                    final int refsCount = way.getRefsCount();
                    long lastRef = 0;
                    boolean activeEdge = false;
                    long startId = 0;
                    long endId;
                    for ( int i = 0; i < refsCount; i++ ) {
                        long ref = way.getRefs( i );
                        lastRef += ref;
                        if ( nodeMap.containsKey( lastRef ) ) {
                            if ( activeEdge ) {
                                endId = lastRef;
                                /*
                                TODO evaluate attributes?
                                 */
                                EdgeAttributes edgeAttributes = wayAttributeParser.parse( "CZ", true, waPairs, 1 );
                                try {
                                    temporaryWriter.write( new SmallEdge( way.getId(), startId, endId, edgeAttributes ) );
                                } catch ( IOException ex ) {
                                    Logger.getLogger( OsmPbfDataMinimizer.class.getName() ).log( Level.SEVERE, null, ex );
                                }
                            }
                            startId = lastRef;
                            activeEdge = true;
                        }
                    }
                }
            }
        }

        @Override
        public void complete() {
            System.out.println( "complete" );
            try {
                temporaryWriter.close();
            } catch ( IOException ex ) {
                Logger.getLogger( OsmPbfDataMinimizer.class.getName() ).log( Level.SEVERE, null, ex );
            }
            printMemory();
        }
    }

    /*
    *************************************** EDGE ENHANCER ********************************************
     */
    private class EdgeEnhancer extends AbstractBinaryParser {

        private final DataDestination destination;
        private final Map<Long, SmallNode> nodeMap;

        public EdgeEnhancer( DataDestination destination, Map<Long, SmallNode> nodeMap ) {
            this.nodeMap = nodeMap;
            this.destination = destination;
            temporaryWriter.addWriter( SmallEdge.class, new TemporaryWriter.Writer<SmallEdge>() {
                @Override
                public void write( XMLStreamWriter streamWriter, SmallEdge edge ) throws XMLStreamException {
                    streamWriter.writeStartElement( NODE.name().toLowerCase() );
                    streamWriter.writeAttribute( ID.name().toLowerCase(), Long.toString( edge.id ) );
                    streamWriter.writeAttribute( WAY_ID.name().toLowerCase(), Long.toString( edge.wayId ) );
                    streamWriter.writeAttribute( SOURCE.name().toLowerCase(), Long.toString( edge.sourceId ) );
                    streamWriter.writeAttribute( TARGET.name().toLowerCase(), Long.toString( edge.targetId ) );
                    streamWriter.writeAttribute( LENGTH.shortLowerName(), Double.toString( edge.edgeAttributes.getLength() ) );
                    streamWriter.writeAttribute( PAID.shortLowerName(), Boolean.toString( edge.edgeAttributes.isPaid() ) );
                    streamWriter.writeEndElement();
                }
            } );
        }

        @Override
        protected void parseWays( List<Osmformat.Way> ways ) {
            WayAttributeParser wayAttributeParser = new WayAttributeParser();
            for ( Osmformat.Way way : ways ) {
                List<Restriction.Pair> pairs = new LinkedList<>();
                for ( int i = 0; i < way.getKeysCount(); i++ ) {
                    String key = getStringById( way.getKeys( i ) );
                    String value = getStringById( way.getVals( i ) );
                    pairs.add( new Restriction.Pair( key, value ) );
                }
                if ( restriction.isAllowed( pairs ) ) {

                    List<WayAttributeParser.Pair> waPairs = new LinkedList<>();
                    for ( int i = 0; i < way.getKeysCount(); i++ ) {
                        String key = getStringById( way.getKeys( i ) );
                        String value = getStringById( way.getVals( i ) );
                        waPairs.add( new WayAttributeParser.Pair( key, value ) );
                    }

                    final int refsCount = way.getRefsCount();
                    long lastRef = 0;
                    boolean activeEdge = false;
                    long startId = 0;
                    long endId;
                    for ( int i = 0; i < refsCount; i++ ) {
                        long ref = way.getRefs( i );
                        lastRef += ref;
                        if ( nodeMap.containsKey( lastRef ) ) {
                            if ( activeEdge ) {
                                endId = lastRef;
                                EdgeAttributes edgeAttributes = wayAttributeParser.parse( "CZ", true, waPairs, 0 );
                                try {
                                    temporaryWriter.write( new SmallEdge( way.getId(), startId, endId, edgeAttributes ) );
                                } catch ( IOException ex ) {
                                    Logger.getLogger( OsmPbfDataMinimizer.class.getName() ).log( Level.SEVERE, null, ex );
                                }
                            }
                            startId = lastRef;
                            activeEdge = true;
                        }
                    }
                }
            }
        }

        @Override
        public void complete() {
            System.out.println( "complete" );
            try {
                temporaryWriter.close();
            } catch ( IOException ex ) {
                Logger.getLogger( OsmPbfDataMinimizer.class.getName() ).log( Level.SEVERE, null, ex );
            }
            printMemory();
        }
    }

    /*
    *************************************** WAYS LOADER ********************************************
     */
    private class WaysLoader extends AbstractBinaryParser {

        private final DataDestination destination;
        private final Map<Long, SmallWay> wayMap;

        public WaysLoader( DataDestination destination ) {
            this.wayMap = new HashMap<>();
            this.destination = destination;
        }

        @Override
        protected void parseWays( List<Osmformat.Way> ways ) {
            for ( Osmformat.Way way : ways ) {
                List<Restriction.Pair> pairs = new LinkedList<>();
                for ( int i = 0; i < way.getKeysCount(); i++ ) {
                    String key = getStringById( way.getKeys( i ) );
                    String value = getStringById( way.getVals( i ) );
                    pairs.add( new Restriction.Pair( key, value ) );
                }
                if ( restriction.isAllowed( pairs ) ) {
                    final int refsCount = way.getRefsCount();
                    SmallWay w = new SmallWay( way.getId(), refsCount );
                    long lastRef = 0;
                    for ( int i = 0; i < refsCount; i++ ) {
                        long ref = way.getRefs( i );
                        lastRef += ref;
                        w.addNode( lastRef );
                    }
                }
            }
        }

        @Override
        public void complete() {
            System.out.println( "complete" );
            try {
                temporaryWriter.close();
            } catch ( IOException ex ) {
                Logger.getLogger( OsmPbfDataMinimizer.class.getName() ).log( Level.SEVERE, null, ex );
            }
            printMemory();
        }
    }

    /*
    *************************************** WAYS ENHANCER ********************************************
     */
    private class WaysEnhancer extends AbstractBinaryParser {

        private final DataDestination destination;
        private final Map<Long, SmallWay> wayMap;

        public WaysEnhancer( DataDestination destination, Map<Long, SmallWay> wayMap ) {
            this.wayMap = wayMap;
            this.destination = destination;
            temporaryWriter.addWriter( SmallWay.class, new TemporaryWriter.Writer<SmallWay>() {
                @Override
                public void write( XMLStreamWriter streamWriter, SmallWay way ) throws XMLStreamException {
                    streamWriter.writeStartElement( WAY_ID.name().toLowerCase() );
                    for ( Long nodeId : way.nodes ) {
                        streamWriter.writeStartElement( NODE.name().toLowerCase() );
                        streamWriter.writeAttribute( ID.name().toLowerCase(), Long.toString( nodeId ) );
                        streamWriter.writeAttribute( LATITUDE.name().toLowerCase(), Double.toString( way.coordinateMap.get( nodeId ).getLatitude() ) );
                        streamWriter.writeAttribute( LONGITUDE.name().toLowerCase(), Double.toString( way.coordinateMap.get( nodeId ).getLongitude() ) );
                        streamWriter.writeEndElement();
                    }
                    streamWriter.writeEndElement();
                }
            } );
        }

        @Override
        protected void parseNode( long id, double latitude, double longitude ) {
            for ( SmallWay way : wayMap.values() ) {
                if ( way.coordinateMap.containsKey( id ) ) {
                    way.addCoordinate( id, new Coordinate( latitude, longitude ) );
                }
            }
        }

        @Override
        public void complete() {
            System.out.println( "complete" );
            try {
                temporaryWriter.close();
            } catch ( IOException ex ) {
                Logger.getLogger( OsmPbfDataMinimizer.class.getName() ).log( Level.SEVERE, null, ex );
            }
            printMemory();
        }
    }

    /*
    *************************************** METHODS ********************************************
     */
    private boolean isValidNode( SmallNode node ) {
        return ( node.edgeCount != 2 && node.edgeCount != 0 ) || ( node.wayChanged );
    }

    private void printMemory() {
        if ( PRINT ) {
            Runtime runtime = Runtime.getRuntime();
            NumberFormat format = NumberFormat.getInstance();
            long maxMemory = runtime.maxMemory();
            long allocatedMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();

            System.out.println( "free memory: " + format.format( freeMemory / 1024 ) );
            System.out.println( "allocated memory: " + format.format( allocatedMemory / 1024 ) );
            System.out.println( "max memory: " + format.format( maxMemory / 1024 ) );
            System.out.println( "total free memory: " + format.format( ( freeMemory + ( maxMemory - allocatedMemory ) ) / 1024 ) );
        }
    }
}
