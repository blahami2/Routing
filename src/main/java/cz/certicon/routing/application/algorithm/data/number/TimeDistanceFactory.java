/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.data.number;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.utils.CoordinateUtils;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class TimeDistanceFactory implements DistanceFactory {

    @Override
    public Distance createZeroDistance() {
        return new DoubleDistance( 0 );
    }

    @Override
    public Distance createInfiniteDistance() {
        return new DoubleDistance();
    }

    @Override
    public Distance createFromEdgeData( EdgeData edgeData ) {
        return new DoubleDistance( 3.6 * edgeData.getLength() / 1000 / edgeData.getSpeed() );
    }

    @Override
    public Distance createFromEdgeDataAndLength( EdgeData edgeData, double length ) {
        return new DoubleDistance( 3.6 * length / edgeData.getSpeed() );
    }

    @Override
    public Distance createApproximateFromCoordinates( Coordinate a, Coordinate b ) {
        return new DoubleDistance( 3.6 * CoordinateUtils.calculateDistance( a, b ) / ( 130 /* WTF is this number??? Answer: it is maximal speed - 130 kmph*/ ) );
    }

}
