/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.model.entity.EdgeAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleEdgeAttributes implements EdgeAttributes {

    private final double length;
    private final boolean isPaid;
    private Map<String, String> attrMap;

    private SimpleEdgeAttributes( double length, boolean isPaid ) {
        this.length = length;
        this.isPaid = isPaid;
    }

    @Override
    public boolean isPaid() {
        return isPaid;
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public EdgeAttributes copyWithNewLength( double length ) {
        return builder().setLength( length ).setPaid( isPaid ).build();
    }

    @Override
    public String toString() {
        return "SimpleEdgeAttributes{" + "length=" + length + ", isPaid=" + isPaid + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) ( Double.doubleToLongBits( this.length ) ^ ( Double.doubleToLongBits( this.length ) >>> 32 ) );
        hash = 97 * hash + ( this.isPaid ? 1 : 0 );
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
        final SimpleEdgeAttributes other = (SimpleEdgeAttributes) obj;
        if ( Double.doubleToLongBits( this.length ) != Double.doubleToLongBits( other.length ) ) {
            return false;
        }
        if ( this.isPaid != other.isPaid ) {
            return false;
        }
        return true;
    }

    @Override
    public void putAdditionalAttribute( String key, String value ) {
        if ( attrMap == null ) {
            attrMap = new HashMap<>();
        }
        attrMap.put( key, value );
    }

    @Override
    public String getAdditionalAttribute( String key ) {
        if ( attrMap == null ) {
            throw new IllegalStateException( "Map not initialized at all" );
        }
        if ( !attrMap.containsKey( key ) ) {
            throw new IllegalArgumentException( "Unknown key: '" + key + "'" );
        }
        return attrMap.get( key );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private double length = 1;
        private boolean isPaid = false;

        public Builder() {
        }

        public Builder setLength( double length ) {
            this.length = length;
            return this;
        }

        public Builder setPaid( boolean isPaid ) {
            this.isPaid = isPaid;
            return this;
        }

        public EdgeAttributes build() {
            return new SimpleEdgeAttributes( length, isPaid );
        }

    }

}
