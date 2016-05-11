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
public class EmptyProgressListener implements ProgressListener {
    
    @Override
    public void onProgressUpdate( double done ) {
    }

    @Override
    public void nextStep() {
    }

    @Override
    public int getNumOfUpdates() {
        return 0;
    }

    @Override
    public void setNumOfUpdates( int numOfUpdates ) {
    }

    @Override
    public void init( int size, double calculationRatio ) {
    }
    
}
