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
public class Trinity<A, B, C> extends Pair<A, B> {

    public final C c;

    public Trinity( A a, B b, C c ) {
        super( a, b );
        this.c = c;
    }
}
