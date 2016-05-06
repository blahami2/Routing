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

    private TimeUnits timeUnits = TimeUnits.MILLISECONDS;
    private long start = -1;
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
        if ( start == -1 ) {
            return 0;
        }
        time = System.nanoTime() - start;
        return timeUnits.fromNano( time );
    }

    /**
     * Returns the last saved elapsed time. Does not start nor stop the timer.
     *
     * @return last saved elapsed time in (@link TimeUnits}
     */
    public long getTimeElapsed() {
        if ( start == -1 ) {
            return 0;
        }
        return timeUnits.fromNano( time );
    }

    /**
     * Returns the elapsed time. Does not stop the timer (does not save it).
     *
     * @return elapsed time in (@link TimeUnits}
     */
    public long getCurrentTimeElapsed() {
        if ( start == -1 ) {
            return 0;
        }
        return timeUnits.fromNano( ( System.nanoTime() - start ) );
    }

    public long restart() {
        long a = stop();
        start();
        return a;
    }

    public void clear() {
        start = -1;
    }

    public String getTimeString() {
        return getCurrentTimeElapsed() + " " + timeUnits.getUnit();
    }
}
