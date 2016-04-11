/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

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

}
