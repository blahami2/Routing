/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface EdgeAttributes {

    public double getSpeed(boolean forward);

    public boolean isOneWay();

    public boolean isPaid();

    public double getLength();
    
    public EdgeAttributes copyWithNewLength(double length);
}
