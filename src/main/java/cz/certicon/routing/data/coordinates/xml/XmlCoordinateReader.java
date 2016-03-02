/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates.xml;

import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import static cz.certicon.routing.data.coordinates.xml.Tag.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlCoordinateReader implements CoordinateReader {

    private final DataSource source;

    public XmlCoordinateReader( DataSource source ) {
        this.source = source;
    }

    @Override
    public CoordinateReader open() throws IOException {
        return this;
    }

    @Override
    public List<Coordinate> findCoordinates( Edge edge ) throws IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            EdgeHandler edgeHandler = new EdgeHandler( edge.getId() );
            saxParser.parse( source.getInputStream(), edgeHandler );
        } catch ( UglyExceptionMechanism notEx ) {
            return notEx.getCoords();
        } catch ( ParserConfigurationException | SAXException ex ) {
            throw new IOException( ex );
        }
        throw new IOException( "Id not found: " + edge.getId() );
    }

    @Override
    public CoordinateReader close() throws IOException {
        return this;
    }

    private static class EdgeHandler extends DefaultHandler {

        private final Edge.Id lookupId;
        private boolean collecting = false;
        private final List<Coordinate> coords = new ArrayList<>();

        public EdgeHandler( Edge.Id lookupId ) {
            this.lookupId = lookupId;
        }

        @Override
        public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {
            if ( qName.equalsIgnoreCase( EDGE.name() ) ) {
                Edge.Id id = Edge.Id.fromString( attributes.getValue( ID.shortLowerName() ) );
//                System.out.println( "current id = " + id );
                if ( id.equals( lookupId ) ) {
                    collecting = true;
                }
            } else if ( qName.equalsIgnoreCase( COORDINATE.name() ) ) {
                if ( collecting ) {
                    double latitude = Double.parseDouble( attributes.getValue( LATITUDE.shortLowerName() ) );
                    double longitude = Double.parseDouble( attributes.getValue( LONGITUDE.shortLowerName() ) );
                    coords.add( new Coordinate( latitude, longitude ) );
                }
            }
        }

        @Override
        public void endElement( String uri, String localName, String qName ) throws SAXException {
            if ( qName.equalsIgnoreCase( EDGE.name() ) ) {
                if ( collecting ) {
                    throw new UglyExceptionMechanism( coords );
                }
            }
        }
    }

    private static class UglyExceptionMechanism extends SAXException {

        private final List<Coordinate> coords;

        public UglyExceptionMechanism( List<Coordinate> coords ) {
            this.coords = coords;
        }

        public List<Coordinate> getCoords() {
            return coords;
        }
    }

}
