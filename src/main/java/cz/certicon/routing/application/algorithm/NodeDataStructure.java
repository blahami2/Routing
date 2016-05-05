/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

/**
 * The root interface for various data structures used by algorithms.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface NodeDataStructure<T> {

    /**
     * Extract node with the minimal distance. Should be as fast as possible.
     *
     * @return node with the minimal distance
     */
    public T extractMin();

    /**
     * Adds node to the structure.
     *
     * @param node node to be added
     * @param value value to be associated with the node
     */
    public void add( T node, double value );

    /**
     * Removes node from the structure.
     *
     * @param node node to be removed
     */
    public void remove( T node );

    /**
     * Notifies the structure about distance change (invoking so called
     * decrease-key operation).
     *
     * @param node node to change
     * @param value value to be associated with the node
     */
    public void notifyDataChange( T node, double value );

    /**
     * Wipes out the data from this structure.
     */
    public void clear();

    /**
     * Returns true or false whether this structure contains nodes or not.
     *
     * @return boolean value
     */
    public boolean isEmpty();

    /**
     * Returns amount of nodes left in the structure.
     *
     * @return integer value
     */
    public int size();

    public boolean contains( T node );
}
