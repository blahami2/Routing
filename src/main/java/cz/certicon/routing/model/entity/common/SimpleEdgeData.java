/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.common;

import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.model.entity.Node;

/**
 * A basic implementation of {@link EdgeData}
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleEdgeData implements EdgeData {

    private final Node sourceNode;
    private final Node targetNode;
    private final int speed;
    private final boolean isPaid;
    private final double length;

    public SimpleEdgeData( Node sourceNode, Node targetNode, int speed, boolean isPaid, double length ) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.speed = speed;
        this.isPaid = isPaid;
        this.length = length;
    }

    @Override
    public Node getSource() {
        return sourceNode;
    }

    @Override
    public Node getTarget() {
        return targetNode;
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public boolean isPaid() {
        return isPaid;
    }

    @Override
    public double getLength() {
        return length;
    }

}
