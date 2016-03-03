/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.graph.GraphIoFactory;
import cz.certicon.routing.data.graph.GraphReader;
import cz.certicon.routing.data.graph.GraphWriter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlGraphIoFactory implements GraphIoFactory {

    @Override
    public GraphReader createGraphReader( DataSource dataSource ) {
        return new XmlGraphReader( dataSource );
    }

    @Override
    public GraphWriter createGraphWriter( DataDestination dataDestination ) {
        return new XmlGraphWriter( dataDestination );
    }

}
