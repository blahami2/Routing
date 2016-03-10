/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Reader<In,Out> {

    public void open() throws IOException;

    public Out read(In in) throws IOException;
    
    public void close() throws IOException;
    
    public boolean isOpen();
}
