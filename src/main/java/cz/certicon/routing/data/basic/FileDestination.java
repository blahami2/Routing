/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.basic;

import cz.certicon.routing.data.DataDestination;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * An implementation of {@link DataDestination} using a {@link File}
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class FileDestination implements DataDestination {

    private final File file;
    private PrintWriter writer;

    public FileDestination( File file ) {
        this.file = file;
    }

    @Override
    public DataDestination open() throws IOException {
        OutputStream os = new BufferedOutputStream( new FileOutputStream( file ) );
        writer = new PrintWriter( os );
        return this;
    }

    @Override
    public DataDestination write( String str ) throws IOException {
        writer.print( str );
        return this;
    }

    @Override
    public DataDestination close() throws IOException {
        writer.close();
        return this;
    }

    @Override
    public DataDestination flush() throws IOException {
        writer.flush();
        return this;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream( file );
    }

}
