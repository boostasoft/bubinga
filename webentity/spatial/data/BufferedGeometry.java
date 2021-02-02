package com.boostasoft.goaway.tilingDB.webentity.spatial.data;

// Parliament is licensed under the BSD License from the Open Source
// Initiative, http://www.opensource.org/licenses/bsd-license.php
//
// Copyright (c) 2001-2009, BBN Technologies, Inc.
// All rights reserved.

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

import com.boostasoft.goaway.tilingDB.generalindex.Constants;
import com.boostasoft.goaway.tilingDB.webentity.spatial.SpatialGeometryFactory;
/**
 * @author Dtsatcha
 */
public class BufferedGeometry extends EphemeralGeometry {
   private static final long serialVersionUID = -2610110325142994450L;
   protected static final Logger LOG = LoggerFactory
         .getLogger(BufferedGeometry.class);
   private Geometry extent;
   private Double distance;
   private Geometry buffer;
   private static CoordinateReferenceSystem WGS84_CRS;
   private static final BufferParameters BUFFER_PARAMS = new BufferParameters(
            Constants.SEGMENTS_PER_QUADRANT, BufferParameters.CAP_ROUND,
      BufferParameters.JOIN_ROUND, BufferParameters.DEFAULT_MITRE_LIMIT);

   static {
      try {
         WGS84_CRS = CRS.decode("EPSG:4326");
      } catch (NoSuchAuthorityCodeException e) {
         LOG.error("NoSuchAuthorityCodeException", e);
      } catch (FactoryException e) {
         LOG.error("FactoryException", e);
      }
   }

   public BufferedGeometry(GeometryFactory factory, Geometry extent) {
      super(factory);
      this.extent = extent;
   }

   public BufferedGeometry(GeometryFactory factory, Geometry extent,
                           double distance) {
      super(factory);
      this.extent = extent;
      this.distance = distance;
   }

   /**
    * @return the distance
    */
   public Double getDistance() {
      return distance;
   }

   /**
    * @param distance
    *           the distance to set
    */
   public void setDistance(Double distance) {
      this.distance = distance;
   }

   /**
    * @return the extent
    */
   public Geometry getExtent() {
      return extent;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      return "BufferedGeometry: " + extent + ", " + distance;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getGeometryType() {
      return "Buffer";
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getNumPoints() {
      return 0;
   }

   public Geometry getBufferedGeometry() {
      if (buffer != null) {
         return buffer;
      }
      if (distance == null) {
         return null;
      }
      CoordinateReferenceSystem destCRS = null;
      int srid = SpatialGeometryFactory.UTMZoneSRID(extent.getEnvelope());

      try {

         destCRS = CRS.decode("EPSG:" + srid);

         Geometry buffered = projectAndBuffer(extent, distance, destCRS);
         boolean canReproject = true;
         try {
            JTS.checkCoordinatesRange(buffered, WGS84_CRS);
            buffer = buffered;
         } catch (PointOutsideEnvelopeException e) {
            canReproject = false;
         }
         if (!canReproject) {
            if (extent instanceof Point) {
               Point point = (Point) extent;
               GeodeticCalculator calc = new GeodeticCalculator();
               calc.setStartingGeographicPoint(point.getX(), point.getY());
               List<Coordinate> coords = new ArrayList<>();
               Coordinate first = null;
               for (int i = -180; i <= 180; i += 30) {
                  calc.setDirection(i, distance);
                  Coordinate coord = new Coordinate(calc
                        .getDestinationGeographicPoint().getX(), calc
                        .getDestinationGeographicPoint().getY());
                  if (null == first) {
                     first = coord;
                  }
                  coords.add(coord);
               }
               if (!coords.get(coords.size() - 1).equals(first)) {
                  coords.add(first);
               }
               LinearRing shell = extent.getFactory()
                     .createLinearRing(coords.toArray(new Coordinate[] {}));
               buffer = extent.getFactory().createPolygon(shell, null);
            } else {
               Envelope e = extent.getEnvelopeInternal();
               GeodeticCalculator calc = new GeodeticCalculator();
               List<Coordinate> coords = new ArrayList<>();
               Coordinate first = null;

               for (int index = 0; index < 4; index++) {
                  Coordinate point = null;
                  int[] degrees;
                  switch (index) {
                  case 0:
                     point = new Coordinate(e.getMaxX(), e.getMaxY());
                     degrees = new int[] { 0, 30, 60, 90 };
                     break;
                  case 1:
                     point = new Coordinate(e.getMaxX(), e.getMinY());
                     degrees = new int[] { 90, 120, 150, 180 };
                     break;
                  case 2:
                     point = new Coordinate(e.getMinX(), e.getMinY());
                     degrees = new int[] { -180, -150, -120, -90 };
                     break;
                  case 3:
                     point = new Coordinate(e.getMinX(), e.getMaxY());
                     degrees = new int[] { -90, -60, -30, 0 };
                     break;
                  default:
                     throw new RuntimeException("Invalid index");
                  }

                  calc.setStartingGeographicPoint(point.x, point.y);
                  for (int i : degrees) {
                     calc.setDirection(i, distance);
                     Coordinate coord = new Coordinate(calc
                           .getDestinationGeographicPoint().getX(), calc
                           .getDestinationGeographicPoint().getY());
                     if (null == first) {
                        first = coord;
                     }
                     coords.add(coord);
                  }
               }
               if (!coords.get(coords.size() - 1).equals(first)) {
                  coords.add(first);
               }
               LinearRing shell = extent.getFactory()
                     .createLinearRing(coords.toArray(new Coordinate[] {}));
               buffer = extent.getFactory().createPolygon(shell, null);
            }
         }
         return buffer;
      } catch (NoSuchAuthorityCodeException e) {
         LOG.error("NoSuchAuthorityCodeException", e);
      } catch (FactoryException e) {
         LOG.error("FactoryException", e);
      } catch (MismatchedDimensionException e) {
         LOG.error("MismatchedDimensionException", e);
      } catch (TransformException e) {
         LOG.error("TransformException", e);
      }
      return null;
   }

   private static Geometry projectAndBuffer(Geometry extent, double distance,
         CoordinateReferenceSystem destination) throws FactoryException,
         MismatchedDimensionException, TransformException {
      LOG.debug("Transforming: {} from {} to {}",
                new Object[] { extent.getEnvelope(),
                      WGS84_CRS.getName().getCode(),
                      destination.getName().getCode() });
      MathTransform transform = CRS.findMathTransform(WGS84_CRS, destination);
      MathTransform reverseTransform = transform.inverse();
      Geometry targetGeometry = JTS.transform(extent, transform);

      BufferOp op = new BufferOp(targetGeometry, BUFFER_PARAMS);

      Geometry buffered = op.getResultGeometry(distance);
      return JTS.transform(buffered, reverseTransform);
   }
}
