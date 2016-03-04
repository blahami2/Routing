/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class GraphUtils {

    public static Path createPath( Graph graph, GraphEntityFactory graphEntityFactory, Node sourceNode, Node targetNode ) {
        Path path = graphEntityFactory.createPathWithTarget( graph, targetNode );
        Node currentNode = targetNode;
        while ( !currentNode.equals( sourceNode ) ) {
            path.addEdgeAsFirst( currentNode.getPredecessorEdge() );
            currentNode = graph.getOtherNodeOf( currentNode.getPredecessorEdge(), currentNode );
        }
        return path;
    }
}
