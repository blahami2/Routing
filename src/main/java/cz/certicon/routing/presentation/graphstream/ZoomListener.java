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
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.view.Camera;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ZoomListener implements MouseWheelListener, MouseMotionListener {

        private static final double MULTIPLIER = 0.7;
        private static final double TOP_LIMIT = 1.0;
        private static final double BOTTOM_LIMIT = 0.001;

        private final Camera camera;
        private double zoom = 1.0;
        private int x = -1;
        private int y = -1;

        public ZoomListener( Camera camera ) {
            this.camera = camera;
            Point3 viewCenter = camera.getViewCenter();
            Point3 centerInPx = camera.transformGuToPx( viewCenter.x, viewCenter.y, viewCenter.z );
            this.x =(int) centerInPx.x;
            this.y =(int) centerInPx.y;
        }

        @Override
        public void mouseWheelMoved( MouseWheelEvent e ) {
            if(x == -1 || y == -1){
                return;
            }
//            System.out.println( "event: " + e.getPreciseWheelRotation() );
//System.out.println( "center= " + camera.getViewCenter() );
//            System.out.println( "trans center = " + camera.transformGuToPx( camera.getViewCenter().x,camera.getViewCenter().y, camera.getViewCenter().z) );
            Point3 point = camera.transformPxToGu( x, y);
//            System.out.println( point );
            camera.setViewCenter( point.x, point.y, 0 );
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
            
//            System.out.println( "zooming to: " + zoom );
        }

        @Override
        public void mouseDragged( MouseEvent e ) {
        }

        @Override
        public void mouseMoved( MouseEvent e ) {
            x = e.getX();
            y = e.getY();
//            System.out.println( "x = " + x + ", y = " + y );
            
            Point3 point = camera.transformPxToGu( x, y);
//            System.out.println( point );
        }

    }
