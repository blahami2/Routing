/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

/**
 * Enumerate containing available time units.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public enum TimeUnits {
    NANOSECONDS( 1, "ns" ), MICROSECONDS( NANOSECONDS.getDivisor() * 1000, "mcs" ), MILLISECONDS( MICROSECONDS.getDivisor() * 1000, "ms" ), SECONDS( MILLISECONDS.getDivisor() * 1000, "s" ), MINUTES( SECONDS.getDivisor() * 60, "min" ), HOURS( MINUTES.getDivisor() * 60, "h" ), DAYS( HOURS.getDivisor() * 24, "days" );

    private final long nanoDivisor;
    private final String unit;

    private TimeUnits( long nanoDivisor, String unit ) {
        this.nanoDivisor = nanoDivisor;
        this.unit = unit;
    }

    private long getDivisor() {
        return nanoDivisor;
    }

    /**
     * Returns nanoseconds converted to these units.
     *
     * @param nanoseconds value in nanometers
     * @return converted value in these units
     */
    public long fromNano( long nanoseconds ) {
        return nanoseconds / nanoDivisor;
    }

    /**
     * Returns nanoseconds converted from these units
     *
     * @param time value in these units
     * @return converted value in nanoseconds
     */
    public long toNano( long time ) {
        return time * nanoDivisor;
    }

    /**
     * Returns string representation of these units
     *
     * @return string representation of these units
     */
    public String getUnit() {
        return unit;
    }
}
