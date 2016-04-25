/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.neighbourlist;

import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of {@link Graph} using neighbor lists.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class NeighborListGraph implements Graph {

    private final Map<Node.Id, Node> nodes;
    private final Map<Edge.Id, Edge> edges;

    public NeighborListGraph() {
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
    }

    @Override
    public Collection<Node> getNodes() {
        return nodes.values();
    }

    @Override
    public Collection<Edge> getEdges() {
        return edges.values();
    }

    @Override
    public Graph addNode( Node node ) {
        nodes.put( node.getId(), node );
        return this;
    }

    @Override
    public Graph removeNode( Node node ) {
        nodes.remove( node.getId() );
        for ( Edge edge : getEdgesOf( node ) ) {
            edges.remove( edge.getId() );
        }
        return this;
    }

    @Override
    public Graph addEdge( Edge edge ) {
        edges.put( edge.getId(), edge );
        safeType( edge.getSourceNode() ).addEdge( edge );
        safeType( edge.getTargetNode() ).addEdge( edge );
        return this;
    }

    @Override
    public Graph addEdge( Node sourceNode, Node targetNode, Edge edge ) {
        Edge e;
        if ( !edge.getSourceNode().equals( sourceNode ) || !edge.getTargetNode().equals( targetNode ) ) {
            e = edge.newNodes( sourceNode, targetNode );
        } else {
            e = edge;
        }
        edges.put( e.getId(), e );
        safeType( sourceNode ).addEdge( e );
        safeType( targetNode ).addEdge( e );
        return this;
    }

    @Override
    public Graph removeEdge( Edge edge ) {
        safeType( edge.getSourceNode() ).removeEdge( edge );
        safeType( edge.getTargetNode() ).removeEdge( edge );
        edges.remove( edge.getId() );
        return this;
    }

    @Override
    public Node getSourceNodeOf( Edge edge ) {
        return safeType( edge ).getSourceNode();
    }

    @Override
    public Node getTargetNodeOf( Edge edge ) {
        return safeType( edge ).getTargetNode();
    }

    @Override
    public Node getOtherNodeOf( Edge edge, Node node ) {
        Node s = getSourceNodeOf( edge );
        if ( s.equals( node ) ) {
            return getTargetNodeOf( edge );
        }
        return s;
    }

    @Override
    public Set<Edge> getEdgesOf( Node node ) {
        return safeType( node ).getEdges();
    }

    @Override
    public Set<Edge> getIncomingEdgesOf( Node node ) {
        return getEdgesOf( node );
//        Set<Edge> edgeSet = new HashSet<>();
//        for ( Edge edge : getEdgesOf( node ) ) {
//            if ( node.equals( edge.getTargetNode() ) ) {
//                edgeSet.add( edge );
//            }
//        }
//        return edgeSet;
    }

    @Override
    public Set<Edge> getOutgoingEdgesOf( Node node ) {
        return getEdgesOf( node );
//        Set<Edge> edgeSet = new HashSet<>();
//        for ( Edge edge : getEdgesOf( node ) ) {
//            if ( node.equals( edge.getSourceNode() ) ) {
//                edgeSet.add( edge );
//            }
//        }
//        return edgeSet;
    }

    @Override
    public int getDegreeOf( Node node ) {
        return getEdgesOf( node ).size();
    }

    @Override
    public int getInDegreeOf( Node node ) {
        return getIncomingEdgesOf( node ).size();
    }

    @Override
    public int getOutDegreeOf( Node node ) {
        return getOutgoingEdgesOf( node ).size();
    }

    private NodeImpl safeType( Node node ) {
        return (NodeImpl) node;
    }

    private EdgeImpl safeType( Edge edge ) {
        return (EdgeImpl) edge;
    }

    @Override
    public Node getNode( Node.Id id ) {
        return nodes.get( id );
    }

    @Override
    public Edge getEdge( Edge.Id id ) {
        return edges.get( id );
    }
}
