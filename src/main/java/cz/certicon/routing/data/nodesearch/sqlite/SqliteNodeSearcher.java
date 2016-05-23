/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.nodesearch.sqlite;

import static cz.certicon.routing.GlobalOptions.MEASURE_TIME;
import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.data.basic.database.AbstractEmbeddedDatabase;
import cz.certicon.routing.data.basic.database.EdgeResultHelper;
import cz.certicon.routing.data.nodesearch.NodeSearcher;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.common.SimpleEdgeData;
import cz.certicon.routing.utils.measuring.TimeLogger;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sqlite.SQLiteConfig;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqliteNodeSearcher implements NodeSearcher {

    private static final double DISTANCE_INIT = 0.001;
    private static final double DISTANCE_MULTIPLIER = 10;
    private final StringDatabase database;

    public SqliteNodeSearcher( Properties properties ) throws IOException {
        try {
            this.database = new StringDatabase( properties );
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
        SQLiteConfig config = new SQLiteConfig();
        config.enableLoadExtension( true );
        for ( Map.Entry<Object, Object> entry : config.toProperties().entrySet() ) {
            properties.put( entry.getKey(), entry.getValue() );
        }
//        this.libspatialitePath = libspatialitePath;
    }

    @Override
    public Pair<Map<Node.Id, Distance>, Long> findClosestNodes( Coordinate coordinates, DistanceFactory distanceFactory, SearchFor searchFor ) throws IOException {
        Map<Node.Id, Distance> distanceMap = new HashMap<>();
        final String pointString = "ST_GeomFromText('POINT(" + coordinates.getLongitude() + " " + coordinates.getLatitude() + ")',4326)";
        final String keyDistanceFromStart = "distance_from_start";
        final String keyDistanceToEnd = "distance_to_end";
        long dataId = -1;
        try {
            ResultSet rs = database.read( "SELECT n.id AS id "
                    + "FROM nodes n "
                    + "JOIN (SELECT * FROM nodes_data d "
                    + "        WHERE d.ROWID IN( "
                    + "            SELECT ROWID FROM SpatialIndex "
                    + "            WHERE f_table_name = 'nodes_data' "
                    + "            AND search_frame = BuildCircleMbr(" + coordinates.getLongitude() + ", " + coordinates.getLatitude() + " , " + DISTANCE_INIT + "  ,4326)"
                    + "        )  ) AS d "
                    + "ON n.data_id = d.id "
                    + "WHERE ST_Equals("
                    + "ST_SnapToGrid(" + pointString + ", 0.000001),"
                    + "ST_SnapToGrid(d.geom, 0.000001)"
                    + ")" );
            boolean isCrossroad = false;
            while ( rs.next() ) { // for all nodes found
                isCrossroad = true;
                long id = rs.getLong( "id" );
                distanceMap.put( Node.Id.createId( id ), distanceFactory.createZeroDistance() );
            }
            if ( !isCrossroad ) {
                boolean found = false;
                double distance = DISTANCE_INIT;
                while ( !found ) {
                    rs = database.read(
                            "SELECT " + EdgeResultHelper.select( EdgeResultHelper.Columns.DATA_ID, EdgeResultHelper.Columns.IS_FORWARD, EdgeResultHelper.Columns.SOURCE, EdgeResultHelper.Columns.TARGET, EdgeResultHelper.Columns.IS_PAID, EdgeResultHelper.Columns.LENGTH )
                            + ", speed_fw, speed_bw "
                            + ", ST_Length(ST_Line_Substring(e2.geom, 0, ST_Line_Locate_Point(e2.geom, e2.point)), 1) AS " + keyDistanceFromStart
                            + ", ST_Length(ST_Line_Substring(e2.geom, ST_Line_Locate_Point(e2.geom, e2.point),1), 1) AS " + keyDistanceToEnd + " "
                            + "FROM edges e "
                            + "JOIN ( "
                            + "	SELECT ed.*, x.point "
                            + "        FROM edges_data ed "
                            + "        JOIN (select " + pointString + " AS point) AS x ON 1 = 1 "
                            + "        WHERE ed.ROWID IN( "
                            + "            SELECT ROWID FROM SpatialIndex "
                            + "            WHERE f_table_name = 'edges_data' "
                            + "            AND search_frame = BuildCircleMbr(" + coordinates.getLongitude() + ", " + coordinates.getLatitude() + " , " + distance + "  ,4326)"
                            + "        )  "
                            + "        ORDER BY Distance( ed.geom, x.point) "
                            + "        LIMIT 1 "
                            + ") AS e2 "
                            + "ON e.data_id = e2.id" );
                    while ( rs.next() ) {
                        found = true;
                        EdgeResultHelper edgeResultHelper = new EdgeResultHelper( rs );
                        dataId = edgeResultHelper.getDataId();
                        int speed;
                        if ( edgeResultHelper.getIsForward() ) {
                            speed = rs.getInt( "speed_fw" );
                        } else {
                            speed = rs.getInt( "speed_bw" );
                        }
                        EdgeData edgeData = new SimpleEdgeData( speed, edgeResultHelper.getIsPaid(), edgeResultHelper.getLength() );
                        Node.Id nodeId;
                        double length;
                        if ( searchFor.equals( SearchFor.SOURCE ) ) {
                            nodeId = Node.Id.createId( edgeResultHelper.getTargetId() );
                            length = ( edgeResultHelper.getIsForward() ) ? rs.getDouble( keyDistanceToEnd ) : rs.getDouble( keyDistanceFromStart );
                        } else {
                            nodeId = Node.Id.createId( edgeResultHelper.getSourceId() );
                            length = ( edgeResultHelper.getIsForward() ) ? rs.getDouble( keyDistanceFromStart ) : rs.getDouble( keyDistanceToEnd );
                        }
                        distanceMap.put( nodeId, distanceFactory.createFromEdgeDataAndLength( edgeData, length ) );
                    }
                    distance *= DISTANCE_MULTIPLIER;
                }
            }
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
        return new Pair<>( distanceMap, dataId );
    }

    private static class StringDatabase extends AbstractEmbeddedDatabase<ResultSet, String> {

        private final String spatialitePath;

        public StringDatabase( Properties connectionProperties ) throws SQLException {
            super( connectionProperties );
            SQLiteConfig config = new SQLiteConfig();
            config.enableLoadExtension( true );
            for ( Map.Entry<Object, Object> entry : config.toProperties().entrySet() ) {
                connectionProperties.put( entry.getKey(), entry.getValue() );
            }
            this.spatialitePath = connectionProperties.getProperty( "spatialite_path" );
        }

        @Override
        public void open() throws IOException {
            super.open();
            try {
                getStatement().execute( "SELECT load_extension('" + spatialitePath + "')" );
            } catch ( SQLException ex ) {
                throw new IOException( ex );
            }
        }

        @Override
        protected ResultSet checkedRead( String in ) throws SQLException {
            return getStatement().executeQuery( in );
        }

        @Override
        protected void checkedWrite( ResultSet in ) throws SQLException {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
