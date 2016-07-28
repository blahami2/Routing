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
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface CoordinateReader {

    public <T> T readCoordinates( CoordinateSetBuilderFactory<T> coordinateSetBuilderFactory, Iterator<Long> edgeIds ) throws IOException;

}
