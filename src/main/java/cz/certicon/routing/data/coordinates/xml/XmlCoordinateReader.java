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
import cz.certicon.routing.model.basic.Pair;

/**
 * An implementation of the {@link CoordinateReader} interfaces using the
 * {@link AbstractXmlReader} class.
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
    protected Map<Edge, List<Coordinate>> checkedRead( Set<Edge> edges ) throws IOException {
        Map<Edge, List<Coordinate>> coordinateMap = new HashMap<>();
        Map<Long, Edge> edgeMap = new HashMap<>();
        int counter = 0;
        for ( Edge edge : edges ) {
            counter++;
            edgeMap.put( edge.getDataId(), edge );
        }
        EdgeHandler edgeHandler;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            edgeHandler = new EdgeHandler( edgeMap );
            saxParser.parse( getDataSource().getInputStream(), edgeHandler );
            close();
            Map<Long, List<Coordinate>> dataCoordinatesMap = edgeHandler.getDataCoordinatesMap();
            for ( Edge edge : edges ) {
                coordinateMap.put( edge, dataCoordinatesMap.get( edge.getDataId() ) );
            }
            return coordinateMap;
        } catch ( ParserConfigurationException | SAXException ex ) {
            throw new IOException( ex );
        }
    }

    private static class EdgeHandler extends DefaultHandler {

        private final Map<Long, Edge> edgeMap;
        private final Map<Long, List<Coordinate>> dataCoordinatesMap;
        private Long collecting = null;
        private List<Coordinate> coordList = null;

        public EdgeHandler( Map<Long, Edge> edgeMap ) {
            this.edgeMap = edgeMap;
            this.dataCoordinatesMap = new HashMap<>();
        }

        @Override
        public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {
            if ( qName.equalsIgnoreCase( EDGE.name() ) ) {
                Long id = Long.parseLong( attributes.getValue( DATA_ID.shortLowerName() ) );
//                System.out.println( "current id = " + id );
                if ( edgeMap.containsKey( id ) ) {
//                    System.out.println( "started collecting" );
                    collecting = id;
                    coordList = new ArrayList<>();
                }
            } else if ( qName.equalsIgnoreCase( COORDINATE.name() ) ) {
                if ( collecting != null ) {
                    double latitude = Double.parseDouble( attributes.getValue( LATITUDE.shortLowerName() ) );
                    double longitude = Double.parseDouble( attributes.getValue( LONGITUDE.shortLowerName() ) );
                    coordList.add(new Coordinate( latitude, longitude ) );
                }
            }
        }

        @Override
        public void endElement( String uri, String localName, String qName ) throws SAXException {
            if ( qName.equalsIgnoreCase( EDGE.name() ) ) {
                if ( collecting != null ) {
                    dataCoordinatesMap.put( collecting, coordList );
//                    System.out.println( "ended collecting" );
                    collecting = null;
                    coordList = null;
                }
            }
        }

        public Map<Long, List<Coordinate>> getDataCoordinatesMap() {
            return dataCoordinatesMap;
        }
    }

}
