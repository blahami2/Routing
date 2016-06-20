/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.neighbourlist;

import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.utils.EffectiveUtils;
import cz.certicon.routing.utils.efficient.BitArray;
import gnu.trove.iterator.TIntIterator;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class NeighbourlistGraph implements Graph {

    private final int edgeSources[];
    private final int edgeTargets[];
    private final float edgeLengths[];
    private final int[][] incomingEdges;
    private final int[][] outgoingEdges;
    private final int[] nodePredecessorsPrototype;
    private final float[] nodeDistancesPrototype;
    private final float[] nodeLatitudes;
    private final float[] nodeLongitudes;
//    private final boolean[] nodeClosedPrototype;
    private final long[] nodeOrigIds;
    private final long[] edgeOrigIds;
    private int[][][] turnRestrictions;

    private final Map<Long, Integer> fromOrigNodesMap;
    private final Map<Long, Integer> fromOrigEdgesMap;

    public NeighbourlistGraph( int nodeCount, int edgeCount ) {
        this.edgeSources = new int[edgeCount];
        this.edgeTargets = new int[edgeCount];
        this.edgeLengths = new float[edgeCount];
        this.nodePredecessorsPrototype = new int[nodeCount];
        this.nodeDistancesPrototype = new float[nodeCount];
        this.nodeLatitudes = new float[nodeCount];
        this.nodeLongitudes = new float[nodeCount];
//        this.nodeClosedPrototype = new boolean[nodeCount];
        this.incomingEdges = new int[nodeCount][];
        this.outgoingEdges = new int[nodeCount][];
        this.nodeOrigIds = new long[nodeCount];
        this.edgeOrigIds = new long[edgeCount];
        this.fromOrigEdgesMap = new HashMap<>();
        this.fromOrigNodesMap = new HashMap<>();
        EffectiveUtils.fillArray( nodePredecessorsPrototype, PREDECESSOR_DEFAULT );
        EffectiveUtils.fillArray( nodeDistancesPrototype, DISTANCE_DEFAULT );
//        EffectiveUtils.fillArray( nodeClosedPrototype, CLOSED_DEFAULT );
    }

    @Override
    public void setSource( int edge, int source ) {
        edgeSources[edge] = source;
    }

    @Override
    public void setTarget( int edge, int target ) {
        edgeTargets[edge] = target;
    }

    @Override
    public void setLength( int edge, float length ) {
        edgeLengths[edge] = length;
    }

    @Override
    public void setIncomingEdges( int node, int[] incomingEdges ) {
        this.incomingEdges[node] = incomingEdges;
    }

    @Override
    public void setOutgoingEdges( int node, int[] outgoingEdges ) {
        this.outgoingEdges[node] = outgoingEdges;
    }

    @Override
    public void resetNodePredecessorArray( int[] nodePredecessors ) {
        System.arraycopy( nodePredecessorsPrototype, 0, nodePredecessors, 0, nodePredecessors.length );
    }

    @Override
    public void resetNodeDistanceArray( float[] nodeDistances ) {
        System.arraycopy( nodeDistancesPrototype, 0, nodeDistances, 0, nodeDistances.length );
    }

    @Override
    public void resetNodeClosedArray( BitArray nodeClosed ) {
        nodeClosed.clear();
    }

    @Override
    public int[] getIncomingEdges( int node ) {
        return incomingEdges[node];
    }

    @Override
    public int[] getOutgoingEdges( int node ) {
        return outgoingEdges[node];
    }

    @Override
    public int getSource( int edge ) {
        return edgeSources[edge];
    }

    @Override
    public int getTarget( int edge ) {
        return edgeTargets[edge];
    }

    @Override
    public float getLength( int edge ) {
        return edgeLengths[edge];
    }

    @Override
    public long getEdgeOrigId( int edge ) {
        return edgeOrigIds[edge];
    }

    @Override
    public long getNodeOrigId( int node ) {
        return nodeOrigIds[node];
    }

    @Override
    public void setEdgeOrigId( int edge, long id ) {
        edgeOrigIds[edge] = id;
        fromOrigEdgesMap.put( id, edge );
    }

    @Override
    public void setNodeOrigId( int node, long id ) {
        nodeOrigIds[node] = id;
        fromOrigNodesMap.put( id, node );
    }

    @Override
    public int getNodeByOrigId( long nodeId ) {
        return fromOrigNodesMap.get( nodeId );
    }

    @Override
    public int getEdgeByOrigId( long edgeId ) {
        return fromOrigEdgesMap.get( edgeId );
    }

    @Override
    public int getNodeCount() {
        return nodeOrigIds.length;
    }

    @Override
    public int getEdgeCount() {
        return edgeOrigIds.length;
    }

    @Override
    public int getOtherNode( int edge, int node ) {
//        System.out.println( "#getOtherNode: edge = " + edge+ ", node = " + node + ", source = " + edgeSources[edge] + ", target = " + edgeTargets[edge] );
        int target = edgeTargets[edge];
        if ( target == node ) {
            return edgeSources[edge];
        }
        return target;
    }

    @Override
    public TIntIterator getIncomingEdgesIterator( int node ) {
        return new IncomingIterator( node );
    }

    @Override
    public TIntIterator getOutgoingEdgesIterator( int node ) {
        return new OutgoingIterator( node );
    }

    @Override
    public boolean containsEdge( long edgeOrigId ) {
        return fromOrigEdgesMap.containsKey( edgeOrigId );
    }

    @Override
    public boolean containsNode( long nodeOrigId ) {
        return fromOrigNodesMap.containsKey( nodeOrigId );
    }

    @Override
    public boolean isValidPredecessor( int predecessor ) {
        return predecessor != PREDECESSOR_DEFAULT;
    }

    @Override
    public void setCoordinate( int node, float latitude, float longitude ) {
        nodeLatitudes[node] = latitude;
        nodeLongitudes[node] = longitude;
    }

    @Override
    public float getLatitude( int node ) {
        return nodeLatitudes[node];
    }

    @Override
    public float getLongitude( int node ) {
        return nodeLongitudes[node];
    }

    @Override
    public int getNodeDegree( int node ) {
        return outgoingEdges[node].length + incomingEdges[node].length;
    }

    @Override
    public boolean isValidWay( int node, int targetEdge, int[] predecessorArray ) {
        if(turnRestrictions == null){ // without turn restrictions, everything is valid
            return true;
        }
        if(turnRestrictions[node] == null){ // without turn restrictions for the concrete node, every turn is valid
            return true;
        }
        for ( int i = 0; i < turnRestrictions[node].length; i++ ) { // for all restrictions for this node
            int[] edgeSequence = turnRestrictions[node][i]; // load the edge sequence of this particular restrictions
            if(edgeSequence[edgeSequence.length - 1] == targetEdge){ // if the last edge of this sequence is the target edge
                int currNode = node;
                for ( int j = edgeSequence.length - 2; j >= 0; j-- ) { // for every edge in the sequence (except for the last, it is already checked) compare it with the predecessor
                    int pred = predecessorArray[currNode];
                    currNode = (edgeTargets[pred] == currNode) ? edgeSources[pred] : edgeTargets[pred];
                    if(pred != edgeSequence[j]){ // the turn restriction edge sequence does not match the way
                        break;
                    }
                    if(j == 0){ // all passed, the turn restriction edge sequence matches the way, therefore it is forbidden
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void setTurnRestrictions( int[][][] turnRestrictions ) {
        this.turnRestrictions = turnRestrictions;
    }

    @Override
    public int[][][] getTurnRestrictions() {
        return turnRestrictions;
    }
   

    private class IncomingIterator implements TIntIterator {

        private final int node;
        private int position = -1;

        public IncomingIterator( int node ) {
            this.node = node;
        }

        @Override
        public boolean hasNext() { // ... position + 1 < lastTwoway[node] // create a helper array of last position of the twoway node so that it does not have to go through the whole array when determining, whether a valid edge follows
            return position + 1 < incomingEdges[node].length;
        }

        @Override
        public int next() {
            return incomingEdges[node][++position];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

    }

    private class OutgoingIterator implements TIntIterator {

        private final int node;
        private int position = -1;

        public OutgoingIterator( int node ) {
            this.node = node;
        }

        @Override
        public boolean hasNext() { // see above, analogically
            return position + 1 < outgoingEdges[node].length;
        }

        @Override
        public int next() {
            return outgoingEdges[node][++position];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
