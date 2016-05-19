/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.utility.iterator;

import java.util.Iterator;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class IntArrayIterator implements Iterator<Integer> {

    private final int[] array;
    private int position = -1;

    public IntArrayIterator( int[] array ) {
        this.array = array;
    }

    @Override
    public boolean hasNext() {
        return position + 1 < array.length;
    }

    @Override
    public Integer next() {
        return array[++position];
    }

}
