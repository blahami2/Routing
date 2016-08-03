/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing;

/**
 * Global options defining mostly debugging and measuring (on/off). These
 * settings may slow the application down, mostly insignificantly. They may also
 * overwhelm the standard output.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class GlobalOptions {

    /**
     * Defines whether debug time should be measured and displayed
     */
    public static boolean DEBUG_TIME = false;
    /**
     * Defines whether debug checks should be evaluated and displayed
     */
    public static boolean DEBUG_CORRECTNESS = false;
    /**
     * Defines whether time should be measured
     */
    public static boolean MEASURE_TIME = false;
    /**
     * Defines whether statistics should be measured
     */
    public static boolean MEASURE_STATS = false;
}
