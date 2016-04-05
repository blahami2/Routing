/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates.xml;

import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.basic.xml.AbstractXmlReader;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import static cz.certicon.routing.data.coordinates.xml.Tag.*;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlCoordinateReader extends AbstractXmlReader<Set<Edge>, Map<Edge, List<Coordinate>>> implements CoordinateReader {
    

    public XmlCoordinateReader( DataSource source ) {
        super( source );
    }

    private Map<Edge, List<Coordinate>> convert( Set<Edge> edges, Map<Edge.Id, List<Coordinate>> coords ) {
        Map<Edge, List<Coordinate>> map = new HashMap<>();
        for ( Edge edge : edges ) {
            map.put( edge, coords.get( edge.getId() ) );
        }
        return map;
    }

    @Override
    protected Map<Edge, List<Coordinate>> openedRead( Set<Edge> edges ) throws IOException {
        EdgeHandler edgeHandler;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            Set<Edge.Id> collect = new HashSet<>();
            for ( Edge edge : edges ) {
                collect.add( edge.getId() );
            }
            edgeHandler = new EdgeHandler( collect );
            saxParser.parse( getDataSource().getInputStream(), edgeHandler );
        } catch ( UglyExceptionMechanism notEx ) {
            close();
            return convert( edges, notEx.getCoords() );
        } catch ( ParserConfigurationException | SAXException ex ) {
            throw new IOException( ex );
        }
        close();
        throw new IOException( "Ids not found (size should be " + edges.size() + " but is " + edgeHandler.getCoords().size() );
    }

    private static class EdgeHandler extends DefaultHandler {

        private final Set<Edge.Id> edgeIds;
        private Edge.Id collecting = null;
        private List<Coordinate> coordList = null;
        private final Map<Edge.Id, List<Coordinate>> coords = new HashMap<>();

        public EdgeHandler( Set<Edge.Id> edgeIds ) {
            this.edgeIds = edgeIds;
        }

        @Override
        public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {
            if ( qName.equalsIgnoreCase( EDGE.name() ) ) {
                Edge.Id id = Edge.Id.fromString( attributes.getValue( ID.shortLowerName() ) );
//                System.out.println( "current id = " + id );
                if ( edgeIds.contains( id ) ) {
//                    System.out.println( "started collecting" );
                    collecting = id;
                    coordList = new ArrayList<>();
                }
            } else if ( qName.equalsIgnoreCase( COORDINATE.name() ) ) {
                if ( collecting != null ) {
                    double latitude = Double.parseDouble( attributes.getValue( LATITUDE.shortLowerName() ) );
                    double longitude = Double.parseDouble( attributes.getValue( LONGITUDE.shortLowerName() ) );
                    coordList.add( new Coordinate( latitude, longitude ) );
                }
            }
        }

        @Override
        public void endElement( String uri, String localName, String qName ) throws SAXException {
            if ( qName.equalsIgnoreCase( EDGE.name() ) ) {
                if ( collecting != null ) {
                    coords.put( collecting, coordList );
//                    System.out.println( "ended collecting" );
                    collecting = null;
                    coordList = null;
                    if ( edgeIds.size() == coords.size() ) {
                        throw new UglyExceptionMechanism( coords );
                    }
                }
            }
        }

        public Map<Edge.Id, List<Coordinate>> getCoords() {
            return coords;
        }
    }

    private static class UglyExceptionMechanism extends SAXException {

        private final Map<Edge.Id, List<Coordinate>> coordMap;

        public UglyExceptionMechanism( Map<Edge.Id, List<Coordinate>> coordMap ) {
            this.coordMap = coordMap;
        }

        public Map<Edge.Id, List<Coordinate>> getCoords() {
            return coordMap;
        }
    }

}
