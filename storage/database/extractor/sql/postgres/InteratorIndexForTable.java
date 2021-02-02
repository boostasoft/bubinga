package com.boostasoft.goaway.tilingDB.storage.database.extractor.sql.postgres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boostasoft.goaway.tilingDB.datastructure.Node;
import com.boostasoft.goaway.tilingDB.datastructure.management.ScaleInfo;
import com.boostasoft.goaway.tilingDB.generalindex.GeometryConverter;
import com.boostasoft.goaway.tilingDB.generalindex.Record;
import com.boostasoft.goaway.tilingDB.webentity.spatial.GeometryRecord;
import com.boostasoft.goaway.tilingDB.webentity.spatial.SpatialIndexException;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.vividsolutions.jts.geom.Geometry;


/*
 * Cette classe permet de fabriquer une liste d'iterateur
 * qui sera utilisé par l'index spatial...
 * cette iterateur devra renvoyer l'echelle des informations sur les noeuds
 * construits 
 * 
 * Dr Dtsatcha
 */


public class InteratorIndexForTable implements
        ClosableIterator<Record<Geometry>> {
    public List<Record<Geometry>> getContent() {
        return content;
    }

    public void setContent(List<Record<Geometry>> content) {
        this.content = content;
    }

    protected static Logger LOG = LoggerFactory
            .getLogger(InteratorIndexForTable.class);

    protected ResultSet resultSet;
    protected String table;
    protected ScaleInfo scale;
   protected String signature;
    private boolean hasNextCalled = false;
    private boolean hasNext = true;
    protected List<Record<Geometry>> content= new ArrayList<Record<Geometry>>();
    private int pos;

    public InteratorIndexForTable(ResultSet rs,
            String tableParam, String signatureParam, ScaleInfo scaleParam) {
       
        this.resultSet = rs;
        this.table = tableParam;
        this.signature = signatureParam;
        this.scale=scaleParam;
        this.pos = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        try {
            if (hasNextCalled) {
                return hasNext;
            }

            if (hasNext) {
                hasNextCalled = true;
                hasNext = resultSet.next();
                if (!hasNext) {
                    close();
                }
            }
            return hasNext;
        } catch (SQLException e) {
            LOG.error("Error while checking whether result set has next", e);
            close();
            throw new RuntimeException(
                    "Error while checking whether result set has next", e);
            // hasNext = false;
            // return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Record<Geometry> next() {
        Record<Geometry> obj = null;
       
        if (!hasNext) {
            close();
            throw new RuntimeException("No more results in result set");
        }
        try {
            if (!hasNextCalled && hasNext) {
                hasNext = resultSet.next();
            }
            hasNextCalled = false;
            pos++;
            // resultSet.getObject(2).toString() designe la valeur du, srid
            obj = GeometryRecord.create(generateNode(table,resultSet.getObject(2).toString(),signature), getGeometry());
            System.out.println(obj.toString());
           //content.add(obj);
        } catch (SQLException e) {
            LOG.error("Error while moving to next row", e);
            close();
            hasNext = false;
            return null;
        }
        return obj;
    }

    public int getPosition() {
        return pos;
    }

    // modifier cette methode plus tard pour avoir les
    // contenu du noeuds de la colonne à extraire
    // cette geometry est formée à partir
    // des donnees string
    // cette methode a été modifie afin de 
    // la construction de la geomatrie en fonction du srid fourni

    public Geometry getGeometry() {
        try {
           /// System.out.println("la valeur de ma geometrie a été generée"+resultSet
                  //  .getObject(1).toString());
            return GeometryConverter.convertSQLGeometry(resultSet
                    .getObject(1).toString(), Integer.parseInt(resultSet.getObject(2).toString()));
        } catch (SQLException e) {
            LOG.error("Error while moving to next row", e);
            close();
            return null;
        } catch (SpatialIndexException e) {
            LOG.error("Error while converting geometry", e);
            close();
            return null;
        }
    }

    
    // permet de gerer un noeud de stockage
    // pour l'index spatial des contenu extraite dans la base données
    // @param row les informations sur de la table
    public Node generateNode(String tableName, String srid, String signature) {
        ScaleInfo scale = null;
       // scale=generateScale(table);
      Node node = new Node(srid,this.scale,signature,tableName,pos);
      return node;
    }

    @Override
    public void remove() {
       throw new UnsupportedOperationException("Can not remove a value");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
       try {
          resultSet.close();
       } catch (SQLException e) {
          LOG.error("Error while closing result set", e);
       }

    }

   
   
}
