/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.data.simple;

import cz.certicon.routing.application.algorithm.Distance;

/**
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
class DistanceImpl implements Distance {

    private final double dist;
    private static final double EPS = 10E-20;

    public DistanceImpl() {
        this.dist = Double.POSITIVE_INFINITY;
    }

    public DistanceImpl( double dist ) {
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
    public Distance add( Distance other ) {
        DistanceImpl otherImpl = (DistanceImpl) other;
        return new DistanceImpl( dist + otherImpl.dist );
    }

    @Override
    public int compareTo( Distance other ) {
        DistanceImpl otherImpl = (DistanceImpl) other;
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
        final DistanceImpl other = (DistanceImpl) obj;
        return this.compareTo( other ) == 0;
    }
    
    

    @Override
    public String toString() {
        return "DistanceImpl{" + "dist=" + dist + '}';
    }

}
