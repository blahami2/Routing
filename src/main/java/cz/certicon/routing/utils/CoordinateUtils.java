/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

import cz.certicon.routing.model.entity.Coordinates;
import java.util.LinkedList;
import java.util.List;

/**
 * Utilities for coordinates
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public class CoordinateUtils {

    /**
     * Calculates the geographical midpoint of the given coordinates.
     *
     * @param coordinates list of coordinates to be accounted into the calculation
     * @return geographical midpoint
     */
    public static Coordinates calculateGeographicMidpoint( List<Coordinates> coordinates ) {
        List<CartesianCoords> ccoords = new LinkedList<>();
        for ( Coordinates coordinate : coordinates ) {
            double lat = Math.toRadians( coordinate.getLatitude() );
            double lon = Math.toRadians( coordinate.getLongitude() );
            ccoords.add( new CartesianCoords(
                    Math.cos( lat ) * Math.cos( lon ),
                    Math.cos( lat ) * Math.sin( lon ),
                    Math.sin( lat )
            ) );
        }
        CartesianCoords mid = new CartesianCoords(
                ccoords.stream().mapToDouble( c -> c.x ).sum() / ccoords.size(),
                ccoords.stream().mapToDouble( c -> c.y ).sum() / ccoords.size(),
                ccoords.stream().mapToDouble( c -> c.z ).sum() / ccoords.size()
        );
        double lon = Math.atan2( mid.y, mid.x );
        double hyp = Math.sqrt( mid.x * mid.x + mid.y * mid.y );
        double lat = Math.atan2( mid.z, hyp );
        return new Coordinates( Math.toDegrees( lat ), Math.toDegrees( lon ) );
    }

    /**
     * Calculates the geographical distance between two points
     *
     * @param a first point in {@link Coordinates}
     * @param b second point in {@link Coordinates}
     * @return calculated distance in double
     */
    public static double calculateDistance( Coordinates a, Coordinates b ) {
        double lat = a.getLatitude() - b.getLatitude();
        double lon = a.getLongitude() - b.getLongitude();
        return Math.sqrt( lat * lat + lon * lon );
    }

    /**
     * Divides path between two points into list of coordinates.
     *
     * @param start starting point in {@link Coordinates}
     * @param end target point in {@link Coordinates}
     * @param count amount of required points in the path
     * @return list of {@link Coordinates} for the given path
     */
    public static List<Coordinates> divideCoordinates( Coordinates start, Coordinates end, int count ) {
        List<Coordinates> coords = new LinkedList<>();
        double aLat = start.getLatitude();
        double aLon = start.getLongitude();
        double bLat = end.getLatitude();
        double bLon = end.getLongitude();
        for ( int i = 0; i < count; i++ ) {
            double avgLat = ( ( count - 1 - i ) * aLat + ( i ) * bLat ) / ( count - 1 );
            double avgLon = ( ( count - 1 - i ) * aLon + ( i ) * bLon ) / ( count - 1 );
            coords.add( new Coordinates( avgLat, avgLon ) );
        }
        return coords;
    }

    private static class CartesianCoords {

        final double x;
        final double y;
        final double z;

        public CartesianCoords( double x, double y, double z ) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

    }
}
