/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.basic.database.impl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * An extension to the {@link StringSqliteReader}, which is read-only and
 * returns {@link ResultSet} based on the given query (String).
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class StringSqliteReader {

    private final AbstractSqliteDatabase<ResultSet, String> database;

    public StringSqliteReader( Properties connectionProperties ) {
        this.database = new AbstractSqliteDatabase<ResultSet, String>( connectionProperties ) {
            @Override
            protected ResultSet checkedRead( String in ) throws SQLException {
                return getStatement().executeQuery( in );
            }

            @Override
            protected void checkedWrite( ResultSet in ) throws SQLException {
                throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }

    /**
     * Returns {@link ResultSet} based on the given query.
     *
     * @param sql SQL query
     * @return result set
     * @throws IOException thrown when an SQL or IO exception appears
     */
    public ResultSet read( String sql ) throws IOException {
        return database.read( sql );
    }

    /**
     * Closes the database connection
     *
     * @throws IOException thrown when an SQL or IO exception appears
     */
    public void close() throws IOException {
        database.close();
    }

}
