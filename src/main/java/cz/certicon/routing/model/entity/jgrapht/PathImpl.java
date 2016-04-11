/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.jgrapht;

import cz.certicon.routing.model.entity.common.SimplePath;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;

/**
 * An implementation of {@link Path} using a {@link SimplePath}
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
class PathImpl extends SimplePath {

    public PathImpl( Graph graph, Node node, boolean isFirst ) {
        super( graph, node, isFirst );
    }
    
}
