/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public enum DistanceType {
    TIME {
        @Override
        public double calculateDistance( double length, double speed ) {
            return length / speed;
        }
    }, LENGTH {
        @Override
        public double calculateDistance( double length, double speed ) {
            return length;
        }
    };

    public abstract double calculateDistance( double length, double speed );
}
