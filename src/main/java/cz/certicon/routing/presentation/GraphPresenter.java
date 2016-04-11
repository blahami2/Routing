/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.presentation;

import cz.certicon.routing.model.entity.Graph;

/**
 * An interface for a visual graph representation.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface GraphPresenter {

    /**
     * Displays a given graph.
     *
     * @param graph an instance of {@link Graph} to be displayed
     */
    public void displayGraph( Graph graph );
}
