/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.osm;

import cz.certicon.routing.model.entity.EdgeAttributes;

/**
 * Minimalistic edge container.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SmallEdge {
    private static long ID_COUNTER = 0;
    
    public final long wayId;
    public final long id;
    public final long sourceId;
    public final long targetId;
    public final EdgeAttributes edgeAttributes;

    public SmallEdge( long wayId, long sourceId, long targetId, EdgeAttributes edgeAttributes ) {
        this.wayId = wayId;
        this.id = ++ID_COUNTER;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.edgeAttributes = edgeAttributes;
    }
}
