/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.utility.iterator;

import java.util.Iterator;

/**
 * Simple iterator of primitive double array
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DoubleArrayIterator implements Iterator<Double> {

    private final double[] array;
    private int position = -1;

    public DoubleArrayIterator( double[] array ) {
        this.array = array;
    }

    @Override
    public boolean hasNext() {
        return position + 1 < array.length;
    }

    @Override
    public Double next() {
        return array[++position];
    }
}
