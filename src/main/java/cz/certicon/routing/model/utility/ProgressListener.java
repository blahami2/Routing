/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.utility;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ProgressListener {

    public int getNumOfUpdates();

    public void setNumOfUpdates( int numOfUpdates );

    public void init( int size, double calculationRatio );

    public void onProgressUpdate( double done );

    public void nextStep();

    public static class Calculator {

        /**
         * Calculates the proper interval for updates based on the given
         * information
         *
         * @param size number of steps performed altogether
         * @param numberOfUpdates number of required regular updates
         * @param calculationRatio ratio of this part in context of the whole
         * process
         * @return suggested interval (number of steps) per each progress update
         */
        public static int calculateInterval( int size, int numberOfUpdates, double calculationRatio ) {
            int n = (int) ( Math.ceil( size / ( numberOfUpdates * calculationRatio ) ) + 0.0000001 );
            if ( n <= 0 ) {
                n = 1;
            }
            return n;
        }
    }
}
