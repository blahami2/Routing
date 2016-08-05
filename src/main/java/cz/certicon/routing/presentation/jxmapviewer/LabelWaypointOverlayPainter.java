/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.presentation.jxmapviewer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import javax.swing.JComponent;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.WaypointPainter;

/**
 * An implementation of {@link WaypointPainter} supporting LabeLWaypoint
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class LabelWaypointOverlayPainter extends WaypointPainter<LabelWaypoint> {

    @Override
    protected void doPaint( Graphics2D g, JXMapViewer jxMapViewer, int width, int height ) {
        for ( LabelWaypoint waypoint : getWaypoints() ) {
            Point2D point = jxMapViewer.getTileFactory().geoToPixel(
                    waypoint.getPosition(), jxMapViewer.getZoom() );
            Rectangle rectangle = jxMapViewer.getViewportBounds();
            int buttonX = (int) ( point.getX() - rectangle.getX() );
            int buttonY = (int) ( point.getY() - rectangle.getY() );
            JComponent button = waypoint.getComponent();
            button.setLocation( buttonX - button.getWidth() / 2, buttonY - button.getHeight() / 2 );
        }
    }
}
