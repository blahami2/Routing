/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

import java.util.Objects;

/**
 * Class representing the length of something. It supports unit conversion,
 * formatting and so on.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Length {

    private final LengthUnits lengthUnits;
    private final long nanometers;

    /**
     * Creates an instance via an amount and units
     *
     * @param lengthUnits the length units in which the length is provided
     * @param length actual value
     */
    public Length( LengthUnits lengthUnits, long length ) {
        this.lengthUnits = lengthUnits;
        this.nanometers = lengthUnits.toNano( length );
    }

    /**
     * Returns current units
     *
     * @return current units
     */
    public LengthUnits getLengthUnits() {
        return lengthUnits;
    }

    /**
     * Returns length in nanometers
     *
     * @return length in nanometers
     */
    public long getNanometers() {
        return nanometers;
    }

    /**
     * Returns length in the current units
     *
     * @return length in the current units
     */
    public long getLength() {
        return lengthUnits.fromNano( nanometers );
    }

    /**
     * Returns length in the provided units
     *
     * @param lengthUnits provided units
     * @return length in the provided units
     */
    public long getLength( LengthUnits lengthUnits ) {
        return lengthUnits.fromNano( nanometers );
    }

    /**
     * Returns string representing the length in the provided units
     *
     * @param lengthUnits provided units
     * @return string in format "%d %s", length, unit
     */
    public String toString( LengthUnits lengthUnits ) {
        return getLength( lengthUnits ) + " " + lengthUnits.getUnit();
    }

    /**
     * Returns string representation of the current units
     *
     * @return string representation of the current units
     */
    public String getUnit() {
        return lengthUnits.getUnit();
    }

    @Override
    public String toString() {
        return "" + getLength();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode( this.lengthUnits );
        hash = 97 * hash + (int) ( this.nanometers ^ ( this.nanometers >>> 32 ) );
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
        final Length other = (Length) obj;
        if ( this.nanometers != other.nanometers ) {
            return false;
        }
        if ( this.lengthUnits != other.lengthUnits ) {
            return false;
        }
        return true;
    }

}
