/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.graph.sqlite;

import cz.certicon.routing.data.basic.database.impl.StringSqliteReader;
import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.data.graph.GraphReader;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.GraphBuilder;
import cz.certicon.routing.model.entity.GraphBuilderFactory;
import cz.certicon.routing.utils.GeometryUtils;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqliteGraphReader implements GraphReader {

    private final StringSqliteReader reader;

    public SqliteGraphReader( Properties connectionProperties ) {
        this.reader = new StringSqliteReader( connectionProperties );
    }

    @Override
    public <T> T readGraph( GraphBuilderFactory<T> GraphBuilderFactory, DistanceType distanceType ) throws IOException {
        try {
            ResultSet rs;
            rs = reader.read( "SELECT COUNT(*) AS nodeCount FROM nodes" );
            if ( !rs.next() ) {
                throw new SQLException( "Could not read node count" );
            }
            int nodeCount = rs.getInt( "nodeCount" );
            rs = reader.read( "SELECT COUNT(*) AS edgeCount FROM edges" );
            if ( !rs.next() ) {
                throw new SQLException( "Could not read edge count" );
            }
            int edgeCount = rs.getInt( "edgeCount" );
            GraphBuilder<T> graphBuilder = GraphBuilderFactory.createGraphBuilder( nodeCount, edgeCount );

            rs = reader.read( "SELECT n.id, n.data_id, ST_AsText(d.geom) AS geom, d.osm_id "
                    + "FROM nodes n "
                    + "JOIN nodes_data d "
                    + "ON n.data_id = d.id" );
            int idIdx = rs.findColumn( "id" );
            int dataIdx = rs.findColumn( "data_id" );
            int pointIdx = rs.findColumn( "geom" );
            int osmIdx = rs.findColumn( "osm_id" );
            while ( rs.next() ) {
                long id = rs.getLong( idIdx );
                long osmId = rs.getLong( osmIdx );
                long dataId = rs.getLong( dataIdx );
                Coordinate coords = GeometryUtils.toCoordinatesFromWktPoint( rs.getString( pointIdx ) );
                graphBuilder.addNode( id, dataId, osmId, coords.getLatitude(), coords.getLongitude() );
            }
            rs = reader.read( "SELECT e.id, e.data_id, e.is_forward, e.source_id, e.target_id, d.osm_id, d.length, d.speed_fw, d.speed_bw, d.is_paid "
                    + "FROM edges e "
                    + "JOIN edges_data d "
                    + "ON e.data_id = d.id;" );
            idIdx = rs.findColumn( "id" );
            dataIdx = rs.findColumn( "data_id" );
            osmIdx = rs.findColumn( "osm_id" );
            int sourceIdx = rs.findColumn( "source_id" );
            int targetIdx = rs.findColumn( "target_id" );
            int lengthIdx = rs.findColumn( "length" );
            int forwardIdx = rs.findColumn( "is_forward" );
            int paidIdx = rs.findColumn( "is_paid" );
            int speedFwIdx = rs.findColumn( "speed_fw" );
            int speedBwIdx = rs.findColumn( "speed_bw" );
            while ( rs.next() ) {
                long id = rs.getLong( idIdx );
                long dataId = rs.getLong( dataIdx );
                long osmId = rs.getLong( osmIdx );
                long source = rs.getLong( sourceIdx );
                long target = rs.getLong( targetIdx );
                double length = rs.getDouble( lengthIdx );
                double speed = rs.getDouble( ( rs.getBoolean( forwardIdx ) ) ? speedFwIdx : speedBwIdx );
                boolean paid = rs.getBoolean( paidIdx );
                graphBuilder.addEdge( id, dataId, osmId, source, target, length, speed, paid );
            }
            reader.close();
            return graphBuilder.build();
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
    }

}
