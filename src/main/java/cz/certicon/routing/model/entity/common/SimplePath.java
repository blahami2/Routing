/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.utils.SpeedUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public abstract class SimplePath implements Path {

    private final LinkedList<Edge> edges;
    private final Graph graph;
    private Node sourceNode = null;
    private Node targetNode = null;

    public SimplePath( Graph graph, Node node, boolean isFirst ) {
        this.graph = graph;
        this.edges = new LinkedList<>();
        this.sourceNode = this.targetNode = node;
    }

    @Override
    public Path addEdge( Edge e ) {
        edges.add( e );
        return this;
    }

    @Override
    public Path addEdgeAsFirst( Edge e ) {
        edges.addFirst( e );
        sourceNode = e.getOtherNode( sourceNode );
        return this;
    }

    @Override
    public Path addEdgeAsLast( Edge e ) {
        edges.addLast( e );
        targetNode = e.getOtherNode( targetNode );
        return this;
    }

    @Override
    public Iterator<Edge> iterator() {
        return edges.iterator();
    }

    @Override
    public Node getSourceNode() {
        return sourceNode;
    }

    @Override
    public Node getTargetNode() {
        return targetNode;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public List<Edge> getEdges() {
        return new ArrayList<>( edges );
    }

    @Override
    public Distance getDistance() {
        Distance result = null;
        for ( Edge e : this ) {
            if ( result == null ) {
                result = e.getDistance();
            } else {
                result = result.add( e.getDistance() );
            }
        }
        return result;
    }

    @Override
    public double getLength() {
        return edges.stream().mapToDouble( edge -> edge.getAttributes().getLength() ).sum();
    }

    @Override
    public double getTime() {
        return edges.stream().mapToDouble( edge -> {
            double speed = SpeedUtils.kmphToMps( edge.getAttributes().getSpeed() );
            double length = edge.getAttributes().getLength();
            return length / speed;
        } ).sum();
    }

    @Override
    public Path connectWith( Path otherPath ) {
        for ( Edge edge : otherPath ) {
            addEdgeAsLast( edge );
        }
        return this;
    }

    @Override
    public int size() {
        return edges.size();
    }

    @Override
    public String toStringLabelPath() {
        StringBuilder sb = new StringBuilder();
        for ( Edge e : this ) {
            sb.append( e.getLabel() );
            sb.append( ":" );
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        final SimplePath other = (SimplePath) obj;
        if ( !Objects.equals( this.edges, other.edges ) ) {
            Iterator<Edge> thisIt = this.edges.iterator();
            Iterator<Edge> otherIt = other.edges.iterator();
            while ( thisIt.hasNext() ) {
                Edge a = thisIt.next();
                Edge b = otherIt.next();
                System.out.println( a );
                System.out.println( b );
                System.out.println( a.equals( b ) );
            }
            System.out.println( "edges not equal" );
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SimplePath{" + "distance=" + getDistance() + ", " + "edges=" + edges + '}';
    }
}
