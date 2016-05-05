/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.ch;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.data.number.LengthDistanceFactory;
import cz.certicon.routing.application.algorithm.data.number.TimeDistanceFactory;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public enum DistanceType {
    LENGTH {
        @Override
        public DistanceFactory getDistanceFactory() {
            return new LengthDistanceFactory();
        }

    }, TIME {
        @Override
        public DistanceFactory getDistanceFactory() {
            return new TimeDistanceFactory();
        }
    };

    abstract public DistanceFactory getDistanceFactory();

    public static int toInt( DistanceType distanceType ) {
        switch ( distanceType ) {
            case LENGTH:
                return 1;
            case TIME:
                return 2;
            default:
                throw new AssertionError();
        }
    }

    public static DistanceType fromInt( int n ) {
        switch ( n ) {
            case 1:
                return LENGTH;
            case 2:
                return TIME;
            default:
                throw new IllegalArgumentException( "Unknown distance type: " + n );
        }
    }
}
