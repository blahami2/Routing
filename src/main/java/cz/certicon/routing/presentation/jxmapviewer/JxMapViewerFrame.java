/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.presentation.jxmapviewer;

import cz.certicon.routing.model.entity.Coordinate;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.OSMTileFactoryInfo;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;

/**
 * An implementation of {@link PathPresenter} using a JXMapViewer library.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class JxMapViewerFrame implements PathPresenter {

    private JXMapViewer mapViewer;
    private JXMapKit mapKit;
    private JFrame frame;
    private DefaultTileFactory tileFactory;
    private final Set<GeoPosition> fitGeoPosition = new HashSet<>();
    private final List<Painter<JXMapViewer>> painters = new ArrayList<>();
    private final Set<LabelWaypoint> waypoints = new HashSet<>();
    private boolean displayNodeText = true;
    private boolean displayEdgeText = true;

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
//        System.out.println( "Source = " + path.getSourceNode() );
//        System.out.println( "Destination = " + path.getTargetNode() );
        List<GeoPosition> track = new ArrayList<>();
//        Edge someEdge;
        Node source = path.getSourceNode();
        Node currentNode = source;
        StringBuilder sb = new StringBuilder();
//        addWaypoint( path.getSourceNode(), "SOURCE = " + path.getSourceNode().getDistance() );
//        for ( Edge edge : path ) {
//            sb.insert( 0, currentNode.getCoordinates() + ", " + currentNode.getDistance() + "\n" );
//            addWaypoint( edge, edge.getLabel() + "\nfrom: " + edge.getSourceNode() + "\nto: " + edge.getTargetNode() );
//            if ( !currentNode.equals( path.getSourceNode() ) && !currentNode.equals( path.getTargetNode() ) ) {
//                addWaypoint( currentNode, currentNode.getDistance().toString() );
//            }
//
//            List<Coordinates> coordinates = edge.getCoordinates();
//            if ( currentNode.equals( edge.getSourceNode() ) ) {
////                System.out.println( "from " + currentNode );
//                for ( int i = 0; i < coordinates.size(); i++ ) {
//                    Coordinates coord = coordinates.get( i );
//                    track.add( new GeoPosition( coord.getLatitude(), coord.getLongitude() ) );
//                }
//                currentNode = edge.getTargetNode();
//            } else {
////                System.out.println( "from " + edge.getTargetNode() );
//                for ( int i = coordinates.size() - 1; i >= 0; i-- ) {
//                    Coordinates coord = coordinates.get( i );
//                    track.add( new GeoPosition( coord.getLatitude(), coord.getLongitude() ) );
//                }
//                currentNode = edge.getSourceNode();
//            }
//        }
        for ( Coordinate coordinate : path.getCoordinates() ) {
            track.add( new GeoPosition( coordinate.getLatitude(), coordinate.getLongitude() ) );
        }
        if ( path.size() > 1 ) {
            System.out.println( "path size = " + path.size() );
        }
        for ( Node node : path.getNodes() ) {
            addWaypoint( node.getCoordinates(), node.getLabel() );
        }
//        addWaypoint( path.getTargetNode(), "TARGET = " + path.getTargetNode().getDistance() + "\n" + sb.toString() );

        fitGeoPosition.addAll( track );
        RoutePainter routePainter = new RoutePainter( track );
        painters.add( routePainter );
//        JToolTip tooltip = new JToolTip();
//        mapViewer.add( tooltip );
        return this;
    }

    @Override
    public PathPresenter display() {
//        SwingUtilities.invokeLater( () -> {
        frame = new JFrame( "map" );
        frame.setContentPane( mapViewer );
        frame.setSize( 800, 600 );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setVisible( true );

        TileFactoryInfo info = new OSMTileFactoryInfo();
        tileFactory = new DefaultTileFactory( info );
        tileFactory.setThreadPoolSize( 8 );
        mapKit.setTileFactory( tileFactory );
//            mapViewer.setTileFactory( tileFactory );
        mapViewer.zoomToBestFit( fitGeoPosition, 0.8 );
        for ( LabelWaypoint waypoint : waypoints ) {
            mapViewer.add( waypoint.getComponent() );
        }
        LabelWaypointOverlayPainter p = new LabelWaypointOverlayPainter();
        p.setWaypoints( waypoints );
        painters.add( p );
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>( painters );
        mapViewer.setOverlayPainter( painter );

//
//            JToolTip tooltip = new JToolTip();
//            mapViewer.add( tooltip );
//        } );
        return this;
    }

    @Override
    public PathPresenter displayPath( Path path ) {
        return displayPath( path, -1 );
    }

    @Override
    public PathPresenter displayPath( Path path, int millis ) {
        frame = new JFrame( "map" );
        frame.setContentPane( mapViewer );
        frame.setSize( 800, 600 );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setVisible( true );
        TileFactoryInfo info = new OSMTileFactoryInfo();
        tileFactory = new DefaultTileFactory( info );
        tileFactory.setThreadPoolSize( 8 );
        mapKit.setTileFactory( tileFactory );

        fitGeoPosition.clear();
        fitGeoPosition.add( new GeoPosition( path.getSourceNode().getCoordinates().getLatitude(), path.getSourceNode().getCoordinates().getLongitude() ) );
        fitGeoPosition.add( new GeoPosition( path.getTargetNode().getCoordinates().getLatitude(), path.getTargetNode().getCoordinates().getLongitude() ) );
        mapViewer.zoomToBestFit( fitGeoPosition, 0.7 );

        List<GeoPosition> track = new ArrayList<>();
        Node source = path.getSourceNode();
        Node currentNode = source;
        StringBuilder sb = new StringBuilder();

        for ( Node node : path.getNodes() ) {
            addWaypoint( node.getCoordinates(), node.getLabel() );
        }

        addWaypoint( path.getSourceNode(), "SOURCE = " + path.getSourceNode().getDistance() );
//        for ( Edge edge : path ) {
//            sb.insert( 0, currentNode.getCoordinates() + ", " + currentNode.getDistance() + "\n" );
////            sb.append( currentNode.getCoordinates() ).append( ", " ).append( currentNode.getDistance() ).append( "\n" );
//            addWaypoint( edge, edge.getLabel() + "\n" + edge );
//            if ( !currentNode.equals( path.getSourceNode() ) && !currentNode.equals( path.getTargetNode() ) ) {
//                addWaypoint( currentNode, currentNode.getDistance().toString() + "\n" + sb.toString() );
//            }
//
//            List<Coordinates> coordinates = edge.getCoordinates();
//            if ( currentNode.equals( edge.getSourceNode() ) ) {
//                for ( int i = 0; i < coordinates.size(); i++ ) {
//                    Coordinates coord = coordinates.get( i );
//                    track.add( new GeoPosition( coord.getLatitude(), coord.getLongitude() ) );
//                }
//                currentNode = edge.getTargetNode();
//            } else {
//                for ( int i = coordinates.size() - 1; i >= 0; i-- ) {
//                    Coordinates coord = coordinates.get( i );
//                    track.add( new GeoPosition( coord.getLatitude(), coord.getLongitude() ) );
//                }
//                currentNode = edge.getSourceNode();
//            }
//            if ( millis > 0 ) {
//                RoutePainter routePainter = new RoutePainter( track );
//                for ( LabelWaypoint waypoint : waypoints ) {
//                    mapViewer.add( waypoint.getComponent() );
//                }
//                painters.add( routePainter );
//                LabelWaypointOverlayPainter p = new LabelWaypointOverlayPainter();
//                p.setWaypoints( waypoints );
//                painters.add( p );
//                CompoundPainter<JXMapViewer> painter = new CompoundPainter<>( painters );
//                mapViewer.setOverlayPainter( painter );
//                try {
//                    Thread.sleep( millis );
//                } catch ( InterruptedException ex ) {
//                    Logger.getLogger( JxMapViewerFrame.class.getName() ).log( Level.SEVERE, null, ex );
//                }
//            }
//        }
        for ( Coordinate coordinate : path.getCoordinates() ) {
            track.add( new GeoPosition( coordinate.getLatitude(), coordinate.getLongitude() ) );
        }

        addWaypoint( path.getTargetNode(), "TARGET = " + path.getTargetNode().getDistance() + "\n" + sb.toString() );

        RoutePainter routePainter = new RoutePainter( track );
        for ( LabelWaypoint waypoint : waypoints ) {
            mapViewer.add( waypoint.getComponent() );
        }
        painters.add( routePainter );
        LabelWaypointOverlayPainter p = new LabelWaypointOverlayPainter();
        p.setWaypoints( waypoints );
        painters.add( p );
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>( painters );
        mapViewer.setOverlayPainter( painter );
        return this;
    }

    private void addWaypoint( Node node, String text ) {
        if ( displayNodeText ) {
            waypoints.add( new LabelWaypoint( text, new GeoPosition( node.getCoordinates().getLatitude(), node.getCoordinates().getLongitude() ) ) );
        }
    }

    @Override
    public PathPresenter setDisplayNodeText( boolean displayNodeText ) {
        this.displayNodeText = displayNodeText;
        return this;
    }

    @Override
    public PathPresenter setDisplayEdgeText( boolean displayEdgeText ) {
        this.displayEdgeText = displayEdgeText;
        return this;
    }

    private void addWaypoint( Edge edge, String text ) {
        if ( displayEdgeText ) {
            Coordinate midpoint = CoordinateUtils.calculateGeographicMidpoint( Arrays.asList( edge.getSourceNode().getCoordinates(), edge.getTargetNode().getCoordinates() ) );
            waypoints.add( new LabelWaypoint( text, new GeoPosition( midpoint.getLatitude(), midpoint.getLongitude() ) ) );
        }
    }

    @Override
    public PathPresenter addWaypoint( Coordinate coordinate, String text ) {
        waypoints.add( new LabelWaypoint( text, new GeoPosition( coordinate.getLatitude(), coordinate.getLongitude() ) ) );
        return this;
    }
}
