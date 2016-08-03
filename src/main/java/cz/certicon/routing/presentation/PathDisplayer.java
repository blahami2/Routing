/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.presentation;

import cz.certicon.routing.model.entity.Path;

/**
 * Interface defining path displaying functionality. Displays given path on the
 * map.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface PathDisplayer {

    /**
     * Displays given path on the map
     *
     * @param path given path
     */
    public void displayPath( Path path );
}
