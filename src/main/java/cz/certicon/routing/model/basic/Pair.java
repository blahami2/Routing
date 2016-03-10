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
public class Pair<A,B> {
    public final A a;
    public final B b;

    public Pair( A a, B b ) {
        this.a = a;
        this.b = b;
    }
}
