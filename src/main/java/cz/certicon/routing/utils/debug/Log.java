/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.debug;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logging class offering somewhat Android-like printing tools. Logs into .log
 * files.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Log {

    private static final Map<String, Logger> LOGGERS_MAP = new HashMap<>();

    /**
     * Log debug message with the given tag (logs into a file [tag].log). Does
     * not add end of line.
     *
     * @param tag name of the target file
     * @param message message to be logged
     */
    public static void d( String tag, String message ) {
        getLogger( tag ).info( message );
    }

    /**
     * Log debug message with the given tag (logs into a file [tag].log). Adds
     * end of line.
     *
     * @param tag name of the target file
     * @param message message to be logged
     */
    public static void dln( String tag, String message ) {
        String msg = message + "\n";
        getLogger( tag ).info( msg );
    }

    private static Logger getLogger( String tag ) {
        Logger logger = LOGGERS_MAP.get( tag );
        if ( logger == null ) {
            try {
                FileHandler fileHandler = new FileHandler( tag + ".log" );
                fileHandler.setFormatter( new PlainTextFormatter() );
                logger = Logger.getLogger( tag );
                logger.addHandler( fileHandler );
                LOGGERS_MAP.put( tag, logger );
            } catch ( IOException | SecurityException ex ) {
                Logger.getLogger( Log.class.getName() ).log( Level.SEVERE, null, ex );
            }
        }
        return logger;
    }
}
