/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import cz.certicon.routing.model.entity.Graph;

/**
 * An interface for callback method (on graph creation) used by {@link DataSource}
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface GraphLoadListener {

    /**
     * Method called when the graph is created.
     *
     * @param graph filled instance of {@link Graph}
     */
    public void onGraphLoaded(Graph graph);
}
