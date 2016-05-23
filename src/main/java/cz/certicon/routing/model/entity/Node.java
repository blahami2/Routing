/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.application.algorithm.Distance;
import java.io.Serializable;

/**
 * The root interface for graph node
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Node {

    /**
     * Getter for id
     *
     * @return an instance of {@link Id}
     */
    public Id getId();

    public long getOsmId();

    public void setOsmId( long osmId );

    /**
     * Creates a copy of this node. Should the node be directly associated with
     * other nodes via edges, those edges are NOT a part of the new copy (it is
     * assumed that other nodes will be changed as well and therefore the
     * current edge set becomes invalid). Should such copying be required, it
     * had to be done manually.
     *
     * @param id an instance of {@link Id} representing id of this node
     * @return a new copy
     */
    public Node createCopyWithNewId( Id id );

    /**
     * Getter for the coordinates of this node
     *
     * @return an instance of {@link Coordinate}
     */
    public Coordinate getCoordinates();

    /**
     * Getter for the label of this node
     *
     * @return an instance of {@link String}
     */
    public String getLabel();

    /**
     * Setter for the label of this node
     *
     * @param label {@link String} representing the node label
     * @return this instance
     */
    public Node setLabel( String label );

    /**
     * Getter for the distance of this node
     *
     * @return an instance of {@link Distance} representing the node's distance
     */
    public Distance getDistance();

    /**
     * Setter for the distance of this node
     *
     * @param distance an instance of {@link Distance}
     * @return this instance
     */
    public Node setDistance( Distance distance );

    /**
     * Getter for the predecessor edge of this node (incoming edge as a part of
     * the route)
     *
     * @return an instance of {@link Edge}
     */
    public Edge getPredecessorEdge();

    /**
     * Setter for the predecessor edge of this node (incoming edge as a part of
     * the route)
     *
     * @param predecessorEdge an instance of {@link Edge}
     * @return this instance
     */
    public Node setPredecessorEdge( Edge predecessorEdge );

    public static class Id implements Serializable, Comparable<Id> {

        private static long counter = 0;

        public static Id generateId() {
            return new Id( counter++ );
        }

        public static Id createId( long id ) {
            return new Id( id );
        }

        public static String toString( Id id ) {
            return id.id + "";
        }

        public static Id fromString( String str ) {
            return createId( Long.parseLong( str ) );
        }

        private final long id;

        private Id( long id ) {
            if(id > counter){
                counter = id + 1;
            }
            this.id = id;
        }

        public long getValue() {
            return id;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + (int) ( this.id ^ ( this.id >>> 32 ) );
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
            final Id other = (Id) obj;
            if ( this.id != other.id ) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Id{" + id + '}';
        }

        @Override
        public int compareTo( Id o ) {
            return Long.compare( this.id, o.id );
        }

    }
}
