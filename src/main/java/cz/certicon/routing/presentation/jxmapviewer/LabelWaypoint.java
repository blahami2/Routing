/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.presentation.jxmapviewer;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.jdesktop.swingx.mapviewer.DefaultWaypoint;
import org.jdesktop.swingx.mapviewer.GeoPosition;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class LabelWaypoint extends DefaultWaypoint {

        private final JLabel button;
        private final String text;

        public LabelWaypoint( String text, GeoPosition coord ) {
            super( coord );
            this.text = text;
            button = new JLabel( "tags" );
            button.setSize( 24, 24 );
            button.setPreferredSize( new Dimension( 24, 24 ) );
            button.addMouseListener( new SwingWaypointMouseListener() );
            button.setVisible( true );
        }

        public JComponent getComponent() {
            return button;
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
