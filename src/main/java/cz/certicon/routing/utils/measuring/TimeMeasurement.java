/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.measuring;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class TimeMeasurement {

    private static final long TO_MILLISECONDS = 1;

    private long start;
    private long time;

    public void start() {
        start = System.nanoTime();
    }

    public long stop() {
        time = getCurrentTimeElapsed();
        return time / TO_MILLISECONDS;
    }

    public long getTimeElapsed() {
        return time / TO_MILLISECONDS;
    }

    public long getCurrentTimeElapsed() {
        return ( System.nanoTime() - start ) / TO_MILLISECONDS;
    }
}
