/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

/**
 *
 * @deprecated java osm parsing not supported anymore, use database or other external application
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface TemporaryMemory {

    public DataDestination getMemoryAsDestination();

    public DataSource getMemoryAsSource();
}
