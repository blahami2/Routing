/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.preprocessing.ch;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
class ShortcutLocator {
    
    private final int node;
    private final int sequenceIndex;

    public ShortcutLocator( int node, int sequenceIndex ) {
        this.node = node;
        this.sequenceIndex = sequenceIndex;
    }

    public int getNode() {
        return node;
    }

    public int getSequenceIndex() {
        return sequenceIndex;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.node;
        hash = 53 * hash + this.sequenceIndex;
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final ShortcutLocator other = (ShortcutLocator) obj;
        if ( this.node != other.node ) {
            return false;
        }
        if ( this.sequenceIndex != other.sequenceIndex ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{" + "" + node + ", " + sequenceIndex + '}';
    }
    
}
