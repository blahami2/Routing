/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates;

import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface CoordinateReader {

    public CoordinateReader open() throws IOException;

    public List<Coordinate> findCoordinates( Edge edge ) throws IOException;

    public CoordinateReader close() throws IOException;
}
