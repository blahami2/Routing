/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.cor;

/**
 * Need to think this through first. TODO
 *
 * @since 1.X, X &gt; 2
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <In> in
 * @param <Out> out
 */
public interface ChainOfResponsibility<In, Out> {

    public Out process( In input, Out output );

    public ChainOfResponsibility<In, Out> add( Handler<In, Out> handler );
}
