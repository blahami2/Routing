/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.memsensitive.model.entity.Path;
import cz.certicon.routing.model.entity.Coordinates;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimplePath implements Path {
    
    private final ArrayList<Coordinates> coordinates;
    private final double length;
    private final double time;

    public SimplePath( ArrayList<Coordinates> coordinates, double length, double time ) {
        this.coordinates = coordinates;
        this.length = length;
        this.time = time;
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public double getTime() {
        return time;
    }

    @Override
    public List<Coordinates> getCoordinates() {
        return coordinates;
    }

}
