/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

import cz.certicon.routing.model.entity.Node;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface EdgeData {
    
    public Node getSource();
    
    public Node getTarget();
    
    public int getSpeed();

    public boolean isPaid();

    public double getLength();
}
