/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SpeedUtils {

    private static final double MPH_TO_KPH_RATIO = 1.609;
    private static final double KNOTS_TO_KPH_RATIO = 1.852;

    public static double mphToKmph( double mph ) {
        return mph * MPH_TO_KPH_RATIO;
    }

    public static double knotToKmph( double knots ) {
        return knots * KNOTS_TO_KPH_RATIO;
    }

    public static double kmphToMps( double kmph ) {
        return kmph / 3.6;
    }
}
