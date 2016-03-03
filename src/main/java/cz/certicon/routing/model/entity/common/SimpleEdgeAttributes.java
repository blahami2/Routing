/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.model.entity.EdgeAttributes;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleEdgeAttributes implements EdgeAttributes {

    private double speed;
    private double length;
    private boolean isOneWay;
    private boolean isPaid;

    private SimpleEdgeAttributes( double speed, double length, boolean isOneWay, boolean isPaid ) {
        this.speed = speed;
        this.length = length;
        this.isOneWay = isOneWay;
        this.isPaid = isPaid;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public boolean isOneWay() {
        return isOneWay;
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
        return builder( speed ).setLength( length ).setOneWay( isOneWay ).setPaid( isPaid ).build();
    }

    @Override
    public String toString() {
        return "SimpleEdgeAttributes{" + "speed=" + speed + ", length=" + length + ", isOneWay=" + isOneWay + ", isPaid=" + isPaid + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) ( Double.doubleToLongBits( this.speed ) ^ ( Double.doubleToLongBits( this.speed ) >>> 32 ) );
        hash = 97 * hash + (int) ( Double.doubleToLongBits( this.length ) ^ ( Double.doubleToLongBits( this.length ) >>> 32 ) );
        hash = 97 * hash + ( this.isOneWay ? 1 : 0 );
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
        if ( Double.doubleToLongBits( this.speed ) != Double.doubleToLongBits( other.speed ) ) {
            return false;
        }
        if ( Double.doubleToLongBits( this.length ) != Double.doubleToLongBits( other.length ) ) {
            return false;
        }
        if ( this.isOneWay != other.isOneWay ) {
            return false;
        }
        if ( this.isPaid != other.isPaid ) {
            return false;
        }
        return true;
    }

    public static Builder builder( double speed ) {
        return new Builder( speed );
    }

    public static class Builder {

        private final double speed;
        private double length = 1;
        private boolean isOneWay = false;
        private boolean isPaid = false;

        public Builder( double speed ) {
            this.speed = speed;
        }

        public Builder setLength( double length ) {
            this.length = length;
            return this;
        }

        public Builder setOneWay( boolean isOneWay ) {
            this.isOneWay = isOneWay;
            return this;
        }

        public Builder setPaid( boolean isPaid ) {
            this.isPaid = isPaid;
            return this;
        }

        public EdgeAttributes build() {
            return new SimpleEdgeAttributes( speed, length, isOneWay, isPaid );
        }

    }

}
