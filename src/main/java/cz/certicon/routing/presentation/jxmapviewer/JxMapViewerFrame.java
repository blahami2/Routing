/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.presentation.jxmapviewer;

import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.presentation.PathPresenter;
import cz.certicon.routing.utils.CoordinateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.OSMTileFactoryInfo;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class JxMapViewerFrame implements PathPresenter {

    private final JXMapViewer mapViewer;
    private final JXMapKit mapKit;
    private final Set<GeoPosition> fitGeoPosition = new HashSet<>();
    private final List<Painter<JXMapViewer>> painters = new ArrayList<>();
    private final Set<LabelWaypoint> waypoints = new HashSet<>();

    public JxMapViewerFrame() {
        this.mapKit = new JXMapKit();
        this.mapViewer = mapKit.getMainMap();
    }

    @Override
    public PathPresenter clearPaths() {
        fitGeoPosition.clear();
        painters.clear();
        waypoints.clear();
        return this;
    }

    @Override
    public PathPresenter addPath( Path path ) {
        List<GeoPosition> track = new ArrayList<>();
//        Edge someEdge;
        Node source = path.getSourceNode();
        Node currentNode = source;
        for ( Edge edge : path ) {
//            someEdge = edge;
//            if ( someEdge != null ) {
//                addWaypoint( path.getSourceNode(), someEdge.getLabel() );
//            }
            addWaypoint( edge, edge.getLabel() );
            addWaypoint( currentNode, currentNode.getLabel() );

            List<Coordinates> coordinates = edge.getCoordinates( path.getGraph() );
            if ( currentNode.equals( edge.getSourceNode() ) ) {
                for ( int i = 0; i < coordinates.size(); i++ ) {
                    Coordinates coord = coordinates.get( i );
                    track.add( new GeoPosition( coord.getLatitude(), coord.getLongitude() ) );
                }
                currentNode = edge.getTargetNode();
            } else {
                for ( int i = coordinates.size() - 1; i >= 0; i-- ) {
                    Coordinates coord = coordinates.get( i );
                    track.add( new GeoPosition( coord.getLatitude(), coord.getLongitude() ) );
                }
                currentNode = edge.getSourceNode();
            }
        }
        if ( path.size() > 1 ) {
            System.out.println( "path size = " + path.size() );
        }

        addWaypoint( path.getSourceNode(), path.getSourceNode().getLabel() );
        addWaypoint( path.getTargetNode(), path.getTargetNode().getLabel() );

        fitGeoPosition.addAll( track );
        RoutePainter routePainter = new RoutePainter( track );
        painters.add( routePainter );
//        JToolTip tooltip = new JToolTip();
//        mapViewer.add( tooltip );
        return this;
    }

    @Override
    public PathPresenter display() {
        SwingUtilities.invokeLater( () -> {
            TileFactoryInfo info = new OSMTileFactoryInfo();
            DefaultTileFactory tileFactory = new DefaultTileFactory( info );
            mapKit.setTileFactory( tileFactory );
            tileFactory.setThreadPoolSize( 8 );
            mapViewer.setTileFactory( tileFactory );
            LabelWaypointOverlayPainter p = new LabelWaypointOverlayPainter();
            p.setWaypoints( waypoints );
            painters.add( p );
            CompoundPainter<JXMapViewer> painter = new CompoundPainter<>( painters );
            mapViewer.setOverlayPainter( painter );
            waypoints.stream().forEach( ( waypoint ) -> {
                mapViewer.add( waypoint.getComponent() );
            } );
            mapKit.setCenterPosition( fitGeoPosition.stream().findAny().get() );
            mapViewer.zoomToBestFit( fitGeoPosition, 0.8 );

            JFrame frame = new JFrame( "map" );

            frame.setContentPane( mapViewer );
            frame.setSize( 1200, 800 );
            frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
            frame.setVisible( true );
        } );
        return this;
    }

    private void addWaypoint( Node node, String text ) {
        waypoints.add( new LabelWaypoint( text, new GeoPosition( node.getCoordinates().getLatitude(), node.getCoordinates().getLongitude() ) ) );
    }

    private void addWaypoint( Edge edge, String text ) {
//        Coordinates sc = edge.getSourceNode().getCoordinates();
//        Coordinates tc = edge.getTargetNode().getCoordinates();
//        double avgLat = (sc.getLatitude() + tc.getLatitude() ) / 2;
//        double avgLon = (sc.getLongitude()+ tc.getLongitude() ) / 2;
//        waypoints.add( new LabelWaypoint( text, new GeoPosition( avgLat, avgLon ) ) );
        Coordinates midpoint = CoordinateUtils.calculateGeographicMidpoint( Arrays.asList( edge.getSourceNode().getCoordinates(), edge.getTargetNode().getCoordinates() ) );
        waypoints.add( new LabelWaypoint( text, new GeoPosition( midpoint.getLatitude(), midpoint.getLongitude() ) ) );
    }
}
