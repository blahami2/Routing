/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.ch;

import cz.certicon.routing.model.entity.DistanceType;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.ch.ChDataBuilder;
import cz.certicon.routing.model.entity.ch.ChDataExtractor;
import cz.certicon.routing.model.entity.ch.ChDataFactory;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleChDataFactory implements ChDataFactory<PreprocessedData> {

    private final Graph graph;
    private final DistanceType distanceType;

    public SimpleChDataFactory( Graph graph, DistanceType distanceType ) {
        this.graph = graph;
        this.distanceType = distanceType;
    }

    @Override
    public ChDataBuilder<PreprocessedData> createChDataBuilder() {
        return new SimpleChDataBuilder( graph, distanceType );
    }

    @Override
    public ChDataExtractor<PreprocessedData> createChDataExtractor( PreprocessedData extracted ) {
        return new SimpleChDataExtractor(graph, distanceType, extracted);
    }

}
