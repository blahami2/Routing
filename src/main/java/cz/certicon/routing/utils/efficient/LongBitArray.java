/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.efficient;

import cz.certicon.routing.utils.EffectiveUtils;

/**
 * Implementation of {@link BitArray} using long as bottom layer data type.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class LongBitArray implements BitArray {

    private final static int ADDRESS_BITS_PER_WORD = 6;
    private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;

    private static final long WORD_MASK = 0xffffffffffffffffL;

    private long[] array;
    private long[] resetArray;
    private int size;

    public LongBitArray() {
    }

    public LongBitArray( int size ) {
        init( size );
    }

    @Override
    public final void init( int size ) {
        this.size = size;
        int arraySize = wordIndex( size - 1 ) + 1;
        array = new long[arraySize];
        resetArray = new long[arraySize];
    }

    @Override
    public void set( int index, boolean value ) {
        if ( value ) {
            array[wordIndex( index )] |= ( 1L << index );
        } else {
            array[wordIndex( index )] &= ~( 1L << index );
        }
    }

    @Override
    public boolean get( int index ) {
        return ( ( array[wordIndex( index )] & ( 1L << index ) ) != 0 );
    }

    private static int wordIndex( int bitIndex ) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    @Override
    public void clear() {
        EffectiveUtils.copyArray( resetArray, array );
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "[" );
        for ( int i = 0; i < size; i++ ) {
            sb.append( get( i ) ? "1" : "0" ).append( ", " );
        }
        if ( sb.length() > 3 ) {
            sb.replace( sb.length() - 2, sb.length(), "" );
        }
        sb.append( "]" );
        return sb.toString();
    }

}
