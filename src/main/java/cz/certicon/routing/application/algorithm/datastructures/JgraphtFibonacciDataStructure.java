/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.datastructures;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.model.entity.Node;
import java.util.HashMap;
import java.util.Map;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class JgraphtFibonacciDataStructure implements NodeDataStructure {

    private final Map<Node, FibonacciHeapNode<Node>> nodeMap;
    private final org.jgrapht.util.FibonacciHeap<Node> fibonacciHeap;

    public JgraphtFibonacciDataStructure() {
        this.fibonacciHeap = new FibonacciHeap<>();
        this.nodeMap = new HashMap<>();
    }

    @Override
    public Node extractMin() {
        FibonacciHeapNode<Node> min = fibonacciHeap.removeMin();
        nodeMap.remove( min.getData() );
        return min.getData();
    }

    @Override
    public NodeDataStructure add( Node node ) {
        FibonacciHeapNode<Node> n = new FibonacciHeapNode<>( node );
        nodeMap.put( node, n );
        fibonacciHeap.insert( n, node.getDistance().getEvaluableValue() );
        return this;
    }

    @Override
    public NodeDataStructure remove( Node node ) {
        FibonacciHeapNode<Node> n = nodeMap.get( node );
        fibonacciHeap.delete( n );
        return this;
    }

    @Override
    public NodeDataStructure notifyDataChange( Node node ) {
        FibonacciHeapNode<Node> n = nodeMap.get( node );
        fibonacciHeap.decreaseKey( n, node.getDistance().getEvaluableValue() );
        return this;
    }

    @Override
    public NodeDataStructure clear() {
        nodeMap.clear();
        fibonacciHeap.clear();
        return this;
    }

    @Override
    public boolean isEmpty() {
        return fibonacciHeap.isEmpty();
    }

    @Override
    public int size() {
        return fibonacciHeap.size();
    }

}
