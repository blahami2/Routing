/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.common;

import cz.certicon.routing.application.Route;
import cz.certicon.routing.model.basic.Pair;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Basic implementation of the {@link Route} interface.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleRoute implements Route {

    private final LinkedList<Pair<Long, Boolean>> edges;
    private final long source;
    private final long target;
    private final long singleEdge;

    public SimpleRoute( LinkedList<Pair<Long, Boolean>> edges, long source, long target, long singleEdge ) {
        this.edges = edges;
        this.source = source;
        this.target = target;
        this.singleEdge = singleEdge;
    }

    @Override
    public Iterator<Pair<Long, Boolean>> getEdgeIterator() {
        return edges.iterator();
    }

    @Override
    public long getTarget() {
        return target;
    }

    @Override
    public long getSource() {
        return source;
    }

    @Override
    public int getEdgeCount() {
        return edges.size();
    }

    @Override
    public boolean isSingleEdged() {
        return singleEdge >= 0;
    }

    @Override
    public long getSingleEdge() {
        return singleEdge;
    }

}
