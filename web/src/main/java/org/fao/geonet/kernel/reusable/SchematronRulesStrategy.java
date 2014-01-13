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

package org.fao.geonet.kernel.reusable;

import static org.fao.geonet.kernel.reusable.Utils.addChild;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;

import org.apache.commons.lang.NotImplementedException;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.jdom.Element;

public final class SchematronRulesStrategy extends ReplacementStrategy {
	private static final String SCHEMA_TABLE = "schematron";
	private static final String TABLE = "schematroncriteria";
	private static final String ID_COL = "id";
	private static final String XPATH_COL = "type";
	private static final String VALUE_COL = "value";
	private static final String SCHEMATRON_COL = "schematron";
	private static final String FILE_COL = "file";
	private static final String ISOSCHEMA_COL = "isoschema";

	private final Dbms _dbms;

	public SchematronRulesStrategy(Dbms dbms, String currentLocale) {
		this._dbms = dbms;
	}

	@Override
	public Pair<Collection<Element>, Boolean> find(Element placeholder,
			Element originalElem, String defaultMetadataLang) throws Exception {

		throw new NotImplementedException("Not really needed... right?");
	}

	private String getSchematron(Integer id) throws SQLException {

		Element results = _dbms.select("SELECT * FROM " + SCHEMA_TABLE
				+ " WHERE " + ID_COL + " = ? LIMIT 1", id);

		@SuppressWarnings("unchecked")
		List<Element> records = results.getChildren("record");
		for (Element record : records) {
			String file = record.getChildTextNormalize(FILE_COL);
			String isoschema = record.getChildTextNormalize(ISOSCHEMA_COL);
			return "[" + isoschema + "] " + file;
		}

		return null;
	}

	private Boolean getRequired(Integer id) throws SQLException {
		Element results = _dbms.select("SELECT * FROM " + SCHEMA_TABLE
				+ " WHERE " + ID_COL + " = ? LIMIT 1", id);

		@SuppressWarnings("unchecked")
		List<Element> records = results.getChildren("record");
		for (Element record : records) {
			String r = record.getChildTextNormalize("required");
			return "true".equalsIgnoreCase(r) || "t".equalsIgnoreCase(r);
		}

		return false;
	}

	@Override
	public Element find(UserSession session, boolean validated)
			throws Exception {
		Element results = _dbms.select("SELECT * FROM " + TABLE);
		Element formats = new Element(REPORT_ROOT);

		@SuppressWarnings("unchecked")
		List<Element> records = results.getChildren(REPORT_ELEMENT);

		for (Element result : records) {

			Element e = new Element(REPORT_ELEMENT);

			addChild(e, REPORT_ID, result.getChildTextTrim(ID_COL));
			addChild(e, REPORT_TYPE, result.getChildTextTrim(XPATH_COL));
			addChild(e, REPORT_DESC, getSchematron(Integer.valueOf(result
					.getChildTextTrim(SCHEMATRON_COL))));
			addChild(
					e,
					"required",
					getRequired(
							Integer.valueOf(result
									.getChildTextTrim(SCHEMATRON_COL)))
							.toString());
			addChild(e, "value", result.getChildTextTrim(VALUE_COL));
			formats.addContent(e);
		}

		return formats;
	}

	@Override
	public boolean isValidated(Dbms dbms, String href) throws Exception {
		return true;
	}

	@Override
	public String toString() {
		return "Schematron Rules Reusable Format";
	}

	public static final class Format {
		final int id;
		final String xpath;
		final String value;
		final boolean required;
		final String schematron;
		final boolean validated;

		public Format(Integer id, String xpath, String value,
				String schematron, Boolean required, Boolean validated) {
			this.id = id;
			this.xpath = xpath;
			this.value = value;
			this.schematron = schematron;
			this.required = "y".equals(required);
			this.validated = "y".equals(validated);
		}

		public boolean match(Format other) {
			boolean sameXpath = xpath.equalsIgnoreCase(other.xpath);
			boolean sameValue = value.equalsIgnoreCase(other.value);
			return sameXpath && sameValue;
		}

		public String sid() {
			return "" + id;
		}
	}

	public static final class Formats implements Iterable<Format> {
		List<Format> formats = new ArrayList<Format>();

		public Formats(Dbms dbms) throws SQLException {
			Element results = dbms.select("SELECT * FROM " + TABLE);

			@SuppressWarnings("unchecked")
			List<Element> records = results.getChildren("record");

			for (Element record : records) {
				Integer id = Integer.valueOf(record
						.getChildTextNormalize(ID_COL));
				String xpath = record.getChildTextNormalize(XPATH_COL);
				String value = record.getChildTextNormalize(VALUE_COL);
				String schematron = getSchematron(
						record.getChildTextNormalize(SCHEMATRON_COL), dbms);
				Boolean required = getRequired(
						record.getChildTextNormalize(SCHEMATRON_COL), dbms);
				Boolean validated = true;
				final Format format = new Format(id, xpath, value, schematron,
						required, validated);
				formats.add(format);
			}
		}

		private String getSchematron(String id, Dbms dbms) throws SQLException {
			Element results = dbms.select("SELECT * FROM " + SCHEMA_TABLE
					+ " WHERE " + ID_COL + " = ? LIMIT 1", id);

			@SuppressWarnings("unchecked")
			List<Element> records = results.getChildren("record");
			for (Element record : records) {
				String file = record.getChildTextNormalize(FILE_COL);
				String isoschema = record.getChildTextNormalize(ISOSCHEMA_COL);
				return "[" + isoschema + "] " + file;
			}

			return null;
		}

		private Boolean getRequired(String id, Dbms dbms) throws SQLException {
			Element results = dbms.select("SELECT * FROM " + SCHEMA_TABLE
					+ " WHERE " + ID_COL + " = ? LIMIT 1", id);

			@SuppressWarnings("unchecked")
			List<Element> records = results.getChildren("record");
			for (Element record : records) {
				String r = record.getChildTextNormalize("required");
				return "true".equalsIgnoreCase(r);
			}

			return false;
		}

		public Iterator<Format> iterator() {
			return formats.iterator();
		}

		public List<Format> matches(Format format) {
			List<Format> matches = new ArrayList<Format>();
			for (Format other : this) {
				if (other.match(format)) {
					if (other.validated) {
						matches.add(0, other);
					} else {
						matches.add(other);
					}
				}
			}
			return matches;
		}

		public int size() {
			return formats.size();
		}
	}

	@Override
	public String[] getInvalidXlinkLuceneField() {
		return new String[] { "invalid_xlink_format" };
	}

	@Override
	public String[] getValidXlinkLuceneField() {
		return new String[] { "valid_xlink_format" };
	}

	@Override
	public String createAsNeeded(String href, UserSession session)
			throws Exception {
		throw new NotImplementedException("should not create from here");
	}

	@Override
	public String createXlinkHref(String id, UserSession session,
			String notRequired) {
		throw new NotImplementedException("Shouldn't insert here");
	}

	@Override
	public String updateHrefId(String oldHref, String id, UserSession session) {
		throw new NotImplementedException("Shouldn't update here");
	}

	@Override
	public void performDelete(String[] ids, Dbms dbms, UserSession session,
			String ignored) throws Exception {
		throw new NotImplementedException("Shouldn't delete here");
	}

	@Override
	public Map<String, String> markAsValidated(String[] ids, Dbms dbms,
			UserSession session) throws Exception {
		throw new NotImplementedException("No validation available");
	}

	@Override
	public Collection<Element> add(Element placeholder, Element originalElem,
			Dbms dbms, String metadataLang) throws Exception {
		throw new NotImplementedException("Shouldn't insert here");
	}

	@Override
	public Collection<Element> updateObject(Element xlink, Dbms dbms,
			String metadataLang) throws Exception {
		throw new NotImplementedException("Shouldn't update here");

	}

}
