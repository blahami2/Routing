/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.presentation.jxmapviewer;

import cz.certicon.routing.presentation.jxmapviewer.*;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.jdesktop.swingx.mapviewer.DefaultWaypoint;
import org.jdesktop.swingx.mapviewer.GeoPosition;

/**
 * A waypoint class representing a waypoint with as a label
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class LabelWaypoint extends DefaultWaypoint {

    private final JLabel button;
    private final String text;
    
    private static final int MAX_LENGTH = 5;

    public LabelWaypoint( String text, GeoPosition coord ) {
        super( coord );
        this.text = text;
        button = new JLabel( ( text.length() < MAX_LENGTH ) ? ( ( text.isEmpty() ) ? "empty" : text ) : text.substring( 0, MAX_LENGTH ) );
//        button.setSize( 24, 24 );
//        button.setPreferredSize( new Dimension( 24, 24 ) );
        button.addMouseListener( new SwingWaypointMouseListener() );
        button.setVisible( true );
    }

    public JComponent getComponent() {
        return button;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode( this.getPosition() );
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final LabelWaypoint other = (LabelWaypoint) obj;
        if ( !Objects.equals( this.getPosition(), other.getPosition() ) ) {
            return false;
        }
        return true;
    }
    
    

    private class SwingWaypointMouseListener implements MouseListener {

        @Override
        public void mouseClicked( MouseEvent e ) {
            JOptionPane.showMessageDialog( button, text );
        }

        @Override
        public void mousePressed( MouseEvent e ) {
        }

        @Override
        public void mouseReleased( MouseEvent e ) {
        }

        @Override
        public void mouseEntered( MouseEvent e ) {
        }

        @Override
        public void mouseExited( MouseEvent e ) {
        }
    }
}
