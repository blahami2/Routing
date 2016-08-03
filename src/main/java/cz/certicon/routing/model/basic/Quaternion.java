/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

import java.util.Objects;

/**
 * A generic container class for four objects.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <A> class of the first object
 * @param <B> class of the second object
 * @param <C> class of the third object
 * @param <D> class of the fourth object
 *
 */
public class Quaternion<A, B, C, D> extends Trinity<A, B, C> {

    public final D d;

    public Quaternion( A a, B b, C c, D d ) {
        super( a, b, c );
        this.d = d;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 41 * hash + d.hashCode();
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
        final Quaternion<?, ?, ?, ?> other = (Quaternion<?, ?, ?, ?>) obj;
        if ( !Objects.equals( this.d, other.d ) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Quaternion{" + "a=" + a + "b=" + b + "c=" + c + "d=" + d + '}';
    }

}
