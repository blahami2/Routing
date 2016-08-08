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

    /**
     * Constructor
     *
     * @param x x
     * @param y y
     * @param z z
     */
    public CartesianCoords( double x, double y, double z ) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns x-axis coordinate
     * @return x-axis coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Returns y-axis coordinate
     * @return y-axis coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Returns z-axis coordinate
     * @return z-axis coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Returns x-axis coordinate as integer (typed)
     * @return x-axis coordinate as integer (typed)
     */
    public int getXAsInt() {
        return Math.round( (float) x );
    }

    /**
     * Returns y-axis coordinate as integer (typed)
     * @return y-axis coordinate as integer (typed)
     */
    public int getYAsInt() {
        return Math.round( (float) y );
    }

    /**
     * Returns z-axis coordinate as integer (typed)
     * @return z-axis coordinate as integer (typed)
     */
    public int getZAsInt() {
        return Math.round( (float) z );
    }
}
