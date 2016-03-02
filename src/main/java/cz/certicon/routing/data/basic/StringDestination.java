/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.basic;

import cz.certicon.routing.data.DataDestination;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public class StringDestination implements DataDestination {

    private final StringBuilder sb = new StringBuilder();

    @Override
    public DataDestination open() throws IOException {
        sb.delete( 0, sb.length() );
        return this;
    }

    @Override
    public DataDestination write( String str ) throws IOException {
        sb.append( str );
        return this;
    }

    @Override
    public DataDestination close() throws IOException {
        return this;
    }
    
    public String getResult(){
        return sb.toString();
    }

    @Override
    public DataDestination flush() throws IOException {
        sb.delete( 0, sb.length() );
        return this;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new StringOutputStream();
    }
    
    private class StringOutputStream extends OutputStream {
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        @Override
        public void write( int b ) throws IOException {
            os.write( b );
        }

        @Override
        public void close() throws IOException {
            super.close();
            sb.append( new String(os.toByteArray(), "UTF-8"));
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            sb.append( new String(os.toByteArray(), "UTF-8"));
        }
        
        
    }
}
