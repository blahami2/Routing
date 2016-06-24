/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.memsensitive.algorithm.RoutingAlgorithm;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.NodeSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleNodeSet implements NodeSet<Graph> {

    private final Map<NodeCategory, Set<NodeEntry>> nodeEntriesMap = new HashMap<>();

    @Override
    public void put( NodeCategory nodeCategory, long edgeId, long nodeId, float distance ) {
        getSet( nodeCategory ).add( new NodeEntry( edgeId, nodeId, distance ) );
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
    public Map<Integer, RoutingAlgorithm.NodeEntry> getMap( Graph graph, NodeCategory nodeCategory ) {
        Map<Integer, RoutingAlgorithm.NodeEntry> map = new HashMap<>();
        Iterator<NodeEntry> it = iterator( nodeCategory );
        while ( it.hasNext() ) {
            NodeEntry entry = it.next();
            int node = graph.getNodeByOrigId( entry.getNodeId() );
            int edge = graph.getEdgeByOrigId( entry.getEdgeId() );
            float distance = entry.getDistance();
            map.put( graph.getNodeByOrigId( entry.getNodeId() ), new RoutingAlgorithm.NodeEntry( edge, node, distance ) );
        }
        return map;
    }
}
