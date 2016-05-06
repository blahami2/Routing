/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.presentation.graphstream;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Calendar;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.view.Camera;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ZoomListener implements MouseWheelListener, MouseMotionListener {

    private static final double MULTIPLIER = 0.7;
    private static final double TOP_LIMIT = 1.0;
    private static final double BOTTOM_LIMIT = 0.0001;
    private static final long TIME_LIMIT = 500;

    private final Camera camera;
    private double zoom = 1.0;
    private long lastZoomTime = 0;

    private int x = -1;
    private int y = -1;

    private int dragX = -1;
    private int dragY = -1;

    public ZoomListener( Camera camera ) {
        this.camera = camera;
        Point3 viewCenter = camera.getViewCenter();
        Point3 centerInPx = camera.transformGuToPx( viewCenter.x, viewCenter.y, viewCenter.z );
        this.x = (int) centerInPx.x;
        this.y = (int) centerInPx.y;
    }

    @Override
    public void mouseWheelMoved( MouseWheelEvent e ) {

        long zoomTime = Calendar.getInstance().getTimeInMillis();
        if ( zoomTime - lastZoomTime > TIME_LIMIT ) {
            Point3 point = camera.transformPxToGu( x, y );
            camera.setViewCenter( point.x, point.y, 0 );
        }

        if ( x == -1 || y == -1 ) {
            return;
        }
        if ( e.getPreciseWheelRotation() < 0 ) {
            zoom *= MULTIPLIER;
        } else {
            zoom /= MULTIPLIER;
        }
        if ( zoom > TOP_LIMIT ) {
            zoom = TOP_LIMIT;
        }
        if ( zoom < BOTTOM_LIMIT ) {
            zoom = BOTTOM_LIMIT;
        }
        camera.setViewPercent( zoom );

        this.lastZoomTime = zoomTime;
//            System.out.println( "zooming to: " + zoom );
    }

    @Override
    public void mouseDragged( MouseEvent e ) {
        if ( dragX != -1 && dragY != -1 ) {
            Point3 currentCenter = camera.getViewCenter();
            Point3 centerInPx = camera.transformGuToPx( currentCenter.x, currentCenter.y, currentCenter.z );
            int xDiff = dragX - e.getX();
            int yDiff = dragY - e.getY();
            Point3 newCenter = camera.transformPxToGu( centerInPx.x + xDiff, centerInPx.y + yDiff );
            camera.setViewCenter( newCenter.x, newCenter.y, newCenter.z );
        }
        dragX = e.getX();
        dragY = e.getY();
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
        x = e.getX();
        y = e.getY();
        dragX = -1;
        dragY = -1;
    }

}
