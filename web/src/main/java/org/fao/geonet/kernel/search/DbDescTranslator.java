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

package org.fao.geonet.kernel.search;

import java.io.IOException;
import java.sql.SQLException;

import jeeves.resources.dbms.Dbms;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * Translates keys into a language from a db table description table.
 *
 * @author jesse, francois
 */
public class DbDescTranslator extends Translator {

	private final Dbms _dbms;
	private final String _langCode;

	/**
	 * Table with ids and keys
	 */
	private final String _tableName;

	/**
	 * Table with description
	 */
	private final String _descTableName;

	public DbDescTranslator(Dbms dbms, String langCode, String tableName)
			throws IOException, JDOMException {
		_tableName = tableName;
		_descTableName = _tableName + "Des";
		_dbms = dbms;
		_langCode = langCode;
	}

	public String translate(String key) {
		if (_tableName == null || _descTableName == null) {
			return key;
		}

		try {
			// --- Get id
			String query = "SELECT id FROM " + _tableName + " WHERE LOWER(name) = ?";
			Element rec = _dbms.select(query, key.toLowerCase()).getChild("record");
			if(rec == null) {
			    return key;
			}
			String id = rec.getChildText("id");

			if (id == null)
				return key;

			// --- Get value in language
			query = "SELECT label FROM " + _descTableName
					+ " WHERE idDes = ? AND langId = ?";
			rec = _dbms.select(query, Integer.parseInt(id), _langCode).getChild("record");
			if( rec==null ){
			    return key;
			}
			String label = rec.getChildText("label");

			if (label == null)
				return key;
			else
				return label;

		} catch (SQLException e) {
			// TODO : Add debug
			return key;
		}
	}

}
