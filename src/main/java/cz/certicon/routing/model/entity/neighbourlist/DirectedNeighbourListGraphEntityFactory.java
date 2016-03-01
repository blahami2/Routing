/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.neighbourlist;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;

/**
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public class DirectedNeighbourListGraphEntityFactory implements GraphEntityFactory {

    @Override
    public Node createNode( double latitude, double longitude ) {
        return new NodeImpl( latitude, longitude );
    }

    @Override
    public Edge createEdge( Node sourceNode, Node targetNode, Distance length ) {
        return new EdgeImpl( sourceNode, targetNode, length );
    }

    @Override
    public Path createPath( Graph graph ) {
        return new PathImpl( graph );
    }

    @Override
    public Graph createGraph() {
        return new DirectedNeighbourListGraph();
    }

}
