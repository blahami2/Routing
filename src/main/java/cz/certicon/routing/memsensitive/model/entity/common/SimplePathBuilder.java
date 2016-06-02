/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.memsensitive.algorithm.Route;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.Path;
import cz.certicon.routing.memsensitive.model.entity.PathBuilder;
import cz.certicon.routing.model.basic.Length;
import cz.certicon.routing.model.basic.LengthUnits;
import cz.certicon.routing.model.basic.Time;
import cz.certicon.routing.model.basic.TimeUnits;
import cz.certicon.routing.model.entity.Coordinate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimplePathBuilder implements PathBuilder<Path, Graph> {

    private final ArrayList<Coordinate> coordinates = new ArrayList<>();
    private double time = 0;
    private double length = 0;

    public SimplePathBuilder() {
    }

    @Override
    public void addEdge( Graph graph, long edgeId, boolean isForward, List<Coordinate> edgeCoordinates, double length, double time ) {
        if ( !isForward ) {
            Collections.reverse( edgeCoordinates );
        }
        this.coordinates.addAll( edgeCoordinates );
        addLength( length );
        addTime( time );
    }

    @Override
    public void addCoordinates( Coordinate coords ) {
        this.coordinates.add( coords );
    }

    @Override
    public void addLength( double length ) {
        this.length += length;
    }

    @Override
    public void addTime( double time ) {
        this.time += time;
    }

    @Override
    public void clear() {
        coordinates.clear();
        time = 0;
        length = 0;
    }

    @Override
    public Path build() {
        return new SimplePath( coordinates, new Length( LengthUnits.NANOMETERS, (long) ( length * 10E9 ) ), new Time( TimeUnits.NANOSECONDS, (long) ( time * 10E9 ) ) );
    }

}
