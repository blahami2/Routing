/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.algorithms.dijkstra;

import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface EndCondition {

    public boolean isFinished( Graph graph, Node sourceNode, Node targetNode, Node currentNode );

    public Path getResult( Graph graph, GraphEntityFactory graphEntityFactory, Node sourceNode, Node targetNode );
}
