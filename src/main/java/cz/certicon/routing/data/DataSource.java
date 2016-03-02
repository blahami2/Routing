/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface DataSource {

    public DataSource open() throws IOException;

    public int read() throws IOException;

    public InputStream getInputStream() throws IOException;

    public DataSource close() throws IOException;
}
