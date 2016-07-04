/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.ch.sqlite;

import static cz.certicon.routing.GlobalOptions.MEASURE_TIME;
import cz.certicon.routing.data.basic.database.AbstractEmbeddedDatabase;
import cz.certicon.routing.data.basic.database.impl.AbstractSqliteDatabase;
import cz.certicon.routing.data.basic.database.impl.StringSqliteReader;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.EdgeDifferenceCalculator;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.NodeRecalculationStrategy;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.Preprocessor;
import cz.certicon.routing.memsensitive.data.ch.ContractionHierarchiesDataRW;
import cz.certicon.routing.memsensitive.data.ch.NotPreprocessedException;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.ch.ChDataBuilder;
import cz.certicon.routing.model.entity.ch.ChDataExtractor;
import cz.certicon.routing.model.entity.ch.ChDataFactory;
import cz.certicon.routing.model.utility.progress.SimpleProgressListener;
import cz.certicon.routing.utils.measuring.TimeLogger;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqliteContractionHierarchiesRW implements ContractionHierarchiesDataRW {

    private static final int BATCH_SIZE = 200;

    private final InnerDatabase db;

    private final int batchSize;

    public SqliteContractionHierarchiesRW( Properties connectionProperties ) {
        this.db = new InnerDatabase( connectionProperties );
        this.batchSize = BATCH_SIZE;
    }

    public SqliteContractionHierarchiesRW( Properties connectionProperties, int batchSize ) {
        this.db = new InnerDatabase( connectionProperties );
        this.batchSize = batchSize;
    }

    @Override
    public <T> T read( ChDataFactory<T> chDataFactory ) throws NotPreprocessedException, IOException {
        T data = read( chDataFactory, null, null );
        if ( data == null ) {
            throw new NotPreprocessedException();
        }
        return data;
    }

    @Override
    public <T> T read( ChDataFactory<T> chDataFactory, Graph graph, Preprocessor<T> preprocessor ) throws IOException {
        try {
            ChDataBuilder<T> chDataBuilder = chDataFactory.createChDataBuilder();
            if ( db.read( "SELECT name FROM sqlite_master WHERE type='table' AND name='shortcuts'" ).next()
                    && db.read( "SELECT name FROM sqlite_master WHERE type='table' AND name='ranks'" ).next()
                    && db.read( "SELECT distanceType FROM ranks WHERE distanceType=" + chDataBuilder.getDistanceType().toInt() + " LIMIT 1" ).next() ) {
                ResultSet rs;
                rs = db.read( "SELECT MIN(id) AS min FROM shortcuts WHERE distanceType=" + chDataBuilder.getDistanceType().toInt() );
                long minId = 0;
                if ( rs.next() ) {
                    minId = rs.getLong( "min" );
                }
                chDataBuilder.setStartId( minId );
                rs = db.read( "SELECT id, edge_source, edge_target FROM shortcuts WHERE distanceType=" + chDataBuilder.getDistanceType().toInt() + " ORDER BY id;" );
                int edgeIdIdx = rs.findColumn( "id" );
                int sourceIdIdx = rs.findColumn( "edge_source" );
                int targetIdIdx = rs.findColumn( "edge_target" );
                while ( rs.next() ) {
                    long id = rs.getLong( edgeIdIdx );
                    long sourceId = rs.getLong( sourceIdIdx );
                    long targetId = rs.getLong( targetIdIdx );
                    chDataBuilder.addShortcut( id, sourceId, targetId );
                }
                rs = db.read( "SELECT node_id, rank FROM ranks WHERE distanceType=" + chDataBuilder.getDistanceType().toInt() );
                int nodeIdIdx = rs.findColumn( "node_id" );
                int rankIdx = rs.findColumn( "rank" );
                while ( rs.next() ) {
                    long id = rs.getLong( nodeIdIdx );
                    int rank = rs.getInt( rankIdx );
                    chDataBuilder.setRank( id, rank );
                }
//                System.out.println( "Database-" + getClass().getSimpleName() + "@read" );
                db.close();
                return chDataBuilder.build();
            } else {
                if ( preprocessor == null ) {
                    return null;
                }
                if ( MEASURE_TIME ) {
                    TimeLogger.log( TimeLogger.Event.PREPROCESSING, TimeLogger.Command.START );
                }
                long startId = 0;
                if ( db.read( "SELECT name FROM sqlite_master WHERE type='table' AND name='shortcuts'" ).next() ) {
                    ResultSet rs = db.read( "SELECT max(id) AS startId FROM shortcuts" );
                    if ( rs.next() ) {
                        startId = rs.getLong( "startId" );
                    }// else the table is empty and then leave it to zero
                } else {
                    ResultSet rs = db.read( "SELECT max(id) AS startId FROM edges" );
                    if ( rs.next() ) {
                        startId = rs.getLong( "startId" );
                    }
                }
                chDataBuilder.setStartId( startId );
                T preprocessedData = preprocessor.preprocess( chDataBuilder, graph, chDataBuilder.getDistanceType(), startId + 1, new SimpleProgressListener( 100 ) {
                    @Override
                    public void onProgressUpdate( double d ) {
                        System.out.println( String.format( "%.0f %%", d * 100 ) );
                    }
                } );
                write( chDataFactory, preprocessedData );
                if ( MEASURE_TIME ) {
                    TimeLogger.log( TimeLogger.Event.PREPROCESSING, TimeLogger.Command.STOP );
                }
//                System.out.println( "Database-" + getClass().getSimpleName() + "@read" );
                db.close();
                return preprocessedData;
            }
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
    }

    @Override
    public <T> void write( ChDataFactory<T> chDataFactory, T entity ) throws IOException {
        try {
            db.open();
            ChDataExtractor<T> chDataExtractor = chDataFactory.createChDataExtractor( entity );
            db.setAutoCommit( false ); //transaction block start

            db.execute( "DROP INDEX IF EXISTS `idx_id_shortcuts`" );
            db.execute( "DROP INDEX IF EXISTS `idx_dist_shortcuts`" );
            db.execute( "DROP INDEX IF EXISTS `idx_id_ranks`" );
            db.execute( "DROP INDEX IF EXISTS `idx_dist_ranks`" );
            db.execute( "DROP INDEX IF EXISTS `idx_dist_tt`" );
            db.execute( "DROP INDEX IF EXISTS `idx_tt_array`" );

            int distanceType = chDataExtractor.getDistanceType().toInt();
            if ( db.read( "SELECT name FROM sqlite_master WHERE type='table' AND name='shortcuts'" ).next() ) {
                db.execute( "DELETE FROM shortcuts WHERE distanceType=" + distanceType );
            } else {
                db.execute( "CREATE TABLE shortcuts ("
                        + "id INTEGER NOT NULL PRIMARY KEY,"
                        + "edge_source INTEGER,"
                        + "edge_target INTEGER,"
                        + "distanceType INTEGER"
                        + ")" );
            }
            PreparedStatement shortcutStatement = db.prepareStatement( "INSERT INTO shortcuts (id, edge_source, edge_target, distanceType) VALUES (?, ?, ?, ?)" );

            int i = 1;
            Iterator<Trinity<Long, Long, Long>> shortcutIterator = chDataExtractor.getShortcutIterator();
            while ( shortcutIterator.hasNext() ) {
                Trinity<Long, Long, Long> shortcut = shortcutIterator.next();
                int idx = 1;
                shortcutStatement.setLong( idx++, shortcut.a );
                shortcutStatement.setLong( idx++, shortcut.b );
                shortcutStatement.setLong( idx++, shortcut.c );
                shortcutStatement.setInt( idx++, distanceType );
                shortcutStatement.addBatch();
                if ( i++ % batchSize == 0 ) {
                    shortcutStatement.executeBatch();
                }
            }
            shortcutStatement.executeBatch();
            if ( db.read( "SELECT name FROM sqlite_master WHERE type='table' AND name='ranks'" ).next() ) {
                db.execute( "DELETE FROM ranks WHERE distanceType=" + distanceType );
            } else {
                db.execute( "CREATE TABLE ranks ("
                        + "node_id INTEGER NOT NULL,"
                        + "rank INTEGER,"
                        + "distanceType INTEGER"
                        + ")" );
            }
            PreparedStatement rankStatement = db.prepareStatement( "INSERT INTO ranks (node_id, rank, distanceType) VALUES (?, ?, ?)" );
            i = 1;
            Iterator<Pair<Long, Integer>> rankIterator = chDataExtractor.getRankIterator();
            while ( rankIterator.hasNext() ) {
                Pair<Long, Integer> rank = rankIterator.next();
                int idx = 1;
                rankStatement.setLong( idx++, rank.a );
                rankStatement.setInt( idx++, rank.b );
                rankStatement.setInt( idx++, distanceType );
                rankStatement.addBatch();
                if ( i++ % batchSize == 0 ) {
                    rankStatement.executeBatch();
                }
            }
            rankStatement.executeBatch();

            i = 1;
            if ( db.read( "SELECT name FROM sqlite_master WHERE type='table' AND name='ch_turn_restrictions'" ).next() ) {
                db.execute( "DELETE FROM ch_turn_restrictions_array "
                        + "WHERE array_id IN ("
                        + "  SELECT from_id FROM ch_turn_restrictions t WHERE distance_type = " + distanceType
                        + " ) " );
                db.execute( "DELETE FROM ch_turn_restrictions "
                        + "WHERE distance_type = " + distanceType );
                ResultSet rs = db.checkedRead( "SELECT max(from_id) AS max_array FROM ch_turn_restrictions" );
                i = rs.getInt( "max_array" ) + 1; // get max and increment
            } else {
                db.execute( "CREATE TABLE ch_turn_restrictions ("
                        + "from_id INTEGER,"
                        + "via_id INTEGER,"
                        + "to_id INTEGER,"
                        + "distance_type INTEGER"
                        + ")" );
            }
            if ( !db.read( "SELECT name FROM sqlite_master WHERE type='table' AND name='ch_turn_restrictions_array'" ).next() ) {
                db.execute( "CREATE TABLE ch_turn_restrictions_array ("
                        + "array_id INTEGER,"
                        + "position INTEGER,"
                        + "edge_id INTEGER"
                        + ")" );
            }
            int i2 = 1;
            PreparedStatement turnTableStatement = db.prepareStatement( "INSERT INTO ch_turn_restrictions (from_id, via_id, to_id, distance_type) VALUES (?, ?, ?, ?)" );
            PreparedStatement turnTableArrayStatement = db.prepareStatement( "INSERT INTO ch_turn_restrictions_array (array_id, position, edge_id) VALUES (?, ?, ?)" );
            Iterator<Trinity<List<Long>, Long, Long>> turnTableIterator = chDataExtractor.getTurnTableIterator();
            while ( turnTableIterator.hasNext() ) {
                Trinity<List<Long>, Long, Long> tt = turnTableIterator.next();
//                System.out.println( "inserting: node#" + tt.b );
                int idx = 1;
                turnTableStatement.setInt( idx++, i ); // array_id
                turnTableStatement.setLong( idx++, tt.b ); // node_id
                turnTableStatement.setLong( idx++, tt.c ); // last_edge_id
                turnTableStatement.setInt( idx++, distanceType ); // distance_type
                turnTableStatement.addBatch();
                for ( int j = 0; j < tt.a.size(); j++ ) {
                    i2++;
                    long edgeId = tt.a.get( j );
                    idx = 1;
                    turnTableArrayStatement.setInt( idx++, i ); // array_id
                    turnTableArrayStatement.setInt( idx++, j ); // position
                    turnTableArrayStatement.setLong( idx++, edgeId ); // edge_id
                    turnTableArrayStatement.addBatch();
                }
                if ( i++ % batchSize == 0 ) {
                    turnTableStatement.executeBatch();
                }
                if ( i2 % batchSize == 0 ) {
                    turnTableArrayStatement.executeBatch();
                }
            }
            turnTableStatement.executeBatch();
            turnTableArrayStatement.executeBatch();

            db.execute( "CREATE INDEX `idx_id_shortcuts` ON `shortcuts` (`id` ASC)" );
            db.execute( "CREATE INDEX `idx_dist_shortcuts` ON `shortcuts` (`distanceType` ASC)" );
            db.execute( "CREATE INDEX `idx_id_ranks` ON `ranks` (`node_id` ASC)" );
            db.execute( "CREATE INDEX `idx_dist_ranks` ON `ranks` (`distanceType` ASC)" );
            db.execute( "CREATE INDEX `idx_dist_tt` ON `ch_turn_restrictions` (`distance_type` ASC)" );
            db.execute( "CREATE INDEX `idx_tt_array` ON `ch_turn_restrictions_array` (`array_id` ASC)" );

            db.commit();
            db.setAutoCommit( true );
//            System.out.println( "Database-" + getClass().getSimpleName() + "@write" );
            db.close();
        } catch ( SQLException ex ) {
            Logger.getLogger( SqliteContractionHierarchiesRW.class.getName() ).log( Level.SEVERE, null, ex );
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
