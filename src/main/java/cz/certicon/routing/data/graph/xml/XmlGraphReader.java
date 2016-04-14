/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph.xml;

import cz.certicon.routing.data.basic.xml.AbstractXmlReader;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.Reader;
import cz.certicon.routing.data.basic.FileSource;
import cz.certicon.routing.data.graph.GraphReader;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import static cz.certicon.routing.data.graph.xml.Tag.*;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.EdgeAttributes;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.common.SimpleEdgeAttributes;
import cz.certicon.routing.model.entity.common.SimpleEdgeData;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An implementation of the {@link GraphReader} interfaces using the
 * {@link AbstractXmlReader} class.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlGraphReader extends AbstractXmlReader<Pair<GraphEntityFactory, DistanceFactory>, Graph> implements GraphReader {

    public XmlGraphReader( DataSource source ) {
        super( source );
    }

    @Override
    protected Graph checkedRead( Pair<GraphEntityFactory, DistanceFactory> in ) throws IOException {
        GraphEntityFactory graphEntityFactory = in.a;
        DistanceFactory distanceFactory = in.b;
        Graph graph = graphEntityFactory.createGraph();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            Handler edgeHandler = new Handler( graph, graphEntityFactory, distanceFactory );
            saxParser.parse( getDataSource().getInputStream(), edgeHandler );
        } catch ( ParserConfigurationException | SAXException ex ) {
            throw new IOException( ex );
        }
        close();
        return graph;
    }

    private static class Handler extends DefaultHandler {

        private final Graph graph;
        private final GraphEntityFactory graphEntityFactory;
        private final DistanceFactory distanceFactory;
        private final Map<Node.Id, Node> nodes;

        public Handler( Graph graph, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory ) {
            this.graph = graph;
            this.graphEntityFactory = graphEntityFactory;
            this.distanceFactory = distanceFactory;
            this.nodes = new HashMap<>();
        }

        @Override
        public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {
            if ( qName.equalsIgnoreCase( NODE.name() ) ) {
                Node.Id id = Node.Id.fromString( attributes.getValue( ID.shortLowerName() ) );
                double latitude = Double.parseDouble( attributes.getValue( LATITUDE.shortLowerName() ) );
                double longitude = Double.parseDouble( attributes.getValue( LONGITUDE.shortLowerName() ) );
                long osmId = Long.parseLong( attributes.getValue( OSM_ID.shortLowerName() ) );
                Node node = graphEntityFactory.createNode( id, latitude, longitude );
                node.setOsmId( osmId );
                nodes.put( id, node );
                graph.addNode( node );
            } else if ( qName.equalsIgnoreCase( EDGE.name() ) ) {
                Edge.Id id = Edge.Id.fromString( attributes.getValue( ID.shortLowerName() ) );
                Node.Id sourceId = Node.Id.fromString( attributes.getValue( SOURCE.shortLowerName() ) );
                Node.Id targetId = Node.Id.fromString( attributes.getValue( TARGET.shortLowerName() ) );
                int speed = Integer.parseInt( attributes.getValue( SPEED.shortLowerName() ) );
                double length = Double.parseDouble( attributes.getValue( LENGTH.shortLowerName() ) );
                boolean isPaid = Boolean.parseBoolean( attributes.getValue( PAID.shortLowerName() ) );
                long osmId = Long.parseLong( attributes.getValue( OSM_ID.shortLowerName() ) );
                EdgeAttributes edgeAttributes = SimpleEdgeAttributes.builder().setLength( length ).setPaid( isPaid ).build();
                Node sourceNode = nodes.get( sourceId );
                Node targetNode = nodes.get( targetId );
                EdgeData edgeData = new SimpleEdgeData( speed, isPaid, length );
                Edge edge = graphEntityFactory.createEdge( id, sourceNode, targetNode, distanceFactory.createFromEdgeData( edgeData ) );
                edge.setOsmId( osmId );
                edge.setSpeed( speed );
                edge.setAttributes( edgeAttributes );
                edge.setLabel( edgeAttributes.toString() );
                graph.addEdge( edge );
            }
        }

        @Override
        public void endElement( String uri, String localName, String qName ) throws SAXException {
        }
    }

}
