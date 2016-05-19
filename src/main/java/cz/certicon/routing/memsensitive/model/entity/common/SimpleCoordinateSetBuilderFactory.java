/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.model.entity.CoordinateSetBuilder;
import cz.certicon.routing.model.entity.CoordinateSetBuilderFactory;
import cz.certicon.routing.model.entity.Coordinates;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleCoordinateSetBuilderFactory implements CoordinateSetBuilderFactory<Map<Long,List<Coordinates>>>{

    @Override
    public CoordinateSetBuilder<Map<Long, List<Coordinates>>> createCoordinateSetBuilder() {
        return new SimpleCoordinateSetBuilder();
    }
    
}
