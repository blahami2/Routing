/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.basic.database.impl;

import cz.certicon.routing.data.basic.database.AbstractEmbeddedDatabase;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import org.sqlite.SQLiteConfig;

/**
 * Abstract extension of the embedded database focused on the SQLite database.
 * Also adds support for the SpatiaLite extension - the provided Properties must
 * contain path to the spatialite library.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <Entity> entity to be read or written
 * @param <AdditionalData> input data for the read
 */
public abstract class AbstractSqliteDatabase<Entity, AdditionalData> extends AbstractEmbeddedDatabase<Entity, AdditionalData> {

    private final String spatialitePath;

    /**
     * Creates an instance of this class.
     *
     * @param connectionProperties must contain properties (more info in
     * {@link AbstractEmbeddedDatabase}) and a path to the spatialite library
     * under key: spatialite_path
     */
    public AbstractSqliteDatabase( Properties connectionProperties ) {
        super( connectionProperties );
        SQLiteConfig config = new SQLiteConfig();
        config.enableLoadExtension( true );
        for ( Map.Entry<Object, Object> entry : config.toProperties().entrySet() ) {
            connectionProperties.put( entry.getKey(), entry.getValue() );
        }
        this.spatialitePath = connectionProperties.getProperty( "spatialite_path" );
    }

    @Override
    public void open() throws IOException {
        super.open();
        try {
//            PreparedStatement prepareStatement = getConnection().prepareStatement( "SELECT load_extension('?')");
//            prepareStatement.setString( 1, spatialitePath);
//            prepareStatement.execute();
            getStatement().execute( "SELECT load_extension('" + spatialitePath + "')" );
//        this.libspatialitePath = libspatialitePath;
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
    }
}
