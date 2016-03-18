/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.basic.database;

import cz.certicon.routing.data.Reader;
import cz.certicon.routing.data.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public abstract class AbstractDatabase<Entity, AdditionalData> implements Reader<AdditionalData, Entity>, Writer<Entity> {

    private Statement statement;
    private Connection connection;
    private boolean isOpened = false;
    private Properties connectionProperties;

    @Override
    public void open() throws IOException {
        if ( connectionProperties == null ) {
            InputStream in = getClass().getClassLoader().getResourceAsStream( "cz/certicon/routing/data/basic/database/database_connection.properties" );
            connectionProperties = new Properties();
            connectionProperties.load( in );
            in.close();
        }
        if ( !isOpened ) {
            try {
                Class.forName( getDriverClass() );
                connection = DriverManager.getConnection( getDatabaseUrl(), getUsername(), getPassword() );
                statement = connection.createStatement();
            } catch ( ClassNotFoundException | SQLException ex ) {
                throw new IOException( ex );
            }
            isOpened = true;
        }
    }

    @Override
    public Entity read( AdditionalData in ) throws IOException {
        if ( !isOpen() ) {
            open();
        }
        try {
            return checkedRead( in );
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
    }

    @Override
    public void write( Entity in ) throws IOException {
        if ( !isOpen() ) {
            open();
        }
        try {
            checkedWrite( in );
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
    }

    protected Statement getStatement() {
        return statement;
    }

    protected Connection getConnection() {
        return connection;
    }

    abstract protected Entity checkedRead( AdditionalData in ) throws SQLException;

    abstract protected void checkedWrite( Entity in ) throws SQLException;

    @Override
    public void close() throws IOException {
        if ( isOpened ) {
            try {
                statement.close();
                connection.close();
            } catch ( SQLException ex ) {
                throw new IOException( ex );
            }
            isOpened = false;
        }
    }

    @Override
    public boolean isOpen() {
        return isOpened;
    }

    protected String getDriverClass() {
        return connectionProperties.getProperty( "driver" );
    }

    protected String getDatabaseUrl() {
        return connectionProperties.getProperty( "url" );
    }

    protected String getUsername() {
        return connectionProperties.getProperty( "user" );
    }

    protected String getPassword() {
        return connectionProperties.getProperty( "password" );
    }

}
