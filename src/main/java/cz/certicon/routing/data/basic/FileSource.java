/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.basic;

import cz.certicon.routing.data.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * An implementation of {@link DataSource} using a {@link File}
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class FileSource implements DataSource {

    private final File file;
    private Scanner scanner;

    /**
     * Constructor
     *
     * @param file file
     */
    public FileSource( File file ) {
        this.file = file;
    }

    @Override
    public DataSource open() throws IOException {
        scanner = new Scanner( file, "UTF-8" );
        return this;
    }

    @Override
    public int read() throws IOException {
        if ( scanner == null ) {
            open();
        }
        return scanner.nextByte();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream( file );
    }

    @Override
    public DataSource close() throws IOException {
        if ( scanner != null ) {
            scanner.close();
        }
        return this;
    }

}
