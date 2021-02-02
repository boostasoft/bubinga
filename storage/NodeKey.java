package com.boostasoft.goaway.tilingDB.storage;


import java.io.Serializable;

/**
 * Representation de la clé d'une map stocké.
 *
 * @author Dtsatcha
 *
 */
public class NodeKey implements Serializable {

   private static final long serialVersionUID = -8015836109240995962L;

   private String node;

   /**
    * Crèe une nouvelle instance.
    *
    * @param node
    *           the node.
    */
   public NodeKey(String node) {
      this.node = node;
   }

   /**
    *Obtient le nom.
    *
    * @return le noeud.
    */
   public String getNode() {
      return node;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      return "NodeKey [node=" + node + "]";
   }
}
