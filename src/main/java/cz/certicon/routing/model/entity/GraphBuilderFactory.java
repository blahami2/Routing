/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

/**
 * Interface for the graph builder factory
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> graph type
 */
public interface GraphBuilderFactory<T> {

    /**
     * Creates and returns graph builder.
     *
     * @param nodeCount amount of nodes in the graph
     * @param edgeCount amount of edges in the graph
     * @return graph builder
     */
    public GraphBuilder<T> createGraphBuilder( int nodeCount, int edgeCount );
}
