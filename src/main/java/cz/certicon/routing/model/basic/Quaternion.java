/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Quaternion<A, B, C, D> extends Trinity<A, B, C> {

    public final D d;

    public Quaternion( A a, B b, C c, D d ) {
        super( a, b, c );
        this.d = d;
    }

}
