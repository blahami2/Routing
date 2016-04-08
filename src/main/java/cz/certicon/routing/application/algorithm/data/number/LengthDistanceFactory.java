/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.data.number;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.utils.CoordinateUtils;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class LengthDistanceFactory implements DistanceFactory {

    @Override
    public Distance createZeroDistance() {
        return new DoubleDistance( 0 );
    }

    @Override
    public Distance createInfiniteDistance() {
        return new DoubleDistance();
    }

//    @Override
//    public Distance createFromDouble( double d ) {
//        return new DistanceImpl( d );
//    }
    @Override
    public Distance createFromEdgeData( EdgeData edgeData ) {
        return new DoubleDistance( edgeData.getLength() );
    }

    @Override
    public Distance createApproximateFromNodes( Node a, Node b ) {
        return new DoubleDistance( CoordinateUtils.calculateDistance( a.getCoordinates(), b.getCoordinates() ) / 1000 );
    }

}
