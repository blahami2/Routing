/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

/**
 * Class representation of Cartesian coordinates.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class CartesianCoords {

    private final double x;
    private final double y;
    private final double z;

    public CartesianCoords( double x, double y, double z ) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getXAsInt() {
        return Math.round( (float) x );
    }

    public int getYAsInt() {
        return Math.round( (float) y );
    }

    public int getZAsInt() {
        return Math.round( (float) z );
    }
}
