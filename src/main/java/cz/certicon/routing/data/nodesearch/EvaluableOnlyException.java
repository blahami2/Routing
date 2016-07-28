/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.nodesearch;

import cz.certicon.routing.model.entity.Coordinate;

/**
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
