/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.efficient;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface BitArray {

    public void init( int size );

    public void set( int index, boolean value );

    public boolean get( int index );
    
    public int size();

    public void clear();
}
