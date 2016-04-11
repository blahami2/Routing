/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

/**
 * Speed utility class.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SpeedUtils {

    private static final double MPH_TO_KPH_RATIO = 1.609;
    private static final double KNOTS_TO_KPH_RATIO = 1.852;

    /**
     * Converts miles per hour to kilometers per hour
     *
     * @param mph speed in Miles/Hour
     * @return speed in km/h
     */
    public static double mphToKmph( double mph ) {
        return mph * MPH_TO_KPH_RATIO;
    }

    /**
     * Converts knots to kilometers per hour
     *
     * @param knots speed in Knots
     * @return speed in km/h
     */
    public static double knotToKmph( double knots ) {
        return knots * KNOTS_TO_KPH_RATIO;
    }

    /**
     * Converts kilometers per hour to meters per second
     *
     * @param kmph speed in km/h
     * @return speed in m/s
     */
    public static double kmphToMps( double kmph ) {
        return kmph / 3.6;
    }
}
