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
public enum MemoryUnits {
    BYTES( 1, "B" ), KILOBYTES( BYTES.getDivisor() * 1000, "KB" ), MEGABYTES( KILOBYTES.getDivisor() * 1000, "MB" ), GIGABYTES( MEGABYTES.getDivisor() * 1000, "GB" );

    private final long byteDivisor;
    private final String unit;

    private MemoryUnits( long byteDivisor, String unit ) {
        this.byteDivisor = byteDivisor;
        this.unit = unit;
    }

    private long getDivisor() {
        return byteDivisor;
    }

    public long fromBytes( long bytes ) {
        return bytes / byteDivisor;
    }

    public String getUnit() {
        return unit;
    }

}
