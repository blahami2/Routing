/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.debug;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Extension to {@link Formatter}. Formats records into plain text.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class PlainTextFormatter extends Formatter {

    @Override
    public String format( LogRecord record ) {
        return record.getMessage();
    }

}
