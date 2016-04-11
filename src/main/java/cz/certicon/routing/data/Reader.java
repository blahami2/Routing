/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import java.io.IOException;

/**
 * A generic reader interface for additional consistency.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <In> additional data for the reader (if it requires any)
 * @param <Out> output of the reader
 */
public interface Reader<In,Out> {

    /**
     * Opens the reader for reading.
     * 
     * @throws IOException exception when opening 
     */
    public void open() throws IOException;

    /**
     * Reads the input and returns the output
     * 
     * @param in additional data
     * @return read output
     * @throws IOException exception when reading 
     */
    public Out read(In in) throws IOException;
    
    /**
     * Closes the reader.
     * 
     * @throws IOException exception when closing 
     */
    public void close() throws IOException;
    
    /**
     * Returns true if the reader is open, false otherwise
     * 
     * @return true or false
     */
    public boolean isOpen();
}
