/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.presentation;

import cz.certicon.routing.model.entity.Coordinates;
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

    /**
     * Draws a single path
     *
     * @param path an instance of {@link Path} to be displayed
     * @return this instance
     */
    public PathPresenter displayPath( Path path );

    /**
     * Draws a single path in steps (time intervals defined by the parameter
     * millis)
     *
     * @param path an instance of {@link Path} to be displayed
     * @param millis time interval in milliseconds between two subsequent steps
     * @return this instance
     */
    public PathPresenter displayPath( Path path, int millis );

    /**
     * Add waypoint to be displayed on the given coordinates.
     *
     * @param coordinate target coordinates
     * @param text label content
     * @return this instance
     */
    public PathPresenter addWaypoint( Coordinates coordinate, String text );

    /**
     * Sets whether to display node labels or not
     *
     * @param displayNodeText boolean
     * @return this instance
     */
    public PathPresenter setDisplayNodeText( boolean displayNodeText );

    /**
     * Sets whether to display edge labels or not
     *
     * @param displayEdgeText boolean
     * @return this instance
     */
    public PathPresenter setDisplayEdgeText( boolean displayEdgeText );
}
