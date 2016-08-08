/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.utils.efficient.BitArray;
import gnu.trove.iterator.TIntIterator;

/**
 * Interface defining the graph. Note#1: there are two types of ids: local id
 * (array index) and global id (outside the application), the algorithm mostly
 * works with the integer local id often referred to as simply 'id' (or 'edge'
 * when it comes to edge id, resp. 'node' when it comes to node id), however, it
 * communicates with the rest of the world via the long global id, often
 * referred to as 'origId' (resp. 'edgeOrigId', 'nodeOrigId'). It is vital not
 * to interchange these.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Graph {

    /**
     * Default distance value - infinity
     */
    public static final float DISTANCE_DEFAULT = Float.MAX_VALUE;
    /**
     * Default closed value - false
     */
    public static final boolean CLOSED_DEFAULT = false;
    /**
     * Default predecessor value - -1
     */
    public static final int PREDECESSOR_DEFAULT = -1;

    /**
     * Setter for edge original id -&gt; id used outside the application
     *
     * @param edge local id (array index)
     * @param id global id (original)
     */
    public void setEdgeOrigId( int edge, long id );

    /**
     * Setter for node original id -&gt; id used outside the application
     *
     * @param node local id (array index)
     * @param id global id (original)
     */
    public void setNodeOrigId( int node, long id );

    /**
     * Sets source for the given edge
     *
     * @param edge given edge
     * @param source id of the source node
     */
    public void setSource( int edge, int source );

    /**
     * Sets target for the given edge
     *
     * @param edge given edge
     * @param target id of the target node
     */
    public void setTarget( int edge, int target );

    /**
     * Sets length[m] for the given edge
     *
     * @param edge given edge
     * @param length its length in meters
     */
    public void setLength( int edge, float length );

    /**
     * Sets incoming edges for the given node
     *
     * @param node given node
     * @param incomingEdges array of edges' ids incoming to the given node
     */
    public void setIncomingEdges( int node, int[] incomingEdges );

    /**
     * Sets outgoing edges for the given node
     *
     * @param node given node
     * @param outgoingEdges array of edges' ids outgoing from the given node
     */
    public void setOutgoingEdges( int node, int[] outgoingEdges );

    /**
     * Sets coordinate for the given node
     *
     * @param node given node
     * @param latitude node's latitude
     * @param longitude node's longitude
     */
    public void setCoordinate( int node, float latitude, float longitude );

    /**
     * Resets the given array of predecessors to default values (invalid values
     * lesser than zero). Mostly efficient.
     *
     * @param nodePredecessors array of nodes' predecessors
     */
    public void resetNodePredecessorArray( int[] nodePredecessors );

    /**
     * Resets the given array of distances to default values (infinity). Mostly
     * efficient.
     *
     * @param nodeDistances array of nodes' distances
     */
    public void resetNodeDistanceArray( float[] nodeDistances );

    /**
     * Resets the given array of closed nodes (resp. indicators, whether the
     * node on its index is closed or not) to default values (false). Mostly
     * efficient.
     *
     * @param nodeClosed array of nodes' close indicators
     */
    public void resetNodeClosedArray( BitArray nodeClosed );

    /**
     * Returns array of edges incoming to the given node
     *
     * @param node given node
     * @return array of edges incoming to the given node
     */
    public int[] getIncomingEdges( int node );

    /**
     * Returns array of edges outgoing from the given node
     *
     * @param node given node
     * @return array of edges outgoing from the given node
     */
    public int[] getOutgoingEdges( int node );

    /**
     * Returns id of the source node of the given edge
     *
     * @param edge given edge
     * @return id of the source node of the given edge
     */
    public int getSource( int edge );

    /**
     * Returns id of the target node of the given edge
     *
     * @param edge given edge
     * @return id of the target node of the given edge
     */
    public int getTarget( int edge );

    /**
     * Returns id of the other node of the given edge (other than the given
     * node)
     *
     * @param edge given edge
     * @param node given node
     * @return id of the other node
     */
    public int getOtherNode( int edge, int node );

    /**
     * Returns length of the given edge
     *
     * @param edge given edge
     * @return length of the given edge
     */
    public float getLength( int edge );

    /**
     * Returns original (global) id of the given edge
     *
     * @param edge given edge
     * @return edge's global id
     */
    public long getEdgeOrigId( int edge );

    /**
     * Returns original (global) id of the given node
     *
     * @param node given node
     * @return node's global id
     */
    public long getNodeOrigId( int node );

    /* temporary solution - delete after having sequence already in the database */
    /**
     * Returns index (local id) by the given global node's id
     *
     * @param nodeId given global id
     * @return node's local id
     */
    public int getNodeByOrigId( long nodeId );

    /**
     * Returns index (local id) by the given global edge's id
     *
     * @param edgeId given global id
     * @return edge's local id
     */
    public int getEdgeByOrigId( long edgeId );

    /**
     * Returns given node's latitude
     *
     * @param node given node
     * @return node's latitude
     */
    public float getLatitude( int node );

    /**
     * Returns given node's longitude
     *
     * @param node given node
     * @return node's longitude
     */
    public float getLongitude( int node );

    /**
     * Returns amount of nodes in this graph
     *
     * @return amount of nodes in this graph
     */
    public int getNodeCount();

    /**
     * Returns amount of edges in this graph
     *
     * @return amount of nodes in this graph
     */
    public int getEdgeCount();

    /**
     * Returns degree of the given node. Node's degree is a result of addition
     * of incoming and outgoing edges.
     *
     * @param node given node
     * @return node's degree
     */
    public int getNodeDegree( int node );

    /**
     * Returns iterator of incoming edges to the given node
     *
     * @param node given node
     * @return iterator of incoming edges
     */
    public TIntIterator getIncomingEdgesIterator( int node );

    /**
     * Returns iterator of outgoing edges from the given node
     *
     * @param node given node
     * @return iterator of outgoing edges
     */
    public TIntIterator getOutgoingEdgesIterator( int node );

    /**
     * Returns true if this graph contains the given edge (by global id), false
     * otherwise
     *
     * @param edgeOrigId edge's global id
     * @return true if this graph contains the given edge, false otherwise
     */
    public boolean containsEdge( long edgeOrigId );

    /**
     * Returns true if this graph contains the given node (by global id), false
     * otherwise
     *
     * @param nodeOrigId node's global id
     * @return true if this graph contains the given node, false otherwise
     */
    public boolean containsNode( long nodeOrigId );

    /**
     * Returns true if the given predecessor is valid, false otherwise. The
     * default value indicates invalid predecessor. See
     * {@link #resetNodePredecessorArray(int[]) resetNodePredecessorArray} for
     * more details.
     *
     * @param predecessor given predecessor
     * @return true if the predecessor is valid, false otherwise
     */
    public boolean isValidPredecessor( int predecessor );

}
