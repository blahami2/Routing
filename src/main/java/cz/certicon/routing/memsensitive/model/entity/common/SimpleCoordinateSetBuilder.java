/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.model.entity.CoordinateSetBuilder;
import cz.certicon.routing.model.entity.Coordinates;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleCoordinateSetBuilder implements CoordinateSetBuilder<Map<Long, List<Coordinates>>> {

    private final Map<Long, List<Coordinates>> coordinatesMap;

    public SimpleCoordinateSetBuilder() {
        this.coordinatesMap = new HashMap<>();
    }

    @Override
    public void addCoordinates( long edgeId, List<Coordinates> coordinates ) {
        coordinatesMap.put( edgeId, coordinates );
    }

    @Override
    public Map<Long, List<Coordinates>> build() {
        return coordinatesMap;
    }

}
