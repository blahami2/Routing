/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph.xml;

import cz.certicon.routing.data.basic.xml.AbstractXmlWriter;
import cz.certicon.routing.data.DataDestination;
import static cz.certicon.routing.data.graph.xml.Tag.*;
import cz.certicon.routing.data.graph.GraphWriter;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

/**
 * An implementation of the {@link GraphWriter} interfaces using the
 * {@link AbstractXmlWriter} class.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlGraphWriter extends AbstractXmlWriter<Graph> implements GraphWriter {

    public XmlGraphWriter( DataDestination destination ) {
        super( destination );
    }

    @Override
    protected void checkedWrite( Graph graph ) throws IOException {
//        if ( graph instanceof DirectedGraph ) {
//            throw new IllegalArgumentException( "ERROR! Directed graph performs hardly reversible operations and is not supported for universal export." );
//        }
        try {
            List<Node> sortedNodes = new ArrayList<>( graph.getNodes() );
            Collections.sort( sortedNodes, new Comparator<Node>() {
                @Override
                public int compare( Node o1, Node o2 ) {
                    return o1.getId().compareTo( o2.getId() );
                }
            } );
            Map<Node.Id, Node> nodeMap = new HashMap<>();
            for ( Node node : sortedNodes ) {
                nodeMap.put( node.getId(), node );

                getWriter().writeStartElement( NODE.shortLowerName() );
                getWriter().writeAttribute( ID.shortLowerName(), Node.Id.toString( node.getId() ) );
                getWriter().writeAttribute( LATITUDE.shortLowerName(), Double.toString( node.getCoordinates().getLatitude() ) );
                getWriter().writeAttribute( LONGITUDE.shortLowerName(), Double.toString( node.getCoordinates().getLongitude() ) );
                getWriter().writeAttribute( OSM_ID.shortLowerName(), Long.toString( node.getOsmId() ) );
                getWriter().writeEndElement();
            }
            List<Edge> sortedEdges = new ArrayList<>( graph.getEdges() );
            Collections.sort( sortedEdges, new Comparator<Edge>() {
                @Override
                public int compare( Edge o1, Edge o2 ) {
                    return o1.getId().compareTo( o2.getId() );
                }
            } );
            for ( Edge edge : sortedEdges ) {
                getWriter().writeStartElement( EDGE.shortLowerName() );
                getWriter().writeAttribute( ID.shortLowerName(), Edge.Id.toString( edge.getId() ) );
                getWriter().writeAttribute( SOURCE.shortLowerName(), Node.Id.toString( edge.getSourceNode().getId() ) );
                getWriter().writeAttribute( TARGET.shortLowerName(), Node.Id.toString( edge.getTargetNode().getId() ) );
                getWriter().writeAttribute( SPEED.shortLowerName(), Integer.toString( edge.getSpeed() ) );
                getWriter().writeAttribute( LENGTH.shortLowerName(), Double.toString( edge.getAttributes().getLength() ) );
                getWriter().writeAttribute( PAID.shortLowerName(), Boolean.toString( edge.getAttributes().isPaid() ) );
                getWriter().writeAttribute( OSM_ID.shortLowerName(), Long.toString( edge.getOsmId() ) );
                getWriter().writeAttribute( DATA_ID.shortLowerName(), Long.toString( edge.getDataId() ) );
                getWriter().writeEndElement();
            }
            getWriter().flush();
        } catch ( XMLStreamException ex ) {
            throw new IOException( ex );
        }
        close();
    }

}
