/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.model.basic.Length;
import cz.certicon.routing.model.basic.Time;
import cz.certicon.routing.model.entity.Coordinate;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Path {

    public Length getLength();

    public Time getTime();

    public List<Coordinate> getCoordinates();
    
}
