/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.presentation.jxmapviewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.Painter;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class NodePainter implements Painter<JXMapViewer> {

    private Color color = Color.GREEN;
    private boolean antiAlias = true;

    private GeoPosition position;

    /**
     * @param position
     */
    public NodePainter( GeoPosition position ) {
        // copy the list so that changes in the 
        // original list do not have an effect here
        this.position = position;
    }

    @Override
    public void paint( Graphics2D g, JXMapViewer map, int w, int h ) {
        g = (Graphics2D) g.create();

        // convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate( -rect.x, -rect.y );

        if ( antiAlias ) {
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        }

        // do the drawing
        g.setColor( Color.BLACK );
        g.setStroke( new BasicStroke( 4 ) );

        drawNode( g, map );

        // do the drawing again
        g.setColor( color );
        g.setStroke( new BasicStroke( 2 ) );

        drawNode( g, map );

        g.dispose();
    }

    /**
     * @param g the graphics object
     * @param map the map
     */
    private void drawNode( Graphics2D g, JXMapViewer map ) {
        // convert geo-coordinate to world bitmap pixel
        Point2D pt = map.getTileFactory().geoToPixel( position, map.getZoom() );

        g.drawOval( (int) pt.getX(), (int) pt.getY(), 8, 8 );
    }
}
