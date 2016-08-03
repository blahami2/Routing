/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

/**
 * Interface for coordinate set builder factory
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> coordinate set type
 */
public interface CoordinateSetBuilderFactory<T> {

    /**
     * Creates new coordinate set builder
     *
     * @return new coordinate set builder
     */
    public CoordinateSetBuilder<T> createCoordinateSetBuilder();
}
