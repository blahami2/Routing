/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.turntables.sqlite;

import cz.certicon.routing.data.basic.database.impl.StringSqliteReader;
import cz.certicon.routing.memsensitive.data.turntables.TurnTablesReader;
import cz.certicon.routing.memsensitive.model.entity.TurnTablesBuilder;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import gnu.trove.list.TLongList;
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
        reader = new StringSqliteReader( connectionProperties );
    }

    @Override
    public <T, G> T read( G graph, TurnTablesBuilder<T, G> builder ) throws IOException {
        try {
            ResultSet rs;
            String query = "SELECT tra.array_id As array_id, n.id as node_via, d1.id as edge_to, d2.id as edge_from, position "
                    + "FROM turn_restrictions tr "
                    + "JOIN turn_restrictions_array tra ON tr.from_id = tra.array_id "
                    + "JOIN nodes n ON tr.via_id = n.data_id "
                    + "JOIN edges d1 ON tr.to_id = d1.data_id AND n.id = d1.source_id "
                    + "JOIN edges d2 ON tra.edge_id = d2.data_id "
                    + "ORDER BY tr.from_id, tra.position;";
            System.out.println( query );
            rs = reader.read( query );
            int nodeId = rs.findColumn( "node_via" );
            int edgeFromId = rs.findColumn( "edge_from" );
            int edgeToId = rs.findColumn( "edge_to" );
            int positionId = rs.findColumn( "position" );
            int arrayIdIdx = rs.findColumn( "array_id" );
            while ( rs.next() ) {
                int position = rs.getInt( positionId );
                int arrayId = rs.getInt( arrayIdIdx );
                long from = rs.getLong( edgeFromId );
                long to = rs.getLong( edgeToId );
                long via = rs.getLong( nodeId );
                builder.addRestriction( graph, arrayId, from, position, via, to );
            }
            reader.close();
            return builder.build( graph );
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
    }

    @Override
    public <T, G> T read( G graph, TurnTablesBuilder<T, G> builder, PreprocessedData preprocessedData ) throws IOException {
        // from = edge, to = edge
        String query = "SELECT tra.array_id As array_id, tr.via_id as node_via, tr.to_id as edge_to, tra.edge_id as edge_from, position "
                + "FROM ch_turn_restrictions tr "
                + "JOIN ch_turn_restrictions_array tra ON tr.from_id = tra.array_id "
                + "WHERE tr.distance_type = " + preprocessedData.getDistanceType().toInt() + " "
                + "ORDER BY tr.from_id, tra.position;";
        addChTurnTables( graph, builder, preprocessedData, query );
//        query = "SELECT tra.array_id As array_id, tr.via_id as node_via, tr.to_id as edge_to, tra.edge_id as edge_from, position "
//                + "FROM ch_turn_restrictions tr "
//                + "JOIN ch_turn_restrictions_array tra ON tr.from_id = tra.array_id "
//                + "WHERE tr.distance_type = " + preprocessedData.getDistanceType().toInt() + " "
//                + "ORDER BY tr.from_id, tra.position;";
//        addChTurnTables( graph, builder, preprocessedData, query );
//        query = "SELECT tra.array_id As array_id, tr.via_id as node_via, tr.to_id as edge_to, tra.edge_id as edge_from, position "
//                + "FROM ch_turn_restrictions tr "
//                + "JOIN ch_turn_restrictions_array tra ON tr.from_id = tra.array_id "
//                + "WHERE tr.distance_type = " + preprocessedData.getDistanceType().toInt() + " "
//                + "ORDER BY tr.from_id, tra.position;";
//        addChTurnTables( graph, builder, preprocessedData, query );
//        query = "SELECT tra.array_id As array_id, tr.via_id as node_via, tr.to_id as edge_to, tra.edge_id as edge_from, position "
//                + "FROM ch_turn_restrictions tr "
//                + "JOIN ch_turn_restrictions_array tra ON tr.from_id = tra.array_id "
//                + "WHERE tr.distance_type = " + preprocessedData.getDistanceType().toInt() + " "
//                + "ORDER BY tr.from_id, tra.position;";
//        addChTurnTables( graph, builder, preprocessedData, query );
        reader.close();
        return builder.build( graph, preprocessedData );
    }

    private <T, G> void addChTurnTables( G graph, TurnTablesBuilder<T, G> builder, PreprocessedData preprocessedData, String query ) throws IOException {
        try {
            ResultSet rs;
            rs = reader.read( query );
            System.out.println( query );
            if ( !rs.isClosed() ) {
//            System.out.println( "Opened = !" + rs.isClosed() );
                System.out.println( query );
                int nodeId = rs.findColumn( "node_via" );
                int edgeFromId = rs.findColumn( "edge_from" );
                int edgeToId = rs.findColumn( "edge_to" );
                int positionId = rs.findColumn( "position" );
                int arrayIdIdx = rs.findColumn( "array_id" );
                while ( rs.next() ) {
                    int position = rs.getInt( positionId );
                    int arrayId = rs.getInt( arrayIdIdx );
                    long from = rs.getLong( edgeFromId );
                    long to = rs.getLong( edgeToId );
                    long via = rs.getLong( nodeId );
                    builder.addRestriction( graph, preprocessedData, arrayId, from, position, via, to );
                }
            }
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
    }
}
