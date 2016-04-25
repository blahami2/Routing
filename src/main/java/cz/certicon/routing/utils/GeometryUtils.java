/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

import cz.certicon.routing.model.entity.CartesianCoords;
import cz.certicon.routing.model.entity.Coordinates;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A geometry utility class.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class GeometryUtils {

    /**
     * Converts {@link CartesianCoords} to {@link Point}
     *
     * @param coords {@link CartesianCoords} coordinates
     * @return {@link Point} representation
     */
    public static Point toPointFromCartesian( CartesianCoords coords ) {
        return new Point( coords.getXAsInt(), coords.getYAsInt() );
    }

    /**
     * Scales the point based on the range of it's set into the given dimension.
     * For example, for numbers x: 5000, 1000, 9000, where 1000 is minimum, 9000
     * is maximum and the target dimension is 200x200: 1000 would be at x=0,
     * 9000 would be at x=199 and 45646 would be at x=100
     *
     * @param min minimal value in the source set of points
     * @param max maximal value in the source set of points
     * @param actual actual {@link Point} to be computed (placed)
     * @param targetDimension {@link Dimension} the point must be scaled into
     * @return scaled {@link Point}
     */
    public static Point getScaledPoint( Point min, Point max, Point actual, Dimension targetDimension ) {
        int width = Math.abs( min.x - max.x );
        int height = Math.abs( min.y - max.y );
        int x = ( actual.x - Math.min( min.x, max.x ) );
        int y = ( actual.y - Math.min( min.y, max.y ) );
//        System.out.println( "result = (actual - min(min, max))" );
//        System.out.println( y + " = " + actual.y + " - min(" + min.y + ", " + max.y + ")" );
//        System.out.println( y + " = " + actual.y + " - " + Math.min( min.y, max.y ) + "" );
        int scaledX = Math.round( (float) ( x * ( targetDimension.width / (double) width ) ) );
        int scaledY = Math.round( (float) ( y * ( targetDimension.height / (double) height ) ) );
        return new Point( scaledX, scaledY );
    }

    public static Coordinates toCoordinatesFromWktPoint( String point ) {
        point = point.substring( "POINT(".length(), point.length() - ")".length() );
        String[] lonlat = point.split( " " );
        return new Coordinates(
                Double.parseDouble( lonlat[1] ),
                Double.parseDouble( lonlat[0] )
        );
    }

    public static String toWktFromCoordinates( Coordinates coordinates ) {
        return "POINT(" + coordinates.getLongitude() + " " + coordinates.getLatitude() + ")";
    }

    public static List<Coordinates> toCoordinatesFromWktLinestring( String linestring ) {
        List<Coordinates> coordinates = new ArrayList<>();
        String content = linestring.substring( "LINESTRING(".length(), linestring.length() - ")".length() );
        for ( String cord : content.split( "," ) ) {
            Coordinates coord = new Coordinates(
                    Double.parseDouble( cord.split( " " )[1] ),
                    Double.parseDouble( cord.split( " " )[0] )
            );
            coordinates.add( coord );
        }
        return coordinates;
    }

    public static String toWktFromCoordinates( List<Coordinates> coordinates ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "LINESTRING(" );
        for ( Coordinates coordinate : coordinates ) {
            sb.append( coordinate.getLongitude() ).append( " " ).append( coordinate.getLatitude() ).append( "," );
        }
        sb.delete( sb.length() - 1, sb.length() );
        sb.append( ")" );
        return sb.toString();
    }
}
