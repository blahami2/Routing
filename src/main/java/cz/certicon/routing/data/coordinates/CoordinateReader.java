/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates;

import cz.certicon.routing.data.Reader;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An interface for {@link Coordinate} reading (based on the set of edges) using a {@link Reader} interface.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface CoordinateReader extends Reader<Set<Edge>, Map<Edge, List<Coordinate>>> {

}
