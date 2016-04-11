/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.measuring;

/**
 * Time measurement class. Uses {@link TimeUnits} to determine the time unit.
 * Default is NANOSECONDS.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class TimeMeasurement {

    private TimeUnits timeUnits = TimeUnits.NANOSECONDS;
    private long start;
    private long time;

    public void start() {
        start = System.nanoTime();
    }

    public void setTimeUnits( TimeUnits timeUnits ) {
        this.timeUnits = timeUnits;
    }

    /**
     * Stops the timer, saves the elapsed time and returns it.
     *
     * @return elapsed time in {@link TimeUnits}
     */
    public long stop() {
        time = getCurrentTimeElapsed();
        return timeUnits.fromNano( time );
    }

    /**
     * Returns the last saved elapsed time. Does not start nor stop the timer.
     *
     * @return last saved elapsed time in (@link TimeUnits}
     */
    public long getTimeElapsed() {
        return timeUnits.fromNano( time );
    }

    /**
     * Returns the elapsed time. Does not stop the timer (does not save it).
     *
     * @return elapsed time in (@link TimeUnits}
     */
    public long getCurrentTimeElapsed() {
        return timeUnits.fromNano( ( System.nanoTime() - start ) );
    }
}
