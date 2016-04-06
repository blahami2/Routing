/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.presentation.graphstream;

import cz.certicon.routing.model.entity.CartesianCoords;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.presentation.GraphPresenter;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.utils.GeometryUtils;
import cz.certicon.routing.utils.GraphUtils;
import java.awt.Dimension;
import java.awt.Point;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.Viewer;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class GraphStreamPresenter implements GraphPresenter {

    @Override
    public void displayGraph( cz.certicon.routing.model.entity.Graph graph ) {
        Graph displayGraph = new org.graphstream.graph.implementations.MultiGraph( "graph-id" );
        displayGraph.addAttribute( "ui.stylesheet", "edge {"
                //+ "shape: line;"
                //+ "fill-color: #222;"
                + "arrow-shape: arrow;"
                + "arrow-size: 8px, 4px;"
                + "}" );

        Dimension scaleDimension = new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE );
        Dimension targetDimension = new Dimension( 800, 800 );
        double minLat = Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double maxLon = Double.MIN_VALUE;
        for ( cz.certicon.routing.model.entity.Node node : graph.getNodes() ) {
            Coordinate c = node.getCoordinates();
            minLat = ( c.getLatitude() < minLat ) ? c.getLatitude() : minLat;
            minLon = ( c.getLongitude() < minLon ) ? c.getLongitude() : minLon;
            maxLat = ( c.getLatitude() > maxLat ) ? c.getLatitude() : maxLat;
            maxLon = ( c.getLongitude() > maxLon ) ? c.getLongitude() : maxLon;
        }
//        System.out.println( "min: " + minLat + ", " + minLon );
//        System.out.println( "max: " + maxLat + ", " + maxLon );
        Point min = CoordinateUtils.toPointFromWGS84( scaleDimension, new Coordinate( minLat, minLon ) );
        Point max = CoordinateUtils.toPointFromWGS84( scaleDimension, new Coordinate( maxLat, maxLon ) );

        for ( cz.certicon.routing.model.entity.Node node : graph.getNodes() ) {
            Node n = displayGraph.addNode( node.getId().toString() );
//            System.out.println( "point: " + node.getCoordinates() );
            Point p = GeometryUtils.getScaledPoint(
                    min,
                    max,
                    CoordinateUtils.toPointFromWGS84( scaleDimension, node.getCoordinates() ),
                    targetDimension );
            n.setAttribute( "xy", p.x, p.y );
//            System.out.println( "printing to: " + p );
            n.setAttribute( "ui.label", node.getCoordinates().toString() );
        }
        for ( Edge edge : graph.getEdges() ) {
            displayGraph.addEdge( edge.getId().toString(), edge.getSourceNode().getId().toString(), edge.getTargetNode().getId().toString(), true );
        }
        Viewer viewer = displayGraph.display();
        viewer.disableAutoLayout();
    }

}
