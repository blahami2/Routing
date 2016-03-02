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
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class FileSource implements DataSource {

    private final File file;
    private Scanner scanner;

    public FileSource( File file ) {
        this.file = file;
    }

    @Override
    public DataSource open() throws IOException {
        scanner = new Scanner( file );
        return this;
    }

    @Override
    public int read() throws IOException {
        return scanner.nextByte();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream( file );
    }

    @Override
    public DataSource close() throws IOException {
        scanner.close();
        return this;
    }

}
