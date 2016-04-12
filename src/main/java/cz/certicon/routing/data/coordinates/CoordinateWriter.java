/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates;

import cz.certicon.routing.data.Writer;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import java.util.List;
import java.util.Map;

/**
 * An interface for {@link Coordinates} writing (using an Edge/Coordinate map) using a {@link Writer} interface.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface CoordinateWriter extends Writer<Map<Edge,List<Coordinates>>> {
}
