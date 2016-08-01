/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph;

import cz.certicon.routing.model.entity.DistanceType;
import cz.certicon.routing.model.entity.GraphBuilderFactory;
import java.io.IOException;

/**
 * Read-only interface for the graph
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface GraphReader {

    /**
     * Reads the graph based on the given distance type (metric). Creates a
     * result using a builder provided by the {@link GraphBuilderFactory}.
     *
     * @param <T> graph type
     * @param GraphBuilderFactory factory providing builder for the graph
     * @param distanceType the metric
     * @return graph
     * @throws IOException thrown when an IO exception occurs
     */
    public <T> T readGraph( GraphBuilderFactory<T> GraphBuilderFactory, DistanceType distanceType ) throws IOException;
}
