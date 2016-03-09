/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph.xml;

import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.graph.Reader;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public abstract class AbstractXmlReader implements Reader {

    private final DataSource source;

    public AbstractXmlReader( DataSource source ) {
        this.source = source;
    }

    @Override
    public void open() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
    
    protected DataSource getDataSource(){
        return source;
    }

}
