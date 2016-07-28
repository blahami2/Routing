/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.ch;

import cz.certicon.routing.model.entity.DistanceType;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import java.util.Iterator;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ChDataExtractor<T> {

    public Iterator<Pair<Long, Integer>> getRankIterator();

    public Iterator<Trinity<Long, Long, Long>> getShortcutIterator();

    public DistanceType getDistanceType();
}
