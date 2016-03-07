/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.neighbourlist;

import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class NeighbourListGraph implements Graph {

    private final Set<Node> nodes;
    private final Set<Edge> edges;

    public NeighbourListGraph() {
        this.nodes = new HashSet<>();
        this.edges = new HashSet<>();
    }

    @Override
    public Set<Node> getNodes() {
        return nodes;
    }

    @Override
    public Set<Edge> getEdges() {
        return edges;
    }

    @Override
    public Graph addNode( Node node ) {
        nodes.add( node );
        return this;
    }

    @Override
    public Graph removeNode( Node node ) {
        nodes.remove( node );
        getEdgesOf( node ).stream().forEach( ( edge ) -> {
            edges.remove( edge );
        } );
        return this;
    }

    @Override
    public Graph addEdge( Edge edge ) {
        edges.add( edge );
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
        edges.add( e );
        safeType( sourceNode ).addEdge( e );
        safeType( targetNode ).addEdge( e );
        return this;
    }

    @Override
    public Graph removeEdge( Edge edge ) {
        safeType( edge.getSourceNode() ).removeEdge( edge );
        safeType( edge.getTargetNode() ).removeEdge( edge );
        edges.remove( edge );
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
        return getEdgesOf( node ).stream()
                .filter( ( edge ) -> ( !edge.getAttributes().isOneWay() || ( node.equals( edge.getTargetNode()) ) ) )
                .collect( Collectors.toSet() );
    }

    @Override
    public Set<Edge> getOutgoingEdgesOf( Node node ) {
        return getEdgesOf( node ).stream()
                .filter( ( edge ) -> ( !edge.getAttributes().isOneWay() || ( node.equals( edge.getSourceNode() ) ) ) )
                .collect( Collectors.toSet() );
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
}
