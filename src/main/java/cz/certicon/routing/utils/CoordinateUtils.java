/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

import cz.certicon.routing.model.entity.CartesianCoords;
import cz.certicon.routing.model.entity.Coordinate;
import java.awt.Dimension;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import static java.lang.Math.*;
import java.util.ArrayList;

/**
 * Utilities for coordinates
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class CoordinateUtils {

    private static final double EARTH_RADIUS = 6371000;
    public static final double COORDINATE_PRECISION = 10E-5; // 0.11 meter accuracy
    public static final double DISTANCE_PRECISION_METERS = 10E-1; // 0.1 meter accuracy

//    private static final CoordinateReferenceSystem COORDINATE_REFERENCE_SYSTEM;
//    static {
    //        try {
//            COORDINATE_REFERENCE_SYSTEM = org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
//        } catch ( FactoryException ex ) {
//            throw new IllegalStateException( ex );
//        }
//    }
    /**
     * Calculates the geographical midpoint of the given coordinates.
     *
     * @param coordinates list of coordinates to be accounted into the
     * calculation
     * @return geographical midpoint
     */
    public static Coordinate calculateGeographicMidpoint( List<Coordinate> coordinates ) {
        List<CartesianCoords> ccoords = new LinkedList<>();
        for ( Coordinate coordinate : coordinates ) {
            double lat = toRadians( coordinate.getLatitude() );
            double lon = toRadians( coordinate.getLongitude() );
            ccoords.add( new CartesianCoords(
                    cos( lat ) * cos( lon ),
                    cos( lat ) * sin( lon ),
                    sin( lat )
            ) );
        }
        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        for ( CartesianCoords c : ccoords ) {
            sumX += c.getX();
            sumY += c.getY();
            sumZ += c.getZ();
        }
        CartesianCoords mid = new CartesianCoords(
                sumX / ccoords.size(),
                sumY / ccoords.size(),
                sumZ / ccoords.size()
        );
        double lon = atan2( mid.getY(), mid.getX() );
        double hyp = sqrt( mid.getX() * mid.getX() + mid.getY() * mid.getY() );
        double lat = atan2( mid.getZ(), hyp );
        return new Coordinate( toDegrees( lat ), toDegrees( lon ) );
    }

    /**
     * Calculates the geographical distance between two points
     *
     * @param a first point in {@link Coordinate}
     * @param b second point in {@link Coordinate}
     * @return calculated distance in meters
     */
    public static double calculateDistance( Coordinate a, Coordinate b ) {
        return calculateDistance( a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude() );
    }

    /**
     * Calculates the geographical distance between two points
     *
     * @param aLat latitude of point A
     * @param aLon longitude of point A
     * @param bLat latitude of point B
     * @param bLon longitude of point B
     * @return calculated distance in meters
     */
    public static double calculateDistance( double aLat, double aLon, double bLat, double bLon ) {
//        System.out.println( "calcualting distance:" );
//        System.out.println( a );
//        System.out.println( b );
        double aLatRad = toRadians( aLat );
        double aLonRad = toRadians( aLon );
        double bLatRad = toRadians( bLat );
        double bLonRad = toRadians( bLon );
        double result;
        // Pythagoras distance
//        double varX = ( aLatRad - bLatRad ) * cos( ( aLonRad + bLonRad ) / 2 );
//        double varY = ( aLonRad - bLonRad );
//        result = sqrt( varX * varX + varY * varY ) * EARTH_RADIUS;
//        System.out.println( "Pythagoras: " + result );
        // Haversine formula
        double deltaLatRad = toRadians( aLat - bLat );
        double deltaLonRad = toRadians( aLon - bLon );
        double varA = sin( deltaLatRad / 2 ) * sin( deltaLatRad / 2 ) + cos( aLatRad ) * cos( bLatRad ) * sin( deltaLonRad / 2 ) * sin( deltaLonRad / 2 );
        double varC = 2 * atan2( sqrt( varA ), sqrt( 1 - varA ) );
        result = EARTH_RADIUS * varC;

        // JTS
//        GeodeticCalculator geodeticCalculator = new GeodeticCalculator( COORDINATE_REFERENCE_SYSTEM );
//        try {
//            geodeticCalculator.setStartingPosition( JTS.toDirectPosition( new Coordinate( a.getLongitude(), a.getLatitude() ), COORDINATE_REFERENCE_SYSTEM ) );
//            geodeticCalculator.setDestinationPosition( JTS.toDirectPosition( new Coordinate( b.getLongitude(), b.getLatitude() ), COORDINATE_REFERENCE_SYSTEM ) );
//        } catch ( TransformException ex ) {
//            throw new RuntimeException( ex );
//        }
//        result = geodeticCalculator.getOrthodromicDistance();
        return result;
    }

    /**
     * Divides path between two points into list of coordinates.
     *
     * @param start starting point in {@link Coordinate}
     * @param end target point in {@link Coordinate}
     * @param count amount of required points in the path
     * @return list of {@link Coordinate} for the given path
     */
    public static List<Coordinate> divideCoordinates( Coordinate start, Coordinate end, int count ) {
        List<Coordinate> coords = new LinkedList<>();
        double aLat = start.getLatitude();
        double aLon = start.getLongitude();
        double bLat = end.getLatitude();
        double bLon = end.getLongitude();
        for ( int i = 0; i < count; i++ ) {
            double avgLat = ( ( count - 1 - i ) * aLat + ( i ) * bLat ) / ( count - 1 );
            double avgLon = ( ( count - 1 - i ) * aLon + ( i ) * bLon ) / ( count - 1 );
            coords.add( new Coordinate( avgLat, avgLon ) );
        }
        return coords;
    }

    /**
     * Converts coordinates in WGS84 format into Cartesian coordinates
     *
     * @param coords {@link Coordinate} in WGS84
     * @return {@link CartesianCoords} representation of the given coordinates
     */
    public static CartesianCoords toCartesianFromWGS84( Coordinate coords ) {
        return new CartesianCoords(
                EARTH_RADIUS * Math.cos( coords.getLatitude() ) * Math.cos( coords.getLongitude() ),
                EARTH_RADIUS * Math.cos( coords.getLatitude() ) * Math.sin( coords.getLongitude() ),
                EARTH_RADIUS * Math.sin( coords.getLatitude() )
        );
    }

    /**
     * Converts WGS84 coordinates to point in the given container.
     *
     * @param container an instance of {@link Dimension} for the point to fit in
     * (scaled)
     * @param coords {@link Coordinate} in WGS84
     * @return scaled {@link Point} for the given container based on the given
     * coordinates
     */
    public static Point toPointFromWGS84( Dimension container, Coordinate coords ) {
//        int x = (int) ( ( container.width / 360.0 ) * ( 180 + coords.getLatitude() ) );
//        int y = (int) ( ( container.height / 180.0 ) * ( 90 - coords.getLongitude() ) );
        int x = (int) ( ( container.width / 360.0 ) * ( coords.getLongitude() ) );
        int y = (int) ( ( container.height / 180.0 ) * ( coords.getLatitude() ) );
        return new Point( x, y );
    }

    public static Coordinate jtsToCoordinates( com.vividsolutions.jts.geom.Coordinate coordinate ) {
        return new Coordinate( coordinate.y, coordinate.x );
    }

    public static Coordinate coordinatesToJts( Coordinate coordinates ) {
        return new Coordinate( coordinates.getLongitude(), coordinates.getLatitude() );
    }

    public static List<Coordinate> jtsToCoordinates( com.vividsolutions.jts.geom.Coordinate[] coordinates ) {
        List<Coordinate> coords = new ArrayList<>();
        for ( com.vividsolutions.jts.geom.Coordinate c : coordinates ) {
            coords.add( jtsToCoordinates( c ) );
        }
        return coords;
    }

    public static Coordinate[] coordinatesToJts( List<Coordinate> coordinates ) {
        Coordinate[] coords = new Coordinate[coordinates.size()];
        for ( int i = 0; i < coordinates.size(); i++ ) {
            coords[i] = coordinatesToJts( coordinates.get( i ) );
        }
        return coords;
    }
    
    public boolean equals(Coordinate a, Coordinate b, double precision){
        return (DoubleComparator.isEqualTo( a.getLatitude(), b.getLatitude(), 10E-6)
                && DoubleComparator.isEqualTo( a.getLongitude(), b.getLongitude(), 10E-6));
    }
}
