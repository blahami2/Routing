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
    private static final String WAY_ID = "WAY_ID";

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
        protected void parseDense( Osmformat.DenseNodes dn ) {
        }

        @Override
        protected void parseNodes( List<Osmformat.Node> list ) {
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
        protected void parseDense( Osmformat.DenseNodes nodes ) {
            long lastId = 0;
            for ( int i = 0; i < nodes.getIdCount(); i++ ) {
                lastId += nodes.getId( i );
                SmallNode n = new SmallNode();
                nodeMap.put( lastId, n );
                if ( PRINT && ++nodeCounter % PRINT_FREQUENCY == 0 ) {
                    System.out.println( "loaded nodes: " + nodeCounter );
                    printMemory();
                }
            }
        }

        @Override
        protected void parseNodes( List<Osmformat.Node> nodes ) {
            for ( Osmformat.Node node : nodes ) {
                long id = node.getId();
                SmallNode n = new SmallNode();
                nodeMap.put( id, n );
                if ( PRINT && ++nodeCounter % PRINT_FREQUENCY == 0 ) {
                    System.out.println( "loaded nodes: " + nodeCounter );
                    printMemory();
                }
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
            System.out.println( "complete" );
            printMemory();
            for ( Iterator<Map.Entry<Long, SmallNode>> it = nodeMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Long, SmallNode> entry = it.next();
                if ( entry.getValue().edgeCount == 2 && !entry.getValue().wayChanged ) {
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
        protected void parseDense( Osmformat.DenseNodes nodes ) {
            long lastId = 0;
            long lastLat = 0;
            long lastLon = 0;
            for ( int i = 0; i < nodes.getIdCount(); i++ ) {
                lastId += nodes.getId( i );
                if ( nodeMap.containsKey( lastId ) ) {
                    lastLat += nodes.getLat( i );
                    lastLon += nodes.getLon( i );
                    try {
                        temporaryWriter.write( new Trinity<>( lastId, parseLat( lastLat ), parseLon( lastLon ) ) );
                    } catch ( IOException ex ) {
                        Logger.getLogger( OsmPbfDataMinimizer.class.getName() ).log( Level.SEVERE, null, ex );
                    }
                    if ( PRINT && ++nodeCounter % PRINT_FREQUENCY == 0 ) {
                        System.out.println( "loaded nodes: " + nodeCounter );
                        printMemory();
                    }
                }
            }
        }

        @Override
        protected void parseNodes( List<Osmformat.Node> nodes ) {
            for ( Osmformat.Node node : nodes ) {
                long id = node.getId();
                if ( nodeMap.containsKey( id ) ) {
                    long lat = node.getLat();
                    long lon = node.getLon();
                    try {
                        temporaryWriter.write( new Trinity<>( id, parseLat( lat ), parseLon( lon ) ) );
                    } catch ( IOException ex ) {
                        Logger.getLogger( OsmPbfDataMinimizer.class.getName() ).log( Level.SEVERE, null, ex );
                    }
                    if ( PRINT && ++nodeCounter % PRINT_FREQUENCY == 0 ) {
                        System.out.println( "loaded nodes: " + nodeCounter );
                        printMemory();
                    }
                }
            }
        }

        @Override
        public void complete() {
            System.out.println( "complete" );
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

        public EdgeParser( DataDestination destination, Map<Long, SmallNode> nodeMap ) {
            this.nodeMap = nodeMap;
            this.destination = destination;
            temporaryWriter.addWriter( SmallEdge.class, new TemporaryWriter.Writer<SmallEdge>() {
                @Override
                public void write( XMLStreamWriter streamWriter, SmallEdge edge ) throws XMLStreamException {
                    streamWriter.writeStartElement( NODE.name().toLowerCase() );
                    streamWriter.writeAttribute( ID.name().toLowerCase(), Long.toString( edge.id ) );
                    streamWriter.writeAttribute( WAY_ID.toLowerCase(), Long.toString( edge.wayId ) );
                    streamWriter.writeAttribute( SOURCE.name().toLowerCase(), Double.toString( edge.sourceId ) );
                    streamWriter.writeAttribute( TARGET.name().toLowerCase(), Double.toString( edge.targetId ) );
                    streamWriter.writeAttribute( SPEED_FORWARD.shortLowerName(), Double.toString( edge.edgeAttributes.getSpeed( true ) ) );
                    streamWriter.writeAttribute( SPEED_BACKWARD.shortLowerName(), Double.toString( edge.edgeAttributes.getSpeed( false ) ) );
                    streamWriter.writeAttribute( LENGTH.shortLowerName(), Double.toString( edge.edgeAttributes.getLength() ) );
                    streamWriter.writeAttribute( ONEWAY.shortLowerName(), Boolean.toString( edge.edgeAttributes.isOneWay() ) );
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
