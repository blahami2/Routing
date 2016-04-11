/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.neighbourlist;

import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.common.SimpleNode;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Node;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of {@link Node} using a {@link SimpleNode}
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
class NodeImpl extends SimpleNode {

    private final Set<Edge> edges = new HashSet<>();

    public NodeImpl( Id id, Coordinate coordinates ) {
        super( id, coordinates );
    }

    public NodeImpl( Id id, double latitude, double longitude ) {
        super( id, latitude, longitude );
    }

    public Node addEdge( Edge edge ) {
        this.edges.add( edge );
        return this;
    }

    public Node removeEdge( Edge edge ) {
        this.edges.remove( edge );
        return this;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    @Override
    public Node createCopyWithNewId( Id id ) {
        NodeImpl node = (NodeImpl) super.createCopyWithNewId( id );
//        node.edges.addAll( edges );
        return node;
    }
    
    

    @Override
    protected Node createNew( Id id, Coordinate coordinates ) {
        return new NodeImpl(id, coordinates );
    }
    
    

}
