/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.data.number;

import cz.certicon.routing.application.algorithm.Distance;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
class DoubleDistance implements Distance {

    private final double dist;
    private static final double EPS = 10E-10;

    public DoubleDistance() {
        this.dist = Double.POSITIVE_INFINITY;
    }

    public DoubleDistance( double dist ) {
        this.dist = dist;
    }

    @Override
    public boolean isGreaterThan( Distance other ) {
        return compareTo( other ) > 0;
    }

    @Override
    public boolean isGreaterOrEqualTo( Distance other ) {
        return compareTo( other ) >= 0;
    }

    @Override
    public boolean isLowerThan( Distance other ) {
        return compareTo( other ) < 0;
    }

    @Override
    public boolean isLowerOrEqualTo( Distance other ) {
        return compareTo( other ) <= 0;
    }

    @Override
    public boolean isEqualTo( Distance other ) {
        return compareTo( other ) == 0;
    }

    @Override
    public double getEvaluableValue() {
        return dist;
    }

    @Override
    public Distance add( Distance other ) {
        DoubleDistance otherImpl = (DoubleDistance) other;
        return new DoubleDistance( dist + otherImpl.dist );
    }

    @Override
    public int compareTo( Distance other ) {
        DoubleDistance otherImpl = (DoubleDistance) other;
        if ( dist < otherImpl.dist - EPS ) {
            return -1;
        }
        if ( dist > otherImpl.dist + EPS ) {
            return 1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) ( Double.doubleToLongBits( this.dist ) ^ ( Double.doubleToLongBits( this.dist ) >>> 32 ) );
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
        final DoubleDistance other = (DoubleDistance) obj;
        return this.compareTo( other ) == 0;
    }

    @Override
    public String toString() {
        return "DistanceImpl{" + "dist=" + dist + '}';
    }

}
