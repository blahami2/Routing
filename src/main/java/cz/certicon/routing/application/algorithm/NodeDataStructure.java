/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

import cz.certicon.routing.model.entity.Node;

/**
 * The root interface for various data structures used by algorithms.
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public interface NodeDataStructure {

    /**
     * Extract node with the minimal distance. Should be as fast as possible.
     *
     * @return node with the minimal distance
     */
    public Node extractMin();

    /**
     * Adds node to the structure.
     *
     * @param node node to be added
     * @return this structure
     */
    public NodeDataStructure add( Node node );

    /**
     * Removes node from the structure.
     *
     * @param node node to be removed
     * @return this structure
     */
    public NodeDataStructure remove( Node node );

    /**
     * Notifies the structure about distance change (invoking so called
     * decrease-key operation).
     *
     * @param node node which had data changed
     * @return this structure
     */
    public NodeDataStructure notifyDataChange( Node node );

    /**
     * Wipes out the data from this structure.
     *
     * @return this structure
     */
    public NodeDataStructure clear();

    /**
     * Returns true or false whether this structure contains nodes or not.
     *
     * @return boolean value
     */
    public boolean isEmpty();
}
