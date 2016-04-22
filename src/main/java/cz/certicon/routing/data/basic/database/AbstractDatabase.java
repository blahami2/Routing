/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.basic.database;

import cz.certicon.routing.data.Reader;
import cz.certicon.routing.data.Writer;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * An abstract implementation of the {@link Reader}/{@link Writer} interfaces
 * for the database access. Encapsulates database access (connection creating),
 * controls the state before reading/writing and opens the connection if
 * necessary.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <Entity> entity to be read or written
 * @param <AdditionalData> input data for the read
 */
public abstract class AbstractDatabase<Entity, AdditionalData> implements Reader<AdditionalData, Entity>, Writer<Entity> {

    private Statement statement;
    private Connection connection;
    private boolean isOpened = false;
    private Properties connectionProperties;

    public AbstractDatabase( Properties connectionProperties ) {
        this.connectionProperties = connectionProperties;
    }

    @Override
    public void open() throws IOException {
//        if ( connectionProperties == null ) {
//            InputStream in = getClass().getClassLoader().getResourceAsStream( "cz/certicon/routing/data/basic/database/database_connection.properties" );
//            connectionProperties = new Properties();
//            connectionProperties.load( in );
//            in.close();
//        }
        if ( !isOpened ) {
            try {
                connection = createConnection( connectionProperties );
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

    /**
     * Checks the state before reading and opens the source if necessary.
     *
     * @param in additional data (passed)
     * @return the read entity
     * @throws SQLException database exception
     */
    abstract protected Entity checkedRead( AdditionalData in ) throws SQLException;

    /**
     * Checks the state before writing and opens the target if necessary.
     *
     * @param in the entity to be written
     * @throws SQLException database exception
     */
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

    /**
     * Create new {@link Connection} based on given connection {@link Properties}
     * @param properties connection data
     * @return an instance of {@link Connection}
     * @throws ClassNotFoundException thrown when the driver is not found
     * @throws SQLException  thrown when the connection cannot be established for some reason
     */
    protected abstract Connection createConnection( Properties properties ) throws ClassNotFoundException, SQLException;

}
