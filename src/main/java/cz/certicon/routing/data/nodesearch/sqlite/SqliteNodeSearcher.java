/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.nodesearch.sqlite;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.data.basic.database.AbstractEmbeddedDatabase;
import cz.certicon.routing.data.nodesearch.NodeSearcher;
import cz.certicon.routing.model.entity.Coordinates;
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
        this.database = new StringDatabase( properties );
    }

    @Override
    public Map<Coordinates, Distance> findClosestNodes( Coordinates coordinates, DistanceFactory distanceFactory ) throws IOException {
        try {
            ResultSet rs = database.read( "SELECT n FROM nodes_view n WHERE ST_Equals("
                    + "ST_SnapToGrid(point, 0.000001),"
                    + "ST_SnapToGrid(n.geom, 0.000001)"
                    + ");" );
            boolean isCrossroad = false;
            while(rs.next()){ // for all nodes found
                // TODO stash changes, create new branch, change NodeSearcher specification to node.ids, distances, merge to development, merge from development to this branch
                
            }
            if(!isCrossroad){
                
            }
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
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
