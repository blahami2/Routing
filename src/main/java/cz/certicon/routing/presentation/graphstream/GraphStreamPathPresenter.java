/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.presentation.graphstream;

import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.presentation.PathPresenter;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.utils.GeometryUtils;
import cz.certicon.routing.utils.GraphUtils;
import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.Viewer;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class GraphStreamPathPresenter implements PathPresenter {

    private final GraphEntityFactory graphEntityFactory;
    private boolean displayNodes = true;
    private boolean displayEdges = true;

    public GraphStreamPathPresenter( GraphEntityFactory graphEntityFactory ) {
        this.graphEntityFactory = graphEntityFactory;
    }

    @Override
    public PathPresenter clearPaths() {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PathPresenter addPath( Path path ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PathPresenter display() {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PathPresenter displayPath( Path path ) {
        List<cz.certicon.routing.model.entity.Node> nodes = path.getNodes();
        cz.certicon.routing.model.entity.Node center = nodes.get( nodes.size() / 2 );
        int distance = nodes.size() / 2 + 2;
        cz.certicon.routing.model.entity.Graph subgraph = GraphUtils.subgraph( path.getGraph(), graphEntityFactory, center, distance );
        int counter = 0;
        Map<Coordinate, Integer> idMap = new HashMap<>();
        Graph displayGraph = new org.graphstream.graph.implementations.MultiGraph( "graph-id" );
        displayGraph.addAttribute( "ui.stylesheet", "edge {"
                //+ "shape: line;"
                //+ "fill-color: #222;"
                + "arrow-shape: arrow;"
                + "arrow-size: 8px, 4px;"
                + "}"
                + "edge.route {"
                + "fill-color: red;"
                + "}" );

        Dimension scaleDimension = new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE );
        Dimension targetDimension = new Dimension( 800, 800 );
        double minLat = Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double maxLon = Double.MIN_VALUE;
        for ( cz.certicon.routing.model.entity.Node node : subgraph.getNodes() ) {
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

        for ( cz.certicon.routing.model.entity.Node node : subgraph.getNodes() ) {
            Node n = displayGraph.addNode( node.getId().toString() );
            Integer id = idMap.get( node.getCoordinates() );
            if ( id == null ) {
                id = counter++;
                idMap.put( node.getCoordinates(), id );
            }
//            System.out.println( "point: " + node.getCoordinates() );
            Point p = GeometryUtils.getScaledPoint(
                    min,
                    max,
                    CoordinateUtils.toPointFromWGS84( scaleDimension, node.getCoordinates() ),
                    targetDimension );
            n.setAttribute( "xy", p.x, p.y );
            if ( displayNodes ) {
                n.setAttribute( "ui.label", id.toString() );
            }
//            System.out.println( "printing to: " + p );
        }
        Map<Edge.Id, Boolean> edgeMap = new HashMap<>();
        for ( Edge edge : path.getEdges() ) {
            edgeMap.put( edge.getId(), Boolean.TRUE );
        }
        for ( Edge edge : subgraph.getEdges() ) {
            org.graphstream.graph.Edge addEdge = displayGraph.addEdge( edge.getId().toString(), edge.getSourceNode().getId().toString(), edge.getTargetNode().getId().toString(), true );
            if ( edgeMap.containsKey( edge.getId() ) ) {
                addEdge.setAttribute( "ui.class", "route" );
            }
        }
        Viewer viewer = displayGraph.display();
        viewer.disableAutoLayout();
        return this;
    }

    @Override
    public PathPresenter displayPath( Path path, int millis ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PathPresenter addWaypoint( Coordinate coordinate, String text ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PathPresenter setDisplayNodeText( boolean displayNodeText ) {
//        displayNodes = displayNodeText;
        return this;
    }

    @Override
    public PathPresenter setDisplayEdgeText( boolean displayEdgeText ) {
        displayEdges = displayEdgeText;
        return this;
    }

}
