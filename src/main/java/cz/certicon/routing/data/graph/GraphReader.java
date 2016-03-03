/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface GraphReader {

    public GraphReader open() throws IOException;

    public Graph load( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory ) throws IOException;

    public GraphReader close() throws IOException;
}
