/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.basic.xml;

import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.Reader;
import java.io.IOException;

/**
 * An abstract implementation of the Reader interfaces for the XML. Encapsulates access, controls the state before reading and opens the stream if necessary.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <In> additional data
 * @param <Out> actual output (of reading)
 */
public abstract class AbstractXmlReader<In,Out> implements Reader<In,Out> {

    private final DataSource source;
    private boolean isOpen = false;

    public AbstractXmlReader( DataSource source ) {
        this.source = source;
    }

    @Override
    public void open() throws IOException {
        this.isOpen = true;
    }

    @Override
    public Out read( In in ) throws IOException {
        if(!isOpen){
            open();
        }
        return checkedRead( in );
    }
        
    /**
     * Checks the state before reading and opens the source if necessary.
     * 
     * @param in additional data (passed)
     * @return the read output
     * @throws IOException reading exception
     */
    protected abstract Out checkedRead(In in) throws IOException;

    @Override
    public void close() throws IOException {
        if(isOpen){
            isOpen = false;
        }
    }
    
    protected DataSource getDataSource(){
        return source;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

}
