/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.jgrapht;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.model.entity.common.SimpleEdge;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Node;

/**
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
class EdgeImpl extends SimpleEdge {

    public EdgeImpl( Node sourceNode, Node targetNode, Distance distance ) {
        super( sourceNode, targetNode, distance );
    }

    public EdgeImpl( Node sourceNode, Node targetNode ) {
        super( sourceNode, targetNode );
    }
    
    @Override
    protected Edge createNew( Node sourceNode, Node targetNode, Distance length ) {
        return new EdgeImpl(sourceNode, targetNode, length );
    }

}
