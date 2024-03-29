package com.boostasoft.goaway.tilingDB.webentity.spatial;

// Parliament is licensed under the BSD License from the Open Source
// Initiative, http://www.opensource.org/licenses/bsd-license.php
//
// Copyright (c) 2001-2009, BBN Technologies, Inc.
// All rights reserved.

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;



/**
 * @author Dtsatcha
 */
public class GeometryConverter {
   private static Logger LOG = LoggerFactory.getLogger(GeometryConverter.class);

   public static Point createPoint(double[] coord) {
      Coordinate c = new Coordinate(coord[1], coord[0]);
      Point p = SpatialGeometryFactory.GEOMETRY_FACTORY.createPoint(c);
      return p;
   }

   public static LineString createLineString(double[] points) {
      List<Coordinate> coords = new ArrayList<>();
      for (int i = 0; i < points.length; i += 2) {
         Coordinate c = new Coordinate(points[i + 1], points[i]);
         coords.add(c);
      }
      LineString string = SpatialGeometryFactory.GEOMETRY_FACTORY
            .createLineString(coords.toArray(new Coordinate[] {}));
      return string;
   }

   public static LinearRing createLinearRing(double[] ring) {
      List<Coordinate> coords = new ArrayList<>();
      for (int i = 0; i < ring.length; i += 2) {
         Coordinate c = new Coordinate(ring[i + 1], ring[i]);
         coords.add(c);
      }
      if (!(coords.get(0).equals(coords.get(coords.size() - 1)))) {
         coords.add(coords.get(0));
      }
      LinearRing shell = SpatialGeometryFactory.GEOMETRY_FACTORY
            .createLinearRing(coords.toArray(new Coordinate[] {}));
      return shell;
   }

   public static Polygon createPolygon(double[] ring) {
      LinearRing shell = createLinearRing(ring);
      Polygon p = SpatialGeometryFactory.GEOMETRY_FACTORY.createPolygon(shell,
                                                                       null);
      return p;
   }

   
   
   
   public static Geometry convertSQLGeometry(Object value)
         throws SpatialIndexException {
      Geometry extent = null;

      byte[] wkb = (byte[]) value;
      WKBReader reader = new WKBReader(SpatialGeometryFactory.GEOMETRY_FACTORY);
      try {
         extent = reader.read(wkb);
      } catch (ParseException e) {
         LOG.error("ParseException", e);
         return null;
      }

      extent.setSRID(Constants.WGS84_SRID);
      return extent;
   }
   
   
   
   

//   public static Node getNodeRepresentation(String representation) {
//      Node result = null;
//      if (representation.startsWith(KbGraph.MAGICAL_BNODE_PREFIX)) {
//         result = Node.createAnon(AnonId.create(representation
//               .substring(KbGraph.MAGICAL_BNODE_PREFIX.length())));
//      } else {
//         result = Node.createURI(representation);
//      }
//      return result;
//   }
//
//   public static String getStringRepresentation(Node n) {
//      String stringRep = n.toString();
//      if (n.isBlank()) {
//         stringRep = KbGraph.MAGICAL_BNODE_PREFIX + stringRep;
//      }
//      return stringRep;
//   }

   public static byte[] convertGeometry(Geometry extent) {
      byte[] wkb = null;

      WKBWriter writer = new WKBWriter();
      wkb = writer.write(extent);
      return wkb;
   }

   public static String makeBoundingBox(Geometry boundExtent) {
      String boundingBox = null;
      Coordinate[] coords = boundExtent.getCoordinates();

      double xmin = Double.MAX_VALUE;
      double xmax = Double.MIN_VALUE;
      double ymin = Double.MAX_VALUE;
      double ymax = Double.MIN_VALUE;
      for (int i = 0; i < coords.length; i++) {
         Coordinate c = coords[i];
         if (c.x < xmin) {
            xmin = c.x;
         }
         if (c.x > xmax) {
            xmax = c.x;
         }
         if (c.y < ymin) {
            ymin = c.y;
         }
         if (c.y > ymax) {
            ymax = c.y;
         }
      }
      boundingBox = xmin + " " + ymin + ", " + xmax + " " + ymax;
      return "BOX(" + boundingBox + ")";
   }
}
