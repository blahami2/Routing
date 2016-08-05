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
     * Returns amount of edges in this route
     *
     * @return amount of edges
     */
    public int getEdgeCount();

    /**
     * Return true, if the route is not actually routable - both points are on
     * the same edge in the correct order - only relevant for displaying
     *
     * @return true if the route is single-edges
     */
    public boolean isSingleEdged();

    /**
     * Returns single edge for display purposes, see
     * {@link #isSingleEdged() isSingleEdged()}
     *
     * @return single edge for display purposes
     */
    public long getSingleEdge();

}
