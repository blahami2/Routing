/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.EdgeAttributes;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Shortcut;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleShortcut implements Shortcut {

    private final Edge.Id id;
    private final Edge sourceEdge;
    private final Edge targetEdge;

    public SimpleShortcut( Edge.Id id, Edge sourceEdge, Edge targetEdge ) {
        this.id = id;
        this.sourceEdge = sourceEdge;
        this.targetEdge = targetEdge;
        if ( sourceEdge.getTargetNode().equals( targetEdge.getSourceNode() ) ) {
            throw new IllegalArgumentException( "Connecting node is not equal for both edges: " + sourceEdge.getId() + ", " + targetEdge.getId() );
        }
    }

    @Override
    public Edge getSourceEdge() {
        return sourceEdge;
    }

    @Override
    public Edge getTargetEdge() {
        return targetEdge;
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public long getOsmId() {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setOsmId( long osmId ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getDataId() {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDataId( long dataId ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Distance getDistance() {
        return sourceEdge.getDistance().add( targetEdge.getDistance() );
    }

    @Override
    public int getSpeed() {
        return ( sourceEdge.getSpeed() + targetEdge.getSpeed() ) / 2;
    }

    @Override
    public void setSpeed( int speed ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EdgeAttributes getAttributes() {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttributes( EdgeAttributes attributes ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getLabel() {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLabel( String label ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDistance( Distance newDistance ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Coordinates> getCoordinates() {
        List<Coordinates> coords = new ArrayList<>( sourceEdge.getCoordinates() );
        coords.addAll( targetEdge.getCoordinates() );
        return coords;
    }

    @Override
    public void setCoordinates( List<Coordinates> coordinates ) {
    }

    @Override
    public Node getSourceNode() {
        return sourceEdge.getSourceNode();
    }

    @Override
    public Node getTargetNode() {
        return targetEdge.getTargetNode();
    }

    @Override
    public Node getOtherNode( Node node ) {
        if ( node.equals( sourceEdge.getSourceNode() ) ) {
            return targetEdge.getTargetNode();
        }
        return sourceEdge.getSourceNode();
    }

    @Override
    public Edge newSourceNode( Node sourceNode ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Edge newTargetNode( Node targetNode ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Edge newNodes( Node sourceNode, Node targetNode ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Edge createCopyWithNewId( Id id ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

}
