/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.collections.array;

import gnu.trove.iterator.TIntIterator;

/**
 * Efficient integer linked list. From the outside it can be only added to. It
 * supports reading and removal via iterator.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class IntLinkedList {

    private Entry first = null;
    private Entry last = null;
    private int size = 0;

    /**
     * Add number to the end of list
     *
     * @param n number to be added
     */
    public void add( int n ) {
        if ( last == null ) {
            first = last = new Entry( n );
        } else {
            last.next = new Entry( n );
            last = last.next;
        }
        size++;
    }

    /**
     * Returns iterator
     *
     * @return iterator
     */
    public TIntIterator iterator() {
        return new IntLinkedListIterator();
    }

    private static class Entry {

        public final int value;
        public Entry next;

        public Entry( int value ) {
            this.value = value;
            this.next = null;
        }
    }

    private class IntLinkedListIterator implements TIntIterator {

        private Entry current = null;
        private Entry previous = null;

        public IntLinkedListIterator() {
        }

        @Override
        public int next() {
            previous = current;
            current = ( current == null ) ? first : current.next;
            return current.value;
        }

        @Override
        public boolean hasNext() {
            return ( current != null ) ? current.next != null : first != null;
        }

        @Override
        public void remove() {
            if ( previous == null ) {
                first = first.next;
            } else {
                previous.next = current.next;
            }
        }

    }
}
