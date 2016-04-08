/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.datastructures;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.model.entity.Node;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class TrivialNodeDataStructure implements NodeDataStructure {

    private final List<Node> nodes;

    public TrivialNodeDataStructure() {
        this.nodes = new LinkedList<>();
    }

    @Override
    public Node extractMin() {
        if ( nodes.isEmpty() ) {
            throw new IllegalStateException( "NodeStructure is empty." );
        }
        Node min = nodes.get( 0 );
        for ( Node node : nodes ) {
            if ( node.getDistance().isLowerThan( min.getDistance() ) ) {
                min = node;
            }
        }
        nodes.remove( min );
        return min;
    }

    @Override
    public NodeDataStructure add( Node node ) {
        nodes.add( node );
        return this;
    }

    @Override
    public NodeDataStructure remove( Node node ) {
        nodes.remove( node );
        return this;
    }

    @Override
    public NodeDataStructure notifyDataChange( Node node ) {
        if ( !nodes.contains( node ) ) {
            add( node );
        }
        return this;
    }

    @Override
    public NodeDataStructure clear() {
        nodes.clear();
        return this;
    }

    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public int size() {
        return nodes.size();
    }

}
