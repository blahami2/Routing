/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity;

import cz.certicon.routing.memsensitive.model.entity.common.SimpleNodeState;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface NodeState {

    public int getNode();

    public int getEdge();

    @Override
    public boolean equals( Object obj );

    @Override
    public int hashCode();

    public static class Factory {

        public static NodeState newInstance( int node, int edge ) {
            return new SimpleNodeState( node, edge );
        }
    }
}
