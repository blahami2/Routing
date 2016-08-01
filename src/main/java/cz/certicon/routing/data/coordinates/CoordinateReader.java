/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates;

import cz.certicon.routing.model.entity.CoordinateSetBuilderFactory;
import java.io.IOException;
import java.util.Iterator;

/**
 * Read-only interface for the edge coordinates
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface CoordinateReader {

    /**
     * Reads coordinates based on the given edge iterator. Creates a result
     * using the {@link CoordinateSetBuilderFactory}.
     *
     * @param <T> coordinates representation
     * @param coordinateSetBuilderFactory factory providing builder for the
     * coordinates representation
     * @param edgeIds iterator providing a sequence of edge ids as they go in
     * the path
     * @return coordinates
     * @throws IOException thrown when an IO exception occurs
     */
    public <T> T readCoordinates( CoordinateSetBuilderFactory<T> coordinateSetBuilderFactory, Iterator<Long> edgeIds ) throws IOException;

}
