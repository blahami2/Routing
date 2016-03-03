/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.DataSource;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface GraphIoFactory {
    public GraphReader createGraphReader(DataSource dataSource);
    public GraphWriter createGraphWriter(DataDestination dataDestination);
}
