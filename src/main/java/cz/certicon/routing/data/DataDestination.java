/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The root interface for data export destination.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface DataDestination {

    /**
     * Opens the channel for writing into the destination.
     *
     * @return this instance
     * @throws IOException thrown when an error appears while opening
     */
    public DataDestination open() throws IOException;

    /**
     * Writes data into the destination.
     *
     * @param str data to be written
     * @return this instance
     * @throws IOException thrown when an error appears while writing
     */
    public DataDestination write( String str ) throws IOException;

    /**
     * Returns output stream of this destination
     * 
     * @return an instance of {@link OutputStream}
     * @throws IOException 
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * Closing the channel.
     *
     * @return this instance
     * @throws IOException thrown when an error appears while closing
     */
    public DataDestination close() throws IOException;

    /**
     * Flushes the data into the destination, clears the channel.
     *
     * @return this instance
     * @throws IOException thrown when an error appears while flushing
     */
    public DataDestination flush() throws IOException;
}
