/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.path.sqlite;

import cz.certicon.routing.GlobalOptions;
import cz.certicon.routing.data.basic.database.impl.AbstractSqliteDatabase;
import cz.certicon.routing.data.basic.database.impl.StringSqliteReader;
import cz.certicon.routing.memsensitive.algorithm.Route;
import cz.certicon.routing.memsensitive.data.path.PathReader;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.PathBuilder;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.TimeUnits;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.utils.GeometryUtils;
import cz.certicon.routing.utils.measuring.TimeLogger;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqlitePathReader implements PathReader<Graph> {

    private static final int BATCH_SIZE = 200;
    private static final double DISTANCE = 0.01;

    private final InnerDatabase reader;

    public SqlitePathReader( Properties connectionProperties ) {
        this.reader = new InnerDatabase( connectionProperties );
    }

    @Override
    public <T> T readPath( PathBuilder<T, Graph> pathBuilder, Graph graph, Route route, Coordinate origSource, Coordinate origTarget ) throws IOException {
        pathBuilder.clear();
        TimeMeasurement time = new TimeMeasurement();
        time.setTimeUnits( TimeUnits.MICROSECONDS );
        if ( GlobalOptions.MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.PATH_LOADING, TimeLogger.Command.START );
        }
        String query = "";
        if ( route.getSource() != route.getTarget() ) {
            time.start();
            reader.open();
            Map<Long, Boolean> forwardMap = new HashMap<>();
            ResultSet rs;
            final String keyDistanceFromStart = "distance_from_start";
            final String keyDistanceToEnd = "distance_to_end";
            final String keyGeomFromStart = "geom_from_start";
            final String keyGeomToEnd = "geom_to_end";
            try {
                // if source is not a crossroad => needs to be loaded
                long sourceId = route.getSource();
                Coordinate routeStartCoord = new Coordinate( graph.getLatitude( graph.getNodeByOrigId( sourceId ) ), graph.getLongitude( graph.getNodeByOrigId( sourceId ) ) );
                if ( !routeStartCoord.equals( origSource ) ) {
                    // really complex SQL query
                    // - find edges connected to the "route.source" node
                    // - order them by distance from "origSource"
                    // - take the first
                    // - find the closest point on edge geometry
                    // - obtain a subgeometry, it's length and speed etc.
                    // - unpack and voila
                    final String pointString = "ST_GeomFromText('POINT(" + origSource.getLongitude() + " " + origSource.getLatitude() + ")',4326)";
                    query = "SELECT e.id, e.is_forward, e.source_id, e.target_id, ed.is_paid, ed.length, ed.speed_fw, ed.speed_bw "
                            + ", ST_Length(ST_Line_Substring(ed.geom, 0, ST_Line_Locate_Point(ed.geom, x.point)), 1) AS " + keyDistanceFromStart + " "
                            + ", ST_Length(ST_Line_Substring(ed.geom, ST_Line_Locate_Point(ed.geom, x.point),1), 1) AS " + keyDistanceToEnd + " "
                            + ", ST_AsText(ST_Line_Substring(ed.geom, 0, ST_Line_Locate_Point(ed.geom, x.point))) AS " + keyGeomFromStart + " "
                            + ", ST_AsText(ST_Line_Substring(ed.geom, ST_Line_Locate_Point(ed.geom, x.point),1)) AS " + keyGeomToEnd + " "
                            + "FROM edges e "
                            + "JOIN ( "
                            + "	SELECT * "
                            + "        FROM edges_data e2 "
                            + "        WHERE e2.ROWID IN( "
                            + "            SELECT ROWID FROM SpatialIndex "
                            + "            WHERE f_table_name = 'edges_data' "
                            + "            AND search_frame = BuildCircleMbr(" + routeStartCoord.getLongitude() + ", " + routeStartCoord.getLatitude() + " , " + DISTANCE + "  ,4326)"
                            + "        )  "
                            + ") AS ed "
                            + "ON e.data_id = ed.id "
                            + "JOIN (SELECT " + pointString + " AS point) AS x ON 1 = 1 "
                            + "WHERE (e.source_id = " + sourceId + " OR e.target_id = " + sourceId + ") "
                            + "ORDER BY Distance( ed.geom, x.point) "
                            + "LIMIT 1 ";
                    rs = reader.read( query );
//                    System.out.println( query );
                    if ( rs.next() ) {
                        long edgeId = rs.getLong( "e.id" );
                        boolean isForward = rs.getInt( "e.is_forward" ) == 1;
                        long sId = rs.getLong( "e.source_id" );
                        long tId = rs.getLong( "e.target_id" );
                        List<Coordinate> coordinates;
                        boolean reverse;
                        int speed;
                        double length;
                        if ( isForward != ( sourceId == tId ) ) {
                            coordinates = getCoordinatesCheckNull( rs, keyGeomFromStart );
                            length = rs.getDouble( keyDistanceFromStart );
                            reverse = true;
                            speed = rs.getInt( "ed.speed_bw" );
                        } else {
                            coordinates = getCoordinatesCheckNull( rs, keyGeomToEnd );
                            length = rs.getDouble( keyDistanceToEnd );
                            reverse = false;
                            speed = rs.getInt( "ed.speed_fw" );
                        }
                        pathBuilder.addStartEdge( graph, edgeId, !reverse, coordinates, length, 3.6 * length / speed );
                    } else {
                        throw new IOException( "Could not find the closest edge:\n" + query );
                    }
                }
                System.out.println( "load coordinates for source: " + time.getCurrentTimeString() );
                time.start();
                long targetId = route.getTarget();
                Coordinate routeEndCoord = new Coordinate( graph.getLatitude( graph.getNodeByOrigId( targetId ) ), graph.getLongitude( graph.getNodeByOrigId( targetId ) ) );
                if ( !routeEndCoord.equals( origTarget ) ) {
                    final String pointString = "ST_GeomFromText('POINT(" + origTarget.getLongitude() + " " + origTarget.getLatitude() + ")',4326)";
                    query = "SELECT e.id, e.is_forward, e.source_id, e.target_id, ed.is_paid, ed.length, ed.speed_fw, ed.speed_bw "
                            + ", ST_Length(ST_Line_Substring(ed.geom, 0, ST_Line_Locate_Point(ed.geom, x.point)), 1) AS " + keyDistanceFromStart + " "
                            + ", ST_Length(ST_Line_Substring(ed.geom, ST_Line_Locate_Point(ed.geom, x.point),1), 1) AS " + keyDistanceToEnd + " "
                            + ", ST_AsText(ST_Line_Substring(ed.geom, 0, ST_Line_Locate_Point(ed.geom, x.point))) AS " + keyGeomFromStart + " "
                            + ", ST_AsText(ST_Line_Substring(ed.geom, ST_Line_Locate_Point(ed.geom, x.point),1)) AS " + keyGeomToEnd + " "
                            + "FROM edges e "
                            + "JOIN ( "
                            + "	SELECT * "
                            + "        FROM edges_data e2 "
                            + "        WHERE e2.ROWID IN( "
                            + "            SELECT ROWID FROM SpatialIndex "
                            + "            WHERE f_table_name = 'edges_data' "
                            + "            AND search_frame = BuildCircleMbr(" + routeEndCoord.getLongitude() + ", " + routeEndCoord.getLatitude() + " , " + DISTANCE + "  ,4326)"
                            + "        )  "
                            + ") AS ed "
                            + "ON e.data_id = ed.id "
                            + "JOIN (SELECT " + pointString + " AS point) AS x ON 1 = 1 "
                            + "WHERE (e.source_id = " + targetId + " OR e.target_id = " + targetId + ") "
                            + "ORDER BY Distance( ed.geom, x.point) "
                            + "LIMIT 1 ";
                    rs = reader.read( query );
                    if ( rs.next() ) {
                        long edgeId = rs.getLong( "e.id" );
                        boolean isForward = rs.getInt( "e.is_forward" ) == 1;
                        long sId = rs.getLong( "e.source_id" );
                        long tId = rs.getLong( "e.target_id" );
                        List<Coordinate> coordinates;
                        boolean reverse;
                        int speed;
                        double length;
                        if ( isForward != ( targetId == sId ) ) {
                            coordinates = getCoordinatesCheckNull( rs, keyGeomToEnd );
                            length = rs.getDouble( keyDistanceToEnd );
                            reverse = true;
                            speed = rs.getInt( "ed.speed_bw" );
                        } else {
                            coordinates = getCoordinatesCheckNull( rs, keyGeomFromStart );
                            length = rs.getDouble( keyDistanceFromStart );
                            reverse = false;
                            speed = rs.getInt( "ed.speed_fw" );
                        }
                        pathBuilder.addEndEdge( graph, edgeId, !reverse, coordinates, length, 3.6 * length / speed );
                    } else {
                        throw new IOException( "Could not find the closest edge:\n" + query );
                    }
                }
                System.out.println( "load coordinates for target: " + time.getCurrentTimeString() );
                time.start();
                // create temporary table
                reader.setAutoCommit( false );
                boolean create = !reader.read( "SELECT name FROM sqlite_master WHERE type='table' AND name='path'" ).next();
                if ( create ) {
                    reader.execute( "CREATE TABLE path ("
                            + "order_id INTEGER,"
                            + "edge_id INTEGER"
                            + ")" );
                } else {
                    reader.execute( "DROP INDEX IF EXISTS `idx_path_order`" );
                }
                PreparedStatement ps = reader.prepareStatement( "INSERT INTO path (order_id, edge_id) VALUES (?, ?)" );
                Iterator<Pair<Long, Boolean>> it = route.getEdgeIterator();
                int i = 1;
                while ( it.hasNext() ) {
                    Pair<Long, Boolean> next = it.next();
                    forwardMap.put( next.a, next.b );
                    int idx = 1;
                    ps.setInt( idx++, i );
                    ps.setLong( idx++, next.a );
                    ps.addBatch();
                    if ( i++ % BATCH_SIZE == 0 ) {
                        ps.executeBatch();
                    }
                }
                System.out.println( "paths inserted in: " + time.getCurrentTimeString() );
                time.start();
                ps.executeBatch();
                reader.execute( "CREATE INDEX `idx_path_order` ON `path` (`order_id` ASC)" );
                query = "SELECT e.id AS edge_id, ST_AsText(d.geom) AS linestring, e.is_forward, d.length, d.speed_fw, d.speed_bw "
                        + "FROM edges e "
                        + "JOIN path p "
                        + "ON e.id = p.edge_id "
                        + "JOIN edges_data d "
                        + "ON e.data_id = d.id "
                        + "ORDER BY p.order_id";
                rs = reader.read( query );
                int idIdx = rs.findColumn( "edge_id" );
                int linestringIdx = rs.findColumn( "linestring" );
                int lengthIdx = rs.findColumn( "length" );
                int speedFwIdx = rs.findColumn( "speed_fw" );
                int speedBwIdx = rs.findColumn( "speed_bw" );
                int forwardIdx = rs.findColumn( "is_forward" );
                while ( rs.next() ) {
                    long id = rs.getLong( idIdx );
                    boolean dbForward = rs.getBoolean( forwardIdx );
                    boolean mapForward = forwardMap.get( id );
                    boolean isForward = ( dbForward && mapForward ) || ( !dbForward && !mapForward );
                    List<Coordinate> coordinates = GeometryUtils.toCoordinatesFromWktLinestring( rs.getString( linestringIdx ) );
                    double length = rs.getDouble( lengthIdx );
                    int speed = rs.getInt( isForward ? speedFwIdx : speedBwIdx );
                    pathBuilder.addEdge( graph, id, isForward, coordinates, length, 3.6 * length / speed );
                }
                System.out.println( "coordinates loaded in: " + time.getCurrentTimeString() );
                time.start();
                // TODO do I have to delete it???
//                reader.execute( "DELETE FROM path" );
//                reader.execute( "DROP INDEX IF EXISTS `idx_path_order`" );
//                reader.setAutoCommit( true );
//                System.out.println( "Autocommit on in: " + time.getCurrentTimeString() );
                time.start();
                reader.close();
                System.out.println( "Connection closed in: " + time.getCurrentTimeString() );
            } catch ( SQLException ex ) {
                throw new IOException( query, ex );
            }
        }
        int sourceNode = graph.getNodeByOrigId( route.getSource() );
        int targetNode = graph.getNodeByOrigId( route.getTarget() );
        T result = pathBuilder.build( graph,
                new Coordinate( graph.getLatitude( sourceNode ), graph.getLongitude( sourceNode ) ),
                new Coordinate( graph.getLatitude( targetNode ), graph.getLongitude( targetNode ) ) );
        if ( GlobalOptions.MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.PATH_LOADING, TimeLogger.Command.STOP );
        }
        return result;
    }

    @Override
    public <T> T readPath( PathBuilder<T, Graph> pathBuilder, Graph graph, long edgeIdd, Coordinate origSource, Coordinate origTarget ) throws IOException {
        pathBuilder.clear();
        if ( GlobalOptions.MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.PATH_LOADING, TimeLogger.Command.START );
        }
        ResultSet rs;
        final String keyDistanceForward = "distance_from_start";
        final String keyDistanceBackward = "distance_to_end";
        final String keyGeomForward = "geom_from_start";
        final String keyGeomBackward = "geom_to_end";
        final String keyClosestStart = "closest_point_x";
        final String keyClosestEnd = "closest_point_y";
        final String startPointString = "ST_GeomFromText('POINT(" + origSource.getLongitude() + " " + origSource.getLatitude() + ")',4326)";
        final String endPointString = "ST_GeomFromText('POINT(" + origTarget.getLongitude() + " " + origTarget.getLatitude() + ")',4326)";
        String query = "SELECT e.id, e.is_forward, e.source_id, e.target_id, ed.is_paid, ed.length, ed.speed_fw, ed.speed_bw "
                + ", ST_AsText(ST_Line_Interpolate_Point(ed.geom, ST_Line_Locate_Point(ed.geom, x.point))) AS " + keyClosestStart + " "
                + ", ST_AsText(ST_Line_Interpolate_Point(ed.geom, ST_Line_Locate_Point(ed.geom, y.point))) AS " + keyClosestEnd + " "
                + ", ST_Length(ST_Line_Substring(ST_Line_Substring(ed.geom, 0, ST_Line_Locate_Point(ed.geom, y.point)), ST_Line_Locate_Point(ed.geom, x.point),1), 1) AS " + keyDistanceForward + " "
                + ", ST_Length(ST_Line_Substring(ST_Line_Substring(ed.geom, 0, ST_Line_Locate_Point(ed.geom, x.point)), ST_Line_Locate_Point(ed.geom, y.point),1), 1) AS " + keyDistanceBackward + " "
                + ", ST_AsText(ST_Line_Substring(ST_Line_Substring(ed.geom, 0, ST_Line_Locate_Point(ed.geom, y.point)), ST_Line_Locate_Point(ed.geom, x.point),1)) AS " + keyGeomForward + " "
                + ", ST_AsText(ST_Line_Substring(ST_Line_Substring(ed.geom, 0, ST_Line_Locate_Point(ed.geom, x.point)), ST_Line_Locate_Point(ed.geom, y.point),1)) AS " + keyGeomBackward + " "
                + "FROM edges e "
                + "JOIN edges_data ed ON e.data_id = ed.id AND (e.id = " + edgeIdd + ") "
                + "JOIN (SELECT " + startPointString + " AS point) AS x ON 1 = 1 "
                + "JOIN (SELECT " + endPointString + " AS point) AS y ON 1 = 1 "
                + "LIMIT 1 ";
        System.out.println( query );
        try {
            rs = reader.read( query );
//            System.out.println( query );
            if ( rs.next() ) {
                long edgeId = rs.getLong( "id" );
                boolean isForward = rs.getInt( "is_forward" ) == 1;
                List<Coordinate> coordinates;
                int speed;
                double length;
                if ( isForward ) {
                    coordinates = getCoordinatesCheckNull( rs, keyGeomForward );
                    length = rs.getDouble( keyDistanceForward );
                    speed = rs.getInt( "speed_fw" );
                } else {
                    coordinates = getCoordinatesCheckNull( rs, keyGeomBackward );
                    length = rs.getDouble( keyDistanceBackward );
                    speed = rs.getInt( "speed_bw" );
                }
                pathBuilder.addEdge( graph, edgeId, isForward, coordinates, length, 3.6 * length / speed );
                Coordinate source = GeometryUtils.toCoordinatesFromWktPoint( rs.getString( keyClosestStart ) );
                Coordinate target = GeometryUtils.toCoordinatesFromWktPoint( rs.getString( keyClosestEnd ) );
                return pathBuilder.build( graph, source, target );
            } else {
                throw new IOException( "Could not find the closest edge:\n" + query );
            }
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        } finally {
            if ( GlobalOptions.MEASURE_TIME ) {
                TimeLogger.log( TimeLogger.Event.PATH_LOADING, TimeLogger.Command.STOP );
            }
        }
    }

    private List<Coordinate> getCoordinatesCheckNull( ResultSet rs, String key ) throws SQLException {
        if ( rs.getString( key ) != null ) {
            return GeometryUtils.toCoordinatesFromWktLinestring( rs.getString( key ) );
        } else {
            return new ArrayList<>();
        }
    }

    private static class InnerDatabase extends AbstractSqliteDatabase<ResultSet, String> {

        public InnerDatabase( Properties connectionProperties ) {
            super( connectionProperties );
        }

        @Override
        protected ResultSet checkedRead( String in ) throws SQLException {
            return getStatement().executeQuery( in );
        }

        @Override
        protected void checkedWrite( ResultSet in ) throws SQLException {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        public void execute( String sql ) throws SQLException {
            getStatement().execute( sql );
        }

        public void setAutoCommit( boolean value ) throws SQLException {
            getConnection().setAutoCommit( value );
        }

        public void commit() throws SQLException {
            getConnection().commit();
        }

        public PreparedStatement prepareStatement( String sql ) throws SQLException {
            return getConnection().prepareStatement( sql );
        }
    }
}
