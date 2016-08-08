/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.cor;

/**
 *
 * @param <In> input type
 * @param <Out> output type
 * @since 1.X, X &gt; 2
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Handler<In, Out> {

    /**
     * Process input, pass and return output
     *
     * @param input input
     * @param output passed output
     * @return returned output
     */
    public Out process( In input, Out output );

    /**
     * Returns next handler in the chain
     *
     * @return next handler
     */
    public Handler getNext();

    /**
     * Sets next handler in the chain
     *
     * @param handler next handler
     */
    public void setNext( Handler handler );
}
