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
public class Length {

    private final LengthUnits lengthUnits;
    private final long nanometers;

    public Length( LengthUnits timeUnits, long length ) {
        this.lengthUnits = timeUnits;
        this.nanometers = timeUnits.toNano( length );
    }

    public LengthUnits getLengthUnits() {
        return lengthUnits;
    }

    public long getNanometers() {
        return nanometers;
    }

    public long getLength() {
        return lengthUnits.fromNano( nanometers );
    }

    public String getUnit() {
        return lengthUnits.getUnit();
    }

    @Override
    public String toString() {
        return "" + getLength();
    }
}
