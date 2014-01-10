//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.metadata.schema;

import jeeves.exceptions.MissingParameterEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import jeeves.utils.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.SchematronCriteriaType;
import org.jdom.Element;

import java.util.List;
import java.util.Map;

/**
 * This class manages the configuration of the validation framework with
 * parameters "add" and "delete"
 * 
 * @author delawen
 */
public class Validation implements Service {

	public static final String TABLE_SCHEMATRON = "schematron";
	public static final String TABLE_SCHEMATRON_CRITERIA = "schematroncriteria";

	public static final String COL_CRITERIA_ID = "id";
	public static final String COL_CRITERIA_SCHEMATRON_ID = "schematron";
	public static final String COL_CRITERIA_TYPE = "type";
	public static final String COL_CRITERIA_VALUE = "value";

	public static final String COL_SCHEMATRON_ID = "id";
	public static final String COL_SCHEMATRON_FILE = "file";
	public static final String COL_SCHEMATRON_ISO_SCHEMA = "isoschema";
	public static final String COL_SCHEMATRON_REQUIRED = "required";

	public Element exec(Element params, ServiceContext context)
			throws Exception {

		Element res = new Element("schematron");

		String action = null;
		try {
			action = Util.getParam(params, "action");
			context.info("Action: " + action);
		} catch (MissingParameterEx ex) {
		}

		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);

		if ("delete".equalsIgnoreCase(action)) {
			final Integer id = Integer.valueOf(Util.getParam(params, "id"));
			dbms.execute("delete from " + TABLE_SCHEMATRON + " where "
					+ COL_SCHEMATRON_ID + " = ?", id);
		} else if ("add".equalsIgnoreCase(action)) {
			String schema = Util.getParam(params, TABLE_SCHEMATRON);

			final Integer schematronId = Integer.valueOf(schema);
			final Integer id = context.getSerialFactory().getSerial(dbms,
					TABLE_SCHEMATRON, "id");
			final SchematronCriteriaType type = SchematronCriteriaType
					.valueOf(Util.getParam(params, "type"));
			final String value = Util.getParam(params, "value");

			dbms.execute("insert into " + TABLE_SCHEMATRON_CRITERIA + " ("
					+ COL_CRITERIA_ID + "," + COL_CRITERIA_SCHEMATRON_ID + ","
					+ COL_CRITERIA_TYPE + "," + COL_CRITERIA_VALUE
					+ ") values (?,?,?,?);", id, schematronId, type.ordinal(),
					value);

		}

		final List<Element> schematrons = dbms.select(
				"select * from " + TABLE_SCHEMATRON).getChildren();

		for (Element schematron : schematrons) {
			schematron.setName("schematron");

			Integer id = Integer.parseInt(schematron
					.getChildText(COL_SCHEMATRON_ID));
			List<Element> schematronCriteria = dbms.select(
					"select * from " + TABLE_SCHEMATRON_CRITERIA + " where "
							+ COL_CRITERIA_SCHEMATRON_ID + "=?", id)
					.getChildren();

			for (Element element : schematronCriteria) {
				element.removeChild(COL_CRITERIA_SCHEMATRON_ID);
				element.addContent((Element) schematron.clone());
			}

		}

		for (Element schematron : schematrons) {
			res.addContent((Element) schematron.clone());
		}

		return res;
	}

	public void init(String appPath, ServiceConfig params) throws Exception {
	}

}
