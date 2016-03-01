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

    private final int speed;
    private final double length;
    private final boolean isOneWay;
    private final boolean isPaid;

    private SimpleEdgeAttributes( int speed, double length, boolean isOneWay, boolean isPaid ) {
        this.speed = speed;
        this.length = length;
        this.isOneWay = isOneWay;
        this.isPaid = isPaid;
    }

    @Override
    public int getSpeed() {
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

    public static Builder builder( int speed ) {
        return new Builder( speed );
    }

    public static class Builder {

        private final int speed;
        private double length = 1;
        private boolean isOneWay = false;
        private boolean isPaid = false;

        public Builder( int speed ) {
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
