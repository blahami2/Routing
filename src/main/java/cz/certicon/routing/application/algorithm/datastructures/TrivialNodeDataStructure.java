/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.datastructures;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Basic {@link NodeDataStructure} implementation using an array.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> node type
 */
public class TrivialNodeDataStructure<T> implements NodeDataStructure<T> {

    private final List<NodeContainer<T>> nodes;

    public TrivialNodeDataStructure() {
        this.nodes = new LinkedList<>();
    }

    @Override
    public T extractMin() {
        if ( nodes.isEmpty() ) {
            throw new IllegalStateException( "NodeStructure is empty." );
        }
        NodeContainer<T> min = nodes.get( 0 );
        for ( NodeContainer<T> node : nodes ) {
            if ( node.value < min.value ) {
                min = node;
            }
        }
        nodes.remove( min );
        return min.node;
    }

    @Override
    public void add( T node, double value ) {
        nodes.add( new NodeContainer<>( node, value ) );
    }

    @Override
    public void remove( T node ) {
        for ( int i = 0; i < nodes.size(); i++ ) {
            if ( nodes.get( i ).node == node ) {
                nodes.remove( i );
            }
        }
    }

    @Override
    public void notifyDataChange( T node, double value ) {
        for ( int i = 0; i < nodes.size(); i++ ) {
            if ( nodes.get( i ).node == node ) {
                nodes.get( i ).value = value;
            }
        }
    }

    @Override
    public void clear() {
        nodes.clear();
    }

    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public boolean contains( T node ) {
        return nodes.contains( node );
    }

    @Override
    public T peek() {
        if ( nodes.isEmpty() ) {
            throw new IllegalStateException( "NodeStructure is empty." );
        }
        NodeContainer<T> min = nodes.get( 0 );
        for ( NodeContainer<T> node : nodes ) {
            if ( node.value < min.value ) {
                min = node;
            }
        }
        return min.node;
    }

    @Override
    public double minValue() {
        if ( nodes.isEmpty() ) {
            throw new IllegalStateException( "NodeStructure is empty." );
        }
        NodeContainer<T> min = nodes.get( 0 );
        for ( NodeContainer<T> node : nodes ) {
            if ( node.value < min.value ) {
                min = node;
            }
        }
        return min.value;
    }

    @Override
    public Iterator<T> iterator() {
        return new NodeIterator();
    }

    private static class NodeContainer<T> {

        public final T node;
        public double value;

        public NodeContainer( T node, double value ) {
            this.node = node;
            this.value = value;
        }
    }

    private class NodeIterator implements Iterator<T> {

        private int counter = -1;

        @Override
        public boolean hasNext() {
            return counter + 1 < nodes.size();
        }

        @Override
        public T next() {
            return nodes.get( ++counter ).node;
        }

    }
}
