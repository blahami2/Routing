/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.utility.progress;

import cz.certicon.routing.model.utility.ProgressListener;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public abstract class SimpleProgressListener implements ProgressListener {

    private int numOfUpdates = 100;
    private int counter = 0;
    private int interval = 1;

    private double last = 0.0;
    private double add = 0.0;

    @Override
    public int getNumOfUpdates() {
        return numOfUpdates;
    }

    @Override
    public void setNumOfUpdates( int numOfUpdates ) {
        this.numOfUpdates = numOfUpdates;
    }

    @Override
    public void nextStep() {
        if ( ++counter % interval == 0 ) {
            last = counter / (double) interval / numOfUpdates;
            onProgressUpdate( add + last );
        }
    }

    @Override
    public void init( int size, double calculationRatio ) {
        add = last;
        counter = 0;
        interval = ProgressListener.Calculator.calculateInterval( size, this.numOfUpdates, calculationRatio );
    }

}
