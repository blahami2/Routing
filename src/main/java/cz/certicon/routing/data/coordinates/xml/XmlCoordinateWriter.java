/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.basic.xml.AbstractXmlWriter;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import static cz.certicon.routing.data.coordinates.xml.Tag.*;
import cz.certicon.routing.data.coordinates.CoordinateWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of the {@link CoordinateWriter} interfaces using the
 * {@link AbstractXmlWriter} class.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlCoordinateWriter extends AbstractXmlWriter<Map<Edge, List<Coordinates>>> implements CoordinateWriter {

    public XmlCoordinateWriter( DataDestination destination ) {
        super( destination );
    }

    @Override
    protected void checkedWrite( Map<Edge, List<Coordinates>> out ) throws IOException {
        Map<Long, Boolean> dataMap = new HashMap<>();
        for ( Map.Entry<Edge, List<Coordinates>> entry : out.entrySet() ) {
            Edge edge = entry.getKey();
            if ( !dataMap.containsKey( edge.getDataId() ) ) {
                List<Coordinates> coordinates = entry.getValue();
                try {
                    getWriter().writeStartElement( EDGE.shortLowerName() );
                    getWriter().writeAttribute( DATA_ID.shortLowerName(), Long.toString( edge.getDataId() ) );
                    for ( Coordinates coordinate : coordinates ) {
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
                dataMap.put( edge.getDataId(), Boolean.TRUE );
            }
        }
    }

}
