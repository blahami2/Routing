/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.measuring;

/**
 * Enumeration for memory units. Supports conversion and string representation.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public enum MemoryUnits {
    /**
     * Bytes
     */
    BYTES( 1, "B" ),
    /**
     * Kilobytes
     */
    KILOBYTES( BYTES.getDivisor() * 1000, "KB" ),
    /**
     * Megabytes
     */
    MEGABYTES( KILOBYTES.getDivisor() * 1000, "MB" ),
    /**
     * Gigabytes
     */
    GIGABYTES( MEGABYTES.getDivisor() * 1000, "GB" );

    private final long byteDivisor;
    private final String unit;

    private MemoryUnits( long byteDivisor, String unit ) {
        this.byteDivisor = byteDivisor;
        this.unit = unit;
    }

    private long getDivisor() {
        return byteDivisor;
    }

    /**
     * Converts bytes to current units
     *
     * @param bytes value in bytes
     * @return value in current units
     */
    public long fromBytes( long bytes ) {
        return bytes / byteDivisor;
    }

    /**
     * Converts current units to bytes
     *
     * @param value value in current units
     * @return value in bytes
     */
    public long toBytes( long value ) {
        return value * byteDivisor;
    }

    /**
     * Returns string representation of current units
     *
     * @return string representation of current units
     */
    public String getUnit() {
        return unit;
    }

}
