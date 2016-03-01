/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

/**
 * The root interface for realization of distances between nodes or lengths of
 * edges.
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public interface Distance extends Comparable<Distance> {

    /**
     * Determines, whether this Distance is greater than the other.
     *
     * @param other other Distance
     * @return true if this Distance is greater than the other, false otherwise
     */
    public boolean isGreaterThan( Distance other );

    /**
     * Determines, whether this Distance is greater than or equal to the other.
     *
     * @param other other Distance
     * @return true if this Distance is greater than or equal to the other,
     * false otherwise
     */
    public boolean isGreaterOrEqualTo( Distance other );

    /**
     * Determines, whether this Distance is lower than the other.
     *
     * @param other other Distance
     * @return true if this Distance is lower than the other, false otherwise
     */
    public boolean isLowerThan( Distance other );

    /**
     * Determines, whether this Distance is lower than or equal to the other.
     *
     * @param other other Distance
     * @return true if this Distance is lower than or equal to the other, false
     * otherwise
     */
    public boolean isLowerOrEqualTo( Distance other );

    /**
     * Determines, whether this Distance is equal to the other.
     *
     * @param other other Distance
     * @return true if this Distance is equal to the other, false otherwise
     */
    public boolean isEqualTo( Distance other );

    /**
     * Add other Distance to this distance (+ operation).
     *
     * @param other other Distance
     * @return result of the addition (new instance)
     */
    public Distance add( Distance other );

}
