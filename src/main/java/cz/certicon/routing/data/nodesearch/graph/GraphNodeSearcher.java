/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.nodesearch.graph;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceComparator;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;
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
    private SpatialIndex quadtree;

    public GraphNodeSearcher( Graph graph ) {
        this.graph = graph;
        quadtree = new Quadtree();
        Map<List<Coordinates>, List<Edge>> map = new HashMap<>();
        for ( Edge edge : graph.getEdges() ) {
            List<Coordinates> coordinates = edge.getCoordinates();
            List<Edge> edges = map.get( coordinates );
            if ( edges == null ) {
                edges = new ArrayList<>();
                map.put( coordinates, edges );
            }
            edges.add( edge );
        }
        GeometryFactory geometryFactory = new GeometryFactory( new PrecisionModel( PrecisionModel.FLOATING ), 4326 );
        for ( Map.Entry<List<Coordinates>, List<Edge>> entry : map.entrySet() ) {
            Coordinate[] coords = new Coordinate[entry.getKey().size()];
            for ( int i = 0; i < entry.getKey().size(); i++ ) {
                Coordinates coordinates = entry.getKey().get( i );
                coords[i] = new Coordinate( coordinates.getLongitude(), coordinates.getLatitude() );
            }
            LineString linestring = geometryFactory.createLineString( coords );
            quadtree.insert( linestring.getEnvelopeInternal(), entry.getValue() );
        }
    }

    @Override
    public Map<Coordinates, Distance> findClosestNodes( Coordinates coordinates, DistanceFactory distanceFactory ) throws IOException {
        Edge closestEdge = null;

        List query = quadtree.query( null );

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
