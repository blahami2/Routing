/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.data.number;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.model.entity.EdgeAttributes;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DoubleDistanceFactory implements DistanceFactory {

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
    public Distance createFromEdgeAttributes( EdgeAttributes edgeAttributes ) {
        return new DoubleDistance( edgeAttributes.getLength() );
    }

}
