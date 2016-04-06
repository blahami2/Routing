/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

import cz.certicon.routing.model.entity.CartesianCoords;
import java.awt.Dimension;
import java.awt.Point;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class GeometryUtils {

    public static Point toPointFromCartesian( CartesianCoords coords ) {
        return new Point( coords.getXAsInt(), coords.getYAsInt() );
    }

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
}
