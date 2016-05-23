/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.path.sqlite;

import cz.certicon.routing.data.basic.database.impl.AbstractSqliteDatabase;
import cz.certicon.routing.data.basic.database.impl.StringSqliteReader;
import cz.certicon.routing.memsensitive.algorithm.Route;
import cz.certicon.routing.memsensitive.data.path.PathReader;
import cz.certicon.routing.memsensitive.model.entity.PathBuilder;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.utils.GeometryUtils;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqlitePathReader implements PathReader {

    private static final int BATCH_SIZE = 200;

    private final InnerDatabase reader;

    public SqlitePathReader( Properties connectionProperties ) {
        this.reader = new InnerDatabase( connectionProperties );
    }

    @Override
    public <T, G> T readPath( PathBuilder<T, G> pathBuilder, G graph, Route route ) throws IOException {
        if ( route.getSource() != route.getTarget() ) {
            reader.open();
            Map<Long, Boolean> forwardMap = new HashMap<>();
            try {
                // create temporary table
                reader.setAutoCommit( false );
                if ( !reader.read( "SELECT name FROM sqlite_master WHERE type='table' AND name='path'" ).next() ) {
                    reader.execute( "CREATE TABLE path ("
                            + "order_id INTEGER,"
                            + "edge_id INTEGER"
                            + ")" );
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
                ps.executeBatch();
                reader.execute( "CREATE INDEX `idx_path_order` ON `path` (`order_id` ASC)" );
                ResultSet rs = reader.read( "SELECT e.id AS edge_id, ST_AsText(d.geom) AS linestring, e.is_forward, d.length, d.speed_fw, d.speed_bw "
                        + "FROM edges e "
                        + "JOIN path p "
                        + "ON e.id = p.edge_id "
                        + "JOIN edges_data d "
                        + "ON e.data_id = d.id "
                        + "ORDER BY p.order_id" );
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
                    pathBuilder.addEdge( graph, id, isForward, coordinates, length, length / speed );
                }
                reader.execute( "DELETE FROM path" );
                reader.execute( "DROP INDEX IF EXISTS `idx_path_order`" );
                reader.setAutoCommit( true );
                reader.close();
            } catch ( SQLException ex ) {
                throw new IOException( ex );
            }
        }
        return pathBuilder.build();
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
