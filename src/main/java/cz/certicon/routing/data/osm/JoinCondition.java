/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.osm;

import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Node;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface JoinCondition {

    public boolean shouldJoin( Node node, List<Edge> edges );

    public static class EdgePair {

        public final Edge first;
        public final Edge second;

        public static EdgePair getSortedPair( Node node, List<Edge> edges ) {
            Edge first;
            Edge second;
            if ( edges.get( 0 ).getTargetNode().equals( node ) ) {
                first = edges.get( 0 );
                second = edges.get( 1 );
            } else {
                first = edges.get( 1 );
                second = edges.get( 0 );
            }
            return new EdgePair( first, second );
        }

        public EdgePair( Edge first, Edge second ) {
            this.first = first;
            this.second = second;
        }
    }
}
