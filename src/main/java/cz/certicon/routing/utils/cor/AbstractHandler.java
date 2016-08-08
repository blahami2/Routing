/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.cor;

/**
 * @since 1.X, X &gt; 2
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <In> in
 * @param <Out> out
 */
public abstract class AbstractHandler<In, Out> implements Handler<In, Out> {

    private Handler<In, Out> next;

    @Override
    public Handler getNext() {
        return next;
    }

    @Override
    public void setNext( Handler next ) {
        this.next = next;
    }

    @Override
    public Out process( In input, Out output ) {
        output = handle( input, output );
        if ( next != null ) {
            return next.process( input, output );
        }
        return output;
    }

    /**
     * Returns output. No idea what is this for
     *
     * @param output output to be returned
     * @return output
     */
    protected Out returnOutput( Out output ) {
        return output;
    }

    /**
     * Continues processing (if next handler is available)
     *
     * @param input input
     * @param output output
     * @return output
     */
    protected Out continueProcessing( In input, Out output ) {
        output = handle( input, output );
        if ( next != null ) {
            return next.process( input, output );
        }
        return output;
    }

    /**
     * Handles the input and enriches the output
     *
     * @param input input
     * @param output output
     * @return processed output
     */
    protected abstract Out handle( In input, Out output );

}
