/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

import java.util.Objects;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Time {

    private final TimeUnits timeUnits;
    private final long nanoseconds;

    public Time( TimeUnits timeUnits, long time ) {
        this.timeUnits = timeUnits;
        this.nanoseconds = timeUnits.toNano( time );
    }

    public TimeUnits getTimeUnits() {
        return timeUnits;
    }

    public long getNanoseconds() {
        return nanoseconds;
    }

    public long getTime() {
        return timeUnits.fromNano( nanoseconds );
    }

    public long getTime( TimeUnits timeUnits ) {
        return timeUnits.fromNano( nanoseconds );
    }

    public String getUnit() {
        return timeUnits.getUnit();
    }

    public Time add( Time time ) {
        return new Time( timeUnits, getTime() + timeUnits.fromNano( time.getNanoseconds() ) );
    }

    public Time divide( long divisor ) {
        return new Time( timeUnits, getTime() / divisor );
    }

    @Override
    public String toString() {
        return "" + getTime();
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
