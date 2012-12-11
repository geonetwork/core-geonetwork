package org.fao.geonet.services;

import java.sql.SQLException;
import java.util.List;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

public class UpdateAllLocalizationTables implements Service {

	@Override
	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	@Override
	public Element exec(Element params, ServiceContext context)
			throws Exception {

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		
		@SuppressWarnings("unchecked")
		List<Element> records = dbms.select("SELECT * FROM Languages").getChildren(Jeeves.Elem.RECORD);
		for (Element object : records) {
			updateLocs(dbms, object.getChildText("id"));
		}
		return new Element("ok");
	}

	private void updateLocs(Dbms dbms, String lang) throws SQLException {
		dbms.execute("insert into groupsdes select distinct on (iddes) iddes, '"+ lang+ "' as langid,label from groupsdes where not iddes in (select iddes from groupsdes where langid='"+ lang + "' group by iddes)");
		dbms.execute("insert into categoriesdes select distinct on (iddes) iddes, '"+lang+"' as langid,label from categoriesdes where not iddes in (select iddes from categoriesdes where langid='"+lang+"' group by iddes)");
		dbms.execute("insert into operationsdes select distinct on (iddes) iddes, '"+lang+"' as langid,label from operationsdes where not iddes in (select iddes from operationsdes where langid='"+lang+"' group by iddes)");
		dbms.execute("insert into regionsdes select distinct on (iddes) iddes, '"+lang+"' as langid,label from regionsdes where not iddes in (select iddes from regionsdes where langid='"+lang+"' group by iddes)");
		dbms.execute("insert into statusvaluesdes select distinct on (iddes) iddes, '"+lang+"' as langid,label from statusvaluesdes where not iddes in (select iddes from statusvaluesdes where langid='"+lang+"' group by iddes)");
	}

}
