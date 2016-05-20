/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity;

import java.util.Iterator;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Graph {

    public void setEdgeOrigId( int edge, long id );

    public void setNodeOrigId( int node, long id );

    public void setSource( int edge, int source );

    public void setTarget( int edge, int target );

    public void setLength( int edge, double length );

    public void setIncomingEdges( int node, int[] incomingEdges );

    public void setOutgoingEdges( int node, int[] outgoingEdges );

    public void resetNodePredecessorArray( int[] nodePredecessors );

    public void resetNodeDistanceArray( double[] nodeDistances );

    public void resetNodeClosedArray( boolean[] nodeClosed );

    public int[] getIncomingEdges( int node );

    public int[] getOutgoingEdges( int node );

    public int getSource( int edge );

    public int getTarget( int edge );

    public int getOtherNode( int edge, int node );

    public double getLength( int edge );

    public long getEdgeOrigId( int edge );

    public long getNodeOrigId( int node );

    /* temporary solution - delete after having sequence already in the database */
    public int getNodeByOrigId( long nodeId );

    public int getEdgeByOrigId( long edgeId );

    public int getNodeCount();

    public int getEdgeCount();

    public Iterator<Integer> getIncomingEdgesIterator( int node );

    public Iterator<Integer> getOutgoingEdgesIterator( int node );

    public boolean isValidPredecessor( int predecessor );
}
