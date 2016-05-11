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
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Log {

    private static Map<String, Logger> loggersMap = new HashMap<>();

    public static void d( String tag, String message ) {
        getLogger( tag ).info( message );
    }

    public static void dln( String tag, String message ) {
        getLogger( tag ).info( message + "\n" );
    }

    private static Logger getLogger( String tag ) {
        Logger logger = loggersMap.get( tag );
        if ( logger == null ) {
            try {
                FileHandler fileHandler = new FileHandler( tag + ".log" );
                fileHandler.setFormatter( new PlainTextFormatter() );
                logger = Logger.getLogger( tag );
                logger.addHandler( fileHandler );
                loggersMap.put( tag, logger );
            } catch ( IOException ex ) {
                Logger.getLogger( Log.class.getName() ).log( Level.SEVERE, null, ex );
            } catch ( SecurityException ex ) {
                Logger.getLogger( Log.class.getName() ).log( Level.SEVERE, null, ex );
            }
        }
        return logger;
    }
}
