/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

import java.util.Objects;

/**
 * A generic container class for two objects.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <A> class of the first object
 * @param <B> class of the second object
 * 
 */
public class Pair<A,B> {
    public final A a;
    public final B b;

    public Pair( A a, B b ) {
        this.a = a;
        this.b = b;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + a.hashCode();
        hash = 83 * hash + b.hashCode();
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
        final Pair<?, ?> other = (Pair<?, ?>) obj;
        if ( !Objects.equals( this.a, other.a ) ) {
            return false;
        }
        if ( !Objects.equals( this.b, other.b ) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Pair{" + "a=" + a + ", b=" + b + '}';
    }
}
