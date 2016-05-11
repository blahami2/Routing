/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Node;
import java.util.Objects;

/**
 * A basic implementation of {@link Node}
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public abstract class SimpleNode implements Node {

    private final Coordinates coordinates;
    private final Id id;
    private long osmId = -1;
    private Distance distance;
    private Edge predecessorEdge;
    private String label;

    private static int charCounter = 0;

    public SimpleNode( Id id, Coordinates coordinates ) {
        this.coordinates = coordinates;
        this.id = id;
        this.distance = null;
        this.predecessorEdge = null;
//        this.label = generateLabel( charCounter++ );
        this.label = Long.toString( id.getValue() );
    }

    public SimpleNode( Id id, double latitude, double longitude ) {
        this.coordinates = new Coordinates( latitude, longitude );
        this.id = id;
        this.distance = null;
        this.predecessorEdge = null;
//        this.label = generateLabel( charCounter++ );
        this.label = Long.toString( id.getValue() );
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public long getOsmId() {
        return osmId;
    }

    @Override
    public void setOsmId( long osmId ) {
        this.osmId = osmId;
    }

    @Override
    public Node createCopyWithNewId( Id id ) {
        Node node = createNew( id, coordinates );
        node.setDistance( distance );
        node.setPredecessorEdge( predecessorEdge );
        node.setLabel( label );
        node.setOsmId( osmId );
        return node;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Node setLabel( String label ) {
        this.label = label;
        return this;
    }

    @Override
    public Coordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public Distance getDistance() {
        return distance;
    }

    @Override
    public Node setDistance( Distance distance ) {
        this.distance = distance;
        return this;
    }

    @Override
    public Edge getPredecessorEdge() {
        return predecessorEdge;
    }

    @Override
    public Node setPredecessorEdge( Edge predecessorEdge ) {
        this.predecessorEdge = predecessorEdge;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode( this.coordinates );
        hash = 59 * hash + Objects.hashCode( this.id );
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
        final SimpleNode other = (SimpleNode) obj;
        if ( !Objects.equals( this.coordinates, other.coordinates ) ) {
            return false;
        }
        if ( !Objects.equals( this.id, other.id ) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SimpleNode{" + "id=" + id + ", coordinates=" + coordinates + ", distance=" + distance + ", hasPredecessor=" + ( getPredecessorEdge() != null ) + '}';
    }

    private String generateLabel( int counter ) {
        int c = counter;
        StringBuilder sb = new StringBuilder();
        do {
            char current = (char) ( 'A' + ( c % 26 ) );
            c /= 26;
//            System.out.println( "current = " + current );
            sb.append( String.format( "%c", current ) );
        } while ( c > 0 );
//        System.out.println( "generating label: " + sb.toString() );
        return sb.toString();
    }

    abstract protected Node createNew( Id id, Coordinates coordinates );

}
