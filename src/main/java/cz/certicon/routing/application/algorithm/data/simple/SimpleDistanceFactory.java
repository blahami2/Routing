/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.data.simple;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;

/**
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleDistanceFactory implements DistanceFactory {

    @Override
    public Distance createZeroDistance() {
        return new DistanceImpl( 0 );
    }

    @Override
    public Distance createInfiniteDistance() {
        return new DistanceImpl();
    }

    @Override
    public Distance createFromDouble( double d ) {
        return new DistanceImpl( d );
    }

}
