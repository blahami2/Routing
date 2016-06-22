/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.turntables.sqlite;

import cz.certicon.routing.data.basic.database.impl.StringSqliteReader;
import cz.certicon.routing.memsensitive.data.turntables.TurnTablesReader;
import cz.certicon.routing.memsensitive.model.entity.TurnTablesBuilder;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqliteTurnTablesReader implements TurnTablesReader {

    private final StringSqliteReader reader;

    public SqliteTurnTablesReader( Properties connectionProperties ) {
        this.reader = new StringSqliteReader( connectionProperties );
    }

    @Override
    public <T, G> T read( G graph, TurnTablesBuilder<T, G> builder ) throws IOException {
        try {
            ResultSet rs;
            String query = "SELECT n.id as node_via, d1.id as edge_to, d2.id as edge_from, position FROM turn_restrictions tr JOIN turn_restrictions_array tra ON tr.from_id = tra.array_id "
                    + "JOIN nodes n ON tr.via_id = n.data_id "
                    + "JOIN edges d1 ON tr.to_id = d1.data_id "
                    + "JOIN edges d2 ON tra.edge_id = d2.data_id "
                    + "ORDER BY tr.from_id, tra.position;";
            rs = reader.read( query );
            TLongList fromList = new TLongArrayList();
            int nodeId = rs.findColumn( "node_via" );
            int edgeFromId = rs.findColumn( "edge_from" );
            int edgeToId = rs.findColumn( "edge_to" );
            int positionId = rs.findColumn( "position" );
            int lastPosition = Integer.MAX_VALUE;
            long lastNode = -1;
            long lastEdgeTo = -1;
            while ( rs.next() ) {
                int position = rs.getInt( positionId );
                if ( position <= lastPosition && fromList.size() > 0 ) { // new
                    builder.addRestriction( graph, fromList.toArray(), lastNode, lastEdgeTo );
                    fromList = new TLongArrayList();
                }
                lastNode = rs.getLong( nodeId );
                lastEdgeTo = rs.getLong( edgeToId );
                long edgeFrom = rs.getLong( edgeFromId );
                fromList.add( edgeFrom );
            }
            if ( fromList.size() > 0 ) {
                builder.addRestriction( graph, fromList.toArray(), lastNode, lastEdgeTo );
            }
            reader.close();
            return builder.build( graph );
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
    }

}
