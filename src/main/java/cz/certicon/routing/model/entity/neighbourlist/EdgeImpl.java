/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.neighbourlist;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.common.SimpleEdge;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
class EdgeImpl extends SimpleEdge {

    boolean isReversed = false;

    public EdgeImpl( Id id, Node sourceNode, Node targetNode ) {
        super( id, sourceNode, targetNode );
    }

    public EdgeImpl( Id id, Node sourceNode, Node targetNode, Distance distance ) {
        super( id, sourceNode, targetNode, distance );
    }

    @Override
    protected Edge createNew( Edge.Id id, Node sourceNode, Node targetNode, Distance length ) {
        return new EdgeImpl( id, sourceNode, targetNode, length );
    }

    public void setReversed( boolean isReversed ) {
        this.isReversed = isReversed;
    }

    @Override
    public List<Coordinate> getCoordinates() {
        List<Coordinate> coords = new ArrayList<>( super.getCoordinates() );
        if ( isReversed ) {
            Collections.reverse( coords );
        }
        return coords;
    }

}
