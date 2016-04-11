/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph.osm2po;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.data.basic.database.AbstractDatabase;
import cz.certicon.routing.data.graph.GraphReader;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.EdgeAttributes;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.common.SimpleEdgeAttributes;
import cz.certicon.routing.model.entity.common.SimpleEdgeData;
import cz.certicon.routing.utils.DoubleComparator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @deprecated osm2po is not supported anymore
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Osm2poGraphReader extends AbstractDatabase<Graph, Pair<GraphEntityFactory, DistanceFactory>> implements GraphReader {

    public Osm2poGraphReader( Properties connectionProperties ) {
        super( connectionProperties );
    }

    @Override
    protected Graph checkedRead( Pair<GraphEntityFactory, DistanceFactory> in ) throws SQLException {
        GraphEntityFactory graphEntityFactory = in.a;
        DistanceFactory distanceFactory = in.b;
        Graph graph = graphEntityFactory.createGraph();
        Map<Long, Node> nodeMap = new HashMap<>();
        ResultSet r = getStatement().executeQuery( "SELECT osm_id, ST_AsX3D(geom_vertex) AS geom FROM prg_2po_vertex;" );
        while ( r.next() ) {
            long id = r.getLong( "osm_id" );
            String[] co = r.getString( "geom" ).split( " " );
            double lon = Double.parseDouble( co[0] );
            double lat = Double.parseDouble( co[1] );
            Node node = graphEntityFactory.createNode( Node.Id.createId( id ), lat, lon );
            nodeMap.put( id, node );
            graph.addNode( node );
        }
        r = getStatement().executeQuery( "SELECT id,osm_source_id, osm_target_id, cost, reverse_cost, km, kmh, osm_meta FROM prg_2po_4pgr;" );
        while ( r.next() ) {
            long id = r.getLong( "id" );
            long source = r.getLong( "osm_source_id" );
            long target = r.getLong( "osm_target_id" );
            double cost = r.getDouble( "cost" );
            double reverseCost = r.getDouble( "reverse_cost" );
            double length = r.getDouble( "km" );
            int speed = r.getInt( "kmh" );
            EdgeData edgeData = new SimpleEdgeData(nodeMap.get( source )
                    , nodeMap.get( target )
                    , speed
                    , false
                    , length );
            EdgeAttributes edgeAttributes = SimpleEdgeAttributes.builder()
                    .setLength( length )
                    .setPaid( false ) // determine paid? See config for details, extend tag stuff class
                    .build();
            Edge edge = graphEntityFactory.createEdge( Edge.Id.createId( id ), edgeData.getSource(), edgeData.getTarget(), distanceFactory.createFromEdgeData( edgeData ) );
            edge.setAttributes( edgeAttributes );
            edge.setLabel( r.getString( "osm_meta" ) );
            graph.addEdge( edge );
        }
        return graph;
    }

    @Override
    protected void checkedWrite( Graph in ) throws SQLException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

}
