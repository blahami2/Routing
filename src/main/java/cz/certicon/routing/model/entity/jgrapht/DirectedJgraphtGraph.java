/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.jgrapht;

import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.DirectedGraph;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import java.util.Set;

/**
 * An implementation of {@link DirectedGraph} adapting {@link org.jgrapht.graph.DirectedMultigraph} object.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
class DirectedJgraphtGraph implements DirectedGraph {

    public final org.jgrapht.graph.DirectedMultigraph<Node, Edge> innerGraph;

    public DirectedJgraphtGraph( org.jgrapht.graph.DirectedMultigraph<Node, Edge> innerGraph ) {
        this.innerGraph = innerGraph;        
    }

    @Override
    public Graph addNode( Node node ) {
        innerGraph.addVertex( node );
        return this;
    }

    @Override
    public Graph removeNode( Node node ) {
        innerGraph.removeVertex( node );
        return this;
    }

    @Override
    public Graph addEdge( Edge edge ) {
        innerGraph.addEdge( edge.getSourceNode(), edge.getTargetNode(), edge );
//        if ( !edge.getAttributes().isOneWay() ) {
//            Edge opposite = edge.createCopyWithNewId( edge.getId() ).newNodes( edge.getTargetNode(), edge.getSourceNode() );
//            safeType( opposite).setReversed( true );
//            innerGraph.addEdge( opposite.getSourceNode(), opposite.getTargetNode(), opposite );
//        }
        return this;
    }

    @Override
    public Graph addEdge( Node sourceNode, Node targetNode, Edge edge ) {
        innerGraph.addEdge( sourceNode, targetNode, edge );
//        if ( !edge.getAttributes().isOneWay() ) {
//            Edge opposite = edge.createCopyWithNewId( edge.getId() ).newNodes( targetNode, sourceNode );
//            safeType( opposite).setReversed( true );
//            innerGraph.addEdge( targetNode, sourceNode, opposite );
//        }
        return this;
    }

    @Override
    public Graph removeEdge( Edge edge ) {
        innerGraph.removeEdge( edge );
        return this;
    }

    @Override
    public Node getSourceNodeOf( Edge edge ) {
        return innerGraph.getEdgeSource( edge );
    }

    @Override
    public Node getTargetNodeOf( Edge edge ) {
        return innerGraph.getEdgeTarget( edge );
    }

    @Override
    public Node getOtherNodeOf( Edge edge, Node node ) {
        Node s = innerGraph.getEdgeSource( edge );
        if ( s.equals( node ) ) {
            return innerGraph.getEdgeTarget( edge );
        }
        return s;
    }

    @Override
    public Set<Edge> getEdgesOf( Node node ) {
        return innerGraph.edgesOf( node );
    }

    @Override
    public Set<Edge> getIncomingEdgesOf( Node node ) {
        return innerGraph.incomingEdgesOf( node );
    }

    @Override
    public Set<Edge> getOutgoingEdgesOf( Node node ) {
        return innerGraph.outgoingEdgesOf( node );
    }

    @Override
    public int getDegreeOf( Node node ) {
        return innerGraph.inDegreeOf( node ) + innerGraph.outDegreeOf( node );
    }

    @Override
    public int getInDegreeOf( Node node ) {
        return innerGraph.inDegreeOf( node );
    }

    @Override
    public int getOutDegreeOf( Node node ) {
        return innerGraph.outDegreeOf( node );
    }

    @Override
    public Set<Node> getNodes() {
        return innerGraph.vertexSet();
    }

    @Override
    public Set<Edge> getEdges() {
        return innerGraph.edgeSet();
    }

    private NodeImpl safeType( Node node ) {
        return (NodeImpl) node;
    }

    private EdgeImpl safeType( Edge edge ) {
        return (EdgeImpl) edge;
    }
}
