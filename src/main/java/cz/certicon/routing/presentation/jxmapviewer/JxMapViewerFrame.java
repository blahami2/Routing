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
import java.util.ArrayList;
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
        Edge someEdge;
        for ( Edge edge : path ) {
            someEdge = edge;
            if ( someEdge != null ) {
                addWaypoint( path.getSourceNode(), someEdge.getLabel() );
            }
            List<Coordinates> coordinates = edge.getCoordinates( path.getGraph() );
            coordinates.stream().forEach( ( coord ) -> {
                track.add( new GeoPosition( coord.getLatitude(), coord.getLongitude() ) );
            } );
        }
        if ( path.size() > 1 ) {
            System.out.println( "path size = " + path.size() );
        }
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
            mapViewer.zoomToBestFit( fitGeoPosition, 0.8 );
            mapKit.setCenterPosition( fitGeoPosition.stream().findAny().get() );

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
}
