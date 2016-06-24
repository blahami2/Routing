/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.presentation.jxmapviewer;

import cz.certicon.routing.data.basic.database.impl.StringSqliteReader;
import cz.certicon.routing.memsensitive.presentation.DebugViewer;
import cz.certicon.routing.model.basic.TimeUnits;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.utils.GeometryUtils;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import java.awt.Color;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class JxDebugViewer implements DebugViewer {

    private JXMapViewer mapViewer;
    private JXMapKit mapKit;
    private JFrame frame;
    private DefaultTileFactory tileFactory;
    private final Set<GeoPosition> fitGeoPosition = new HashSet<>();
    private final List<Painter<JXMapViewer>> painters = new ArrayList<>();
    private final Set<LabelWaypoint> waypoints = new HashSet<>();
    private final StringSqliteReader reader;
    private Map<Long, Painter<JXMapViewer>> painterMap = new HashMap<>();
    private final long delay;
    private boolean centered = false;

    public JxDebugViewer( Properties properties, long delayInMillis ) {
        this.mapKit = new JXMapKit();
        this.mapViewer = mapKit.getMainMap();
        this.delay = delayInMillis;
        frame = new JFrame( "map" );
        frame.setContentPane( mapViewer );
        frame.setSize( 800, 600 );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setVisible( true );
        TileFactoryInfo info = new OSMTileFactoryInfo();
        tileFactory = new DefaultTileFactory( info );
        tileFactory.setThreadPoolSize( 8 );
        mapKit.setTileFactory( tileFactory );
        reader = new StringSqliteReader( properties );
    }

    @Override
    public void blinkEdge( long edgeId ) {
        displayEdge( edgeId );
        removeEdge( edgeId );
    }

    @Override
    public void displayEdge( long edgeId ) {
        TimeMeasurement time = new TimeMeasurement();
        time.setTimeUnits( TimeUnits.MILLISECONDS );
        time.start();
        List<Coordinate> coords = loadCoordinates( edgeId );
        List<GeoPosition> track = new ArrayList<>();
        for ( Coordinate coordinate : coords ) {
            track.add( new GeoPosition( coordinate.getLatitude(), coordinate.getLongitude() ) );
        }
        fitGeoPosition.addAll( track );
        RoutePainter routePainter = new RoutePainter( track );
        painterMap.put( edgeId, routePainter );
        painters.add( routePainter );

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>( painters );
        mapViewer.setOverlayPainter( painter );

        if ( !centered ) {
            centered = true;
            mapViewer.zoomToBestFit( fitGeoPosition, 0.7 );
        }
        long timeLeft = delay - time.stop();
        if ( timeLeft < 0 ) {
            timeLeft = 0;
        }
        try {
            Thread.sleep( timeLeft );
        } catch ( InterruptedException ex ) {
            Logger.getLogger( JxDebugViewer.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    @Override
    public void removeEdge( long edgeId ) {
        Painter<JXMapViewer> p = painterMap.get( edgeId );
        painterMap.remove( edgeId );
        painters.remove( p );
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>( painters );
        mapViewer.setOverlayPainter( painter );
    }

    @Override
    public void closeEdge( long edgeId ) {
        TimeMeasurement time = new TimeMeasurement();
        time.setTimeUnits( TimeUnits.MILLISECONDS );
        time.start();
        if ( !painterMap.containsKey( edgeId ) ) {
            displayEdge( edgeId );
        }
        RoutePainter p = (RoutePainter) painterMap.get( edgeId );
        p.setColor( Color.blue );
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>( painters );
        mapViewer.setOverlayPainter( painter );
        fitGeoPosition.removeAll( p.track );
        if ( !centered ) {
            centered = true;
            mapViewer.zoomToBestFit( fitGeoPosition, 0.7 );
        }
        long timeLeft = delay - time.stop();
        if ( timeLeft < 0 ) {
            timeLeft = 0;
        }
        try {
            Thread.sleep( timeLeft );
        } catch ( InterruptedException ex ) {
            Logger.getLogger( JxDebugViewer.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch ( IOException ex ) {
            Logger.getLogger( JxDebugViewer.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    private List<Coordinate> loadCoordinates( long edgeId ) {
        try {
            String query = "SELECT ST_AsText(geom) AS geom FROM edges e JOIN edges_data d ON e.data_id = d.id WHERE e.id = " + edgeId + ";";
            ResultSet rs = reader.read( query );
            if ( rs.next() ) {
                return GeometryUtils.toCoordinatesFromWktLinestring( rs.getString( "geom" ) );
            }
        } catch ( IOException | SQLException ex ) {
            Logger.getLogger( JxDebugViewer.class.getName() ).log( Level.SEVERE, null, ex );
        }
        return new ArrayList<>();
    }
}
