/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface MapDataMinimizer {

    public MapDataMinimizer setRestrictions( Restriction restriction );

    public void loadGraph( GraphMinimizeListener graphMinimizeListener ) throws IOException;

    /**
     * An interface for callback method (on graph creation) used by
     * {@link DataSource}
     */
    public interface GraphMinimizeListener {

        /**
         * Method called when the graph is created.
         *
         * @param graphDataSource {@link DataSource} of the minimized graph
         */
        public void onGraphMinimized( DataSource graphDataSource );
    }
}
