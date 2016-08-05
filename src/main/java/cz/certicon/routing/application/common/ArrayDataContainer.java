/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.common;

import cz.certicon.routing.utils.EffectiveUtils;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ArrayDataContainer implements DataContainer {
    
    private int[] predecessorsPrototype = new int[0];
    private double[] distancesPrototype = new double[0];

    public ArrayDataContainer( int nodeCount ) {
        int[] predecessors = new int[nodeCount];
        double[] distances = new double[nodeCount];
        if ( predecessorsPrototype.length != predecessors.length ) {
            EffectiveUtils.fillArray( predecessors, -1 );
            predecessorsPrototype = new int[predecessors.length];
            EffectiveUtils.copyArray( predecessors, predecessorsPrototype );
        } else {
            EffectiveUtils.copyArray( predecessorsPrototype, predecessors );
        }
        if ( distancesPrototype.length != distances.length ) {
            EffectiveUtils.fillArray( distances, Double.MAX_VALUE );
            distancesPrototype = new double[distances.length];
            EffectiveUtils.copyArray( distances, distancesPrototype );
        } else {
            EffectiveUtils.copyArray( distancesPrototype, distances );
        }
    }

    @Override
    public double getDistance( int node ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDistance( int node, double distance ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPredecessor( int node, int edge ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getPredecessor( int node ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
}
