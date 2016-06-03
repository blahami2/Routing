/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.memsensitive.data.nodesearch.EvaluableOnlyException;
import cz.certicon.routing.memsensitive.model.entity.NodeSet.NodeCategory;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T>
 */
public interface NodeSetBuilder<T> {

    public void addNode( NodeCategory nodeCategory, long nodeId, long edgeId, float length, float speed );

    public void addCrossroad( NodeCategory nodeCategory, long nodeId );

    public T build() throws EvaluableOnlyException;
}
