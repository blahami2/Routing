/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public enum LengthUnits {
    NANOMETERS( 1, "nm" ), MICROMETERS( NANOMETERS.getDivisor() * 1000, "mcm" ), MILLIMERERS( MICROMETERS.getDivisor() * 1000, "mm" ), METERS( MILLIMERERS.getDivisor() * 1000, "m" ), KILOMETERS( METERS.getDivisor() * 1000, "km" );

    private final long nanoDivisor;
    private final String unit;

    private LengthUnits( long nanoDivisor, String unit ) {
        this.nanoDivisor = nanoDivisor;
        this.unit = unit;
    }

    private long getDivisor() {
        return nanoDivisor;
    }

    public long fromNano( long nanoseconds ) {
        return nanoseconds / nanoDivisor;
    }

    public long toNano( long length ) {
        return length * nanoDivisor;
    }

    public String getUnit() {
        return unit;
    }
}
