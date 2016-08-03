/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.model.basic.Length;
import cz.certicon.routing.model.basic.Time;
import cz.certicon.routing.model.entity.Coordinate;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of the {@link Path} interface. Uses maps internally.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimplePath implements Path {

    private final ArrayList<Coordinate> coordinates;
    private final Length length;
    private final Time time;

    /**
     * Constructor of {@link SimplePath}
     *
     * @param coordinates path coordinates sequence
     * @param length path length
     * @param time path time
     */
    public SimplePath( ArrayList<Coordinate> coordinates, Length length, Time time ) {
        this.coordinates = coordinates;
        this.length = length;
        this.time = time;
    }

    @Override
    public Length getLength() {
        return length;
    }

    @Override
    public Time getTime() {
        return time;
    }

    @Override
    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

}
