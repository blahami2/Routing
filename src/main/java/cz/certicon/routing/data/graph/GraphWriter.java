/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph;

import cz.certicon.routing.model.entity.Graph;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface GraphWriter {

    public GraphWriter open() throws IOException;

    public GraphWriter write( Graph graph ) throws IOException;

    public GraphWriter close() throws IOException;
}
