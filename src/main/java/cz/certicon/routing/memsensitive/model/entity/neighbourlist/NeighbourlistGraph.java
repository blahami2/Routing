/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.neighbourlist;

import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.utils.EffectiveUtils;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class NeighbourlistGraph implements Graph {

    private final int edgeSources[];
    private final int edgeTargets[];
    private final double edgeLengths[];
    private final int[][] incomingEdges;
    private final int[][] outgoingEdges;
    private final int[] nodePredecessorsPrototype;
    private final double[] nodeDistancesPrototype;
    private final int[] nodeOrigIds;
    private final int[] edgeOrigIds;

    private static final double DISTANCE_DEFAULT = Double.MAX_VALUE;
    private static final int PREDECESSOR_DEFAULT = -1;

    private final Map<Integer, Integer> fromOrigNodesMap;
    private final Map<Integer, Integer> fromOrigEdgesMap;

    public NeighbourlistGraph( int nodeCount, int edgeCount ) {
        this.edgeSources = new int[edgeCount];
        this.edgeTargets = new int[edgeCount];
        this.edgeLengths = new double[edgeCount];
        this.nodePredecessorsPrototype = new int[nodeCount];
        this.nodeDistancesPrototype = new double[nodeCount];
        this.incomingEdges = new int[nodeCount][];
        this.outgoingEdges = new int[nodeCount][];
        this.nodeOrigIds = new int[nodeCount];
        this.edgeOrigIds = new int[edgeCount];
        this.fromOrigEdgesMap = new HashMap<>();
        this.fromOrigNodesMap = new HashMap<>();
        EffectiveUtils.fillArray( nodePredecessorsPrototype, PREDECESSOR_DEFAULT );
        EffectiveUtils.fillArray( nodeDistancesPrototype, DISTANCE_DEFAULT );
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
    public void setLength( int edge, double length ) {
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
    public void resetNodeDistanceArray( double[] nodeDistances ) {
        System.arraycopy( nodeDistancesPrototype, 0, nodeDistances, 0, nodeDistances.length );
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
    public double getLength( int edge ) {
        return edgeLengths[edge];
    }

    @Override
    public int getEdgeOrigId( int edge ) {
        return edgeOrigIds[edge];
    }

    @Override
    public int getNodeOrigId( int node ) {
        return nodeOrigIds[node];
    }

    @Override
    public void setEdgeOrigId( int edge, int id ) {
        edgeOrigIds[edge] = id;
        fromOrigEdgesMap.put( id, edge );
    }

    @Override
    public void setNodeOrigId( int node, int id ) {
        nodeOrigIds[node] = id;
        fromOrigNodesMap.put( id, node );
    }

    @Override
    public int getNodeByOrigId( int nodeId ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getEdgeByOrigId( int nodeId ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

}
