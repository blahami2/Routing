/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.measuring;

/**
 * Enumerate containing available time units.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public enum TimeUnits {
    NANOSECONDS( 1 ), MICROSECONDS( NANOSECONDS.getDivisor() * 1000 ), MILLISECONDS( MICROSECONDS.getDivisor() * 1000 ), SECONDS( MILLISECONDS.getDivisor() * 1000 ), MINUTES( SECONDS.getDivisor() * 60 ), HOURS( MINUTES.getDivisor() * 60 ), DAYS( HOURS.getDivisor() * 24 );

    private final long nanoDivisor;

    private TimeUnits( long nanoDivisor ) {
        this.nanoDivisor = nanoDivisor;
    }

    private long getDivisor() {
        return nanoDivisor;
    }

    public long fromNano( long nanoseconds ) {
        return nanoseconds / nanoDivisor;
    }
}
