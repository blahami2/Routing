/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

/**
 * An exception representing a bad result (when no path is found between two points)
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public class NoPathException extends Exception {

    private final Node source;
    private final Node target;

    /**
     * Constructor
     *
     * @param source an instance of {@link Node} representing a source
     * @param target an instance of {@link Node} representing a target
     */
    public NoPathException( Node source, Node target ) {
        super( "No path found between the two nodes." );
        this.source = source;
        this.target = target;
    }

    /**
     * Getter for a source node
     *
     * @return an instance of {@link Node}
     */
    public Node getSource() {
        return source;
    }

    /**
     * Getter for a target node
     *
     * @return an instance of {@link Node}
     */
    public Node getTarget() {
        return target;
    }

}
