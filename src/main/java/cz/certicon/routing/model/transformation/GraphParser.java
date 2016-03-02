/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.transformation;

import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.data.MapDataSource;

/**
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public interface GraphParser {

    public Graph parse( MapDataSource dataSource );
}
