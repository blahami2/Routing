/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.preprocessing;

import cz.certicon.routing.model.utility.progress.SimpleProgressListener;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.utility.ProgressListener;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <PreprocessedData> data as a result of preprocessing
 */
public interface Preprocessor<PreprocessedData> {

    /**
     * Returns preprocessed data based on the implementation
     *
     * @param graphInput an instance of {@link Graph} to calculate on
     * @param graphEntityFactory an instance of {@link GraphEntityFactory}
     * related with the given graph
     * @param distanceFactory an instance of {@link DistanceFactory} related
     * with the given graph
     * @return an instance of {@link PreprocessedData}
     */
    public PreprocessedData preprocess( Graph graphInput, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory );

    /**
     * Returns preprocessed data based on the implementation
     *
     * @param graph an instance of {@link Graph} to calculate on
     * @param graphEntityFactory an instance of {@link GraphEntityFactory}
     * related with the given graph
     * @param distanceFactory an instance of {@link DistanceFactory} related
     * with the given graph
     * @param progressListener listener for progress update
     * @param minimalShortcutId minimal shortcut ID to be set for new shortcuts =&gt; current max + 1
     * @return an instance of {@link PreprocessedData}
     */
    public PreprocessedData preprocess( Graph graph, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, ProgressListener progressListener, long minimalShortcutId );

    
}
