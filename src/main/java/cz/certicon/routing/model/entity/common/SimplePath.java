/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.utils.GraphUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A basic implementation of {@link Path}
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public abstract class SimplePath implements Path {

    private final LinkedList<Edge> edges;
    private final Graph graph;
    private Node sourceNode = null;
    private Node targetNode = null;

    private double distFromStart = 0;
    private double distToEnd = 0;

    private int speedFromStart = 1;
    private int speedToEnd = 1;

    private final List<Coordinates> coordsFromStart = new ArrayList<>();
    private final List<Coordinates> coordsToEnd = new ArrayList<>();

    private long sourceDataId = -1;
    private long targetDataId = -1;

    private Coordinates origSource = null;
    private Coordinates origTarget = null;

    private Edge sourceEdge = null;
    private Edge targetEdge = null;

    public SimplePath( Graph graph, Node node, boolean isFirst ) {
        this.graph = graph;
        this.edges = new LinkedList<>();
        this.sourceNode = this.targetNode = node;
    }

    @Override
    public Path addEdge( Edge e ) {
        addEdgeAsLast( e );
        return this;
    }

    @Override
    public Path addEdgeAsFirst( Edge e ) {
        edges.addFirst( e );
        sourceNode = e.getOtherNode( sourceNode );
        return this;
    }

    @Override
    public Path addEdgeAsLast( Edge e ) {
        edges.addLast( e );
        targetNode = e.getOtherNode( targetNode );
        return this;
    }

    @Override
    public Iterator<Edge> iterator() {
        return edges.iterator();
    }

    @Override
    public List<Node> getNodes() {
        Node currentNode = sourceNode;
        List<Node> nodes = new ArrayList<>();
        nodes.add( sourceNode );
        for ( Edge edge : this ) {
            nodes.add( currentNode = edge.getOtherNode( currentNode ) );
        }
        return nodes;
    }

    @Override
    public List<Coordinates> getCoordinates() {
        Coordinates currentNode = sourceNode.getCoordinates();
        List<Coordinates> coordinates = new LinkedList<>();
        coordinates.addAll( coordsFromStart );
        for ( Edge edge : this ) {
            List<Coordinates> edgeCoordinates = edge.getCoordinates();
            if ( currentNode.equals( edge.getCoordinates().get( 0 ) ) ) {
                for ( int i = 0; i < edgeCoordinates.size(); i++ ) {
                    coordinates.add( edgeCoordinates.get( i ) );
                    currentNode = edgeCoordinates.get( i );
                }
            } else {
                for ( int i = edgeCoordinates.size() - 1; i >= 0; i-- ) {
                    coordinates.add( edgeCoordinates.get( i ) );
                    currentNode = edgeCoordinates.get( i );
                }
            }
        }
        coordinates.addAll( coordsToEnd );
        return coordinates;
    }

    @Override
    public Node getSourceNode() {
        return sourceNode;
    }

    @Override
    public Node getTargetNode() {
        return targetNode;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public List<Edge> getEdges() {
        return new ArrayList<>( edges );
    }

    @Override
    public Distance getDistance() {
        Distance result = null;
        for ( Edge e : this ) {
            if ( result == null ) {
                result = e.getDistance();
            } else {
                result = result.add( e.getDistance() );
            }
        }
        return result;
    }

    @Override
    public double getLength() {
        double sum = 0;
        for ( Edge edge : edges ) {
            sum += edge.getAttributes().getLength();
        }
        sum += distFromStart;
        sum += distToEnd;
        return sum;
    }

    @Override
    public double getTime() {
        Node source = sourceNode;
        double time = 0;
        for ( Edge edge : edges ) {
            double speed = edge.getSpeed(); // kmh
            double length = edge.getAttributes().getLength(); // kilometers
//            speed /= 3.6; // to mps
            time += 3600 * length / speed;
            source = edge.getOtherNode( source );
        }
        time += 3600 * distFromStart / speedFromStart;
        time += 3600 * distToEnd / speedToEnd;
        return time;
    }

    @Override
    public Path connectWith( Path otherPath ) {
        for ( Edge edge : otherPath ) {
            addEdgeAsLast( edge );
        }
        return this;
    }

    @Override
    public int size() {
        return edges.size();
    }

    @Override
    public String toStringLabelPath() {
        StringBuilder sb = new StringBuilder();
        for ( Edge e : this ) {
            sb.append( e.getLabel() );
            sb.append( ":" );
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        final SimplePath other = (SimplePath) obj;
        if ( !Objects.equals( this.edges, other.edges ) ) {
            Iterator<Edge> thisIt = this.edges.iterator();
            Iterator<Edge> otherIt = other.edges.iterator();
            while ( thisIt.hasNext() ) {
                Edge a = thisIt.next();
                Edge b = otherIt.next();
                System.out.println( a );
                System.out.println( b );
                System.out.println( a.equals( b ) );
            }
            System.out.println( "edges not equal" );
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SimplePath{" + "distance=" + getDistance() + ", " + "edges=" + edges + '}';
    }

    @Override
    public void setSourceOrigin( Coordinates origSource, Long edgeDataId ) {
        this.origSource = origSource;
        this.sourceDataId = edgeDataId;
    }

    @Override
    public void setTargetOrigin( Coordinates origTarget, Long edgeDataId ) {
        this.origTarget = origTarget;
        this.targetDataId = edgeDataId;
    }

    @Override
    public void loadCoordinates( CoordinateReader cr ) throws IOException {
        if ( origSource == null || origTarget == null || sourceDataId == -1 || targetDataId == -1 ) {
            throw new IllegalStateException( "use setSourceOrigin and setTargetOrigin methods first!" );
        }

        Set<Edge> edgeSet = new HashSet<>( getEdges() );
        sourceEdge = null;
        targetEdge = null;
        if ( origSource != null && sourceDataId != -1 ) {
            sourceEdge = getCorrectEdge( sourceDataId, getSourceNode() );
            edgeSet.add( sourceEdge );
        }

        if ( origTarget != null && targetDataId != -1 ) {
            targetEdge = getCorrectEdge( targetDataId, getTargetNode() );
            edgeSet.add( targetEdge );
        }

        cr.open();
        Map<Edge, List<Coordinates>> coordinateMap = cr.read( edgeSet );
        cr.close();
        GraphUtils.fillWithCoordinates( getEdges(), coordinateMap );

        if ( sourceEdge != null ) {
            sourceEdge.setCoordinates( coordinateMap.get( sourceEdge ) );
            distFromStart = getAdditionalLength( coordsFromStart, sourceEdge, true, origSource );
            speedFromStart = sourceEdge.getSpeed();
        }

        if ( targetEdge != null ) {
            targetEdge.setCoordinates( coordinateMap.get( targetEdge ) );
            distToEnd = getAdditionalLength( coordsToEnd, targetEdge, false, origTarget );
            speedToEnd = targetEdge.getSpeed();
        }
    }

    private Edge getCorrectEdge( long edgeDataId, Node node ) {
        Edge correctEdge = null;
        Set<Edge> edgesOf = getGraph().getEdgesOf( node );
        for ( Edge edge : edgesOf ) {
            if ( edge.getDataId() == edgeDataId ) {
                correctEdge = edge;
                break;
            }
        }
        if ( correctEdge == null ) {
            throw new IllegalStateException( "Invalid source data id: " + edgeDataId + " for node: " + node.getId() );
        }
        return correctEdge;
    }

    private double getAdditionalLength( List<Coordinates> outCoordinates, Edge edge, boolean from, Coordinates orig ) {

        double min = Double.MAX_VALUE;
        int minIndex = 0;
        List<Coordinates> edgeCoordinates = edge.getCoordinates();
        for ( int i = 0; i < edgeCoordinates.size(); i++ ) {
            Coordinates coordinate = edgeCoordinates.get( i );
            double dist = CoordinateUtils.calculateDistance( coordinate, orig );
            if ( dist < min ) {
                min = dist;
                minIndex = i;
            }
        }
        double length = 0;
        int start;
        int end;
        if ( from ) {
            start = minIndex;
            end = edgeCoordinates.size();
        } else {
            start = 0;
            end = minIndex + 1;
        }
        for ( int i = start; i < end; i++ ) {
            Coordinates coordinate = edgeCoordinates.get( i );
            outCoordinates.add( coordinate );
            if ( i > start ) {
                length += CoordinateUtils.calculateDistance( edgeCoordinates.get( i - 1 ), coordinate ) / 1000;
            }
        }
        return length;
    }

    @Override
    public void clear() {
        if ( sourceEdge != null ) {
            sourceEdge.setCoordinates( null );
        }
        if ( targetEdge != null ) {
            targetEdge.setCoordinates( null );
        }
        for ( Edge e : this ) {
            e.setCoordinates( null );
        }
        distFromStart = 0;
        distToEnd = 0;

        speedFromStart = 1;
        speedToEnd = 1;

        coordsFromStart.clear();
        coordsToEnd.clear();

        sourceDataId = -1;
        targetDataId = -1;

        origSource = null;
        origTarget = null;

        sourceEdge = null;
        targetEdge = null;
    }
}
