/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.neighbourlist;

import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.DirectedGraph;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of {@link DirectedGraph} using neighbor lists.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
class DirectedNeighborListGraph implements DirectedGraph {

    private final Set<Node> nodes;
    private final Set<Edge> edges;

    public DirectedNeighborListGraph() {
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
        for ( Edge edge : getEdgesOf( node ) ) {
            edges.remove( edge );
        }
        return this;
    }

    @Override
    public Graph addEdge( Edge edge ) {
        edges.add( edge );
//        if ( !edge.getAttributes().isOneWay() ) {
//            Edge opposite = edge.createCopyWithNewId( edge.getId() ).newNodes( edge.getTargetNode(), edge.getSourceNode() );
//            safeType( opposite.getSourceNode() ).addEdge( opposite );
//            safeType( opposite ).setReversed( true );
//            edges.add( opposite );
//        }
        safeType( edge.getSourceNode() ).addEdge( edge );
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
//        if ( !edge.getAttributes().isOneWay() ) {
//            Edge opposite = edge.createCopyWithNewId( edge.getId() ).newNodes( targetNode, sourceNode );
//            safeType( opposite.getSourceNode() ).addEdge( opposite );
//            safeType( opposite ).setReversed( true );
//            edges.add( opposite );
//        }
        safeType( sourceNode ).addEdge( e );
        return this;
    }

    @Override
    public Graph removeEdge( Edge edge ) {
        edges.remove( edge );
        safeType( edge.getSourceNode() ).removeEdge( edge );
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
        Set<Edge> edgeSet = getIncomingEdgesOf( node );
        edgeSet.addAll( getOutgoingEdgesOf( node ) );
        return edgeSet;
    }

    @Override
    public Set<Edge> getIncomingEdgesOf( Node node ) {
        Set<Edge> edgeSet = new HashSet<>();
        for(Edge edge : edges){
            if(safeType( edge ).getTargetNode().equals( node )){
                edgeSet.add( edge );
            }
        }
        return edgeSet;
    }

    @Override
    public Set<Edge> getOutgoingEdgesOf( Node node ) {
        return safeType( node ).getEdges();
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