/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import java.util.List;

/**
 * Interface defining functionality for coordinate set builder
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> coordinate set type
 */
public interface CoordinateSetBuilder<T> {

    /**
     * Add coordinates to the set
     *
     * @param edgeId id of the edge, whose geometry is being added
     * @param coordinates added geometry
     */
    public void addCoordinates( long edgeId, List<Coordinate> coordinates );

    /**
     * Creates new coordinate set
     *
     * @return new coordinate set
     */
    public T build();
}
