/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import java.io.IOException;

/**
 * The root interface for data import.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface MapDataSource {

    public MapDataSource setRestrictions( Restriction restriction );

    /**
     * Loads a graph from the data source. The graph is created using factories.
     * It is created asynchronously using the {@link GraphLoadListener} as
     * callback.
     *
     * @param graphEntityFactory factory for creating graph-related objects
     * @param distanceFactory factory for creating distances
     * @param graphLoadListener callback method for graph-creation completion
     * @throws IOException thrown when an error occurs during the data retrieval
     */
    public void loadGraph( GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, GraphLoadListener graphLoadListener ) throws IOException;

    /**
     * An interface for callback method (on graph creation) used by
     * {@link DataSource}
     */
    public interface GraphLoadListener {

        /**
         * Method called when the graph is created.
         *
         * @param graph filled instance of {@link Graph}
         */
        public void onGraphLoaded( Graph graph );
    }
}
