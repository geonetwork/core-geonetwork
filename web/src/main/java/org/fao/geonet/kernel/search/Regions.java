package org.fao.geonet.kernel.search;

import jeeves.resources.dbms.Dbms;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

import java.sql.SQLException;

/**
 * Caches the regions data to avoid retrieve each time from db
 *
 * @author josegar
 */
class RegionsData {
    private static Element regions = null;

    public static Element getRegions(Dbms dbms) throws SQLException {
        if (regions == null) {
            regions = Lib.db.select(dbms, "Regions", "region");
        }

        return (Element) regions.clone();
    }
}

