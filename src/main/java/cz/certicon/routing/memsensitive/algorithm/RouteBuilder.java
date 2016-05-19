/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface RouteBuilder<T> {

    public void addEdgeAsFirst( long edgeId );

    public void addEdgeAsLast( long edgeId );

    public T build();
}
