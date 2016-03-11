/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.osm;

import cz.certicon.routing.data.DataDestination;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class TemporaryWriter {

    private static final String ROOT = "root";

    private final DataDestination destination;
    private OutputStream output;
    private XMLStreamWriter writer;
    private boolean isOpen = false;
    private final Map<Class, Writer> writers = new HashMap<>();

    public TemporaryWriter( DataDestination destination ) {
        this.destination = destination;
    }

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

    public <T> void write( T in ) throws IOException {
        if(!isOpen){
            open();
        }
        try {
            writers.get( in.getClass() ).write( getWriter(), in );
        } catch ( XMLStreamException ex ) {
            throw new IOException(ex );
        }
    }

    public void addWriter( Class cls, Writer writer ) {
        writers.put( cls, writer );
    }

    public boolean isOpen() {
        return isOpen;
    }
    
    public static interface Writer<Out> {
        public void write(XMLStreamWriter streamWriter, Out out) throws XMLStreamException;
    }
}
