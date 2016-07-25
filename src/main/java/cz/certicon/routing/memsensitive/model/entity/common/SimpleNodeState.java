/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.memsensitive.model.entity.NodeState;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleNodeState implements NodeState {

    private final int node;
    private final int edge;

    public SimpleNodeState( int node, int edge ) {
        this.node = node;
        this.edge = edge;
    }

    @Override
    public int getNode() {
        return node;
    }

    @Override
    public int getEdge() {
        return edge;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.node;
        hash = 37 * hash + this.edge;
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final SimpleNodeState other = (SimpleNodeState) obj;
        if ( this.node != other.node ) {
            return false;
        }
        if ( this.edge != other.edge ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{" + node + "," + edge + "}";
    }

}
