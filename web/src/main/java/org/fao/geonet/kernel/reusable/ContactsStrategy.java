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

import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.PasswordUtil;
import jeeves.utils.SerialFactory;
import jeeves.utils.Xml;
import jeeves.xlink.Processor;
import jeeves.xlink.XLink;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.util.ElementFinder;
import org.fao.geonet.util.LangUtils;
import org.fao.geonet.util.XslUtil;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fao.geonet.constants.Geocat.Profile.SHARED;
import static org.fao.geonet.kernel.reusable.Utils.addChild;
import static org.fao.geonet.util.LangUtils.FieldType.STRING;
import static org.fao.geonet.util.LangUtils.FieldType.URL;

public final class ContactsStrategy extends ReplacementStrategy
{

    private final Dbms   _dbms;
    private final String _styleSheet;
    //private final String _baseURL;
    private final String _appPath;
    private SerialFactory _serialFactory;

    public ContactsStrategy(Dbms dbms, String appPath, String baseURL, String currentLocale, SerialFactory serialFactory)
    {
        this._serialFactory = serialFactory;
//        this._baseURL = baseURL;
        this._dbms = dbms;
        _styleSheet = appPath + Utils.XSL_REUSABLE_OBJECT_DATA_XSL;
        this._appPath = appPath;
    }

    public Pair<Collection<Element>, Boolean> find(Element placeholder, Element originalElem, String defaultMetadataLang)
            throws Exception
    {
        if (XLink.isXLink(originalElem))
            return NULL;

        String email = lookupElement(originalElem, "electronicMailAddress", defaultMetadataLang);
        String firstname = lookupElement(originalElem, "individualFirstName", defaultMetadataLang);
        String lastname = lookupElement(originalElem, "individualLastName", defaultMetadataLang);

        String key = email + firstname + lastname;

        Element roleElem = Utils.nextElement(originalElem.getDescendants(new ElementFinder("CI_RoleCode",
                XslUtil.GMD_NAMESPACE, "role")));
        String query = "select id,validated,organisation from Users where COALESCE(TRIM(email),'')||COALESCE(TRIM(name),'')||COALESCE(TRIM(surname),'') ILIKE ? AND profile=? order by validated DESC";
        List<Element> records = _dbms.select(query, key, SHARED).getChildren();

        for (Element record : records) {
            String role;
            if (roleElem == null) {
                role = "";
                Log.warning(Geocat.Module.REUSABLE,
                        "A contact does not have a role associated with it: " + Xml.getString(originalElem));
            } else {
                role = roleElem.getAttributeValue("codeListValue");
            }
            Element recordOrg = LangUtils.loadInternalMultiLingualElem(record.getChildTextTrim("organisation"));

            String elemOrg = lookupElement(originalElem, "organisationName", defaultMetadataLang);

            if (translation(recordOrg, defaultMetadataLang).equalsIgnoreCase(elemOrg)) {
                String id = record.getChildTextTrim("id");
                String validatedString = record.getChildTextTrim("validated");
                boolean validated = !("n".equalsIgnoreCase(validatedString));
                Collection<Element> xlinkIt = xlinkIt(originalElem, role, id, validated);
                Log.debug("Reusable Objects", Xml.getString(record));
                return Pair.read(xlinkIt, true);
            }
        }

        return NULL;
    }

    private String translation(Element elemOrg, String defaultMetadataLang)
    {
        defaultMetadataLang = defaultMetadataLang.substring(0, 2);
        for (Element e : (List<Element>) elemOrg.getChildren()) {
            if (e.getName().equalsIgnoreCase(defaultMetadataLang)) {
                return e.getTextTrim();
            }
        }
        if (elemOrg.getTextTrim().length() > 0) {
            return elemOrg.getTextTrim();
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    public static String lookupElement(Element originalElem, String name, final String defaultMetadataLang)
    {
        Element elem = Utils.nextElement(originalElem.getDescendants(new ContactElementFinder("CharacterString",
                XslUtil.GCO_NAMESPACE, name)));
        if (elem == null) {
            Iterator freeTexts = originalElem.getDescendants(new ContactElementFinder("PT_FreeText",
                    XslUtil.GMD_NAMESPACE, name));
            while (freeTexts.hasNext()) {
                Element next = (Element) freeTexts.next();
                Iterator<Element> defaultLangElem = next.getDescendants(new Filter()
                {

                    private static final long serialVersionUID = 1L;

                    public boolean matches(Object arg0)
                    {
                        if (arg0 instanceof Element) {
                            Element element = (Element) arg0;
                            return element.getName().equals("LocalisedCharacterString")
                                    && ("#" + defaultMetadataLang)
                                            .equalsIgnoreCase(element.getAttributeValue("locale"));
                        }
                        return false;
                    }

                });

                if (defaultLangElem.hasNext()) {
                    return defaultLangElem.next().getTextTrim();
                }

            }

            return "";
        } else {
            return elem.getTextTrim();
        }
    }

    public static String baseHref(String id) {
        return  XLink.LOCAL_PROTOCOL+"xml.user.get?id=" + id;
    }
    private Collection<Element> xlinkIt(Element originalElem, String role, String id, boolean validated)
    {
        String schema = "iso19139";
        if (originalElem.getChild("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE) != null) {
            schema = "iso19139.che";
        }

        originalElem.removeContent();
        // param order is important, id param must be first
        originalElem.setAttribute(XLink.HREF,baseHref(id) + "&schema=" + schema
                + "&role=" + role, XLink.NAMESPACE_XLINK);

        if (!validated) {
            originalElem
                    .setAttribute(XLink.ROLE, ReusableObjManager.NON_VALID_ROLE, XLink.NAMESPACE_XLINK);
        }
        originalElem.setAttribute(XLink.SHOW, XLink.SHOW_EMBED, XLink.NAMESPACE_XLINK);

        originalElem.detach();
        return Collections.singleton(originalElem);
    }

    public Collection<Element> add(Element placeholder, Element originalElem, Dbms dbms, String metadataLang)
            throws Exception
    {
        int id = _serialFactory.getSerial(dbms, "Users");

        return doAdd (originalElem, id, dbms, metadataLang);
    }

    private String insertQuery(int id) {
        return "INSERT INTO Users (id, username, password, surname, name, profile, "
            + "address, state, zip, country, email, organisation, kind, streetnumber, "
            + "streetname, postbox, city, phone, facsimile, positionname, onlineresource, "
            + "hoursofservice, contactinstructions, publicaccess, orgacronym, directnumber, mobile, validated, "
            + "email1, phone1, facsimile1, email2, phone2, facsimile2, onlinename, onlinedescription, parentInfo) "
            + "VALUES (" + id + ", ?, ?, ?, ?, '" + SHARED
            + "', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
            + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    
    private Collection<Element> doAdd(Element originalElem, int id, Dbms dbms, String metadataLang) throws SQLException, Exception
    {

        String role = processQuery(originalElem, dbms, id, insertQuery(id), false, metadataLang, true);

        return xlinkIt(originalElem, role, String.valueOf(id), false);
    }

    /**
     * Executes the query using all the user data from the element. There must
     * be exactly 28 ? in the query. id is not one of them
     *
     * @param metadataLang
     */
    private String processQuery(Element originalElem, Dbms dbms, int id, String query, boolean validated,
            String metadataLang, boolean insert) throws Exception, SQLException
    {
        Element xml = Xml.transform((Element) originalElem.clone(), _styleSheet);
        // not all strings need to default to a space so they show up in editor.
        // If not then they will be lost
        String email1 = getTextPadEmpty(xml, "email1");
        String name = Utils.getText(xml, "firstName");
        String surname = Utils.getText(xml, "lastName");
        String username = Utils.getText(xml, "email1", UUID.randomUUID().toString());

        if (notEmpty(email1)) {
            username = email1 + "_" + id;
        } else if (notEmpty(name) || notEmpty(surname)) {
            username = name + "_" + surname + "_" + id;
        }

        if (username.trim().length() == 0) {
            username = "" + id;
        }
        username = username.replaceAll("\\s+", "_");

        if (insert) {
            while (dbms.select("SELECT username FROM Users WHERE username='" + username + "'").getChildren().size() > 0) {
                username = UUID.randomUUID().toString();
            }
        }

        String passwd = UUID.randomUUID().toString().substring(0, 12);

        String address = Utils.getText(xml, "addressLine");
        String state = Utils.getText(xml, "adminArea");
        String zip = Utils.getText(xml, "postalCode");
        String country = Utils.getText(xml, "country");
        String organ = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("orgName"), STRING);
        String streetnb = Utils.getText(xml, "streetNumber");
        String street = Utils.getText(xml, "streetName");
        String postbox = Utils.getText(xml, "postBox");
        String city = Utils.getText(xml, "city");
        String phone1 = getTextPadEmpty(xml, "voice1");
        String fac1 = getTextPadEmpty(xml, "facsimile1");

        String email2 = getTextPadEmpty(xml, "email2");
        String phone2 = getTextPadEmpty(xml, "voice2");
        String fac2 = getTextPadEmpty(xml, "facsimile2");

        String email3 = getTextPadEmpty(xml, "email3");
        String phone3 = getTextPadEmpty(xml, "voice3");
        String fac3 = getTextPadEmpty(xml, "facsimile3");

        String position = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("position"), STRING);
        String online = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("online"), URL);
        String onlinename = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("name"), STRING);
        String onlinedesc = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("desc"), STRING);

        String hours = Utils.getText(xml, "hoursOfService");
        String instruct = Utils.getText(xml, "contactInstructions");
        String orgacronym = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("acronym"), STRING);
        String directnumber = Utils.getText(xml, "directNumber");
        String mobile = Utils.getText(xml, "mobile");
        String role = Utils.getText(xml, "role");

        Integer parentInfo = processParent(originalElem, xml.getChild("parentInfo"), _serialFactory.getSerial(dbms, "Users"), dbms, metadataLang);

        String kind = "";
        ServiceContext context = ServiceContext.get();
        dbms.execute(query, username, PasswordUtil.encode(context, passwd), surname, name, address, state, zip, country, email1,
                organ, kind, streetnb, street, postbox, city, phone1, fac1, position, online, hours, instruct, "y",
                orgacronym, directnumber, mobile, validated ? "y" : "n", email2, phone2, fac2, email3, phone3, fac3,
                onlinename, onlinedesc, parentInfo);
        return role;
    }

    private Integer processParent(Element original, Element xml, int id, Dbms dbms, String metadataLang) throws Exception
    {
        if (xml==null || xml.getChildren().isEmpty()) return null;

        Element parentInfo = xml.getChild("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE);
        Element toReplace = Utils.nextElement(original.getDescendants(new ElementFinder("parentResponsibleParty",
                XslUtil.CHE_NAMESPACE, "CHE_CI_ResponsibleParty")));


        Integer finalId = null;

        if (toReplace.getAttribute(XLink.HREF, XLink.NAMESPACE_XLINK)==null){
            Collection<Element> result;
            Element placeholder = new Element("placeholder");
            Pair<Collection<Element>, Boolean> findResult = find(placeholder, parentInfo, metadataLang);
            if(!findResult.two()){
                result = doAdd(parentInfo, id, dbms, metadataLang);
            } else {
                result = findResult.one();
            }


            Element xlinkedParent = result.iterator().next();

            try {
                int parsedId = Integer.parseInt(Utils.extractUrlParam(xlinkedParent, "id"));
                Element parent = toReplace.getParentElement();

                toReplace.detach();

                parent.addContent(xlinkedParent);
                finalId = parsedId;

            } catch (NumberFormatException e) {
                Log.error(Geocat.Module.REUSABLE, "Error parsing the id of the parentResponsibleParty: "
                        + Utils.extractUrlParam(xlinkedParent, "id"));
            }
        } else if( !ReusableObjManager.isValidated(toReplace)){
            Processor.uncacheXLinkUri(toReplace.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK));

            updateObject((Element)toReplace.clone(), dbms, metadataLang);
            int parsedId = Integer.parseInt(Utils.extractUrlParam(toReplace, "id"));
            finalId = parsedId;
        }
        return finalId;

    }

    private String getTextPadEmpty(Element xml, String name)
    {
        String val = Utils.getText(xml, name, null);
        if (val == null)
            return null;
        // need space so xslt from user-xml.xsl doesnt get rid of the element
        if (val.length() == 0)
            return " ";
        return val;
    }

    private boolean notEmpty(String email)
    {
        return email != null && email.trim().length() > 0;
    }

    public Element find(UserSession session, boolean validated) throws Exception
    {

        final String query = "SELECT id,email,username,name,surname FROM Users WHERE profile='" + SHARED
                + "' AND validated=?";
        List<Element> results = _dbms.select(query, validated ? 'y' : 'n').getChildren("record");
        Element category = new Element(REPORT_ROOT);
        for (Element result : results) {
            Element e = new Element(REPORT_ELEMENT);
            String id = result.getChildTextTrim("id");
            String url = XLink.LOCAL_PROTOCOL+"shared.user.edit?closeOnSave&id=" + id + "&validated=n&operation=fullupdate";

            addChild(e, REPORT_URL, url);
            addChild(e, REPORT_ID, id);
            addChild(e, REPORT_TYPE, "contact");
            addChild(e, REPORT_XLINK, createXlinkHref(id, session, "") + "*");
            String email = result.getChildTextTrim("email");
            String username = result.getChildTextTrim("username");
            String name = result.getChildTextTrim("name");
            String surname = result.getChildTextTrim("surname");
            String desc = "";
            if (email == null || email.length() == 0) {
                desc = username;
            } else {
                desc = email;
            }

            desc = name + " " + surname + " &lt;" + desc + "&gt;";

            addChild(e, REPORT_DESC, desc);
            addChild(e, REPORT_SEARCH, id+desc);

            category.addContent(e);
        }

        return category;
    }

    public void performDelete(String[] ids, Dbms dbms, UserSession session, String ignored) throws Exception
    {
        dbms.execute("UPDATE Users SET parentinfo=null WHERE "+Utils.constructWhereClause("parentinfo", ids));
        String whereClause = Utils.constructWhereClause("id", ids);
        dbms.execute("DELETE FROM Users WHERE " + whereClause + " AND profile='" + SHARED + "'");
        whereClause = Utils.constructWhereClause("userId", ids);
        dbms.execute("DELETE FROM UserGroups WHERE " + whereClause);
    }

    public String createXlinkHref(String id, UserSession session, String notRequired)
    {
        return XLink.LOCAL_PROTOCOL+"xml.user.get?id=" + id;
    }

    public String updateHrefId(String oldHref, String id, UserSession session)
    {
        return oldHref.replaceAll("id=\\d+","id="+id).replaceAll("/fra/|/deu/|/ita/|/___/","/eng/");
    }

    public Map<String, String> markAsValidated(String[] ids, Dbms dbms, UserSession session) throws Exception
    {
        String whereClause = Utils.constructWhereClause("id", ids);
        dbms.execute("UPDATE Users SET validated='y' WHERE profile='" + SHARED + "' AND " + whereClause);

        Map<String, String> idMap = new HashMap<String, String>();

        for (String id : ids) {
            idMap.put(id, id);
        }
        return idMap;
    }

    public Collection<Element> updateObject(Element xlink, Dbms dbms, String metadataLang) throws Exception
    {
        int id = Integer.parseInt(Utils.extractUrlParam(xlink, "id"));

        String query = "UPDATE Users SET username=?, password=?, surname=?, name=?, "
                + "address=?, state=?, zip=?, country=?, email=?, organisation=?, kind=?, streetnumber=?, "
                + "streetname=?, postbox=?, city=?, phone=?, facsimile=?, positionname=?, onlineresource=?, "
                + "hoursofservice=?, contactinstructions=?, publicaccess=?, orgacronym=?, directnumber=?, "
                + "mobile=?, validated=?, email1=?, phone1=?, facsimile1=?, "
                + "email2=?, phone2=?, facsimile2=?, onlinename=?, onlinedescription=?, parentInfo=? " + "WHERE profile='" + SHARED
                + "' AND (id=" + id + ")";

        String role = processQuery(xlink, dbms, id, query, false, metadataLang, false);

        String href = xlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK);
        int roleIndex = href.indexOf("role=");
        href = href.substring(0, roleIndex) + "role=" + role;
        xlink.setAttribute(XLink.HREF, href, XLink.NAMESPACE_XLINK);

        return Collections.emptySet();
    }

    public boolean isValidated(Dbms dbms, String href) throws NumberFormatException, SQLException
    {
        String id = Utils.id(href);
        if(id==null) return false;
        try {
            int group = Integer.parseInt(id);
            Element record = dbms.select("SELECT validated FROM users WHERE id=?", group).getChild("record");
    
            return record == null || !record.getChildTextTrim("validated").equalsIgnoreCase("n");
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return "Reusable Contact";
    }

    private static class ContactElementFinder extends ElementFinder {

        public ContactElementFinder(String name, Namespace ns, String parent)
        {
            super(name, ns, parent);
        }

        private static final long serialVersionUID = 1L;

        @Override
        protected boolean otherChecks(Element elem)
        {
            return !isParentResponsibleParty(elem);
        }

        public boolean isParentResponsibleParty(Element elem)
        {
            if(elem == null) return false;
            if(elem.getName().equals("parentResponsibleParty")){
                return true;
            }
            return isParentResponsibleParty(elem.getParentElement());
        }
    }

    @Override
    public String[] getInvalidXlinkLuceneField() {
        return new String[]{"invalid_xlink_contact"};
    }
    @Override
    public String[] getValidXlinkLuceneField() {
    	return new String[]{"valid_xlink_contact"};
    }

    @Override
    public String createAsNeeded(String href, UserSession session) throws Exception {
        String startId = Utils.id(href);
        if(startId!=null) return href;

        final String regex = ".+\\?.*role=([^&#]+)&?.*";
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(href);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("cannot find role");
        }
        String role = matcher.group(1);
        
        Dbms dbms = (Dbms) ServiceContext.get().getResourceManager().open(Geonet.Res.MAIN_DB);
        int id = _serialFactory.getSerial(dbms, "Users");
        String username = UUID.randomUUID().toString();
        String email = username+"@generated.org";
        String sql = "INSERT INTO Users (id, username, password, profile, email, validated) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        
        String validated = "n";
        dbms.execute(sql, id, username, "", SHARED, email, validated);
        return XLink.LOCAL_PROTOCOL+"xml.user.get?id="+id+"&schema=iso19139.che&role="+role;
    }
}
