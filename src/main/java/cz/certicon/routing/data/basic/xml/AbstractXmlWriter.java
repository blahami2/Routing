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
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
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
        openedWrite( out );
    }

    abstract protected void openedWrite( Out out ) throws IOException;

    @Override
    public boolean isOpen() {
        return isOpen;
    }
}
