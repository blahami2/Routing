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
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public interface Node {
    
    public Id getId();
    
    public Node createCopyWithNewId(Id id);

    /**
     * Getter for the coordinates of this node
     *
     * @return an instance of {@link Coordinates}
     */
    public Coordinates getCoordinates();
    
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
    public Node setLabel(String label);

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
     * Getter for the predecessor edge of this node (incoming edge as a part of the route)
     *
     * @return an instance of {@link Edge}
     */
    public Edge getPredecessorEdge();

    /**
     * Setter for the predecessor edge of this node (incoming edge as a part of the route)
     *
     * @param predecessorEdge an instance of {@link Edge}
     * @return this instance
     */
    public Node setPredecessorEdge( Edge predecessorEdge );

    public static class Id implements Serializable {

        private static int counter = 0;

        public static Id generateId() {
            return new Id( counter++ );
        }

        public static Id createId( int id ) {
            return new Id( id );
        }

        private final int id;

        private Id( int id ) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + this.id;
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
            return "Id{"  + id + '}';
        }

    }
}
