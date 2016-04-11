/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph;

import cz.certicon.routing.data.Reader;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;

/**
 * An interface for {@link Graph} reading using a {@link Reader} interface.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface GraphReader extends Reader<Pair<GraphEntityFactory,DistanceFactory>,Graph> {

}
