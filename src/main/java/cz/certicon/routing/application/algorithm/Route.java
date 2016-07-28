/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

import cz.certicon.routing.application.algorithm.common.SimpleRoute;
import cz.certicon.routing.model.basic.Pair;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Route {

    public Iterator<Pair<Long, Boolean>> getEdgeIterator();

    public long getTarget();

    public long getSource();

    public static class Factory {

        public static Route createSimpleRoute( LinkedList<Pair<Long, Boolean>> edges, long source, long target ) {
            return new SimpleRoute( edges, source, target );
        }
    }
}
