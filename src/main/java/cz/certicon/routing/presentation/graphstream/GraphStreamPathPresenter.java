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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.Viewer;

/**
 * An implementation of {@link PathPresenter} using a GraphStream library.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class GraphStreamPathPresenter implements PathPresenter {

    private static final int MOVE = 15;
    private static final int NUM_COLORS = 20;

    private final GraphEntityFactory graphEntityFactory;
    private boolean displayNodes = true;
    private boolean displayEdges = true;

    private final List<Color> colorList;
//    private final String stylesheet;
    private int colorCounter = 0;

    public GraphStreamPathPresenter( GraphEntityFactory graphEntityFactory ) {
        this.graphEntityFactory = graphEntityFactory;
//        StringBuilder styleSheetBuilder = new StringBuilder();
//        styleSheetBuilder.append( "edge {"
//                //+ "shape: line;"
//                //+ "fill-color: #222;"
//                + "arrow-shape: arrow;"
//                + "arrow-size: 8px, 4px;"
//                + "}"
//                + "edge.route {"
//                + "fill-color: red;"
//                + "}" );
        this.colorList = new ArrayList<>();
        float interval = 360 / ( NUM_COLORS );
        for ( float x = 0; x < 360; x += interval ) {
            Color c = Color.getHSBColor( x / 360, 1, 1 );
            colorList.add( c );
//            styleSheetBuilder.append( "node.color" ).append( Integer.toString( c.getRGB() ) ).append( "{" )
//                    .append( "fill-color: " );
        }
//        stylesheet = styleSheetBuilder.toString();
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
        int distance = nodes.size() / 2 + 3;
        cz.certicon.routing.model.entity.Graph subgraph = GraphUtils.subgraph( path.getGraph(), graphEntityFactory, center, distance );
        Map<Coordinate, List<cz.certicon.routing.model.entity.Node>> nodeMap = new HashMap<>();
        Graph displayGraph = new org.graphstream.graph.implementations.MultiGraph( "graph-id" );
        displayGraph.addAttribute( "ui.stylesheet", "edge {"
                //+ "shape: line;"
                //+ "fill-color: #222;"
                + "arrow-shape: arrow;"
                + "arrow-size: 8px, 4px;"
                + "}"
                + "edge.route {"
                + "fill-color: red;"
                + "size: 3px;"
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
            List<cz.certicon.routing.model.entity.Node> nodeList = nodeMap.get( node.getCoordinates() );
            if ( nodeList == null ) {
                nodeList = new ArrayList<>();
                nodeMap.put( node.getCoordinates(), nodeList );
            }
            nodeList.add( node );
        }
        for ( List<cz.certicon.routing.model.entity.Node> nodeList : nodeMap.values() ) {
            Color c = nextColor();
            String fillColor = "fill-color: " + toCssRgb( c ) + ";";
            Point p = GeometryUtils.getScaledPoint(
                    min,
                    max,
                    CoordinateUtils.toPointFromWGS84( scaleDimension, nodeList.get( 0 ).getCoordinates() ),
                    targetDimension );
            if ( nodeList.size() == 1 ) {
                Node n = displayGraph.addNode( nodeList.get( 0 ).getId().toString() );
                n.setAttribute( "xy", p.x, p.y );
                n.addAttribute( "ui.style", fillColor );
                if ( displayNodes ) {
                    n.setAttribute( "ui.label", Integer.toString( nodeList.get( 0 ).getCoordinates().hashCode() ) );
                }
            } else {
                int step = 360 / nodeList.size();
                int degree = 0;
                for ( int i = 0; i < nodeList.size(); i++ ) {
                    double alpha = 2 * Math.PI * degree / 360;
                    int xDiff = (int) Math.round( MOVE * Math.cos( alpha ) );
                    int yDiff = (int) Math.round( MOVE * Math.sin( alpha ) );
                    Node n = displayGraph.addNode( nodeList.get( i ).getId().toString() );
                    n.setAttribute( "xy", p.x + xDiff, p.y + yDiff );
                    n.addAttribute( "ui.style", fillColor );
                    if ( displayNodes ) {
                        n.setAttribute( "ui.label", Integer.toString( nodeList.get( i ).getCoordinates().hashCode() ) );
                    }
                    degree += step;
                }
            }
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
        displayNodes = displayNodeText;
        return this;
    }

    @Override
    public PathPresenter setDisplayEdgeText( boolean displayEdgeText ) {
        displayEdges = displayEdgeText;
        return this;
    }

    private Color nextColor() {
        return colorList.get( colorCounter++ % colorList.size() );
    }

    private String toCssRgb( Color color ) {
        return "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
    }

}
