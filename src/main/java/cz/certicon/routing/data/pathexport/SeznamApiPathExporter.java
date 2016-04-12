/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.pathexport;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.PathExporter;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.utils.CoordinateUtils;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of {@link PathExporter} for the JavaScript seznam API
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SeznamApiPathExporter implements PathExporter {

    @Override
    public void exportPath( DataDestination destination, Path path ) throws IOException {
        destination.open();
        destination.write(
                "<!doctype html>\n"
                + "        <html>\n"
                + "        <head>\n"
                + "            <script src=\"http://api4.mapy.cz/loader.js\"></script>\n"
                + "            <script>Loader.load();</script>\n"
                + "        </head>\n"
                + "\n"
                + "        <body>\n"
                + "            <div id=\"map\" style=\"width:1280px; height:1024px;\"></div>\n"
                + "            <script type=\"text/javascript\">\n"
        );

        List<Coordinates> coordinates = new LinkedList<>();
        for ( Edge edge : path ) {
            coordinates.add( path.getGraph().getSourceNodeOf( edge ).getCoordinates() );
        }
        coordinates.add( path.getTargetNode().getCoordinates() );
        Coordinates midpoint = CoordinateUtils.calculateGeographicMidpoint( coordinates );

        destination.write( "                var coords = [];\n" );
        for ( Coordinates coord : path.getCoordinates()) {
                destination.write(
                        "                coords.push(\n"
                        + "                    SMap.Coords.fromWGS84(" + coord.getLongitude() + ", " + coord.getLatitude() + ")\n"
                        + "                );\n"
                );
        }
        destination.write(
                "                var centerMap = SMap.Coords.fromWGS84(" + midpoint.getLongitude() + ", " + midpoint.getLatitude() + ");\n"
                + "                var m = new SMap(JAK.gel(\"map\"), centerMap);\n" // 3rd parameter is zoom - integer with unknown range (optional)
                + "                var l = m.addDefaultLayer(SMap.DEF_BASE).enable();\n"
                + "                m.addDefaultControls();                    \n"
                + "\n"
                + "                    var layer = new SMap.Layer.Geometry();\n"
                + "                    m.addLayer(layer).enable();\n"
                + "                    var g = new SMap.Geometry(SMap.GEOMETRY_POLYLINE, null, coords);\n"
                + "                    layer.addGeometry(g);\n"
                //                + "                }\n"
                + "\n"
        );

//        for ( Edge edge : path ) {
//            destination.write(
//                    "                new SMap.Route([\n"
//                    + "                    SMap.Coords.fromWGS84(" + edge.getStartNode().getCoordinates().getLatitude() + ", " + edge.getStartNode().getCoordinates().getLongitude() + "),\n"
//                    + "                    SMap.Coords.fromWGS84(" + edge.getEndNode().getCoordinates().getLatitude() + ", " + edge.getEndNode().getCoordinates().getLongitude() + ")\n"
//                    + "                ], onRouteFound);\n"
//            );
//        }
        destination.write(
                "        \n"
                + "                var cz = m.computeCenterZoom(coords);\n"
                + "                m.setCenterZoom(cz[0], cz[1]);\n" // might break the centering, test
                + "        \n"
                + "                var marksLayer = new SMap.Layer.Marker();\n"
                + "                m.addLayer(marksLayer);\n"
                + "                marksLayer.enable();\n"
                + "                \n"
                + "                var options = {};\n"
                + "                var marker = new SMap.Marker(coords[0], \"Start\", options);\n"
                + "                marksLayer.addMarker(marker);\n"
                + "                var marker = new SMap.Marker(coords[coords.length - 1], \"Destination\", options);\n"
                + "                marksLayer.addMarker(marker);\n"
                + "            </script>\n"
                + "        </body>\n"
                + "        </html>"
        );
        destination.close();
        /*
            <!doctype html>
            <html>
            <head>
            <script src="http://api4.mapy.cz/loader.js"></script>
            <script>Loader.load();</script>
            </head>
            <body>
            <div id="map" style="width:600px; height:400px;"></div>
            <script type="text/javascript">
            var centerMap = SMap.Coords.fromWGS84(14.40, 50.08);
            var m = new SMap(JAK.gel("map"), centerMap, 16);
            var l = m.addDefaultLayer(SMap.DEF_BASE).enable();
            m.addDefaultControls();
            var onRouteFound = function(route) {
            var layer = new SMap.Layer.Geometry();
            m.addLayer(layer).enable();
            var coords = route.getResults().geometry;
            var cz = m.computeCenterZoom(coords);
            m.setCenterZoom(cz[0], cz[1]);
            var g = new SMap.Geometry(SMap.GEOMETRY_POLYLINE, null, coords);
            layer.addGeometry(g);
            }
            var coords = [
            SMap.Coords.fromWGS84(14.434, 50.084),
            SMap.Coords.fromWGS84(14.4, 50.195)
            ];
            var route = new SMap.Route(coords, onRouteFound);
            </script>
            </body>
            </html>
         */
    }

}
