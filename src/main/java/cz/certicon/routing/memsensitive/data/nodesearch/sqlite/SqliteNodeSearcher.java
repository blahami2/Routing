/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.nodesearch.sqlite;

import cz.certicon.routing.data.basic.database.EdgeResultHelper;
import cz.certicon.routing.data.basic.database.impl.StringSqliteReader;
import cz.certicon.routing.memsensitive.data.nodesearch.NodeSearcher;
import cz.certicon.routing.model.entity.NodeSetBuilder;
import cz.certicon.routing.model.entity.NodeSetBuilderFactory;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqliteNodeSearcher implements NodeSearcher {

    private static final double DISTANCE_INIT = 0.001;
    private static final double DISTANCE_MULTIPLIER = 10;

    private final StringSqliteReader reader;
    private final double distanceInit;
    private final double distanceMultiplier;

    public SqliteNodeSearcher( Properties connectionProperties ) {
        this.reader = new StringSqliteReader( connectionProperties );
        this.distanceInit = DISTANCE_INIT;
        this.distanceMultiplier = DISTANCE_MULTIPLIER;
    }

    public SqliteNodeSearcher( StringSqliteReader reader, double distanceInit, double distanceMultiplier ) {
        this.reader = reader;
        this.distanceInit = distanceInit;
        this.distanceMultiplier = distanceMultiplier;
    }

    @Override
    public <T> T findClosestNodes( NodeSetBuilderFactory<T> nodeSetBuilderFactory, double latitude, double longitude, SearchFor searchfor ) throws IOException {
        NodeSetBuilder<T> nodeSetBuilder = nodeSetBuilderFactory.createNodeSetBuilder();

        final String pointString = "ST_GeomFromText('POINT(" + longitude + " " + latitude + ")',4326)";
        final String keyDistanceFromStart = "distance_from_start";
        final String keyDistanceToEnd = "distance_to_end";
        try {
            ResultSet rs = reader.read( "SELECT n.id AS id "
                    + "FROM nodes n "
                    + "JOIN (SELECT * FROM nodes_data d "
                    + "        WHERE d.ROWID IN( "
                    + "            SELECT ROWID FROM SpatialIndex "
                    + "            WHERE f_table_name = 'nodes_data' "
                    + "            AND search_frame = BuildCircleMbr(" + longitude + ", " + latitude + " , " + distanceInit + "  ,4326)"
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
                nodeSetBuilder.addCrossroad( id );
            }
            if ( !isCrossroad ) {
                boolean found = false;
                double distance = distanceInit;
                while ( !found ) {
                    rs = reader.read(
                            "SELECT e.id, e.is_forward, e.source_id, e.target_id, e2.is_paid, e2.length"
                            + ", e2.speed_fw, e2.speed_bw "
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
                            + "            AND search_frame = BuildCircleMbr(" + longitude + ", " + latitude + " , " + distance + "  ,4326)"
                            + "        )  "
                            + "        ORDER BY Distance( ed.geom, x.point) "
                            + "        LIMIT 1 "
                            + ") AS e2 "
                            + "ON e.data_id = e2.id" );
                    while ( rs.next() ) {
                        found = true;
                        EdgeResultHelper edgeResultHelper = new EdgeResultHelper( rs );
                        long nodeId;
                        double length;
                        if ( searchfor.equals( SearchFor.SOURCE ) ) {
                            nodeId = edgeResultHelper.getTargetId();
                            length = rs.getDouble( edgeResultHelper.getIsForward() ? keyDistanceToEnd : keyDistanceFromStart );
                        } else {
                            nodeId = edgeResultHelper.getSourceId();
                            length = rs.getDouble( edgeResultHelper.getIsForward() ? keyDistanceFromStart : keyDistanceToEnd );
                        }
                        nodeSetBuilder.addNode( nodeId, edgeResultHelper.getId(), length, rs.getInt( edgeResultHelper.getIsForward() ? "speed_fw" : "speed_bw" ) );
                    }
                    distance *= DISTANCE_MULTIPLIER;
                }
            }
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }

        return nodeSetBuilder.build();
    }

}
