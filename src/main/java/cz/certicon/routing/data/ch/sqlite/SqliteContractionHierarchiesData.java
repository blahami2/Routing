/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.ch.sqlite;

import cz.certicon.routing.application.preprocessing.ch.ContractionHierarchiesPreprocessor;
import cz.certicon.routing.data.basic.database.impl.AbstractSqliteDatabase;
import cz.certicon.routing.data.ch.ContractionHierarchiesData;
import cz.certicon.routing.data.ch.DistanceType;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Shortcut;
import cz.certicon.routing.model.entity.common.SimpleShortcut;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqliteContractionHierarchiesData extends AbstractSqliteDatabase<Trinity<Map<Node.Id, Integer>, List<Shortcut>, DistanceType>, Trinity<Graph, GraphEntityFactory, DistanceType>> implements ContractionHierarchiesData {

    private static final int BATCH_SIZE = 200;

    public SqliteContractionHierarchiesData( Properties connectionProperties ) {
        super( connectionProperties );
    }
//SELECT name FROM sqlite_master WHERE type='table' AND name='table_name';

    @Override
    protected Trinity<Map<Node.Id, Integer>, List<Shortcut>, DistanceType> checkedRead( Trinity<Graph, GraphEntityFactory, DistanceType> in ) throws SQLException {
        Trinity<Map<Node.Id, Integer>, List<Shortcut>, DistanceType> result;
        if ( getStatement().executeQuery( "SELECT name FROM sqlite_master WHERE type='table' AND name=`shortcuts`" ).next()
                && getStatement().executeQuery( "SELECT name FROM sqlite_master WHERE type='table' AND name=`ranks`" ).next() ) {
            Map<Node.Id, Integer> rankMap = new HashMap<>();
            List<Shortcut> shortcuts = new ArrayList<>();

            ResultSet rs = getStatement().executeQuery( "SELECT id, edge_source, edge_target FROM shortcuts WHERE distanceType=" + DistanceType.toInt( in.c ) );
            while ( rs.next() ) {
                shortcuts.add( new SimpleShortcut(
                        Edge.Id.createId( rs.getLong( "id" ) ),
                        in.a.getEdge( Edge.Id.createId( rs.getLong( "edge_source" ) ) ),
                        in.a.getEdge( Edge.Id.createId( rs.getLong( "edge_target" ) ) ) ) );
            }
            rs = getStatement().executeQuery( "SELECT node_id, rank FROM ranks WHERE distanceType=" + DistanceType.toInt( in.c ) );
            while ( rs.next() ) {
                rankMap.put( Node.Id.createId( rs.getLong( "node_id" ) ), rs.getInt( "rank" ) );
            }

            result = new Trinity<>( rankMap, shortcuts, in.c );
        } else {
            TimeMeasurement time = new TimeMeasurement();
            System.out.println( "Preprocessed data not found, preprocessing..." );
            time.start();
            ContractionHierarchiesPreprocessor preprocessor = new ContractionHierarchiesPreprocessor();
            Pair<Map<Node.Id, Integer>, List<Shortcut>> preprocessedData = preprocessor.preprocess( in.a, in.b, in.c.getDistanceFactory() );
            System.out.println( "Preprocessing done in " + time.restart() + " ms! Importing into database..." );
            result = new Trinity<>( preprocessedData.a, preprocessedData.b, in.c );
            checkedWrite( result );
            System.out.println( "Inserting done in " + time.stop() + " ms!" );
        }

        return result;
    }

    @Override
    protected void checkedWrite( Trinity<Map<Node.Id, Integer>, List<Shortcut>, DistanceType> in ) throws SQLException {
        getConnection().setAutoCommit( false ); //transaction block start
        getStatement().execute( "DROP TABLE IF EXISTS shortcut;" );
        getStatement().execute( "CREATE TABLE shortcuts ("
                + "id INTEGER NOT NULL PRIMARY KEY,"
                + "edge_source INTEGER,"
                + "edge_target INTEGER,"
                + "distanceType INTEGER"
                + ")" );
        int distanceType = DistanceType.toInt( in.c );
        PreparedStatement shortcutStatement = getConnection().prepareStatement( "INSERT INTO shortcuts (id, edge_source, edge_target, distanceType) VALUES (?, ?, ?, ?)" );

        int i = 1;
        for ( Shortcut shortcut : in.b ) {
            int idx = 1;
            shortcutStatement.setLong( idx++, shortcut.getId().getValue() );
            shortcutStatement.setLong( idx++, shortcut.getSourceEdge().getId().getValue() );
            shortcutStatement.setLong( idx++, shortcut.getTargetEdge().getId().getValue() );
            shortcutStatement.setInt( idx++, distanceType );
            if ( i++ % BATCH_SIZE == 0 ) {
                shortcutStatement.executeBatch();
            }
        }
        shortcutStatement.executeBatch();
        getStatement().execute( "DROP TABLE IF EXISTS ranks;" );
        getStatement().execute( "CREATE TABLE ranks ("
                + "node_id INTEGER NOT NULL PRIMARY KEY,"
                + "rank INTEGER,"
                + "distanceType INTEGER"
                + ")" );
        PreparedStatement rankStatement = getConnection().prepareStatement( "INSERT INTO ranks (node_id, rank, distanceType) VALUES (?, ?, ?)" );
        i = 1;
        for ( Map.Entry<Node.Id, Integer> entry : in.a.entrySet() ) {
            int idx = 1;
            rankStatement.setLong( idx++, entry.getKey().getValue() );
            rankStatement.setInt( idx++, entry.getValue() );
            rankStatement.setInt( idx++, distanceType );
            if ( i++ % BATCH_SIZE == 0 ) {
                rankStatement.executeBatch();
            }
        }
        rankStatement.executeBatch();

        getStatement().execute( "CREATE UNIQUE INDEX `idx_id_shortcuts` ON `shortcuts` (`id` ASC)" );
        getStatement().execute( "CREATE UNIQUE INDEX `idx_dist_shortcuts` ON `shortcuts` (`distanceType` ASC)" );
        getStatement().execute( "CREATE UNIQUE INDEX `idx_id_ranks` ON `ranks` (`id` ASC)" );
        getStatement().execute( "CREATE UNIQUE INDEX `idx_dist_ranks` ON `ranks` (`distanceType` ASC)" );

        getConnection().commit();
        getConnection().setAutoCommit( true );
    }

}
