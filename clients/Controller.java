package com.boostasoft.goaway.tilingDB.clients;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.boostasoft.goaway.tilingDB.datastructure.Node;
import com.boostasoft.goaway.tilingDB.datastructure.RTreeIndex;
import com.boostasoft.goaway.tilingDB.datastructure.management.ManageRtreeMultiLevel;
import com.boostasoft.goaway.tilingDB.datastructure.management.ScaleInfo;
import com.boostasoft.goaway.tilingDB.generalindex.Record;
import com.boostasoft.goaway.tilingDB.storage.database.extractor.sql.postgres.InteratorIndexForTable;
import com.boostasoft.goaway.tilingDB.storage.database.extractor.sql.postgres.PostgresExtractor;
import com.boostasoft.goaway.tilingDB.util.GeometryUtil;
import com.boostasoft.goaway.tilingDB.webentity.spatial.SpatialIndex;
import com.vividsolutions.jts.geom.Geometry;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiPoint;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.FeatureType;
import fr.ign.cogit.geoxygene.util.conversion.WktGeOxygene;

/*
 * Cette classe permet d'effecteur la communication entre un client
 * la base de données et l'index associé...
 *  @author Dr Tsatcha
 */

public class Controller {

	/** Logger. */
	private final static Logger LOGGER = Logger.getLogger(Controller.class.getName());

	/*
	 * @param myextractor designe l'extracteur de la base de données postgre
	 */
	protected PostgresExtractor myextractor;
	/*
	 * @param designe le manager qui s'occupe de la gestion du Rtree
	 * multi-echelle
	 */

	protected ManageRtreeMultiLevel manager;

	/**
	 * @param manager
	 *            designe le manager qui permet de gerer l'index
	 * @param myextractorParam
	 *            se charge pour la connexion à la base de données
	 * 
	 */

	private boolean bulkLoading = false;

	public Controller(ManageRtreeMultiLevel managerParam, PostgresExtractor myextractorParam) {
		super();
		this.manager = managerParam;
		this.myextractor = myextractorParam;
	}

	/*
	 * permet de generer une population d'une table pour affichage à geoxygene
	 * 
	 * @param tableName designe la table à laquelle on s'interesse
	 * 
	 * @param signature elle peut etre : geometrie ou empreinte
	 * 
	 * @return retourne une population geoxygene de DefaultFeature
	 */
	public Population<DefaultFeature> generateTablePopFromPostgis(String tableName, String signature, ScaleInfo scale,
			SpatialIndex index) {

		Population<DefaultFeature> pop = new Population<DefaultFeature>();

		// retourne un iterateur d'information de la base de données
		InteratorIndexForTable it = myextractor.generateIteratorTable(tableName, signature, scale);

		return loadValuesAndConvert(index, it);

	}

	/*
	 * L'interet de cette approche est de fabriquer le noeud afin d'aller
	 * recuperer ces geometries dans la table d'index comme des interateur...
	 * l'index existe deja par contre la table peut ne pas exister... afin de
	 * les afficher
	 * 
	 * @param tableName designe la table à laquelle on s'interesse
	 * 
	 * @param signature elle peut etre : geometrie ou empreinte
	 * 
	 * @return retourne une population geoxygene de DefaultFeature
	 */

	Population<DefaultFeature> obtainTableFromIndex(String tableName, String signature, ScaleInfo scale,
			SpatialIndex index) {

		Population<DefaultFeature> pop = new Population<DefaultFeature>();
		index.open();
		// on demande un interateur des indexes... de cette echelle
		// contenu dans la base de stockage

		Iterator<Record<Geometry>> it = index.doIterator();
		String geotype = null;
		int i = 0;
		if (it == null) {
			Controller.LOGGER.error("Aucune donnée n'a été extraite de l'index");
		} else {

			while (it.hasNext()) {

				// System.out.println(geotype);
				Record<Geometry> content = it.next();
				Node node = content.getKey();

				index.add(content);
				Geometry geo = content.getValue();
				if (i == 0) {
					geotype = GeometryUtil.getType(geo);
					i++;
				}

				// voir comment charge les attributs

				// on verifie que le noeud contient bien les
				// information associé à l'entrée.
				if (node.toString().contains(tableName) && node.toString().contains(signature)
						&& node.toString().contains(scale.toString())) {
					Geometry geom = content.getValue();
					// on fabrique une geometrie geoxygene
					IGeometry igeo = getFromtext(geom.toText(), node.getSrid());
					// on surchage le node pour un DefaultFeauture
					DefaultFeature defaultIgeo = new DefaultFeature(igeo);
					// defaultIgeo.setAttribute(node.toString(), igeo);
					pop.add(defaultIgeo);
				}
			}

			return setPopulationType(pop, geotype, tableName);

		}

		// dans le cas que l'index existe mais la
		// la table n'est pas inddiqué on recupère de la
		// la base de données

		if (pop.size() == 0 && null != pop) {
			// l'echelle existe mais la table en question n'existe pas
			return generateTablePopFromPostgis(tableName, signature, scale, index);

		}
		//
		// RTreeIndex index = manager.useAnLevel(scale.getMinScaleValue());
		// loadValues(index, it);
		//

		return pop;
	}

	// pour recuperer les valeurs de geometries pour une echelle donnée
	/*
	 * @value designe une valeur quelque dont on voudrait afficher ces
	 * informations sur geoxygene
	 */

	Population<DefaultFeature> obtainGeoFromScale(int value) {

		RTreeIndex index = manager.useAnLevel(value);
		index.open();
		Population<DefaultFeature> pop = new Population<DefaultFeature>();
		int i = 0;
		String geotype = null;
		;
		Iterator<Record<Geometry>> it = index.iterator();
		while (it.hasNext()) {

			Record<Geometry> content = it.next();
			Geometry geo = content.getValue();

			if (i == 0) {
				geotype = GeometryUtil.getType(geo);
				i++;
			}
			Node node = content.getKey();
			IGeometry igeo = getFromtext(geo.toText(), node.getSrid());
			DefaultFeature defaultIgeo = new DefaultFeature(igeo);
			// voir comment charge les attributs
			pop.add(defaultIgeo);

		}
		//
		// RTreeIndex index = manager.useAnLevel(scale.getMinScaleValue());
		// loadValues(index, it);
		//

		return setPopulationType(pop, geotype, "scale" + String.valueOf(value));
	}

	/*
	 * Cette methode permet de verifier si un index est deja creer afin de
	 * choisir dans quelle direction ou aller rechercher l'information si
	 * l'information n'est pas encore dans l'index on le cree
	 */
	public Population<DefaultFeature> obtainTable(String tableName, String signature, ScaleInfo scale) {

		if (manager.existAnLevel(scale.getMinScaleValue())) {
			RTreeIndex index = manager.useAnLevel(scale.getMinScaleValue());
			return obtainTableFromIndex(tableName, signature, scale, index);

		} else {
			manager.createOneLevel(scale.getMinScaleValue(), scale.getMaxScaleValue());
			RTreeIndex index = manager.useAnLevel(scale.getMinScaleValue());
			//
			return generateTablePopFromPostgis(tableName, signature, scale, index);

		}

	}

	/*
	 * @param index designe l'index spatial
	 * 
	 * @param it designe l'iterateur de géometries construits depuis une base de
	 * données
	 */
	public void loadValues(SpatialIndex index, InteratorIndexForTable it) {
		if (it != null) {
			index.open();
			while (it.hasNext()) {
				index.add(it.next());
			}
			index.close();
		}
	}

	public Population<DefaultFeature> loadValuesAndConvert(SpatialIndex index, InteratorIndexForTable it) {
		Population<DefaultFeature> pop = new Population<DefaultFeature>();
		int i = 0;
		String geotype = null;
		String tableName = "Any";
		if (it != null) {
			index.open();
			while (it.hasNext()) {
				Record<Geometry> content = it.next();
				index.add(content);
				Geometry geo = content.getValue();
				if (i == 0) {
					geotype = GeometryUtil.getType(geo);
					i++;
				}

				Node node = content.getKey();
				tableName = node.getTableName();
				IGeometry igeo = getFromtext(geo.toText(), node.getSrid());
				DefaultFeature defaultIgeo = new DefaultFeature(igeo);
				// voir comment charge les attributs
				pop.add(defaultIgeo);

			}
			index.close();
		}

		return setPopulationType(pop, geotype, tableName);
	}

	/*
	 * @param strGeom designe un string de geometrie
	 * 
	 * @param sridT designe la valeur en string du srid utilisé
	 * 
	 * @return retour la valeur une IIGeometry
	 */

	public IGeometry getFromtext(String strGeom, String sridT) {

		/*
		 * In version 1.0.x of PostGIS, SRID is added to the beginning of the
		 * pgGeom string
		 */

		if (strGeom == null) {
			throw new RuntimeException(strGeom + " est une valeur null");

		} else if (sridT == null) {

			throw new RuntimeException(sridT + " est une valeur null");
		}

		String geom = strGeom;

		int srid = Integer.parseInt(sridT);
		IGeometry geOxyGeom = null;
		try {
			geOxyGeom = WktGeOxygene.makeGeOxygene(strGeom);
		} catch (fr.ign.cogit.geoxygene.util.conversion.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (geOxyGeom instanceof IMultiPoint) {
			IMultiPoint aggr = (IMultiPoint) geOxyGeom;
			if (aggr.size() == 1) {
				aggr.get(0);
				aggr.setCRS(srid);
				return aggr;
			}
		}

		if (geOxyGeom instanceof IMultiCurve) {
			IMultiCurve<IOrientableCurve> aggr = (IMultiCurve<IOrientableCurve>) geOxyGeom;
			if (aggr.size() == 1) {
				aggr.get(0);
				aggr.setCRS(srid);
				return aggr;
			}
		}

		if (geOxyGeom instanceof IMultiSurface) {
			IMultiSurface<IOrientableSurface> aggr = (IMultiSurface<IOrientableSurface>) geOxyGeom;
			if (aggr.size() == 1) {
				aggr.get(0);
				aggr.setCRS(srid);
				return aggr;
			}
		}
		geOxyGeom.setCRS(srid);
		return geOxyGeom;

	}

	/*
	 * @param geom designe un string de geometrie de jts
	 * 
	 * @return retour la valeur une IGeometry compactible avec geoxygene
	 */

	public IGeometry getFromtext(Geometry geo) {

		/*
		 * In version 1.0.x of PostGIS, SRID is added to the beginning of the
		 * pgGeom string
		 */

		if (geo == null) {
			throw new RuntimeException(geo + " est une valeur null");

		}

		String geom = geo.toText();
		int srid = geo.getSRID();
		IGeometry geOxyGeom = null;
		try {
			geOxyGeom = WktGeOxygene.makeGeOxygene(geom);
		} catch (fr.ign.cogit.geoxygene.util.conversion.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (geOxyGeom instanceof IMultiPoint) {
			IMultiPoint aggr = (IMultiPoint) geOxyGeom;
			if (aggr.size() == 1) {
				aggr.get(0);
				aggr.setCRS(srid);
				return aggr;
			}
		}

		if (geOxyGeom instanceof IMultiCurve) {
			IMultiCurve<IOrientableCurve> aggr = (IMultiCurve<IOrientableCurve>) geOxyGeom;
			if (aggr.size() == 1) {
				aggr.get(0);
				aggr.setCRS(srid);
				return aggr;
			}
		}

		if (geOxyGeom instanceof IMultiSurface) {
			IMultiSurface<IOrientableSurface> aggr = (IMultiSurface<IOrientableSurface>) geOxyGeom;
			if (aggr.size() == 1) {
				aggr.get(0);
				aggr.setCRS(srid);
				return aggr;
			}
		}
		geOxyGeom.setCRS(srid);
		return geOxyGeom;

	}

	protected Population<DefaultFeature> doAddPopulation(InteratorIndexForTable records) {

		Population<DefaultFeature> pop = new Population<DefaultFeature>();
		int i = 0;

		while (records.hasNext()) {
			Node node = records.next().getKey();
			// on verifie que le noeud contient bien les
			// information associé à l'entrée.
			Geometry geom = records.next().getValue();
			// on fabrique une geometrie geoxygene
			IGeometry igeo = getFromtext(geom.toText(), node.getSrid());
			// on surchage le node pour un DefaultFeauture
			DefaultFeature defaultIgeo = new DefaultFeature();
			defaultIgeo.setAttribute(node.toString(), igeo);
			pop.add(defaultIgeo);

		}

		return pop;
	}

	/*
	 * @param pop designe une population
	 * 
	 * @param geotype designe le type des geometries utilisées retourne une
	 * population adaptée à géoxygene Cette methode doit etre etendue chaque
	 * fois qu'on a une nouvelle structure dans geoxygene
	 */

	public Population<DefaultFeature> setPopulationType(Population<DefaultFeature> pop, String geotype,
			String tableName) {
		Population<DefaultFeature> newpop = new Population<DefaultFeature>("TilingPlugin " + " " + tableName);
		FeatureType newFeatureType = new FeatureType();

		if (geotype.contains("POINT")) {

			newFeatureType.setGeometryType(IPoint.class);

		} else

		if (geotype.contains("LINESTRING")) {

			newFeatureType.setGeometryType(ILineString.class);

		} else

		if (geotype.contains("POLYGON")) {

			newFeatureType.setGeometryType(IPolygon.class);
			// System.out.println("oui c'est cela");

		} else {

			throw new RuntimeException(
					"la geometrie est inconnue mettre à jour" + "le segmentateur la classe GeometryConverter");
		}

		newpop.setClasse(DefaultFeature.class);
		newpop.setPersistant(false);
		newFeatureType.setGeometryType(IPolygon.class);
		newpop.setFeatureType(newFeatureType);
		newpop.addAll(pop);

		return newpop;

	}

}
