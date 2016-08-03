/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

/**
 * Factory for {@link NodeSetBuilder}
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> node set type
 */
public interface NodeSetBuilderFactory<T> {

    /**
     * Creates and returns {@link NodeSetBuilder}
     *
     * @return {@link NodeSetBuilder}
     */
    public NodeSetBuilder<T> createNodeSetBuilder();
}
