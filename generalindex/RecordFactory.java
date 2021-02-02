package com.boostasoft.goaway.tilingDB.generalindex;

import com.hp.hpl.jena.graph.Graph;

/**
 * A factory for creating {@link Record}s. Each {@link Index} must provide a
 * <code>RecordFactory</code> that will be used to generate records from
 * <code>Graphe</code>s.
 *
 * @author Dtsatcha
 */
public interface RecordFactory<T> {
	/** permet de retourner un graphe de noeuds construit */
	public Record<T> createRecord(Graph graph);
	     

}
