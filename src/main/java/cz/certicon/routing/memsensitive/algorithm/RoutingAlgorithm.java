/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm;

import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface RoutingAlgorithm {

    public <T> T route( RouteBuilder<T> routeBuilder, Map<Integer, Double> from, Map<Integer, Double> to );
}
