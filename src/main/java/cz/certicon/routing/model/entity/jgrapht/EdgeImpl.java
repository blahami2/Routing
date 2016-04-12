/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.jgrapht;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.common.SimpleEdge;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link Edge} using a {@link SimpleEdge}
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
class EdgeImpl extends SimpleEdge {

    public EdgeImpl( Id id, Node sourceNode, Node targetNode ) {
        super( id, sourceNode, targetNode );
    }

    public EdgeImpl( Id id, Node sourceNode, Node targetNode, Distance distance ) {
        super( id, sourceNode, targetNode, distance );
    }

    @Override
    protected Edge createNew( Id id, Node sourceNode, Node targetNode, Distance length ) {
        return new EdgeImpl( id, sourceNode, targetNode, length );
    }

    @Override
    public List<Coordinates> getCoordinates() {
        List<Coordinates> coords = new ArrayList<>( super.getCoordinates() );
        return coords;
    }
}
