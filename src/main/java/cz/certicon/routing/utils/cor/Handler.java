/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.cor;

/**
 *
 * @param <In>
 * @param <Out>
 * @since 1.X, X &gt; 2
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Handler<In,Out> {
    public Out process(In input, Out output);
    public Handler getNext();
    public void setNext(Handler handler);
}
