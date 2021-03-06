/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.ch;

import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.utils.EffectiveUtils;
import gnu.trove.iterator.TIntIterator;
import java.util.Arrays;

/**
 * Class wrapper for the graph functionality enriched by the CH data. * # means
 * index
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

    private final float[] lengths;

    private final long startId;

    /**
     * Constructor
     *
     * @param nodeCount amount of nodes in the graph
     * @param edgeCount amount of edges in the graph
     * @param shortcutCount amount of shortcuts in the CH data
     * @param startId startId for the given shortcuts, see {@link ChDataBuilder}
     * for more details
     */
    public PreprocessedData( int nodeCount, int edgeCount, int shortcutCount, long startId ) {
        this.ranks = new int[nodeCount];
        this.incomingShortcuts = new int[nodeCount][];
        this.outgoingShortcuts = new int[nodeCount][];
        this.sources = new int[shortcutCount];
        this.targets = new int[shortcutCount];

        this.startEdges = new int[shortcutCount];
        this.endEdges = new int[shortcutCount];
        this.lengths = new float[shortcutCount];
        EffectiveUtils.fillArray( lengths, -1 );
        this.startId = startId;
    }

    /**
     * Constructor
     *
     * @param ranks array of ranks for nodes: (node) -> rank
     * @param incomingShortcuts 2D array of incoming shortcuts: (node, #) ->
     * shortcut
     * @param outgoingShortcuts 2D array of outgoing shortcuts: (node, #) ->
     * shortcut
     * @param sources array of sources: shortcut -> node
     * @param targets array of targets: shortcut -> node
     * @param startEdges array of start edges: shortcut -> edge
     * @param endEdges array of end edges: shortcut -> edge
     * @param startId startId for the given shortcuts, see {@link ChDataBuilder}
     * for more details
     */
    public PreprocessedData( int[] ranks, int[][] incomingShortcuts, int[][] outgoingShortcuts, int[] sources, int[] targets, int[] startEdges, int[] endEdges, long startId ) {
        this.ranks = ranks;
        this.incomingShortcuts = incomingShortcuts;
        this.outgoingShortcuts = outgoingShortcuts;
        this.sources = sources;
        this.targets = targets;
        this.startEdges = startEdges;
        this.endEdges = endEdges;
        this.lengths = new float[sources.length];
        EffectiveUtils.fillArray( lengths, -1 );
        this.startId = startId;
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

    public int getShortcutCount() {
        return sources.length;
    }

    public int getSource( int shortcut ) {
        return sources[shortcut];
    }

    public int getSource( int edge, Graph graph ) {
        if ( edge < graph.getEdgeCount() ) {
            return graph.getSource( edge );
        }
        return sources[edge - graph.getEdgeCount()];
    }

    public int[] getTargets() {
        return targets;
    }

    public int getTarget( int shortcut ) {
        return targets[shortcut];
    }

    public int getTarget( int edge, Graph graph ) {
        if ( edge < graph.getEdgeCount() ) {
            return graph.getTarget( edge );
        }
        return targets[edge - graph.getEdgeCount()];
    }

    /**
     * Returns other node than the given node, which lies on the given edge
     * (there are two nodes on each edge, returns the node on the other end of
     * the edge)
     *
     * @param edge containing the node
     * @param node this node
     * @param graph graph to be calculated upon
     * @return the other node
     */
    public int getOtherNode( int edge, int node, Graph graph ) {
        if ( edge < graph.getEdgeCount() ) {
            return graph.getOtherNode( edge, node );
        }
        edge -= graph.getEdgeCount();
        if ( targets[edge] != node ) {
            return targets[edge];
        }
        return sources[edge];
    }

    public int getStartEdge( int shortcut ) {
        return startEdges[shortcut];
    }

    public int getEndEdge( int shortcut ) {
        return endEdges[shortcut];
    }

    public long getStartId() {
        return startId;
    }

    public float getLength( int shortcut, Graph graph ) {
        if ( shortcut < graph.getEdgeCount() ) {
            return graph.getLength( shortcut );
        }
        shortcut -= graph.getEdgeCount();
        if ( lengths[shortcut] < 0 ) {
            int start = startEdges[shortcut];
            int end = endEdges[shortcut];
            lengths[shortcut] = getLength( start, graph ) + getLength( end, graph );
        }
        return lengths[shortcut];
    }

    public TIntIterator getIncomingEdgesIterator( int node, Graph graph ) {
        return new IncomingIterator( graph, node );
    }

    public TIntIterator getOutgoingEdgesIterator( int node, Graph graph ) {
        return new OutgoingIterator( graph, node );
    }

    private class IncomingIterator implements TIntIterator {

        private final int node;
        private final Graph graph;
        private int position = -1;

        public IncomingIterator( Graph graph, int node ) {
            this.node = node;
            this.graph = graph;
        }

        @Override
        public boolean hasNext() { // ... see note at NeighbourListGraph
            return position + 1 < graph.getIncomingEdges( node ).length + incomingShortcuts[node].length;
        }

        @Override
        public int next() {
            if ( position + 1 < graph.getIncomingEdges( node ).length ) {
                return graph.getIncomingEdges( node )[++position];
            } else {
                return incomingShortcuts[node][++position - graph.getIncomingEdges( node ).length] + graph.getEdgeCount();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

    }

    private class OutgoingIterator implements TIntIterator {

        private final int node;
        private int position = -1;
        private final Graph graph;

        public OutgoingIterator( Graph graph, int node ) {
            this.node = node;
            this.graph = graph;
        }

        @Override
        public boolean hasNext() { // see above, analogically
            return position + 1 < graph.getOutgoingEdges( node ).length + outgoingShortcuts[node].length;
        }

        @Override
        public int next() {
            if ( position + 1 < graph.getOutgoingEdges( node ).length ) {
                return graph.getOutgoingEdges( node )[++position];
            } else {
                return outgoingShortcuts[node][++position - graph.getOutgoingEdges( node ).length] + graph.getEdgeCount();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Arrays.hashCode( this.ranks );
        hash = 53 * hash + Arrays.deepHashCode( this.incomingShortcuts );
        hash = 53 * hash + Arrays.deepHashCode( this.outgoingShortcuts );
        hash = 53 * hash + Arrays.hashCode( this.sources );
        hash = 53 * hash + Arrays.hashCode( this.targets );
        hash = 53 * hash + Arrays.hashCode( this.startEdges );
        hash = 53 * hash + Arrays.hashCode( this.endEdges );
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final PreprocessedData other = (PreprocessedData) obj;
        if ( !Arrays.equals( this.ranks, other.ranks ) ) {
            return false;
        }
        if ( !Arrays.deepEquals( this.incomingShortcuts, other.incomingShortcuts ) ) {
            return false;
        }
        if ( !Arrays.deepEquals( this.outgoingShortcuts, other.outgoingShortcuts ) ) {
            return false;
        }
        if ( !Arrays.equals( this.sources, other.sources ) ) {
            return false;
        }
        if ( !Arrays.equals( this.targets, other.targets ) ) {
            return false;
        }
        if ( !Arrays.equals( this.startEdges, other.startEdges ) ) {
            return false;
        }
        if ( !Arrays.equals( this.endEdges, other.endEdges ) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder isb = new StringBuilder();
        isb.append( "[" );
        for ( int[] i : incomingShortcuts ) {
            isb.append( Arrays.toString( i ) ).append( "," );
        }
        isb.append( "]" );
        StringBuilder osb = new StringBuilder();
        osb.append( "[" );
        for ( int[] i : outgoingShortcuts ) {
            osb.append( Arrays.toString( i ) ).append( "," );
        }
        osb.append( "]" );
        return "PreprocessedData{"
                + "ranks=" + Arrays.toString( ranks )
                + ", incomingShortcuts=" + isb.toString()
                + ", outgoingShortcuts=" + osb.toString()
                + ", sources=" + Arrays.toString( sources )
                + ", targets=" + Arrays.toString( targets )
                + ", startEdges=" + Arrays.toString( startEdges )
                + ", endEdges=" + Arrays.toString( endEdges ) + '}';
    }

}
