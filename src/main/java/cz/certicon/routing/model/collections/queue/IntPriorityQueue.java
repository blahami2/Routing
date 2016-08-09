/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.collections.queue;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface IntPriorityQueue {

    int extractMin();

    void decreaseKey( int element, double key );

    int findMin();

    void insert( int element, double key );

    void delete( int element );

    double minValue();

    int size();

    boolean isEmpty();

    void clear();

    boolean contains( int element );
}
