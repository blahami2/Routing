/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm;

import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface RoutingAlgorithm<G> {

    public <R> R route( RouteBuilder<R, G> routeBuilder, Map<Integer, Float> from, Map<Integer, Float> to ) throws RouteNotFoundException;
}
