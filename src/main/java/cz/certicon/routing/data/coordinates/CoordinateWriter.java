/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates;

import cz.certicon.routing.data.Writer;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface CoordinateWriter extends Writer<Map<Edge,List<Coordinate>>> {
}
