package com.boostasoft.goaway.tilingDB.webentity.spatial;

import com.hp.hpl.jena.graph.Graph;
import com.vividsolutions.jts.geom.Geometry;




public interface GeometryRecordFactory extends com.boostasoft.goaway.tilingDB.generalindex.RecordFactory<Geometry> {
   /**
    * Create a record from a triple.
    *
    * @param triple
    *           a triple.
    * @return a record based on information in the triple, or <code>null</code>
    *         if no record could be made.
    */
   @Override
   public GeometryRecord createRecord(Graph graph);
}
