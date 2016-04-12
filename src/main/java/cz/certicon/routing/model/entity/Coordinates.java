/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.utils.DoubleComparator;

/**
 * A class for representing geographical coordinates (latitude, longitude). When
 * comparing coordinates, it uses {@link DoubleComparator} with precision of
 * 10E-15.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Coordinates {

    private static final double EPS = 10E-10;

    private final double latitude;
    private final double longitude;

    /**
     * Constructor
     *
     * @param latitude double representation of latitude
     * @param longitude double representation of longitude
     */
    public Coordinates( double latitude, double longitude ) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Getter for latitude
     *
     * @return latitude double representation of latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Getter for longitude
     *
     * @return longitude double representation of longitude
     */
    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "Coordinates{" + "latitude=" + latitude + ", longitude=" + longitude + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) ( Double.doubleToLongBits( this.latitude ) ^ ( Double.doubleToLongBits( this.latitude ) >>> 32 ) );
        hash = 97 * hash + (int) ( Double.doubleToLongBits( this.longitude ) ^ ( Double.doubleToLongBits( this.longitude ) >>> 32 ) );
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
        final Coordinates other = (Coordinates) obj;
        if ( !DoubleComparator.isEqualTo( this.latitude, other.latitude, EPS ) ) {
            return false;
        }
        if ( !DoubleComparator.isEqualTo( this.longitude, other.longitude, EPS ) ) {
            return false;
        }
        return true;
    }

}
