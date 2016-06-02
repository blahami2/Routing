/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity;

import cz.certicon.routing.utils.efficient.BitArray;
import gnu.trove.iterator.TIntIterator;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Graph {

    public static final float DISTANCE_DEFAULT = Float.MAX_VALUE;
    public static final boolean CLOSED_DEFAULT = false;
    public static final int PREDECESSOR_DEFAULT = -1;

    public void setEdgeOrigId( int edge, long id );

    public void setNodeOrigId( int node, long id );

    public void setSource( int edge, int source );

    public void setTarget( int edge, int target );

    public void setLength( int edge, float length );

    public void setIncomingEdges( int node, int[] incomingEdges );

    public void setOutgoingEdges( int node, int[] outgoingEdges );

    public void setCoordinate( int node, float latitude, float longitude );

    public void resetNodePredecessorArray( int[] nodePredecessors );

    public void resetNodeDistanceArray( float[] nodeDistances );

    public void resetNodeClosedArray( BitArray nodeClosed );

    public int[] getIncomingEdges( int node );

    public int[] getOutgoingEdges( int node );

    public int getSource( int edge );

    public int getTarget( int edge );

    public int getOtherNode( int edge, int node );

    public float getLength( int edge );

    public long getEdgeOrigId( int edge );

    public long getNodeOrigId( int node );

    /* temporary solution - delete after having sequence already in the database */
    public int getNodeByOrigId( long nodeId );

    public int getEdgeByOrigId( long edgeId );

    public float getLatitude( int node );

    public float getLongitude( int node );

    public int getNodeCount();

    public int getEdgeCount();

    public int getNodeDegree( int node );

    public TIntIterator getIncomingEdgesIterator( int node );

    public TIntIterator getOutgoingEdgesIterator( int node );

    public boolean containsEdge( long edgeOrigId );

    public boolean containsNode( long nodeOrigId );

    public boolean isValidPredecessor( int predecessor );
    
}
