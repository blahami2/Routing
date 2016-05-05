/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.datastructures;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import java.util.HashMap;
import java.util.Map;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

/**
 * {@link NodeDataStructure} implementation using Fibonacci's heap (adapter to
 * JGraphT lib. Fibonacci).
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> node type
 */
public class JgraphtFibonacciDataStructure<T> implements NodeDataStructure<T> {

    private final Map<T, FibonacciHeapNode<T>> nodeMap;
    private final org.jgrapht.util.FibonacciHeap<T> fibonacciHeap;

    public JgraphtFibonacciDataStructure() {
        this.fibonacciHeap = new FibonacciHeap<>();
        this.nodeMap = new HashMap<>();
    }

    @Override
    public T extractMin() {
        FibonacciHeapNode<T> min = fibonacciHeap.removeMin();
        nodeMap.remove( min.getData() );
        return min.getData();
    }

    @Override
    public void add( T node, double value ) {
//        System.out.println( "Adding " + node + " with value " + value );
        FibonacciHeapNode<T> n = new FibonacciHeapNode<>( node );
        nodeMap.put( node, n );
        fibonacciHeap.insert( n, value );
    }

    @Override
    public void remove( T node ) {
        FibonacciHeapNode<T> n = nodeMap.get( node );
        nodeMap.remove( node );
        fibonacciHeap.delete( n );
    }

    @Override
    public void notifyDataChange( T node, double value ) {
//        System.out.println( "Changing " + node + " to value " + value );
        FibonacciHeapNode<T> n = nodeMap.get( node );
        if ( n == null ) {
            throw new IllegalArgumentException( "Unknown node: " + node );
        }
        if ( value < n.getKey() ) {
            fibonacciHeap.decreaseKey( n, value );
        } else if ( value > n.getKey() ) {
            remove( node );
            add( node, value );
        }
    }

    @Override
    public void clear() {
        nodeMap.clear();
        fibonacciHeap.clear();
    }

    @Override
    public boolean isEmpty() {
        return fibonacciHeap.isEmpty();
    }

    @Override
    public int size() {
        return fibonacciHeap.size();
    }

    @Override
    public boolean contains( T node ) {
        return nodeMap.containsKey( node );
    }

}
