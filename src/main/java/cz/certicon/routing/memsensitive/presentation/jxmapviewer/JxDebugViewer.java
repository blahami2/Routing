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
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.utils.GeometryUtils;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    private JPanel infoPanel;
    private DefaultTileFactory tileFactory;
    private final Set<GeoPosition> fitGeoPosition = new HashSet<>();
    private final List<Painter<JXMapViewer>> painters = new ArrayList<>();
    private final Set<LabelWaypoint> waypoints = new HashSet<>();
    private final StringSqliteReader reader;
    private Map<Long, Painter<JXMapViewer>> painterMap = new HashMap<>();
    private Map<Long, Painter<JXMapViewer>> nodePainterMap = new HashMap<>();
    private final Map<Long, ClickableArea> clickableNodeMap = new HashMap<>();
    private final Map<Long, ClickableArea> clickableEdgeMap = new HashMap<>();
    private final Map<Long, List<Long>> edgeMap = new HashMap<>();
    private final long delay;
    private boolean centered = false;
    private PreprocessedData preprocessedData;
    private Graph graph;
    private boolean stepByInput = false;

    {
        this.mapKit = new JXMapKit();
        this.mapViewer = mapKit.getMainMap();
        frame = new JFrame( "map" );
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout( new BorderLayout() );
        mainPanel.add( mapViewer, BorderLayout.CENTER );
        infoPanel = new JPanel();
        infoPanel.setSize( 400, 600 );
        mainPanel.add( infoPanel, BorderLayout.EAST );
        frame.setContentPane( mainPanel );
        frame.setSize( 1000, 600 );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        frame.setVisible( true );
        TileFactoryInfo info = new OSMTileFactoryInfo();
        tileFactory = new DefaultTileFactory( info );
        tileFactory.setThreadPoolSize( 8 );
        mapKit.setTileFactory( tileFactory );
        mapViewer.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseClicked( MouseEvent e ) {
                onClick( e.getX(), e.getY() );
            }

        } );
    }

    public void onClick( int x, int y ) {
//        System.out.println( "clicked: " + x + ", " + y );
        for ( ClickableArea clickable : clickableNodeMap.values() ) {
            if ( clickable.contains( mapViewer, x, y ) ) {
                clickable.display( infoPanel );
                frame.repaint();
                frame.invalidate();
                frame.revalidate();
                frame.validate();
                frame.repaint();
                return;
            }
        }
        for ( ClickableArea clickable : clickableEdgeMap.values() ) {
            if ( clickable.contains( mapViewer, x, y ) ) {
                clickable.display( infoPanel );
                frame.repaint();
                frame.invalidate();
                frame.revalidate();
                frame.validate();
                frame.repaint();
                return;
            }
        }
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
    public void displayNode( long nodeId ) {
        TimeMeasurement time = new TimeMeasurement();
        time.setTimeUnits( TimeUnits.MILLISECONDS );
        time.start();
        NodeData nodeData = loadNodeData( nodeId );
        clickableNodeMap.put( nodeId, nodeData );
        NodePainter nodePainter = new NodePainter( nodeData.coordinate );

        nodePainterMap.put( nodeId, nodePainter );
        painters.add( nodePainter );
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>( painters );
        mapViewer.setOverlayPainter( painter );

        if ( !centered ) {
            centered = true;
            mapViewer.zoomToBestFit( fitGeoPosition, 0.7 );
        }
        nextStep( time.stop() );
    }

    @Override
    public void removeNode( long nodeId ) {
        Painter<JXMapViewer> p = nodePainterMap.get( nodeId );
        nodePainterMap.remove( nodeId );
        clickableNodeMap.remove( nodeId );
        painters.remove( p );
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>( painters );
        mapViewer.setOverlayPainter( painter );
    }

    @Override
    public void displayEdge( long edgeId ) {
        TimeMeasurement time = new TimeMeasurement();
        time.setTimeUnits( TimeUnits.MILLISECONDS );
        time.start();

        List<EdgeData> edgesData = loadEdge( edgeId );
        List<Long> edges = new ArrayList<>();
        for ( EdgeData edgeData : edgesData ) {
            edges.add( edgeData.id );
            clickableEdgeMap.put( edgeData.id, edgeData );
            fitGeoPosition.addAll( edgeData.coordinates );
            RoutePainter routePainter = new RoutePainter( edgeData.coordinates );
            painterMap.put( edgeData.id, routePainter );
            painters.add( routePainter );
        }
        edgeMap.put( edgeId, edges );

//        List<Coordinate> coords = loadCoordinates( edgeId );
//        List<GeoPosition> track = new ArrayList<>();
//        for ( Coordinate coordinate : coords ) {
//            track.add( new GeoPosition( coordinate.getLatitude(), coordinate.getLongitude() ) );
//        }
//        fitGeoPosition.addAll( track );
//        RoutePainter routePainter = new RoutePainter( track );
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
        List<Long> list = edgeMap.get( edgeId );
        edgeMap.remove( edgeId );
        for ( Long edge : list ) {
            clickableEdgeMap.remove( edge );
            Painter<JXMapViewer> p = painterMap.get( edge );
            painterMap.remove( edge );
            painters.remove( p );
        }
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
        List<Long> list = edgeMap.get( edgeId );
        for ( Long edge : list ) {
            RoutePainter p = (RoutePainter) painterMap.get( edgeId );
            p.setColor( Color.blue );
//        fitGeoPosition.removeAll( p.track );
        }
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>( painters );
        mapViewer.setOverlayPainter( painter );
        if ( !centered ) {
            centered = true;
            mapViewer.zoomToBestFit( fitGeoPosition, 0.7 );
        }
        nextStep( time.stop() );
    }

    @Override
    public void clear() {
        Set<Long> nodes = new HashSet<>(clickableNodeMap.keySet());
        for ( long node : nodes ) {
            removeNode( node );
        }
        Set<Long> edges = new HashSet<>(edgeMap.keySet());
        for ( long edge : edges ) {
            removeEdge( edge );
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

    private NodeData loadNodeData( long nodeId ) {
        String query;
        ResultSet rs;
        try {
            TLongList incomingEdges = new TLongArrayList();
            TLongList outgoingEdges = new TLongArrayList();
            query = "SELECT e.id FROM edges e WHERE e.target_id = " + nodeId + ";";
            rs = reader.read( query );
            while ( rs.next() ) {
                incomingEdges.add( rs.getLong( "id" ) );
            }
            incomingEdges.sort();
            query = "SELECT e.id FROM edges e WHERE e.source_id = " + nodeId + ";";
            rs = reader.read( query );
            while ( rs.next() ) {
                outgoingEdges.add( rs.getLong( "id" ) );
            }
            outgoingEdges.sort();
            query = "SELECT n.id, ST_AsText(d.geom) AS geom, r.rank FROM nodes n JOIN nodes_data d ON n.data_id = d.id JOIN ranks r ON n.id = r.node_id WHERE n.id = " + nodeId + ";";
            rs = reader.read( query );
            if ( rs.next() ) {
                long id = rs.getLong( "id" );
                int rank = rs.getInt( "rank" );
                Coordinate coordinate = GeometryUtils.toCoordinatesFromWktPoint( rs.getString( "geom" ) );
                return new NodeData( coordinate, id, rank, incomingEdges, outgoingEdges );
            }
            throw new UnsupportedOperationException( "Not implemented yet." );
        } catch ( IOException | SQLException ex ) {
            Logger.getLogger( JxDebugViewer.class.getName() ).log( Level.SEVERE, null, ex );
        }
        return null;
    }

    private EdgeData loadEdgeData( long edgeId ) {
        String query;
        ResultSet rs;
        try {
            List<Trinity<Long, Long, Long>> shortcuts = new ArrayList<>();
            LinkedList<Long> queue = new LinkedList<>();
            queue.push( edgeId );
            while ( !queue.isEmpty() ) {
                long id = queue.pop();
                query = "SELECT s.id, s.edge_source, s.edge_target FROM shortcuts s WHERE s.edge_source = " + id + " OR s.edge_target = " + id + ";";
                rs = reader.read( query );
                while ( rs.next() ) {
                    long sId = rs.getLong( "id" );
                    shortcuts.add( new Trinity<>( sId, rs.getLong( "edge_source" ), rs.getLong( "edge_target" ) ) );
                    queue.push( sId );
                }
            }
            Collections.sort( shortcuts, new Comparator<Trinity<Long, Long, Long>>() {
                @Override
                public int compare( Trinity<Long, Long, Long> o1, Trinity<Long, Long, Long> o2 ) {
                    return Long.compare( o1.a, o2.a );
                }
            } );
            query = "SELECT e.id, e.source_id, e.target_id, ST_AsText(d.geom) AS geom FROM edges e JOIN edges_data d ON e.data_id = d.id WHERE e.id = " + edgeId + ";";
            rs = reader.read( query );
            if ( rs.next() ) {
                long id = rs.getLong( "id" );
                long targetId = rs.getLong( "target_id" );
                long sourceId = rs.getLong( "source_id" );
                List<Coordinate> coordinates = GeometryUtils.toCoordinatesFromWktLinestring( rs.getString( "geom" ) );
                return new EdgeData( coordinates, id, sourceId, targetId, shortcuts );
            }
            throw new UnsupportedOperationException( "Not implemented yet." );
        } catch ( IOException | SQLException ex ) {
            Logger.getLogger( JxDebugViewer.class.getName() ).log( Level.SEVERE, null, ex );
        }
        return null;
    }

    private List<EdgeData> loadEdge( long edgeId ) {
        List<EdgeData> edgeDataList = new ArrayList<>();
        try {
            String query = "SELECT id FROM edges e WHERE e.id = " + edgeId + ";";
            ResultSet rs = reader.read( query );
            if ( rs.next() ) {
                edgeDataList.add( loadEdgeData( edgeId ) );
            } else if ( preprocessedData != null ) {
                LinkedList<Long> edges = new LinkedList<>();
                int edge = preprocessedData.getEdgeByOrigId( edgeId, graph );
                addEdgeAsLast( edges, edge, preprocessedData, graph );
                for ( long e : edges ) {
                    edgeDataList.add( loadEdgeData( e ) );
                }
            }
        } catch ( IOException | SQLException ex ) {
            Logger.getLogger( JxDebugViewer.class.getName() ).log( Level.SEVERE, null, ex );
        }
        return edgeDataList;
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
//                System.out.println( "edge = " + edge );
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
//        System.out.println( "adding edge: " + preprocessedData.getEdgeOrigId( edge, graph ) );
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

    private static class EdgeData implements ClickableArea {

        public final List<GeoPosition> coordinates;
        public final long id;
        public final long source;
        public final long target;
        public final List<Trinity<Long, Long, Long>> shortcuts;

        public EdgeData( List<Coordinate> coordinates, long id, long source, long target, List<Trinity<Long, Long, Long>> shortcuts ) {
            this.coordinates = new ArrayList<>();
            for ( Coordinate coordinate : coordinates ) {
                this.coordinates.add( new GeoPosition( coordinate.getLatitude(), coordinate.getLongitude() ) );
            }
            this.id = id;
            this.source = source;
            this.target = target;
            this.shortcuts = shortcuts;
        }

        @Override
        public boolean contains( JXMapViewer map, int x, int y ) {
//            System.out.println( "validating #" + id + " against: " + x + "," + y );
            Rectangle rectangle = map.getViewportBounds();
            int lastX = Integer.MAX_VALUE;
            int lastY = Integer.MAX_VALUE;
            for ( GeoPosition coordinate : coordinates ) {
                Point2D pt = map.getTileFactory().geoToPixel( coordinate, map.getZoom() );
                int currX = (int) ( pt.getX() - rectangle.getX() );
                int currY = (int) ( pt.getY() - rectangle.getY() );
                if ( lastX != Integer.MAX_VALUE && lastY != Integer.MAX_VALUE && Comparison.inLineRange( lastX, lastY, currX, currY, x, y ) ) {
                    return true;
                }
                if ( Comparison.inRange( currX, currY, x, y ) ) {
                    return true;
                }
                lastX = currX;
                lastY = currY;
            }
            return false;
        }

        @Override
        public void display( JPanel panel ) {
//            JFrame frame = new JFrame( "Edge#" + id );
//            frame.setSize( 400, 500 );
//            frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
//            JPanel panel = new JPanel();
//            frame.setContentPane( panel );
            panel.removeAll();
            panel.setLayout( new GridLayout( 0, 4 ) );
            Presentation.addRow( panel, "id", id, "", "" );
            Presentation.addRow( panel, "source", source, "", "" );
            Presentation.addRow( panel, "target", target, "", "" );

            int i = 0;
            Presentation.addRow( panel, "shortcut", "id", "source", "target" );
            for ( Trinity<Long, Long, Long> shortcut : shortcuts ) {
                Presentation.addRow( panel, "shortcut#" + ++i, shortcut.a, shortcut.b, shortcut.c );
            }
//            frame.setVisible( true );
        }

    }

    private static class NodeData implements ClickableArea {

        public final GeoPosition coordinate;
        public final long id;
        public final int rank;
        public final TLongList incomingEdges;
        public final TLongList outgoingEdges;

        public NodeData( Coordinate coordinate, long id, int rank, TLongList incomingEdges, TLongList outgoingEdges ) {
            this.coordinate = new GeoPosition( coordinate.getLatitude(), coordinate.getLongitude() );
            this.id = id;
            this.rank = rank;
            this.incomingEdges = incomingEdges;
            this.outgoingEdges = outgoingEdges;
        }

        @Override
        public boolean contains( JXMapViewer map, int x, int y ) {
            Point2D pt = map.getTileFactory().geoToPixel( coordinate, map.getZoom() );
            Rectangle rectangle = map.getViewportBounds();
            return Comparison.inRange( (int) ( pt.getX() - rectangle.getX() ), (int) ( pt.getY() - rectangle.getY() ), x, y );
        }

        @Override
        public void display( JPanel panel ) {
//            System.out.println( "displaying frame" );
//            JFrame frame = new JFrame( "Node#" + id );
//            frame.setSize( 200, 500 );
//            frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
//            JPanel panel = new JPanel();
//            frame.setContentPane( panel );
            panel.removeAll();
            panel.setLayout( new GridLayout( 0, 2 ) );
            Presentation.addRow( panel, "id", id );
            Presentation.addRow( panel, "rank", rank );
//            System.out.println( "incoming: " + incomingEdges.size() );
            TLongIterator incIt = incomingEdges.iterator();
            int i = 0;
            while ( incIt.hasNext() ) {
                Presentation.addRow( panel, "incoming#" + ++i, incIt.next() );
            }
            TLongIterator outIt = outgoingEdges.iterator();
//            System.out.println( "outgoing: " + outgoingEdges.size() );
            i = 0;
            while ( outIt.hasNext() ) {
                Presentation.addRow( panel, "outgoing#" + ++i, outIt.next() );
            }
//            System.out.println( "setting visible" );
//            frame.setVisible( true );
//            System.out.println( "done" );
        }
    }

    private interface ClickableArea {

        boolean contains( JXMapViewer map, int x, int y );

        void display( JPanel panel );

        static class Comparison {

            public static boolean inRange( int base, int compared ) {
                return Math.abs( base - compared ) <= PRECISION;
            }

            public static boolean inRange( int baseX, int baseY, int x, int y ) {
//                System.out.println( "comparing: " + baseX + ", " + baseY + " to " + x + ", " + y );
                return inRange( baseX, x ) && inRange( baseY, y );
            }

            public static boolean inLineRange( int ax, int ay, int bx, int by, int x, int y ) {
                return GeometryUtils.pointToLineDistance( ax, ay, bx, by, x, y ) <= PRECISION;
            }
            public static final int PRECISION = 5;
        }

        static class Presentation {

            public static void addRow( JPanel panel, String left, String right ) {
                panel.add( new JLabel( " " + left ) );
                panel.add( new JLabel( " " + right ) );
            }

            public static void addRow( JPanel panel, String left, long right ) {
                addRow( panel, left, right + "" );
            }

            public static void addRow( JPanel panel, String left, double right ) {
                addRow( panel, left, right + "" );
            }

            public static void addRow( JPanel panel, String col1, Object col2, Object col3, Object col4 ) {
                panel.add( new JLabel( " " + col1 ) );
                panel.add( new JLabel( " " + col2.toString() ) );
                panel.add( new JLabel( " " + col3.toString() ) );
                panel.add( new JLabel( " " + col4.toString() ) );
            }

            public static String asHtml( Object str ) {
                return "<html>" + str + "</html>";
            }

            public static String asBold( Object str ) {
                return "<html><b>" + str + "</b></html>";
            }
        }
    }
}
