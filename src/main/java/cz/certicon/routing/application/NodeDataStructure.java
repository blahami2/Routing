/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application;

import java.util.Iterator;

/**
 * The root interface for various data structures used by algorithms.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> node type
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

    /**
     * Returns true if the structure contains the given node.
     * 
     * @param node element whose presence is to be tested
     * @return true if this structure contains the specified element
     */
    public boolean contains( T node );
    
    /**
     * Returns node with the minimal value. Does not remove it.
     * 
     * @return node with the minimal value
     */
    public T peek();
    
    /**
     * Returns the minimal value in this structure
     * 
     * @return the minimal value
     */
    public double minValue();
    
    /**
     * Returns iterator over all the elements in this structure
     * 
     * @return iterator over the elements
     */
    public Iterator<T> iterator();
}
