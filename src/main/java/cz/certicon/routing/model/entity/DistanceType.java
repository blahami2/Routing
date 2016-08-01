/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.utils.CoordinateUtils;

/**
 * Enumeration for available metrics.
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

    /**
     * Calculates abstract distance from the given length and speed
     *
     * @param length length
     * @param speed speed
     * @return abstract distance
     */
    public abstract double calculateDistance( double length, double speed );

    /**
     * Calculates approximate abstract distance for the given coordinates
     * (between A and B)
     *
     * @param aLat latitude of the point A
     * @param aLon longitude of the point A
     * @param bLat latitude of the point B
     * @param bLon longitude of the point B
     * @return approximate abstract distance between A and B
     */
    public abstract double calculateApproximateDistance( double aLat, double aLon, double bLat, double bLon );

    /**
     * Returns integer representation of this distance type
     *
     * @return integer representation of this distance type
     */
    public int toInt() {
        return toInt( this );
    }

    /**
     * Converts the given distanceType to its integer representation
     *
     * @param distanceType distance type to be converted
     * @return given distanceType to its integer representation
     */
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

    /**
     * Converts the given integer representation to its distanceType
     *
     * @param n integer representation to be converted
     * @return given integer representation to its distanceType
     */
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
