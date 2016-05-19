/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.graph.sqlite;

import cz.certicon.routing.data.basic.database.impl.AbstractSqliteDatabase;
import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.data.graph.GraphRW;
import cz.certicon.routing.memsensitive.model.entity.neighbourlist.NeighbourlistGraph;
import cz.certicon.routing.utils.CollectionUtils;
import static cz.certicon.routing.utils.CollectionUtils.getList;
import static cz.certicon.routing.utils.CollectionUtils.toIntArray;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqliteGraphRW extends AbstractSqliteDatabase<Graph, DistanceType> implements GraphRW {

    public SqliteGraphRW( Properties connectionProperties ) {
        super( connectionProperties );
    }

    @Override
    protected Graph checkedRead( DistanceType in ) throws SQLException {

        ResultSet rs;
        rs = getStatement().executeQuery( "SELECT COUNT(*) AS nodeCount FROM nodes" );
        if ( !rs.next() ) {
            throw new SQLException( "Could not read node count" );
        }
        int nodeCount = rs.getInt( "nodeCount" );
        rs = getStatement().executeQuery( "SELECT COUNT(*) AS edgeCount FROM edges" );
        if ( !rs.next() ) {
            throw new SQLException( "Could not read edge count" );
        }
        int edgeCount = rs.getInt( "edgeCount" );

        Graph graph = new NeighbourlistGraph( nodeCount, edgeCount );
        rs = getStatement().executeQuery( "SELECT n.id AS id "
                + "FROM nodes n;" );
        int idColumnIdx = rs.findColumn( "id" );
        int nodeCounter = 0;
        while ( rs.next() ) {
            int idx = rs.getInt( idColumnIdx );
            graph.setNodeOrigId( nodeCounter++, idx );
        }
        rs = getStatement().executeQuery( "SELECT e.id, e.is_forward, e.source_id, e.target_id, d.length, d.speed_fw, d.speed_bw "
                + "FROM edges e "
                + "JOIN edges_data d ON e.data_id = d.id;" );
        idColumnIdx = rs.findColumn( "id" );
        int sourceColumnIndex = rs.findColumn( "source_id" );
        int targetColumnIndex = rs.findColumn( "target_id" );
        int lengthColumnIndex = rs.findColumn( "length" );
        int forwardColumnIndex = rs.findColumn( "is_forward" );
        int speedFwColumnIndex = rs.findColumn( "speed_fw" );
        int speedBwColumnIndex = rs.findColumn( "speed_bw" );
        int edgeCounter = 0;
        Map<Long, List<Integer>> outgoingEdgesMap = new HashMap<>();
        Map<Long, List<Integer>> incomingEdgesMap = new HashMap<>();
        while ( rs.next() ) {
            int idx = rs.getInt( idColumnIdx );
            graph.setEdgeOrigId( edgeCounter, idx );
            int source = rs.getInt( sourceColumnIndex );
            int target = rs.getInt( targetColumnIndex );

            getList( outgoingEdgesMap, Long.valueOf( source ) ).add( edgeCounter );
            getList( incomingEdgesMap, Long.valueOf( target ) ).add( edgeCounter );

            double length = rs.getDouble( lengthColumnIndex );
            double speed = rs.getDouble( ( rs.getBoolean( forwardColumnIndex ) ) ? speedFwColumnIndex : speedBwColumnIndex );
            graph.setLength( edgeCounter, in.calculateDistance( length, speed ) );

            edgeCounter++;
        }
        for ( Map.Entry<Long, List<Integer>> entry : outgoingEdgesMap.entrySet() ) {
            long origNodeId = entry.getKey();
            int[] outgoingEdgesArray = toIntArray( entry.getValue() );
            graph.setOutgoingEdges( graph.getNodeByOrigId( origNodeId ), outgoingEdgesArray );
        }
        for ( Map.Entry<Long, List<Integer>> entry : incomingEdgesMap.entrySet() ) {
            long origNodeId = entry.getKey();
            int[] incomingEdgesArray = toIntArray( entry.getValue() );
            graph.setIncomingEdges( graph.getNodeByOrigId( origNodeId ), incomingEdgesArray );
        }
        return graph;
    }

    @Override
    protected void checkedWrite( Graph in ) throws SQLException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

}
