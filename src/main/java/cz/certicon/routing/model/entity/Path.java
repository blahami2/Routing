/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.model.basic.Length;
import cz.certicon.routing.model.basic.Time;
import java.util.List;

/**
 * Interface defining the path - path from source to target with all the
 * required data, such as geometry, length, time, etc.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Path {

    /**
     * Returns {@link Length} of the path
     *
     * @return {@link Length} of the path
     */
    public Length getLength();

    /**
     * Returns {@link Time} of the path (time required to travel from source to
     * target)
     *
     * @return {@link Time} of the path
     */
    public Time getTime();

    /**
     * Returns path's geometry (list of coordinates)
     *
     * @return path's geometry
     */
    public List<Coordinate> getCoordinates();

}
