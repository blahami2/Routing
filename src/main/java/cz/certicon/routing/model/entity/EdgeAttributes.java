/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

/**
 * Class representation of edge attributes.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface EdgeAttributes {

    public boolean isPaid();

    public double getLength();
    
    public void putAdditionalAttribute(String key, String value);
    
    public String getAdditionalAttribute(String key);
    
    public EdgeAttributes copyWithNewLength(double length);
}
