/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.cor;

/**
 *
 * @since 1.X, X &gt; 2
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <In> in
 * @param <Out> out
 */
public class LinkedChainOfResponsibility<In, Out> implements ChainOfResponsibility<In, Out> {

    private Handler<In, Out> first;

    @Override
    public Out process( In input, Out output ) {
        return first.process( input, output );
    }

    @Override
    public ChainOfResponsibility<In, Out> add( Handler<In, Out> handler ) {
        if ( first == null ) {
            first = handler;
        } else {
            Handler<In, Out> prev = first;
            Handler<In, Out> current = first;
            while ( current != null ) {
                prev = current;
                current = current.getNext();
            }
            prev.setNext( handler );
        }
        return this;
    }

}
