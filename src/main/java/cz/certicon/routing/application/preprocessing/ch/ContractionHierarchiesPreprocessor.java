/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.preprocessing.ch;

import cz.certicon.routing.application.preprocessing.Preprocessor;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Shortcut;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ContractionHierarchiesPreprocessor extends Preprocessor<Pair<Map<Node.Id, Integer>, List<Shortcut>>> {

}
