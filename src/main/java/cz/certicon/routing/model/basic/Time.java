/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

import java.util.Objects;

/**
 * Class representation of time. Supports conversion, formatting, etc.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Time {

    private final TimeUnits timeUnits;
    private final long nanoseconds;

    /**
     * Creates an instance via an amount and units
     *
     * @param timeUnits time units in which the length is provided
     * @param time actual value
     */
    public Time( TimeUnits timeUnits, long time ) {
        this.timeUnits = timeUnits;
        this.nanoseconds = timeUnits.toNano( time );
    }

    /**
     * Returns current units
     *
     * @return current units
     */
    public TimeUnits getTimeUnits() {
        return timeUnits;
    }

    /**
     * Returns time in nanoseconds
     *
     * @return time in nanoseconds
     */
    public long getNanoseconds() {
        return nanoseconds;
    }

    /**
     * Returns time in the current units
     *
     * @return time in the current units
     */
    public long getTime() {
        return timeUnits.fromNano( nanoseconds );
    }

    /**
     * Returns time in the provided units
     *
     * @param timeUnits provided units
     * @return time in the provided units
     */
    public long getTime( TimeUnits timeUnits ) {
        return timeUnits.fromNano( nanoseconds );
    }

    /**
     * Returns string representation of the current units
     *
     * @return string representation of the current units
     */
    public String getUnit() {
        return timeUnits.getUnit();
    }

    /**
     * Adds time to this time. Returns new instance.
     *
     * @param time other time
     * @return new instance of {@link Time} as a result of addition of this time
     * and the other time
     */
    public Time add( Time time ) {
        return new Time( timeUnits, getTime() + timeUnits.fromNano( time.getNanoseconds() ) );
    }

    /**
     * Divides this time by a provided divisor. Returns new instance.
     *
     * @param divisor number for the time to be divided by
     * @return new instance of {@link Time} as a result of division of this time
     * by the divisor
     */
    public Time divide( long divisor ) {
        return new Time( timeUnits, getTime() / divisor );
    }

    @Override
    public String toString() {
        return "" + getTime();
    }

    /**
     * Returns string representing the time in the provided units
     *
     * @param timeUnits provided units
     * @return string in format "%d %s", time, unit
     */
    public String toString( TimeUnits timeUnits ) {
        return getTime( timeUnits ) + " " + timeUnits.getUnit();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode( this.timeUnits );
        hash = 71 * hash + (int) ( this.nanoseconds ^ ( this.nanoseconds >>> 32 ) );
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final Time other = (Time) obj;
        if ( this.nanoseconds != other.nanoseconds ) {
            return false;
        }
        if ( this.timeUnits != other.timeUnits ) {
            return false;
        }
        return true;
    }

}
