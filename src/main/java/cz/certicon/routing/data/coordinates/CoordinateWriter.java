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
public interface CoordinateWriter {

    public CoordinateWriter open() throws IOException;

    public CoordinateWriter write( Edge edge, List<Coordinate> coordinates ) throws IOException;

    public CoordinateWriter close() throws IOException;
}
