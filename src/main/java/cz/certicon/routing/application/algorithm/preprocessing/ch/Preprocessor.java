/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.preprocessing.ch;

import cz.certicon.routing.model.entity.DistanceType;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.ch.ChDataBuilder;
import cz.certicon.routing.model.utility.ProgressListener;

/**
 * Preprocessor class for creating the {@link PreprocessedData}
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <PreprocessedData> data as a result of preprocessing
 */
public interface Preprocessor<PreprocessedData> {

    /**
     * Returns preprocessed data based on the implementation
     *
     * @param dataBuilder an instance of {@link ChDataBuilder}
     * @param graphInput an instance of {@link Graph} to calculate on
     * @param distanceType distance metric
     * @return an instance of {@link PreprocessedData}
     * @param minimalShortcutId minimal shortcut ID to be set for new shortcuts
     * =&gt; current max + 1
     */
    public PreprocessedData preprocess( ChDataBuilder<PreprocessedData> dataBuilder, Graph graphInput, DistanceType distanceType, long minimalShortcutId );

    /**
     * Returns preprocessed data based on the implementation
     *
     * @param dataBuilder an instance of {@link ChDataBuilder}
     * @param graph an instance of {@link Graph} to calculate on
     * @param distanceType distance metric
     * @param minimalShortcutId minimal shortcut ID to be set for new shortcuts
     * =&gt; current max + 1
     * @param progressListener listener for progress update
     * @return an instance of {@link PreprocessedData}
     */
    public PreprocessedData preprocess( ChDataBuilder<PreprocessedData> dataBuilder, Graph graph, DistanceType distanceType, long minimalShortcutId, ProgressListener progressListener );

    public void setNodeRecalculationStrategy( NodeRecalculationStrategy nodeRecalculationStrategy );

    public void setEdgeDifferenceCalculator( EdgeDifferenceCalculator edgeDifferenceCalculator );

}
