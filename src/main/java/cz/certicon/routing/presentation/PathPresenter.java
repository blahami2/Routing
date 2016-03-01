/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.presentation;

import cz.certicon.routing.model.entity.Path;

/**
 * The root interface for presentation. Serves as a displayer for given paths.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface PathPresenter {

    /**
     * Clears all the paths so that the map is empty.
     *
     * @return this instance
     */
    public PathPresenter clearPaths();

    /**
     * Adds path into the presenter. Only shows after calling the
     * {@link #display() display} method.
     *
     * @param path an instance of {@link Path} to be added and displayed
     * @return this instance
     */
    public PathPresenter addPath( Path path );

    /**
     * Displays the content, draws the paths onto the map.
     *
     * @return this instance
     */
    public PathPresenter display();
}
