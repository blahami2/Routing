/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.basic.xml.AbstractXmlWriter;
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
import cz.certicon.routing.model.basic.Pair;

//static import cz.certicon.routing.data.coordinates.xml.Tags;
/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlCoordinateWriter extends AbstractXmlWriter<Pair<Edge, List<Coordinate>>> implements CoordinateWriter {

    public XmlCoordinateWriter( DataDestination destination ) {
        super( destination );
    }

    @Override
    protected void openedWrite( Pair<Edge, List<Coordinate>> out ) throws IOException {
        Edge edge = out.a;
        List<Coordinate> coordinates = out.b;
        try {
            getWriter().writeStartElement( EDGE.shortLowerName() );
            getWriter().writeAttribute( ID.shortLowerName(), Edge.Id.toString( edge.getId() ) );
            for ( Coordinate coordinate : coordinates ) {
                getWriter().writeStartElement( COORDINATE.shortLowerName() );
                getWriter().writeAttribute( LATITUDE.shortLowerName(), Double.toString( coordinate.getLatitude() ) );
                getWriter().writeAttribute( LONGITUDE.shortLowerName(), Double.toString( coordinate.getLongitude() ) );
                getWriter().writeEndElement();
            }
            getWriter().writeEndElement();
            getWriter().flush();
        } catch ( XMLStreamException ex ) {
            throw new IOException( ex );
        }
    }

}
