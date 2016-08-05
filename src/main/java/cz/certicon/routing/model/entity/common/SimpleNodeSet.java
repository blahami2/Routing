/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.NodeSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Simple implementation of the {@link NodeSet} interface. Uses maps internally.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleNodeSet implements NodeSet<Graph> {

    private final Map<NodeCategory, Set<NodeEntry>> nodeEntriesMap = new HashMap<>();
    private Pair<NodeEntry, NodeEntry> upperBoundEntries = null;
    private float upperBound = Float.MAX_VALUE;

    @Override
    public void put( Graph graph, NodeCategory nodeCategory, long edgeId, long nodeId, float distance ) {
        getSet( nodeCategory ).add(
                new NodeEntry(
                        edgeId < 0 ? -1 : graph.getEdgeByOrigId( edgeId ),
                        nodeId < 0 ? -1 : graph.getNodeByOrigId( nodeId ),
                        distance ) );
    }

    @Override
    public Iterator<NodeEntry> iterator( NodeCategory nodeCategory ) {
        return getSet( nodeCategory ).iterator();
    }

    private Set<NodeEntry> getSet( NodeCategory nodeCategory ) {
        Set<NodeEntry> set = nodeEntriesMap.get( nodeCategory );
        if ( set == null ) {
            set = new HashSet<>();
            nodeEntriesMap.put( nodeCategory, set );
        }
        return set;
    }

    @Override
    public Map<Integer, NodeEntry> getMap( Graph graph, NodeCategory nodeCategory ) {
        Map<Integer, NodeEntry> map = new HashMap<>();
        Iterator<NodeEntry> it = iterator( nodeCategory );
        while ( it.hasNext() ) {
            NodeEntry entry = it.next();
            map.put( entry.getNodeId(), entry );
        }
        return map;
    }

    @Override
    public boolean hasUpperBound() {
        return upperBoundEntries != null;
    }

    @Override
    public float getUpperBound() {
        return upperBound;
    }

    @Override
    public Pair<NodeEntry, NodeEntry> getUpperBoundEntries() {
        return upperBoundEntries;
    }

    @Override
    public void putUpperBound( NodeEntry source, NodeEntry target, float distance ) {
        if ( distance < upperBound ) {
            upperBoundEntries = new Pair<>( source, target );
            upperBound = distance;
        }
    }
}
