/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.utility;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ProgressListener {

    public int getNumOfUpdates();

    public void setNumOfUpdates( int numOfUpdates );

    public void init( int size, double calculationRatio );

    public void onProgressUpdate( double done );

    /**
     * Returns true when the progress update was called, false otherwise
     *
     * @return boolean value indicating, whether the progress update was called
     * or not
     */
    public boolean nextStep();
}
