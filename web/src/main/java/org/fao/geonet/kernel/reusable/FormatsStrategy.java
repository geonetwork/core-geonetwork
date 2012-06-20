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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;
import jeeves.utils.Xml;
import jeeves.xlink.XLink;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.util.ElementFinder;
import org.fao.geonet.util.XslUtil;
import org.jdom.Element;

public final class FormatsStrategy extends ReplacementStrategy
{
    private static final String TABLE = "Formats";
    private static final String ID_COL = "id";
    private static final String NAME_COL = "name";
    private static final String VERSION_COL = "version";
    private static final String VALIDATED_COL = "validated";


    private final Dbms   _dbms;
    private final String _styleSheet;
    private final String _currentLocale;
    private final SerialFactory _serialFactory;


    public FormatsStrategy(Dbms dbms, String appPath, String baseURL, String currentLocale, SerialFactory serialFactory)
    {
        this._serialFactory = serialFactory;
        
        this._dbms = dbms;
        this._styleSheet = appPath + Utils.XSL_REUSABLE_OBJECT_DATA_XSL;
        this._currentLocale = currentLocale;
    }

    public Pair<Collection<Element>, Boolean> find(Element placeholder, Element originalElem, String defaultMetadataLang)
            throws Exception
    {

        if (XLink.isXLink(originalElem))
            return NULL;

        Element name = name(originalElem);
        if (name != null && name.getChild("CharacterString", XslUtil.GCO_NAMESPACE) != null) {
            String query = "SELECT "+ID_COL+","+VALIDATED_COL+" FROM "+TABLE+" WHERE TRIM("+NAME_COL+") ILIKE TRIM(?)";
            String sname = name.getChildTextTrim("CharacterString", XslUtil.GCO_NAMESPACE);
            Element version = version(originalElem);
            Element el;
            if (version != null && version.getChild("CharacterString", XslUtil.GCO_NAMESPACE) != null) {
                query += " AND TRIM("+VERSION_COL+") ILIKE TRIM(?) ORDER By validated DESC";
                String sversion = version.getChildTextTrim("CharacterString", XslUtil.GCO_NAMESPACE);
                el = _dbms.select(query, sname, sversion);
            } else {
                el = _dbms.select(query+" ORDER By validated DESC", sname);
            }

            if (el.getChildren("record").size() > 0) {
                Element record = el.getChild("record");
                originalElem.removeContent();
                String id = record.getChildTextTrim(ID_COL);
                String validatedString = record.getChildTextTrim(VALIDATED_COL);
                boolean validated = !("n".equalsIgnoreCase(validatedString));
                xlinkIt(originalElem, id, validated);
                Collection<Element> results = Collections.singleton(originalElem);
                return Pair.read(results, true);
            }
            Log.debug("Reusable Objects", Xml.getString(el));
        }

        return NULL;
    }

    private Element version(Element originalElem)
    {
        List<Element> version = Utils.convertToList(originalElem.getDescendants(new ElementFinder("version",
                XslUtil.GMD_NAMESPACE, "MD_Format")), Element.class);

        if(version.isEmpty()) return null;
        return version.get(0);
    }

    private Element name(Element originalElem)
    {
        List<Element> name = Utils.convertToList(originalElem.getDescendants(new ElementFinder("name",
                XslUtil.GMD_NAMESPACE, "MD_Format")), Element.class);

        if(name.isEmpty()) {
            return null;
        } else {
            return name.get(0);
        }
    }

    public Element findNonValidated(UserSession session) throws Exception
    {
        List<Element> results = _dbms.select("SELECT "+ID_COL+","+NAME_COL+","+VERSION_COL+" FROM "+TABLE+" WHERE "+VALIDATED_COL+"='n'").getChildren("record");
        Element formats = new Element(REPORT_ROOT);

        for (Element result : results) {
            Element e = new Element(REPORT_ELEMENT);
            String id = result.getChildTextTrim(ID_COL);
            String url = XLink.LOCAL_PROTOCOL+"format.admin?id=" + id + "&dialog=true";
            String name = result.getChildTextTrim(NAME_COL);
            if (name == null || name.length() == 0) {
                name = id;
            }
            String version = result.getChildTextTrim(VERSION_COL);
            if (version == null) {
                version = "";
            }
            addChild(e, REPORT_URL, url);
            addChild(e, REPORT_DESC, name+" ("+version+")");
            addChild(e, REPORT_ID, id);

            formats.addContent(e);
        }

        return formats;
    }

    public String createXlinkHref(String id, UserSession session, String strategySpecificData)
    {
        return XLink.LOCAL_PROTOCOL+"xml.format.get?id=" + id;
    }

    public void performDelete(String[] ids, Dbms dbms, UserSession session, String ignored) throws Exception
    {
        String whereClause = Utils.constructWhereClause(ID_COL, ids);
        dbms.execute("DELETE FROM "+TABLE+" WHERE " + whereClause);
    }

    public String updateHrefId(String oldHref, String id, UserSession session)
    {
        return createXlinkHref(id, session, null).replace("/___/","/eng/");
    }

    public Map<String, String> markAsValidated(String[] ids, Dbms dbms, UserSession session) throws Exception
    {
        String whereClause = Utils.constructWhereClause(ID_COL, ids);
        dbms.execute("UPDATE "+TABLE+" SET "+VALIDATED_COL+"='y' WHERE " + whereClause);

        Map<String, String> idMap = new HashMap<String, String>();

        for (String id : ids) {
            idMap.put(id, id);
        }
        return idMap;
    }

    private void xlinkIt(Element originalElem, String id, boolean validated)
    {
        originalElem.setAttribute(XLink.HREF, XLink.LOCAL_PROTOCOL+"xml.format.get?id=" + id,
                XLink.NAMESPACE_XLINK);

        if (!validated) {
            originalElem
                    .setAttribute(XLink.ROLE, ReusableObjManager.NON_VALID_ROLE, XLink.NAMESPACE_XLINK);
        }
        originalElem.setAttribute(XLink.SHOW, XLink.SHOW_EMBED, XLink.NAMESPACE_XLINK);

        originalElem.detach();
    }

    public Collection<Element> add(Element placeholder, Element originalElem, Dbms dbms, String metadataLang)
            throws Exception
    {
        List<Element> xml = Xml.transform(originalElem, _styleSheet).getChildren("format");
        if (!xml.isEmpty()) {
            List<Element> results = new ArrayList<Element>();
            for (Element element : xml) {
                String name = element.getAttributeValue("name");
                String version = element.getAttributeValue("version");
                int id = insertNewFormat(dbms, name, version);
                Element newElem = (Element) originalElem.clone();
                xlinkIt(newElem, String.valueOf(id), false);
                results.add(newElem);
            }
            return results;
        }
        return Collections.emptySet();
    }

    private int insertNewFormat(Dbms dbms, String name, String version) throws SQLException {
        if (version == null) {
            version = "";
        }
        int id = _serialFactory.getSerial(dbms, TABLE);

        String query = "INSERT INTO "+TABLE+"("+ID_COL+", "+NAME_COL+", "+VERSION_COL+", "+VALIDATED_COL+") VALUES (?, TRIM(?), TRIM(?), 'n')";
        dbms.execute(query, id, name, version);
        return id;
    }

    public Collection<Element> updateObject(Element xlink, Dbms dbms, String metadataLang) throws Exception
    {
        @SuppressWarnings("unchecked")
		List<Element> xml = Xml.transform((Element) xlink.clone(), _styleSheet).getChildren("format");

        if(!xml.isEmpty()) {
	        Element element = xml.get(0);
	        String id = Utils.extractUrlParam(xlink, "id");
	
	        String name = element.getAttributeValue("name");
	        String version = element.getAttributeValue("version");
	        if (version == null) {
	            version = "";
	        }
	
	        String query = "UPDATE "+TABLE+" SET "+NAME_COL+"=TRIM(?), "+VERSION_COL+"=TRIM(?) WHERE "+ID_COL+"=" + id;
	        dbms.execute(query, name, version);
        }
        return Collections.emptyList();

    }

    public boolean isValidated(Dbms dbms, String href) throws Exception
    {
        String id = Utils.id(href);
        if(id==null) return false;
        try {
            int group = Integer.parseInt(id);
            Element record = dbms.select("SELECT "+VALIDATED_COL+" FROM "+TABLE+" WHERE "+ID_COL+"=?", group).getChild("record");
    
            return record == null || !"n".equalsIgnoreCase(record.getChildTextTrim(VALIDATED_COL));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return "Reusable Format";
    }


    public static final class Format {
        final int id;
        final String name, version;
        final boolean validated;

        public Format(String id, String name, String version, String validated) {
            this.id = Integer.parseInt(id);
            this.name = name;
            this.version = version;
            this.validated = "y".equals(validated);
        }

        public boolean match(Format other) {
            boolean sameName = name.equalsIgnoreCase(other.name);
            boolean sameVersion = version.equalsIgnoreCase(other.version);
            return sameName && sameVersion;
        }

        public String sid() {
            return ""+id;
        }
    }

    public static final class Formats implements Iterable<Format> {
        List<Format> formats = new ArrayList<Format>();

        public Formats(Dbms dbms) throws SQLException {
            Element results = dbms.select("SELECT * FROM "+TABLE);

            List<Element> records = results.getChildren("record");

            for (Element record : records) {
                String id = record.getChildTextNormalize(ID_COL);
                String name = record.getChildTextNormalize(NAME_COL);
                String version = record.getChildTextNormalize(VERSION_COL);
                String validated = record.getChildTextNormalize(VALIDATED_COL);
                final Format format = new Format(id, name, version, validated);
                formats.add(format);
            }
        }

        public Iterator<Format> iterator() {
            return formats.iterator();
        }

        public List<Format> matches(Format format) {
            List<Format> matches = new ArrayList<Format>();
            for (Format other : this) {
                if(other.match(format)) {
                    if(other.validated) {
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
        return new String[]{"invalid_xlink_format"};
    }

    @Override
    public String createAsNeeded(String href, UserSession session) throws Exception {

        String startId = Utils.id(href);
        if(startId!=null) return href;
         
        Dbms dbms = (Dbms) ServiceContext.get().getResourceManager().open(Geonet.Res.MAIN_DB);
        int id = insertNewFormat(dbms, "", "");
        return XLink.LOCAL_PROTOCOL+"xml.format.get?id="+id;
    }
}
