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
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public abstract class AbstractSqliteDatabase<Entity, AdditionalData> extends AbstractEmbeddedDatabase<Entity, AdditionalData> {

    private String spatialitePath;

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
            getStatement().execute( "SELECT load_extension('" + spatialitePath + "')" );
//        this.libspatialitePath = libspatialitePath;
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
    }
}
