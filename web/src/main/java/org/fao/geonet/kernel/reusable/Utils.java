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

import com.google.common.base.Function;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import jeeves.xlink.XLink;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.Email;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.LangUtils;
import org.geotools.gml3.GMLConfiguration;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import static java.lang.Double.parseDouble;

/**
 * Utility methods for this package
 *
 * @author jeichar
 */
public final class Utils {

    public static final GMLConfiguration gml3Conf = new GMLConfiguration();
    public static final org.geotools.gml2.GMLConfiguration gml2Conf = new org.geotools.gml2.GMLConfiguration();

    public static String id( String href ) {
        if (href.indexOf("id=") < 0) {
            return null; // nothing to be done
        }
        String id = href.substring(href.indexOf("id=") + 3);
        if (id.contains("&")) {
            id = id.substring(0, id.indexOf('&'));
        }
        return id;
    }

    /**
     * Finds xlinks with the href that contains the fragment provided in the constructor
     *
     * @author jeichar
     */
    public static class FindXLinks implements Filter {
        private static final long serialVersionUID = 1L;
        private final Pattern _fragment;

        public FindXLinks( String fragment ) {
            String[] split = fragment.split("___");
            StringBuilder builder = new StringBuilder();
            for( String string : split ) {
                if (builder.length() > 0) {
                    builder.append("\\w\\w\\w");
                }
                builder.append(Pattern.quote(string));
            }
            this._fragment = Pattern.compile(builder.toString());
        }

        public boolean matches( Object arg0 ) {
            if (arg0 instanceof Element) {
                Element element = (Element) arg0;
                String href = element.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK);
                return href != null && _fragment.matcher(href).find();
            }
            return false;
        }

    }

    static final String XSL_REUSABLE_OBJECT_DATA_XSL = "xsl/reusable-object-snippet-flatten.xsl";

    static <T> List<T> convertToList( Iterator iter, Class<T> class1 ) {
        List<T> placeholders = new ArrayList<T>();
        while( iter.hasNext() ) {
            placeholders.add(class1.cast(iter.next()));
        }
        return placeholders;
    }

    static Element nextElement( Iterator<Content> elements ) {
        Content originalElem = null;
        while( !(originalElem instanceof Element) && elements.hasNext() ) {
            originalElem = elements.next();
        }
        return (Element) originalElem;
    }

    static String getText( Element xml, String name, String defaultName ) {
        String text = xml.getChildText(name);
        if (text != null) {
            return text.trim();
        }
        return defaultName;
    }

    static String getText( Element xml, String name ) {
        return getText(xml, name, "");
    }

    public static boolean isEmpty( Collection<Element> xlinks ) {
        return xlinks == null || xlinks.isEmpty();
    }

    public static String constructWhereClause( String columnName, String[] ids ) {
        return columnName + "=" + mkString(Arrays.asList(ids), " OR " + columnName + "=");
    }

    /**
     * Get all the metadata that use the xlink. This does a sql like query so only a unique portion
     * of the sql is required
     *
     * @param context
     */
    public static Set<MetadataRecord> getReferencingMetadata(final ServiceContext context,
                                                             final FindMetadataReferences strategy,
                                                             final List<String> fields,
                                                             final String id,
                                                             final Boolean isValidated,
                                                             final boolean loadMetadata,
                                                             final Function<String, String> idConverter ) throws Exception {

        String concreteId = idConverter.apply(id);
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        SearchManager searchManager = gc.getSearchmanager();

        IndexAndTaxonomy indexAndTaxonomy = searchManager.getIndexReader(null, -1);
		GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;

        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            TreeSet<MetadataRecord> results = new TreeSet<MetadataRecord>(new Comparator<MetadataRecord>(){

                public int compare( MetadataRecord o1, MetadataRecord o2 ) {
                    return o1.id.compareTo(o2.id);
                }

            });

            for( String field : fields ) {
                Set<String> requiredFields = new HashSet<String>();
                requiredFields.add("_id");
                requiredFields.add("_owner");
                requiredFields.add(field);
                Query query;
                if (isValidated == null) {
                    BooleanQuery booleanQuery = new BooleanQuery();
                    booleanQuery.add(strategy.createFindMetadataQuery(field, id, true), BooleanClause.Occur.SHOULD);
                    booleanQuery.add(strategy.createFindMetadataQuery(field, id, false), BooleanClause.Occur.SHOULD);
                    query = booleanQuery;
                } else {
                    query = strategy.createFindMetadataQuery(field, concreteId, isValidated);
                }
                TopDocs tdocs = searcher.search(query, Integer.MAX_VALUE);

                for( ScoreDoc sdoc : tdocs.scoreDocs ) {
                    Document element = reader.document(sdoc.doc, requiredFields);

                    HashSet<String> xlinks = new HashSet<String>();
                    for( String value : element.getValues(field) ) {
                        if (equalIds(value, concreteId)) {
                            xlinks.add(value);
                        }
                    }
                    if (!xlinks.isEmpty()) {
                        MetadataRecord record = new MetadataRecord(gc.getXmlSerializer(), element, xlinks, dbms, loadMetadata);
                        results.add(record);
                    }

                }
            }
            return results;
        } finally {
            searchManager.releaseIndexReader(indexAndTaxonomy);
        }
    }

    private static boolean equalIds( String value, String id2 ) {
        // ids are normally ints bug some (like keywords) are strings

        String id1 = Utils.id(value);
        try {
            double id1Double = parseDouble(id1);
            double id2Double = parseDouble(id2);
            return Math.abs(id1Double - id2Double) < 0.1;
        } catch (NumberFormatException e) {
            return id1.equals(id2);
        }
    }

    public static String mkString( Iterable< ? extends Object> iterable ) {
        return mkString(iterable, "", ",", "");
    }

    public static String mkString( Iterable< ? extends Object> iterable, String separator ) {
        return mkString(iterable, "", separator, "");
    }

    public static String mkString( Iterable< ? extends Object> iterable, String pre, String separator, String post ) {
        StringBuilder out = new StringBuilder();

        for( Object object : iterable ) {
            if (out.length() == 0) {
                out.append(pre);
            } else {
                out.append(separator);
            }
            out.append(object);
        }
        out.append(post);

        return out.toString();
    }

    public static String mkBaseURL( String baseURL, SettingManager settingMan ) {
        String host = settingMan.getValue("system/server/host").trim();
        String portNumber = settingMan.getValue("system/server/port").trim();
        if (host.length() == 0) {
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e1) {
                host = "http://localhost";
            }
        }
        if (portNumber.length() == 0) {
            portNumber = "8080";
        }

        try {
            new URL(host);
        } catch (MalformedURLException e) {
            try {
                new URL("http://" + host);
                host = "http://" + host;
            } catch (MalformedURLException e2) {
                throw new RuntimeException(e);
            }
        }
        if (host.length() == 0) {
            return baseURL;
        } else {
            return host + ":" + portNumber + baseURL;
        }
    }

    public static String extractUrlParam( Element xlink, String paramName ) {
        String href = xlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK);
        if (href == null) {
            return null;
        }
        int beginIndex = href.indexOf(paramName + "=") + paramName.length() + 1;
        String frag = href.substring(beginIndex);
        int index = frag.indexOf('&');
        if (index < 0) {
            index = frag.length();
        }
        String idString = frag.substring(0, index);
        return idString;
    }

    public static void unpublish( Collection<String> results, ServiceContext context ) throws Exception {
        if (results.size() > 0) {
            Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
            StringBuilder query = new StringBuilder("DELETE FROM OperationAllowed WHERE (groupId <= 1 ) AND (metadataId=");
            Iterator<String> iter = results.iterator();
            query.append(iter.next());
            while( iter.hasNext() ) {
                query.append(" OR metadataId=");
                query.append(iter.next());
            }
            query.append(")");
            dbms.execute(query.toString());
        }
    }

    public static ReplacementStrategy strategy( ReusableTypes reusableType, ServiceContext context ) throws Exception {
        ReplacementStrategy strategy = null;
        String appPath = context.getAppPath();
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        String baseUrl = mkBaseURL(context.getBaseUrl(), gc.getSettingManager());
        String language = context.getLanguage();

        switch( reusableType ) {
        case extents:
            strategy = new ExtentsStrategy(baseUrl, appPath, gc.getExtentManager(), language);
            break;
        case keywords:
            strategy = new KeywordsStrategy(gc.getThesaurusManager(), appPath, baseUrl, language);
            break;
        case formats:
            strategy = new FormatsStrategy(dbms, appPath, baseUrl, language, context.getSerialFactory());
            break;
        case contacts:
            strategy = new ContactsStrategy(dbms, appPath, baseUrl, language, context.getSerialFactory());
            break;
        default:
            break;
        }
        return strategy;
    }

    public static void addChild( Element record, String elemName, String text ) {
        Element e = new Element(elemName);
        record.addContent(e);
        e.setText(text);
    }

    public static void sendEmail( SendEmailParameter args ) throws SQLException, MessagingException, AddressException {
        String query = "SELECT email,id FROM Users WHERE id=" + mkString(args.emailInfo.keySet(), " OR id=");
        Element emailRecords = args.dbms.select(query);
        GeonetContext gc = (GeonetContext) args.context.getHandlerContext(Geonet.CONTEXT_NAME);

        try {
            Set<String> unnotifiedIds = new HashSet<String>();

            for( Element element : (List<Element>) emailRecords.getChildren() ) {
                final String id = element.getChildText("id");
                String email = element.getChildText("email");
                if (!Email.isValidEmailAddress(email)) {
                    unnotifiedIds.addAll(args.emailInfo.get(id));
                } else {
                    String emailBody = "\n\n\n"+args.msg + "\n" + args.baseURL + "/srv/eng/metadata.show?id="
                            + mkString(args.emailInfo.get(id), "\n" + args.baseURL + "/srv/eng/metadata.show?id=");

                    gc.getEmail().send(email, args.subject, args.msgHeader + emailBody, args.testing);
                }
            }

            if (!unnotifiedIds.isEmpty()) {
                String emailBody = args.msg + "\n" + args.baseURL + "/srv/eng/metadata.show?id="
                        + mkString(unnotifiedIds, "\n" + args.baseURL + "/srv/eng/metadata.show?id=");
                gc.getEmail().sendToAdmin(args.subject, emailBody, args.testing);
                Log.warning(Geocat.Module.REUSABLE, emailBody);
            }
        } catch (Exception e) {
            Log.error(Geocat.Module.REUSABLE, "The System Configuration is not correctly configured and there for emails cannot be sent.  "
                    + "Make sure the email/feedback settings are configured");
        }
    }

    public static String translate( String appPath, String langCode, String key, String separator ) throws IOException, JDOMException {
        String[] translations = {LangUtils.translate(appPath, "ger", key, ""), LangUtils.translate(appPath, "fre", key, ""),
                LangUtils.translate(appPath, "eng", key, ""), LangUtils.translate(appPath, "ita", key, "")};

        StringBuilder result = new StringBuilder();

        for( String string : translations ) {
            if (string != null && string.trim().length() > 0) {
                if (result.length() > 0) {
                    result.append(separator);
                }
                result.append(string);
            }
        }
        return result.toString();
    }

    @SuppressWarnings("unchecked")
    public static boolean equalElem( Element originalElem1, Element current1 ) {
        if (originalElem1 == null || current1 == null) {
            return false;
        }
        Element originalElem = (Element) originalElem1.clone();
        Element current = (Element) current1.clone();

        if (originalElem.getName().equals(current.getName()) && originalElem.getNamespaceURI() != null
                && originalElem.getNamespaceURI().equals(current.getNamespaceURI())) {

            if (XLink.isXLink(originalElem) || XLink.isXLink(current)) {
                String origHRef = XLink.getHRef(originalElem);
                if (origHRef == null) return false;
                return origHRef.equals(XLink.getHRef(current));
            } else if (originalElem.getChildren().size() == current.getChildren().size()) {
                if (!equalAtts(originalElem, current)) return false;
                List<Element> originalChildren = originalElem.getChildren();
                List<Element> currentChildren = current.getChildren();
                for( int i = 0; i < originalElem.getChildren().size(); i++ ) {
                    if (!findSameChild(currentChildren, originalChildren.get(i))) {
                        return false;
                    }
                }

                if (originalChildren.isEmpty()) {
                    return originalElem.getTextTrim().equals(current.getTextTrim());
                }
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    static boolean equalAtts( Element originalElem, Element current ) {
        List<Attribute> currAtts = filterUnimportantAtts(current.getAttributes());

        if (filterUnimportantAtts(originalElem.getAttributes()).size() != currAtts.size()) return false;

        for( Attribute attribute : currAtts ) {
            String value = originalElem.getAttributeValue(attribute.getName(), attribute.getNamespace());
            if (value == null && attribute.getValue() != null) return false;
            if (!value.equals(attribute.getValue())) return false;
        }
        return true;
    }

    private static List<Attribute> filterUnimportantAtts( List<Attribute> attributes ) {
        ArrayList<Attribute> filtered = new ArrayList<Attribute>();
        for( Attribute att : attributes ) {
            if ("http://www.w3.org/2001/XMLSchema-instance".equals(att.getNamespace().getURI())) continue;
            filtered.add(att);
        }
        return filtered;
    }

    private static boolean findSameChild( List<Element> currentChildren, Element required ) {
        String rstring = Xml.getString(required);
        for( Element child : currentChildren ) {
            String cstring = Xml.getString(child);
            if (equalElem(required, child)) {
                child.detach();
                return true;
            }
        }
        return false;
    }

}
