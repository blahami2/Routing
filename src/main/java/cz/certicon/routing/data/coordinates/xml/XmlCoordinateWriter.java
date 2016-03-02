/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import static cz.certicon.routing.data.coordinates.xml.Tag.*;
import cz.certicon.routing.data.coordinates.CoordinateWriter;

//static import cz.certicon.routing.data.coordinates.xml.Tags;
/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlCoordinateWriter implements CoordinateWriter {

    private final DataDestination destination;
    private OutputStream output;
    private XMLStreamWriter writer;

    XmlCoordinateWriter( DataDestination destination ) {
        this.destination = destination;
    }

    @Override
    public CoordinateWriter open() throws IOException {
        output = destination.getOutputStream();
        XMLOutputFactory xmlOutFact = XMLOutputFactory.newInstance();
        try {
            writer = xmlOutFact.createXMLStreamWriter( output );
            writer.writeStartDocument();
            writer.writeStartElement( ROOT.shortLowerName() );
        } catch ( XMLStreamException ex ) {
            throw new IOException( ex );
        }
        return this;
    }

    @Override
    public CoordinateWriter write( Edge edge, List<Coordinate> coordinates ) throws IOException {
        try {
            writer.writeStartElement( EDGE.shortLowerName() );
            writer.writeAttribute( ID.shortLowerName(), Edge.Id.toString( edge.getId() ) );
            for ( Coordinate coordinate : coordinates ) {
                writer.writeStartElement( COORDINATE.shortLowerName() );
                writer.writeAttribute( LATITUDE.shortLowerName(), Double.toString( coordinate.getLatitude() ) );
                writer.writeAttribute( LONGITUDE.shortLowerName(), Double.toString( coordinate.getLongitude() ) );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.flush();
        } catch ( XMLStreamException ex ) {
            throw new IOException( ex );
        }
        return this;
    }

    @Override
    public CoordinateWriter close() throws IOException {
        try {
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
        } catch ( XMLStreamException ex ) {
            throw new IOException( ex );
        }
//        output.close();
        return this;
    }

}
