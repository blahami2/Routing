/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph;

import cz.certicon.routing.data.Writer;
import cz.certicon.routing.model.entity.Graph;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface GraphWriter extends Writer<Graph> {

    @Override
    public void open() throws IOException;

    @Override
    public void write( Graph graph ) throws IOException;

    @Override
    public void close() throws IOException;
}
