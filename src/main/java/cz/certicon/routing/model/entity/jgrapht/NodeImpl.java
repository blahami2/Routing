/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.jgrapht;

import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.common.SimpleNode;
import cz.certicon.routing.model.entity.Node;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
class NodeImpl extends SimpleNode {

    public NodeImpl( Id id, Coordinate coordinates ) {
        super( id, coordinates );
    }

    public NodeImpl( Id id, double latitude, double longitude ) {
        super( id, latitude, longitude );
    }

    @Override
    protected Node createNew( Id id, Coordinate coordinates ) {
        return new NodeImpl( id, coordinates );
    }

}
