/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application;

import cz.certicon.routing.application.common.SimpleRoute;
import cz.certicon.routing.model.basic.Pair;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * An interface defining the route - a sequence of edges with a source and a
 * target
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Route {

    /**
     * Returns edge iterator, which traverses edges in the sequence order
     *
     * @return edge iterator
     */
    public Iterator<Pair<Long, Boolean>> getEdgeIterator();

    /**
     * Returns id of the target node
     *
     * @return id of the target node
     */
    public long getTarget();

    /**
     * Returns id of the source node
     *
     * @return id of the source node
     */
    public long getSource();

    /**
     * Factory for SimpleRoute
     */
    public static class Factory {

        /**
         * Creates a simple implementation object of the route
         * 
         * @param edges list of edges in form &lt;global_edgeId, isEdgeForward&gt;
         * @param source source node of the route
         * @param target target node of the route
         * @return simple implementation object of the route
         */
        public static Route createSimpleRoute( LinkedList<Pair<Long, Boolean>> edges, long source, long target ) {
            return new SimpleRoute( edges, source, target );
        }
    }
}
