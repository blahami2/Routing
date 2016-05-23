/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.ch;

import cz.certicon.routing.data.DistanceType;
import cz.certicon.routing.application.preprocessing.ch.ContractionHierarchiesPreprocessor;
import cz.certicon.routing.data.Reader;
import cz.certicon.routing.data.Writer;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Shortcut;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ContractionHierarchiesDataRW extends
        Writer<Trinity<Map<Node.Id, Integer>, List<Shortcut>, DistanceType>>,
        Reader<Trinity<Graph, GraphEntityFactory, DistanceType>, Trinity<Map<Node.Id, Integer>, List<Shortcut>, DistanceType>> {

    public void setPreprocessor(ContractionHierarchiesPreprocessor preprocessor);
}
