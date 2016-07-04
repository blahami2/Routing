/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.presentation.jxmapviewer;

import cz.certicon.routing.data.basic.database.impl.StringSqliteReader;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import cz.certicon.routing.memsensitive.presentation.DebugViewer;
import cz.certicon.routing.model.basic.TimeUnits;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.utils.GeometryUtils;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import java.awt.Color;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
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
    private PreprocessedData preprocessedData;
    private Graph graph;
    private boolean stepByInput = false;

    {
        this.mapKit = new JXMapKit();
        this.mapViewer = mapKit.getMainMap();
        frame = new JFrame( "map" );
        frame.setContentPane( mapViewer );
        frame.setSize( 800, 600 );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setVisible( true );
        TileFactoryInfo info = new OSMTileFactoryInfo();
        tileFactory = new DefaultTileFactory( info );
        tileFactory.setThreadPoolSize( 8 );
        mapKit.setTileFactory( tileFactory );
    }

    public JxDebugViewer( Properties properties, long delayInMillis ) {
        this.delay = delayInMillis;
        reader = new StringSqliteReader( properties );
    }

    public JxDebugViewer( Properties properties, long delayInMillis, PreprocessedData preprocessedData, Graph graph ) {
        this.delay = delayInMillis;
        reader = new StringSqliteReader( properties );
        this.preprocessedData = preprocessedData;
        this.graph = graph;
    }

    @Override
    public void setStepByInput( boolean stepByInput ) {
        this.stepByInput = stepByInput;
    }

    @Override
    public void blinkEdge( long edgeId ) {
        displayEdge( edgeId );
        removeEdge( edgeId );
    }

    @Override
    public void displayNode( long nodeId, Graph graph ) {
        TimeMeasurement time = new TimeMeasurement();
        time.setTimeUnits( TimeUnits.MILLISECONDS );
        time.start();
        int node = graph.getNodeByOrigId( nodeId );
        painters.add( new NodePainter( new GeoPosition( graph.getLatitude( node ), graph.getLongitude( node ) ) ) );
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>( painters );
        mapViewer.setOverlayPainter( painter );

        if ( !centered ) {
            centered = true;
            mapViewer.zoomToBestFit( fitGeoPosition, 0.7 );
        }
        nextStep( time.stop() );
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
        nextStep( time.stop() );
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
        nextStep( time.stop() );
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch ( IOException ex ) {
            Logger.getLogger( JxDebugViewer.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    private void nextStep( long elapsedTime ) {
        if ( stepByInput ) {
            try {
                System.in.read();
            } catch ( IOException ex ) {
                Logger.getLogger( JxDebugViewer.class.getName() ).log( Level.SEVERE, null, ex );
            }
        } else {
            long timeLeft = delay - elapsedTime;
            if ( timeLeft < 0 ) {
                timeLeft = 0;
            }
            try {
                Thread.sleep( timeLeft );
            } catch ( InterruptedException ex ) {
                Logger.getLogger( JxDebugViewer.class.getName() ).log( Level.SEVERE, null, ex );
            }

        }
    }

    private List<Coordinate> loadCoordinates( long edgeId ) {
        try {
//            System.out.println( "edge id = " + edgeId );
            String query = "SELECT ST_AsText(geom) AS geom FROM edges e JOIN edges_data d ON e.data_id = d.id WHERE e.id = " + edgeId + ";";
            ResultSet rs = reader.read( query );
            if ( rs.next() ) {
//                System.out.println( "query = " + query );
                return GeometryUtils.toCoordinatesFromWktLinestring( rs.getString( "geom" ) );
            } else if ( preprocessedData != null ) {
                LinkedList<Long> edges = new LinkedList<>();
                int edge = preprocessedData.getEdgeByOrigId( edgeId, graph );
                addEdgeAsLast( edges, edge, preprocessedData, graph );
//                System.out.println( "edges = " + edges );
                StringBuilder sb = new StringBuilder();
                sb.append( "SELECT ST_AsText(geom) AS geom, e.id AS id FROM edges e JOIN edges_data d ON e.data_id = d.id WHERE e.id IN (" );
                for ( Long e : edges ) {
                    sb.append( e ).append( "," );
                }
                sb.replace( sb.length() - 1, sb.length(), "" );
                sb.append( ")" );
//                System.out.println( "query = " + sb.toString() );
                rs = reader.read( sb.toString() );
                Map<Long, List<Coordinate>> coordMap = new HashMap<>();
                while ( rs.next() ) {
                    coordMap.put( rs.getLong( "id" ), GeometryUtils.toCoordinatesFromWktLinestring( rs.getString( "geom" ) ) );
                }
                List<Coordinate> coords = new ArrayList<>();
                boolean first = true;
                for ( Long e : edges ) {
                    List<Coordinate> list = coordMap.get( e );
                    if ( !coords.isEmpty() ) {
                        Coordinate a = coords.get( 0 );
                        Coordinate b = coords.get( coords.size() - 1 );
                        Coordinate c = list.get( 0 );
                        Coordinate d = list.get( list.size() - 1 );
                        double bc = simpleDistanceIndicator( b, c );
                        double bd = simpleDistanceIndicator( b, d );
                        if ( first ) {
                            double ac = simpleDistanceIndicator( a, c );
                            double ad = simpleDistanceIndicator( a, d );
                            if ( ( ac < ad && ac < bc && ac < bd ) || ( ad < ac && ad < bc && ad < bd ) ) {
                                Collections.reverse( coords );
                                bc = ac; // swap, coz its swapped
                                bd = ad;
                            }
                            first = false;
                        }
                        if ( bd < bc ) {
                            Collections.reverse( list );
                        }
                    }
                    coords.addAll( list );
                }
                return coords;
            } else {
                return new ArrayList<>();
            }
        } catch ( IOException | SQLException ex ) {
            Logger.getLogger( JxDebugViewer.class.getName() ).log( Level.SEVERE, null, ex );
        }
        return new ArrayList<>();
    }

    private void addEdgeAsLast( LinkedList<Long> edges, int edge, PreprocessedData preprocessedData, Graph graph ) {
//        System.out.println( "adding edge: " + edge );
        if ( edge < graph.getEdgeCount() ) { // edge
            edges.add( preprocessedData.getEdgeOrigId( edge, graph ) );
        } else { // shortcut
            edge -= graph.getEdgeCount();
            addEdgeAsLast( edges, preprocessedData.getStartEdge( edge ), preprocessedData, graph );
            addEdgeAsLast( edges, preprocessedData.getEndEdge( edge ), preprocessedData, graph );
        }
    }

    private double simpleDistanceIndicator( Coordinate a, Coordinate b ) {
        double h = a.getLatitude() - b.getLatitude();
        double w = a.getLongitude() - b.getLongitude();
        return h * h + w * w;
    }
}
