/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.nodesearch;

import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.NodeSetBuilderFactory;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface NodeSearcher {

    public <T> T findClosestNodes( NodeSetBuilderFactory<T> nodeSetBuilderFactory, Coordinate source, Coordinate target ) throws IOException, EvaluableOnlyException;

    public static enum SearchFor {
        SOURCE, TARGET;
    }
}
