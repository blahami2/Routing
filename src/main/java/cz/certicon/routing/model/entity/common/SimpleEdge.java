/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.application.algorithm.data.number.LengthDistanceFactory;
import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.EdgeAttributes;
import cz.certicon.routing.model.entity.Node;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A basic abstract implementation of {@link Edge}.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public abstract class SimpleEdge implements Edge {

    private static final double GRANULARITY_DIVISOR = 0.0005; // determines distance between coordinates in the path

    private final Node sourceNode;
    private final Node targetNode;
    private final Edge.Id id;
    private int speed;
    private Distance distance;
    private String label;
    private List<Coordinate> coordinates;
    private EdgeAttributes attributes;

    public SimpleEdge( Edge.Id id, Node sourceNode, Node targetNode ) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.id = id;
        this.label = generateLabel( sourceNode, targetNode );
        this.attributes = SimpleEdgeAttributes.builder().build();
        this.distance = new LengthDistanceFactory().createFromEdgeData( new SimpleEdgeData( sourceNode, targetNode, 50, false, 1 ) );
    }

    public SimpleEdge( Edge.Id id, Node sourceNode, Node targetNode, Distance distance ) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.id = id;
        this.distance = distance;
        this.label = generateLabel( sourceNode, targetNode );
        this.attributes = SimpleEdgeAttributes.builder().build();
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed( int speed ) {
        this.speed = speed;
    }

    @Override
    public EdgeAttributes getAttributes() {
        return attributes;
    }

    @Override
    public Edge setAttributes( EdgeAttributes attributes ) {
        this.attributes = attributes;
        return this;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Edge setLabel( String label ) {
        this.label = label;
        return this;
    }

    @Override
    public Distance getDistance() {
        return distance;
    }

    @Override
    public Edge setDistance( Distance distance ) {
        this.distance = distance;
        return this;
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
    public Node getOtherNode( Node node ) {
        if ( node.equals( sourceNode ) ) {
            return targetNode;
        }
        return sourceNode;
    }

    @Override
    public Edge createCopyWithNewId( Id id ) {
        Edge edge = createNew( id, sourceNode, targetNode, distance );
        edge.setCoordinates( coordinates );
        edge.setAttributes( attributes );
        edge.setLabel( label );
        return edge;
    }

    @Override
    public Edge newSourceNode( Node sourceNode ) {
        return fillCopy( createNew( getId(), sourceNode, getTargetNode(), getDistance() ) );
    }

    @Override
    public Edge newTargetNode( Node targetNode ) {
        return fillCopy( createNew( getId(), getSourceNode(), targetNode, getDistance() ) );
    }

    @Override
    public Edge newNodes( Node sourceNode, Node targetNode ) {
        return fillCopy( createNew( getId(), sourceNode, targetNode, getDistance() ) );
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode( this.id );
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
        final SimpleEdge other = (SimpleEdge) obj;
        if ( !Objects.equals( this.sourceNode, other.sourceNode ) ) {
            return false;
        }
        if ( !Objects.equals( this.targetNode, other.targetNode ) ) {
            return false;
        }
        if ( !Objects.equals( this.id, other.id ) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SimpleEdge{" + "id=" + id + ", distance=" + distance + ", \n\tsourceNode=" + sourceNode + ", \n\ttargetNode=" + targetNode + ",\n\tattributes=" + attributes + '}';
    }

    @Override
    public List<Coordinate> getCoordinates() {
        if ( coordinates != null ) {
            return coordinates;
        }
        List<Coordinate> coords = Arrays.asList( getSourceNode().getCoordinates(), getTargetNode().getCoordinates() );
//        int count = (int) ( Math.ceil( CoordinateUtils.calculateDistance( graph.getSourceNodeOf( this ).getCoordinates(), graph.getTargetNodeOf( this ).getCoordinates() ) / GRANULARITY_DIVISOR ) + 0.1 );
//        List<Coordinates> coords = CoordinateUtils.divideCoordinates( graph.getSourceNodeOf( this ).getCoordinates(), graph.getTargetNodeOf( this ).getCoordinates(), count );
        return coords;
    }

    @Override
    public Edge setCoordinates( List<Coordinate> coordinates ) {
        this.coordinates = coordinates;
        return this;
    }

    private String generateLabel( Node sourceNode, Node targetNode ) {
        return sourceNode.getLabel() + "-" + targetNode.getLabel();
    }

    private Edge fillCopy( Edge edge ) {
        edge.setCoordinates( coordinates );
        edge.setAttributes( attributes );
        edge.setLabel( label );
        return edge;
    }

    abstract protected Edge createNew( Edge.Id id, Node sourceNode, Node targetNode, Distance length );

}
