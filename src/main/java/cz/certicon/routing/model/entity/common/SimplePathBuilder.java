/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.model.entity.PathBuilder;
import cz.certicon.routing.model.basic.Length;
import cz.certicon.routing.model.basic.LengthUnits;
import cz.certicon.routing.model.basic.Time;
import cz.certicon.routing.model.basic.TimeUnits;
import cz.certicon.routing.model.entity.Coordinate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple implementation of the {@link PathBuilder} interface. Uses maps
 * internally.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimplePathBuilder implements PathBuilder<Path, Graph> {

    private final ArrayList<Coordinate> coordinates = new ArrayList<>();
    private List<Coordinate> start = null;
    private List<Coordinate> end = null;
    private double time = 0;
    private double length = 0;

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
    public void addStartEdge( Graph graph, long edgeId, boolean isForward, List<Coordinate> coordinates, double length, double time ) {
        if ( !isForward ) {
            Collections.reverse( coordinates );
        }
        if ( this.coordinates.isEmpty() ) {
            this.coordinates.addAll( coordinates );
        } else {
            start = coordinates;
        }
        addLength( length );
        addTime( time );
    }

    @Override
    public void addEndEdge( Graph graph, long edgeId, boolean isForward, List<Coordinate> coordinates, double length, double time ) {
        if ( !isForward ) {
            Collections.reverse( coordinates );
        }
        end = coordinates;
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
//        System.out.println( "adding length: " + length );
    }

    @Override
    public void addTime( double time ) {
        this.time += time;
//        System.out.println( "adding time: " + time );
    }

    @Override
    public void clear() {
        coordinates.clear();
        start = null;
        end = null;
        time = 0;
        length = 0;
    }

    @Override
    public Path build( Graph graph, Coordinate sourceCoordinate, Coordinate targetCoordinate ) {
        ArrayList<Coordinate> coords;
        if ( start != null ) {
            coords = new ArrayList<>( start );
            coords.addAll( coordinates );
        } else {
            coords = coordinates;
        }
        if ( end != null ) {
            coords.addAll( end );
        }
        if ( coords.isEmpty() ) {
            coords.add( sourceCoordinate );
            coords.add( targetCoordinate );
        }
        return new SimplePath( coords, new Length( LengthUnits.METERS, (long) length ), new Time( TimeUnits.SECONDS, (long) time ) );
    }

}
