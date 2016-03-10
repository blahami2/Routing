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
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
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
        return openedRead( in );
    }
    
    protected abstract Out openedRead(In in) throws IOException;

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
