/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.path;

import cz.certicon.routing.memsensitive.algorithm.Route;
import cz.certicon.routing.memsensitive.model.entity.PathBuilder;
import cz.certicon.routing.model.entity.Coordinate;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface PathReader<G> {

    public <T> T readPath( PathBuilder<T, G> pathBuilder, G graph, Route route, Coordinate origSource, Coordinate origTarget ) throws IOException;
}
