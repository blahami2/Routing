/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity;

import cz.certicon.routing.utils.CoordinateUtils;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public enum DistanceType {

    TIME {
        @Override
        public double calculateDistance( double length, double speed ) { // m, km/s
            return 3.6 * length / speed;
        }

        @Override
        public double calculateApproximateDistance( double alat, double alon, double blat, double blon ) {
            return calculateDistance( CoordinateUtils.calculateDistance( alat, alon, blat, blon ), MAX_SPEED );
        }
    }, LENGTH {
        @Override
        public double calculateDistance( double length, double speed ) {
            return length;
        }

        @Override
        public double calculateApproximateDistance( double alat, double alon, double blat, double blon ) {
            return CoordinateUtils.calculateDistance( alat, alon, blat, blon );
        }
    };

    private static final int MAX_SPEED = 130;

    public abstract double calculateDistance( double length, double speed );

    public abstract double calculateApproximateDistance( double aLat, double aLon, double bLat, double bLon );

    public int toInt() {
        return toInt( this );
    }

    public static int toInt( DistanceType distanceType ) {
        switch ( distanceType ) {
            case LENGTH:
                return 1;
            case TIME:
                return 2;
            default:
                throw new AssertionError( "Unknown distance type: " + distanceType.name() );
        }
    }

    public static DistanceType fromInt( int n ) {
        switch ( n ) {
            case 1:
                return LENGTH;
            case 2:
                return TIME;
            default:
                throw new IllegalArgumentException( "Unknown distance type number: " + n );
        }
    }
}
