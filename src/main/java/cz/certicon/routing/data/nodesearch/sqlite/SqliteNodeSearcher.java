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
import org.sqlite.SQLiteConfig;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqliteNodeSearcher implements NodeSearcher {

    private final StringDatabase database;

    public SqliteNodeSearcher( Properties properties ) throws IOException {
        this.database = new StringDatabase( properties );
    }

    @Override
    public Map<Node.Id, Distance> findClosestNodes( Coordinates coordinates, DistanceFactory distanceFactory, SearchFor searchFor ) throws IOException {
        Map<Node.Id, Distance> distanceMap = new HashMap<>();
        final String pointString = "ST_GeomFromText('POINT(" + coordinates.getLongitude() + " " + coordinates.getLatitude() + ")',4326)";
        final String keyDistanceFromStart = "distance_from_start";
        final String keyDistanceToEnd = "distance_to_end";
        try {
            ResultSet rs = database.read( "SELECT n.id FROM nodes_view n WHERE ST_Equals("
                    + "ST_SnapToGrid(" + pointString + ", 0.000001),"
                    + "ST_SnapToGrid(n.geom, 0.000001)"
                    + ")" );
            boolean isCrossroad = false;
            while ( rs.next() ) { // for all nodes found
                isCrossroad = true;
                long id = rs.getLong( "id" );
                distanceMap.put( Node.Id.createId( id ), distanceFactory.createZeroDistance() );
            }
            if ( !isCrossroad ) {
                rs = database.read(
                        "SELECT " + EdgeResultHelper.select( EdgeResultHelper.Columns.IS_FORWARD, EdgeResultHelper.Columns.SOURCE, EdgeResultHelper.Columns.TARGET, EdgeResultHelper.Columns.SPEED, EdgeResultHelper.Columns.IS_PAID, EdgeResultHelper.Columns.LENGTH ) + ", ST_Length(ST_LineSubstring(e.geom, 0, ST_LineLocatePoint(e.geom, ST_ClosestPoint(e.geom, e2.point))), true) AS " + keyDistanceFromStart + ", ST_Length(ST_LineSubstring(e.geom, ST_LineLocatePoint(e.geom, ST_ClosestPoint(e.geom, e2.point)),1), true) AS " + keyDistanceToEnd + " "
                        + "FROM edges_view e "
                        + "JOIN ( "
                        + "	SELECT e.data_id, x.point "
                        + "        FROM edges_view e "
                        + "        JOIN (select " + pointString + " AS point) AS x ON true "
                        // + "        WHERE ST_DWithin(x.point, e.geom, 300) "
                        + "        ORDER BY e.geom <-> x.point "
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

        public StringDatabase( Properties connectionProperties ) {
            super( connectionProperties );
            SQLiteConfig config = new SQLiteConfig();
            config.enableLoadExtension( true );
            for ( Map.Entry<Object, Object> entry : config.toProperties().entrySet() ) {
                connectionProperties.put( entry.getKey(), entry.getValue() );
            }
        }

        @Override
        protected ResultSet checkedRead( String in ) throws SQLException {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected void checkedWrite( ResultSet in ) throws SQLException {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
