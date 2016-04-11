/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.basic.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.Writer;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * An abstract implementation of the {@link Writer} interfaces for the XML. Encapsulates access, controls the state before writing and opens the stream if necessary.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <Out> actual input (to be written)
 */
public abstract class AbstractXmlWriter<Out> implements Writer<Out> {

    private static final String ROOT = "root";

    private final DataDestination destination;
    private OutputStream output;
    private XMLStreamWriter writer;
    private boolean isOpen = false;

    public AbstractXmlWriter( DataDestination destination ) {
        this.destination = destination;
    }

    @Override
    public void open() throws IOException {
        output = destination.getOutputStream();
        XMLOutputFactory xmlOutFact = XMLOutputFactory.newInstance();
        try {
            writer = xmlOutFact.createXMLStreamWriter( output );
            writer.writeStartDocument();
            writer.writeStartElement( ROOT );
        } catch ( XMLStreamException ex ) {
            throw new IOException( ex );
        }
        isOpen = true;
    }

    @Override
    public void close() throws IOException {
        if ( isOpen ) {
            try {
                writer.writeEndElement();
                writer.writeEndDocument();
                writer.close();
            } catch ( XMLStreamException ex ) {
                throw new IOException( ex );
            }
            output.close();
            isOpen = false;
        }
    }

    protected XMLStreamWriter getWriter() {
        return writer;
    }

    @Override
    public void write( Out out ) throws IOException {
        if ( !isOpen ) {
            open();
        }
        checkedWrite( out );
    }
    
    /**
     * Checks the state before writing and opens the source if necessary.
     * 
     * @param out output to be written
     * @throws IOException reading exception
     */
    abstract protected void checkedWrite( Out out ) throws IOException;

    @Override
    public boolean isOpen() {
        return isOpen;
    }
}
