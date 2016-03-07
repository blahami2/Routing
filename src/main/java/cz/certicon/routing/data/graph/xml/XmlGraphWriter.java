/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph.xml;

import cz.certicon.routing.data.DataDestination;
import static cz.certicon.routing.data.graph.xml.Tag.*;
import cz.certicon.routing.data.graph.GraphWriter;
import cz.certicon.routing.model.entity.DirectedGraph;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlGraphWriter implements GraphWriter {

    private final DataDestination destination;
    private OutputStream output;
    private XMLStreamWriter writer;

    XmlGraphWriter( DataDestination destination ) {
        this.destination = destination;
    }

    @Override
    public GraphWriter open() throws IOException {
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
    public GraphWriter write( Graph graph ) throws IOException {
        if ( graph instanceof DirectedGraph ) {
            throw new IllegalArgumentException( "ERROR! Directed graph performs hardly reversible operations and is not supported for universal export." );
        }
        try {
            List<Node> sortedNodes = new ArrayList<>( graph.getNodes() );
            Collections.sort(sortedNodes, new Comparator<Node>() {
                @Override
                public int compare( Node o1, Node o2 ) {
                    return o1.getId().compareTo( o2.getId() );
                }
            });
            for ( Node node : sortedNodes ) {
                writer.writeStartElement( NODE.shortLowerName() );
                writer.writeAttribute( ID.shortLowerName(), Node.Id.toString( node.getId() ) );
                writer.writeAttribute( LATITUDE.shortLowerName(), Double.toString( node.getCoordinates().getLatitude() ) );
                writer.writeAttribute( LONGITUDE.shortLowerName(), Double.toString( node.getCoordinates().getLongitude() ) );
                writer.writeEndElement();
            }
            List<Edge> sortedEdges = new ArrayList<>( graph.getEdges() );
            Collections.sort(sortedEdges, new Comparator<Edge>() {
                @Override
                public int compare( Edge o1, Edge o2 ) {
                    return o1.getId().compareTo( o2.getId() );
                }
            });
            for ( Edge edge : sortedEdges ) {
                writer.writeStartElement( EDGE.shortLowerName() );
                writer.writeAttribute( ID.shortLowerName(), Edge.Id.toString( edge.getId() ) );
                writer.writeAttribute( SOURCE.shortLowerName(), Node.Id.toString( edge.getSourceNode().getId()) );
                writer.writeAttribute( TARGET.shortLowerName(), Node.Id.toString( edge.getTargetNode().getId()) );
                writer.writeAttribute( SPEED_FORWARD.shortLowerName(), Double.toString( edge.getAttributes().getSpeed(true) ) );
                writer.writeAttribute( SPEED_BACKWARD.shortLowerName(), Double.toString( edge.getAttributes().getSpeed(false) ) );
                writer.writeAttribute( LENGTH.shortLowerName(), Double.toString( edge.getAttributes().getLength() ) );
                writer.writeAttribute( ONEWAY.shortLowerName(), Boolean.toString( edge.getAttributes().isOneWay() ) );
                writer.writeAttribute( PAID.shortLowerName(), Boolean.toString( edge.getAttributes().isPaid() ) );
                writer.writeEndElement();
            }
            writer.flush();
        } catch ( XMLStreamException ex ) {
            throw new IOException( ex );
        }
        return this;
    }

    @Override
    public GraphWriter close() throws IOException {
        try {
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
        } catch ( XMLStreamException ex ) {
            throw new IOException( ex );
        }
        output.close();
        return this;
    }

}
