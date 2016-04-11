/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import java.io.IOException;
import java.io.InputStream;

/**
 * The root interface for data source.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface DataSource {

    /**
     * Opens the channel for reading from the source.
     *
     * @return this instance
     * @throws IOException thrown when an error appears while opening
     */
    public DataSource open() throws IOException;

     /**
     * Reads data from the source.
     *
     * @return next byte
     * @throws IOException thrown when an error appears while reading
     */
    public int read() throws IOException;
    
    /**
     * Returns input stream of this source
     * 
     * @return an instance of {@link InputStream}
     * @throws IOException 
     */
    public InputStream getInputStream() throws IOException;
    
    /**
     * Closing the channel.
     *
     * @return this instance
     * @throws IOException thrown when an error appears while closing
     */
    public DataSource close() throws IOException;
}
