package com.boostasoft.goaway.tilingDB.util;


import java.util.Map;

import com.boostasoft.goaway.tilingDB.webentity.spatial.SpatialIndexException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
/*
 * @author Dr Tsatcha
 * Cette classe permet de recuperer information
 * relative à une geometrie.
 */


public class GeometryUtil {

    /*
     * Cette methode permet de retourner le nom de la geometrie
     */
    public static String getType(Geometry geo) throws SpatialIndexException {
        String geoname = null;
        if (geo == null) {

            throw new RuntimeException("La geometrie n'est pas renseigné");
        } else {

            String[] twopart = geo.toText().split("\\(");
            int begin = twopart[0].length();
            String use = geo.toText().substring(begin, geo.toText().length());
            // on récupère la prémière composante de la segmentation
            geoname = twopart[0];
            // correction des multi-points de geoxygene en realité sont des
            // polygones
            // sans trous...

            if (geoname.contains("MULTIPOLYGON")) {
                // permet de corriger les faux polygones
                // de la base de données.
                return "POLYGON";

            }

        }
        return geoname;
    }

}
