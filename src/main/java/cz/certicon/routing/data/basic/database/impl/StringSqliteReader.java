/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.basic.database.impl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class StringSqliteReader {

    private final AbstractSqliteDatabase<ResultSet, String> database;

    public StringSqliteReader( Properties connectionProperties ) {
        this.database = new AbstractSqliteDatabase<ResultSet, String>( connectionProperties ) {
            @Override
            protected ResultSet checkedRead( String in ) throws SQLException {
//                System.out.println( "Gonna return resultset" );
                Statement st = getStatement();
//                System.out.println( "statement closed = " + st.isClosed() );
                ResultSet rs = st.executeQuery( in );
//                System.out.println( "Returning resultset => closed = " + rs.isClosed() );
                return rs;
            }

            @Override
            protected void checkedWrite( ResultSet in ) throws SQLException {
                throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }

    public ResultSet read( String sql ) throws IOException {
        return database.read( sql );
    }

    public void close() throws IOException {
        database.close();
    }

}
