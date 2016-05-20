/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.graph;

import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.model.entity.GraphBuilderFactory;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface GraphReader {

    public <T> T readGraph( GraphBuilderFactory<T> GraphBuilderFactory, DistanceType distanceType ) throws IOException;
}
