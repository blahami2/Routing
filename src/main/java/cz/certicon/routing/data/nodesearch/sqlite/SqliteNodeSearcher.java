/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.nodesearch.sqlite;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.data.basic.database.AbstractEmbeddedDatabase;
import cz.certicon.routing.data.basic.database.EdgeResultHelper;
import cz.certicon.routing.data.nodesearch.NodeSearcher;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.common.SimpleEdgeData;
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
    public Map<Node.Id, Distance> findClosestNodes( Coordinates coordinates, DistanceFactory distanceFactory, SearchFor searchFor ) throws IOException {
        Map<Node.Id, Distance> distanceMap = new HashMap<>();
        final String pointString = "ST_GeomFromText('POINT(" + coordinates.getLongitude() + " " + coordinates.getLatitude() + ")',4326)";
        final String keyDistanceFromStart = "distance_from_start";
        final String keyDistanceToEnd = "distance_to_end";
        try {
            ResultSet rs = database.read( "SELECT n.id FROM nodes n JOIN nodes_data d ON n.data_id = d.id WHERE ST_Equals("
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
                System.out.println( "SELECT " + EdgeResultHelper.select( EdgeResultHelper.Columns.IS_FORWARD, EdgeResultHelper.Columns.SOURCE, EdgeResultHelper.Columns.TARGET, EdgeResultHelper.Columns.SPEED, EdgeResultHelper.Columns.IS_PAID, EdgeResultHelper.Columns.LENGTH ) + ", ST_Length(ST_LineSubstring(e.geom, 0, ST_LineLocatePoint(e.geom, ST_ClosestPoint(e.geom, e2.point))), true) AS " + keyDistanceFromStart + ", ST_Length(ST_LineSubstring(e.geom, ST_LineLocatePoint(e.geom, ST_ClosestPoint(e.geom, e2.point)),1), true) AS " + keyDistanceToEnd + " "
                        + "FROM edges e "
                        + "JOIN ( "
                        + "	SELECT ed.id, x.point "
                        + "        FROM edges_data ed "
                        + "        JOIN (select " + pointString + " AS point) AS x ON true "
                        // + "        WHERE ST_DWithin(x.point, e.geom, 300) "
                        + "        ORDER BY ed.geom <-> x.point "
                        + "        LIMIT 1 "
                        + ") AS e2 "
                        + "ON e.data_id = e2.data_id" );
                rs = database.read(
                        "SELECT " + EdgeResultHelper.select( EdgeResultHelper.Columns.IS_FORWARD, EdgeResultHelper.Columns.SOURCE, EdgeResultHelper.Columns.TARGET, EdgeResultHelper.Columns.SPEED, EdgeResultHelper.Columns.IS_PAID, EdgeResultHelper.Columns.LENGTH ) + ", ST_Length(ST_LineSubstring(e.geom, 0, ST_LineLocatePoint(e.geom, ST_ClosestPoint(e.geom, e2.point))), true) AS " + keyDistanceFromStart + ", ST_Length(ST_LineSubstring(e.geom, ST_LineLocatePoint(e.geom, ST_ClosestPoint(e.geom, e2.point)),1), true) AS " + keyDistanceToEnd + " "
                        + "FROM edges e "
                        + "JOIN ( "
                        + "	SELECT ed.id, x.point "
                        + "        FROM edges_data ed "
                        + "        JOIN (select " + pointString + " AS point) AS x ON true "
                        // + "        WHERE ST_DWithin(x.point, e.geom, 300) "
                        + "        ORDER BY ed.geom <-> x.point "
                        + "        LIMIT 1 "
                        + ") AS e2 "
                        + "ON e.data_id = e2.data_id" );
                while ( rs.next() ) {
                    EdgeResultHelper edgeResultHelper = new EdgeResultHelper( rs );
                    EdgeData edgeData = new SimpleEdgeData( edgeResultHelper.getSpeed(), edgeResultHelper.getIsPaid(), edgeResultHelper.getLength() );
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
            }
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
        return distanceMap;
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
