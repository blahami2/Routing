/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.collections.array;

/**
 * Efficient array of bits.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface BitArray {

    /**
     * Initializes the array with the given size
     *
     * @param size given size
     */
    public void init( int size );

    /**
     * Sets the value at the given index to the given value
     *
     * @param index target index
     * @param value given value
     */
    public void set( int index, boolean value );

    /**
     * Returns value at the given index
     *
     * @param index target index
     * @return value at the index
     */
    public boolean get( int index );

    /**
     * Returns size of this array
     *
     * @return size of this array
     */
    public int size();

    /**
     * Clears this array (sets all to false)
     */
    public void clear();
}
