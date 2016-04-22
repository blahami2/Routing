/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.nodesearch.graph;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.data.nodesearch.NodeSearcher;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.common.SimpleEdgeData;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.utils.DoubleComparator;
import cz.certicon.routing.utils.GeometryUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class GraphNodeSearcher implements NodeSearcher {

    private static final double EPS = 10E-9;

    private Graph graph;

    public GraphNodeSearcher( Graph graph ) {
        this.graph = graph;
    }

    @Override
    public Map<Coordinates, Distance> findClosestNodes( Coordinates coordinates, DistanceFactory distanceFactory ) throws IOException {
        Edge closestEdge = null;
        Coordinates closestCoords = null;
        double distance = Double.MAX_VALUE;
        for ( Edge edge : graph.getEdges() ) {
            double currentMin = Double.MAX_VALUE;
            Coordinates currentCoords = null;
            for ( Coordinates coordinate : edge.getCoordinates() ) {
                double tmp = CoordinateUtils.calculateDistance( coordinate, coordinates );
                if ( DoubleComparator.isLowerThan( tmp, currentMin, EPS ) ) {
                    currentMin = tmp;
                    currentCoords = coordinate;
                }
            }
            if ( DoubleComparator.isLowerThan( currentMin, distance, EPS ) ) {
                distance = currentMin;
                closestEdge = edge;
                closestCoords = currentCoords;
            }
        }
        if ( closestEdge != null ) {
            Map<Coordinates, Distance> distanceMap = new HashMap<>();
            EdgeData edgeData = new SimpleEdgeData( closestEdge.getSpeed(), closestEdge.getAttributes().isPaid(), closestEdge.getAttributes().getLength() );
            Coordinates start = closestEdge.getCoordinates().get( 0 );
            Coordinates end = closestEdge.getCoordinates().get( closestEdge.getCoordinates().size() - 1 );
            double length1 = 0;
            double length2 = 0;
            double tmpLength = 0;
            for ( Coordinates coordinate : closestEdge.getCoordinates() ) {
                tmpLength += CoordinateUtils.calculateDistance( start, coordinate );
                if ( coordinate.equals( closestCoords ) ) {
                    length1 = tmpLength;
                    tmpLength = 0;
                }
            }
            length2 = tmpLength;
            distanceMap.put( start, distanceFactory.createFromEdgeDataAndLength( edgeData, length1 ) );
            distanceMap.put( end, distanceFactory.createFromEdgeDataAndLength( edgeData, length2 ) );
            return distanceMap;
        } else {
            return null;
        }
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph( Graph graph ) {
        this.graph = graph;
    }

}
