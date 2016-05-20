/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class EffectiveUtils {

    public static void fillArray( int[] array, int value ) {
        int len = array.length;
        if ( len > 0 ) {
            array[0] = value;
        }
        for ( int i = 1; i < len; i += i ) {
            System.arraycopy( array, 0, array, i, ( ( len - i ) < i ) ? ( len - i ) : i );
        }
    }

    public static void fillArray( double[] array, double value ) {
        int len = array.length;
        if ( len > 0 ) {
            array[0] = value;
        }
        for ( int i = 1; i < len; i += i ) {
            System.arraycopy( array, 0, array, i, ( ( len - i ) < i ) ? ( len - i ) : i );
        }
    }

    public static void fillArray( boolean[] array, boolean value ) {
        int len = array.length;
        if ( len > 0 ) {
            array[0] = value;
        }
        for ( int i = 1; i < len; i += i ) {
            System.arraycopy( array, 0, array, i, ( ( len - i ) < i ) ? ( len - i ) : i );
        }
    }

    public static void copyArray( int[] source, int[] target ) {
        System.arraycopy( source, 0, target, 0, target.length );
    }

    public static void copyArray( double[] source, double[] target ) {
        System.arraycopy( source, 0, target, 0, target.length );
    }

    public static void copyArray( boolean[] source, boolean[] target ) {
        System.arraycopy( source, 0, target, 0, target.length );
    }
}
