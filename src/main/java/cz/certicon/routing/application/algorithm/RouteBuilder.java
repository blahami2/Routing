/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

/**
 * 
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface RouteBuilder<R,G> {

    public void setSourceNode( G graph, long nodeId );

    public void setTargetNode( G graph, long nodeId );

    public void addEdgeAsFirst( G graph, long edgeId );

    public void addEdgeAsLast( G graph, long edgeId );
    
    public void clear();

    public R build();
}
