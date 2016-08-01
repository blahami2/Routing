/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

/**
 * Enumeration representing the possible length units.
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

    /**
     * Returns nanometers converted to these units.
     *
     * @param nanometers value in nanometers
     * @return converted value in these units
     */
    public long fromNano( long nanometers ) {
        return nanometers / nanoDivisor;
    }

    /**
     * Returns nanometers converted from these units
     *
     * @param length value in these units
     * @return converted value in nanometers
     */
    public long toNano( long length ) {
        return length * nanoDivisor;
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
