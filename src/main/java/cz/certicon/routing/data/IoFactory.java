/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

/**
 *
 * @deprecated it does not make any sense to use this
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface IoFactory<R extends Reader, W extends Writer> {

    public R createReader( DataSource dataSource );

    public W createWriter( DataDestination dataDestination );

}
