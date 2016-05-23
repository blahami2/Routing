/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.presentation.jxmapviewer;

import cz.certicon.routing.memsensitive.model.entity.Path;
import cz.certicon.routing.memsensitive.presentation.PathDisplayer;
import cz.certicon.routing.model.entity.Coordinate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class JxNodeViewer implements PathDisplayer {

    private JXMapViewer mapViewer;
    private JXMapKit mapKit;
    private JFrame frame;
    private DefaultTileFactory tileFactory;
    private final Set<GeoPosition> fitGeoPosition = new HashSet<>();
    private final List<Painter<JXMapViewer>> painters = new ArrayList<>();
    private final Set<LabelWaypoint> waypoints = new HashSet<>();

    public JxNodeViewer() {
        this.mapKit = new JXMapKit();
        this.mapViewer = mapKit.getMainMap();
    }

    @Override
    public void displayPath( Path path ) {
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
        if ( path.getCoordinates().size() > 0 ) {
            Coordinate source = path.getCoordinates().get( 0 );
            Coordinate target = path.getCoordinates().get( path.getCoordinates().size() - 1 );
            fitGeoPosition.add( new GeoPosition( source.getLatitude(), source.getLongitude() ) );
            fitGeoPosition.add( new GeoPosition( target.getLatitude(), target.getLongitude() ) );
            mapViewer.zoomToBestFit( fitGeoPosition, 0.7 );

            List<GeoPosition> track = new ArrayList<>();
            for ( Coordinate coordinate : path.getCoordinates() ) {
                track.add( new GeoPosition( coordinate.getLatitude(), coordinate.getLongitude() ) );
            }
            NodePainter routePainter = new NodePainter(track );
            for ( LabelWaypoint waypoint : waypoints ) {
                mapViewer.add( waypoint.getComponent() );
            }
            painters.add( routePainter );
            LabelWaypointOverlayPainter p = new LabelWaypointOverlayPainter();
            p.setWaypoints( waypoints );
            painters.add( p );
            CompoundPainter<JXMapViewer> painter = new CompoundPainter<>( painters );
            mapViewer.setOverlayPainter( painter );
        }
    }

}
