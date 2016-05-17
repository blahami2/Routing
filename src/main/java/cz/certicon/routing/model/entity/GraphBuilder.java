/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> Graph class
 */
public interface GraphBuilder<T> {

    public void addNode( long id, long dataId, long osmId, double latitude, double longitude );

    public void addEdge( long id, long dataId, long osmId, long sourceId, long targetId, double length, double speed, boolean isPaid );
    
    public T build();
}
