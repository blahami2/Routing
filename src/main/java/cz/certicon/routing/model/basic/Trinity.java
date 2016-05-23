/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

import java.util.Objects;

/**
 * A generic container class for three objects.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <A> class of the first object
 * @param <B> class of the second object
 * @param <C> class of the third object
 *
 */
public class Trinity<A, B, C> extends Pair<A, B> {

    public final C c;

    public Trinity( A a, B b, C c ) {
        super( a, b );
        this.c = c;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 53 * hash + Objects.hashCode( this.c );
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
        final Trinity<?, ?, ?> other = (Trinity<?, ?, ?>) obj;
        if ( !Objects.equals( this.c, other.c ) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Trinity{" + "a=" + a + "b=" + b + "c=" + c + '}';
    }
}
