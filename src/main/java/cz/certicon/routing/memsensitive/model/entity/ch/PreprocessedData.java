/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.ch;

import cz.certicon.routing.memsensitive.model.entity.Graph;
import java.util.Iterator;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class PreprocessedData {

    private final int[] ranks;
    private final int[][] incomingShortcuts;
    private final int[][] outgoingShortcuts;
    private final int[] sources;
    private final int[] targets;

    /* more memory, more efficiency */
    private final int[] startEdges;
    private final int[] endEdges;

    public PreprocessedData( int nodeCount, int edgeCount, int shortcutCount ) {
        this.ranks = new int[nodeCount];
        this.incomingShortcuts = new int[nodeCount][];
        this.outgoingShortcuts = new int[nodeCount][];
        this.sources = new int[shortcutCount];
        this.targets = new int[shortcutCount];

        this.startEdges = new int[shortcutCount];
        this.endEdges = new int[shortcutCount];
    }

    public PreprocessedData( int[] ranks, int[][] incomingShortcuts, int[][] outgoingShortcuts, int[] sources, int[] targets, int[] startEdges, int[] endEdges ) {
        this.ranks = ranks;
        this.incomingShortcuts = incomingShortcuts;
        this.outgoingShortcuts = outgoingShortcuts;
        this.sources = sources;
        this.targets = targets;
        this.startEdges = startEdges;
        this.endEdges = endEdges;
    }

    public void setRank( int node, int rank ) {
        ranks[node] = rank;
    }

    public void setIncomingShortcuts( int node, int[] incomingShortcuts ) {
        this.incomingShortcuts[node] = incomingShortcuts;
    }

    public void setOutgoingShortcuts( int node, int[] outgoingShortcuts ) {
        this.outgoingShortcuts[node] = outgoingShortcuts;
    }

    public void setShortcutSource( int shortcut, int node ) {
        sources[shortcut] = node;
    }

    public void setShortcutTarget( int shortcut, int node ) {
        targets[shortcut] = node;
    }

    public void setStartEdge( int shortcut, int edge ) {
        startEdges[shortcut] = edge;
    }

    public void setEndEdge( int shorcut, int edge ) {
        endEdges[shorcut] = edge;
    }

    public int[] getRanks() {
        return ranks;
    }

    public int getRank( int rank ) {
        return ranks[rank];
    }

    public int[][] getIncomingShortcuts() {
        return incomingShortcuts;
    }

    public int[] getIncomingShortcuts( int node ) {
        return incomingShortcuts[node];
    }

    public int[][] getOutgoingShortcuts() {
        return outgoingShortcuts;
    }

    public int[] getOutgoingShortcuts( int node ) {
        return outgoingShortcuts[node];
    }

    public int[] getSources() {
        return sources;
    }

    public int getSource( int shortcut ) {
        return sources[shortcut];
    }

    public int[] getTargets() {
        return targets;
    }

    public int getTarget( int shortcut ) {
        return targets[shortcut];
    }

//    public int getStartEdge( int shortcut ) {
//        return startEdges[shortcut];
//    }
//
//    public int getEndEdge( int shortcut ) {
//        return endEdges[shortcut];
//    }

    public float getLength( int shortcut, Graph graph ) {
        int start = startEdges[shortcut];
        int end = endEdges[shortcut];
        float length = 0;
        if ( start >= graph.getEdgeCount() ) {
            length += getLength( start - graph.getEdgeCount(), graph );
        } else {
            length += graph.getLength( start );
        }
        if ( end >= graph.getEdgeCount() ) {
            length += getLength( end - graph.getEdgeCount(), graph );
        } else {
            length += graph.getLength( end );
        }
        return length;
    }

}
