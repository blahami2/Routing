/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.nodesearch;

import cz.certicon.routing.model.entity.Coordinate;

/**
 * Exception thrown when the two coordinates map to a single edge, then routing
 * is pointless and only a simple evaluation is necessary. This exception
 * therefore provides basic data about the situation for the application to
 * solve. This is not a critical exception - it can be solved in runtime.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class EvaluableOnlyException extends Exception {

    private final long edgeId;
    private final Coordinate source;
    private final Coordinate target;

    public EvaluableOnlyException( long edgeId, Coordinate source, Coordinate target ) {
        this.edgeId = edgeId;
        this.source = source;
        this.target = target;
    }

    public long getEdgeId() {
        return edgeId;
    }

    public Coordinate getSource() {
        return source;
    }

    public Coordinate getTarget() {
        return target;
    }
}
