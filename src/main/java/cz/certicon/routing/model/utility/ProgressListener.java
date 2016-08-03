/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.utility;

/**
 * Interface defining the progress listener functionality. It can be adjusted
 * via number of updates (number steps taken from 0 to number of updates) or
 * size and calculation ratio ({@link #init(int, double) init}).
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ProgressListener {

    /**
     * Returns number of updates
     *
     * @return number of updates
     */
    public int getNumOfUpdates();

    /**
     * Sets number of updates. Number of updates is the amount of steps taken
     * from the beginning to the end.
     *
     * @param numOfUpdates number of updates
     */
    public void setNumOfUpdates( int numOfUpdates );

    /**
     * Initializes the progress listener. Can be called multiple times - sum of
     * the calculation ratios of multiple calls must add up to 1.
     *
     * @param size amount of operations - calls to next step
     * @param calculationRatio ratio of the current set of updates - for
     * example, having size = 100 and calculation ratio = 0.5 and number of
     * updates = 100, progress update is performed every 2 steps, since size 100
     * is related to only 50 updates (100 * 0.5).
     */
    public void init( int size, double calculationRatio );

    /**
     * Method called when enough progress has been made to call an update.
     *
     * @param done ratio of finished operations to all the operations
     */
    public void onProgressUpdate( double done );

    /**
     * Returns true when the progress update was called, false otherwise
     *
     * @return boolean value indicating, whether the progress update was called
     * or not
     */
    public boolean nextStep();
}
