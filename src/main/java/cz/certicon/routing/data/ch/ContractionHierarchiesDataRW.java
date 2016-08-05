/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.ch;

import cz.certicon.routing.application.preprocessing.ch.Preprocessor;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.ch.ChDataFactory;
import java.io.IOException;

/**
 * Read/write interface for the data of the ContractionHierarchies algorithm. It
 * is type-independent as the data class is determined by the provided
 * factories.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ContractionHierarchiesDataRW {

    /**
     * Reads the CH data without a preprocessing option - throws Exception
     * instead. Use the
     * {@link #read(cz.certicon.routing.model.entity.ch.ChDataFactory, cz.certicon.routing.model.entity.Graph, cz.certicon.routing.application.preprocessing.ch.Preprocessor) read(factory, graph, preprocessor)}
     * instead if you need the preprocessing option.
     *
     *
     * @param <T> CH data type
     * @param chDataFactory factory for the CH data builder and extractor
     * @return CH data
     * @throws NotPreprocessedException thrown when the data are not present in
     * the given data source
     * @throws IOException thrown when an IO exception occurs
     */
    public <T> T read( ChDataFactory<T> chDataFactory ) throws NotPreprocessedException, IOException;

    /**
     * Reads the CH data. If the data are not present, the preprocessing is
     * performed on the data source, which is therefore updated with the
     * preprocessed data. The preprocessing can take a long time to compute.
     *
     * @param <T> CH data type
     * @param chDataFactory factory for the CH data builder and extractor
     * @param graph an instance of {@link Graph} to be preprocessed upon
     * @param preprocessor the preprocessor implementation
     * @return CH data
     * @throws IOException thrown when an IO exception occurs
     */
    public <T> T read( ChDataFactory<T> chDataFactory, Graph graph, Preprocessor<T> preprocessor ) throws IOException;

    /**
     * Writes the CH data into the data target
     *
     * @param <T> CH data type
     * @param chDataFactory factory for the CH data builder and extractor
     * @param entity CH data
     * @throws IOException thrown when an IO exception occurs
     */
    public <T> void write( ChDataFactory<T> chDataFactory, T entity ) throws IOException;
}
