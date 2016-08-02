/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.utility.progress;

import cz.certicon.routing.model.utility.ProgressListener;
import cz.certicon.routing.utils.DoubleComparator;

/**
 * Simple implementation of the {@link ProgressListener}. Implicitly it travels
 * over percentages: from 0 to 100 via steps of size 1. It can be adjusted via
 * number of updates (number steps taken from 0 to number of updates) or size
 * and calculation ratio ({@link #init(int, double) init}).
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public abstract class SimpleProgressListener implements ProgressListener {

    private static final double PRECISION = 10E-9;

    private int numOfUpdates = 100;
    private long counter = 0;
    private int size = 1;
    private double calculationRatio = 0.0;

    private double last = 0.0;
    private double add = 0.0;
    private double step = 1.0;

    public SimpleProgressListener() {
    }

    public SimpleProgressListener( int numberOfUpdates ) {
        this.numOfUpdates = numberOfUpdates;
    }

    @Override
    public int getNumOfUpdates() {
        return numOfUpdates;
    }

    @Override
    public void setNumOfUpdates( int numOfUpdates ) {
        this.numOfUpdates = numOfUpdates;
    }

    @Override
    public boolean nextStep() {
        double current = (double) ++counter / size;
        if ( DoubleComparator.isLowerOrEqualTo( last + step, current, PRECISION ) ) {
            last = current;
            onProgressUpdate( add + ( last * calculationRatio ) );
            return true;
        }
        if ( counter == size ) {
            onProgressUpdate( add + calculationRatio );
            return true;
        }
        return false;
    }

    @Override
    final public void init( int size, double calculationRatio ) {
        add += this.calculationRatio;
        this.calculationRatio = calculationRatio;
        this.size = size;
        step = 1 / ( numOfUpdates * calculationRatio );
        counter = 0;
        last = 0;
    }

}
