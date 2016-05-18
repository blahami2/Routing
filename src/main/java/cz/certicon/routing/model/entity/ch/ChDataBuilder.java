/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.ch;

import cz.certicon.routing.data.ch.DistanceType;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ChDataBuilder<T> {
    
    public void setRank(long nodeId, int rank);
    
    public void addShortcut(long shortcutId, long sourceEdgeId, long targetEdgeId);
    
    public DistanceType getDistanceType();
    
    public T build();
}
