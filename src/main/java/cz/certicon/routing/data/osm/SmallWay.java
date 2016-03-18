/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.osm;

import cz.certicon.routing.model.entity.Coordinate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SmallWay {

    public final long wayId;
    public final List<Long> nodes;
    public final Map<Long, Coordinate> coordinateMap = new HashMap<>();

    public SmallWay( long wayId, int size ) {
        this.wayId = wayId;
        this.nodes = new ArrayList<>( size );
    }

    public void addNode( Long nodeId ) {
        nodes.add( nodeId );
        coordinateMap.put( nodeId, null );
    }

    public void addCoordinate( Long nodeId, Coordinate coordinate ) {
        if ( coordinateMap.containsKey( nodeId ) ) {
            coordinateMap.put( nodeId, coordinate );
        }
    }

}
