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

package org.fao.geonet.util;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jeeves.server.dispatchers.guiservices.XmlFile;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.utils.XmlFileCacher;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeImpl;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.GeoNetworkAnalyzer;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;

/**
 * Used to get translations and strings from the strings xml files
 *
 * @author jeichar
 */
public final class LangUtils
{

    
    public static List<Element> loadCodeListFile(String schemaDir, String langCode, String codeListName)
            throws IOException, JDOMException
    {
        List<Element> codeList = new LinkedList<Element>();
        File file = new File(schemaDir + "/loc/" + langCode + "/codelists.xml");
        addCodeLists(codeListName, codeList, file);
        
        file = new File(schemaDir +"/../iso19139/loc/"+langCode + "/codelists.xml");
        addCodeLists(codeListName, codeList, file);
        return codeList;
    }

    private static void addCodeLists(String codeListName, List<Element> codeList, File file) throws IOException, JDOMException {
        if (file.exists()) {
            Element xmlDoc = Xml.loadFile(file);

            List<Element> codelists = xmlDoc.getChildren("codelist");
            for (Element element : codelists) {
                if (element.getAttributeValue("name").equals(codeListName)) {
                    codeList.addAll(element.getChildren("entry"));
                    break;
                }
            }
        }
    }

    private static String translate(String[] path, String defaultVal, List<Element> children)
    {
        for (Element e : children) {
            if (e.getName().equals(path[0])) {
                if (path.length == 1) {
                    return e.getTextTrim();
                } else {
                    String[] result = new String[path.length - 1];
                    System.arraycopy(path, 1, result, 0, result.length);
                    return translate(result, defaultVal, e.getChildren());
                }
            }
        }

        return defaultVal;
    }

    public static List<Element> loadStrings(String appDir, String langCode) throws IOException, JDOMException
    {
        File file = new File(appDir + "/loc/" + langCode + "/xml/strings.xml");
        if (file.exists()) {
            return Xml.loadFile(file).getChildren();

        }
        return Collections.emptyList();
    }

    public static String translate(String appDir, String langCode, String key, String defaultVal) throws IOException,
            JDOMException
    {
        String[] path = key.split("/");
        return translate(path, defaultVal, loadStrings(appDir, langCode));
    }

    /**
     * Returns the default language of the metadata
     */
    public static String iso19139DefaultLang(Element xml)
    {

        while (xml.getParentElement() !=null ){
            xml = xml.getParentElement();
        }

        Iterator iter = xml.getDescendants(new ElementFinder("language", XslUtil.GMD_NAMESPACE,
                "CHE_MD_Metadata"));
        if (!iter.hasNext()) {
            iter = xml
                    .getDescendants(new ElementFinder("language", XslUtil.GMD_NAMESPACE, "MD_Metadata"));
        }

        String defaultLang = "eng";
        if (iter.hasNext()) {
            Element langElem = (Element) iter.next();
            if (langElem.getChild("CharacterString", XslUtil.GCO_NAMESPACE) != null) {
                defaultLang = langElem.getChildTextTrim("CharacterString", XslUtil.GCO_NAMESPACE);
            }
        }
        return defaultLang;
    }

    /**
     * Returns the text of the element translated if possible. The preferredLang
     * is first choice then the metadata default lang is next
     *
     * @param element
     *            element with CharacterString or PT_FreeText element children
     */
    public static String iso19139TranslatedText(Element element, String preferredLang, String defaultLang)
    {
        Element charString = element.getChild("CharacterString", XslUtil.GCO_NAMESPACE);
        if (preferredLang.equalsIgnoreCase(defaultLang)
                && charString != null) {
            return element.getChild("CharacterString", XslUtil.GCO_NAMESPACE).getTextTrim();
        }

        String fallback = null;

        ElementFinder finder = new ElementFinder("LocalisedCharacterString", XslUtil.GMD_NAMESPACE, "textGroup");
        Iterator<Element> localised = element.getDescendants(finder);

        while( localised.hasNext() ){
            Element next = localised.next();

            String langcode = next.getAttributeValue("locale").substring(1);
            if( preferredLang.toLowerCase().startsWith(langcode) ){
                return next.getTextTrim();
            }
            if( defaultLang.toLowerCase().startsWith(langcode) ){
                fallback = next.getTextTrim();
            }
        }

        if( fallback==null && charString!=null ) {
            return charString.getTextTrim();
        }

        if(fallback == null ){
            Iterator children = element.getDescendants(finder);
            return ((Element) children.next()).getTextTrim();
        }
        return fallback;
    }

    /**
     * Accesses a translation from the simplified multiLang xml
     *
     * @see org.fao.geonet.util.LangUtils#createDescFromParams(org.jdom.Element, String)
     */
    public static String getTranslation(String descAt, String locale) throws IOException, JDOMException
    {
        Element desc = loadInternalMultiLingualElem(descAt);
        if( locale==null ){
            if( desc.getText()==null ){
                if( !desc.getChildren().isEmpty() ){
                    return ((Element) desc.getChildren()).getTextTrim();
                }
            } else {
                return desc.getTextTrim();
            }
        }else{
            String code = locale.substring(0, 2);
            for (Element child : (List<Element>) desc.getChildren()) {
                if( child.getName().equalsIgnoreCase(code) ){
                    return child.getTextTrim();
                }
            }
            if( desc.getText() != null && desc.getTextTrim().length()>0 ){
                return desc.getTextTrim();
            } else if (desc.getChildren().size() > 0){
                return ((Element) desc.getChildren().get(0)).getTextTrim();
            }
        }
        return "";
    }

    public static String encodeXmlText(String data) {
        return "<![CDATA["+data+"]]>";
    }

    /**
     * Creates the simplified translation XML from the parameters
     */
    public static String createDescFromParams(Element params, String paramBaseName)
    {

        final String descDe = encodeXmlText(Util.getParam(params, paramBaseName + "DE",""));
        final String descIt = encodeXmlText(Util.getParam(params, paramBaseName + "IT",""));
        final String descFr = encodeXmlText(Util.getParam(params, paramBaseName + "FR",""));
        final String descEn = encodeXmlText(Util.getParam(params, paramBaseName + "EN",""));
        final String descRm = encodeXmlText(Util.getParam(params, paramBaseName + "RM",""));

        return String.format("<DE>%s</DE><FR>%s</FR><IT>%s</IT><EN>%s</EN><RM>%s</RM>", descDe, descFr, descIt, descEn, descRm);
    }

    public static Element toIsoMultiLingualElem(String appPath, String basicValue) throws Exception
    {


        final Element desc = loadInternalMultiLingualElem(basicValue);
        return Xml.transform(desc, appPath+"xsl/iso-internal-multilingual-conversion.xsl");
    }

    public static Element loadInternalMultiLingualElem(String basicValue) throws IOException
    {

        final String xml = "<description>" + basicValue.replaceAll("(<\\w+>)\\s*(\\<!\\[CDATA\\[)*\\s*(.*?)\\s*(\\]\\]\\>)*(</\\w+>)","$1<![CDATA[$3]]>$5") + "</description>";

        if(org.apache.log4j.Logger.getLogger(Geonet.GEONETWORK).isDebugEnabled()) {
            System.out.println("Parsing xml to get languages: \n"+xml);
        }
        Log.debug(Geonet.GEONETWORK, "Parsing xml to get languages: \n"+xml);

        Element desc;
        try {
            desc = Xml.loadString(xml, false);
        } catch(JDOMException jdomParse) {
            try {
                String encoded = URLEncoder.encode(basicValue, "UTF-8");
                desc = Xml.loadString(String.format("<description><EN>%1$s</EN><DE>%1$s</DE><FR>%1$s</FR><IT>%1$s</IT></description>", encoded),false);
            } catch (JDOMException e) {
                Element en = new Element("EN").setText("Error setting parsing text: " + basicValue);
                desc = new Element("description").addContent(en);
            }
        }
        return desc;
    }
    public static List<Content> loadInternalMultiLingualElemCollection(String basicValue) throws IOException, JDOMException
    {
        Element multiLingualElem = loadInternalMultiLingualElem(basicValue);
        List<Content> nodes = new ArrayList<Content>(multiLingualElem.getContent());
        Set<String> locales = new HashSet<String>();

        for (Iterator<Content> iter = nodes.iterator(); iter.hasNext(); ) {
            Content node = iter.next();
            if( node instanceof Element){
                String locale = ((Element)node).getName();
                if( locales.contains(locale) ){
                    iter.remove();
                }else{
                    locales.add(locale);
                }
            }
            node.detach();
        }
        return nodes;
    }

    public static List<String> analyzeForSearch(Reader reader) throws IOException {
        ArrayList<String> strings = new ArrayList<String>();
        GeoNetworkAnalyzer analyzer = new GeoNetworkAnalyzer();
        TokenStream stream = analyzer.tokenStream(null, reader);
        stream.reset();
        do {
            Iterator<AttributeImpl> iterator = stream.getAttributeImplsIterator();
            while (iterator.hasNext()) {
                AttributeImpl next = iterator.next();
                if(next instanceof TermAttribute) {
                    String term = ((TermAttribute) next).term();
                    if(term.length() > 0)
                        strings.add(term);
                }
            }
        } while (stream.incrementToken());
        return strings;
    }

    public static String to2CharLang(String s) {
        if(s.length() > 2) {
            s = s.substring(0,2);
        }
        return s;
    }

    public enum FieldType { URL, STRING }
    public static String toInternalMultilingual(String metadataLang, String appPath, Element descElem2, FieldType fieldType) throws Exception
    {
        if( descElem2==null ){
            return null;
        }

        Element descElem = (Element) descElem2.clone();
        descElem.setName("root");
        descElem.setNamespace(null);

        String desc;
        Map<String, String> params = new HashMap<String, String>();
        params.put("metadataLang", metadataLang);
        String styleSheet;
        switch (fieldType)
        {
        case URL:
            styleSheet = "xsl/iso-internal-multilingual-conversion-url.xsl";
            break;
        case STRING:
            styleSheet = "xsl/iso-internal-multilingual-conversion.xsl";
            break;
        default:
            throw new IllegalArgumentException(fieldType+" needs to be supported");
        }

        Element converted = Xml.transform(descElem, appPath+styleSheet, params);

        List<Element> allTranslations = converted.getChildren();
        StringBuilder builder = new StringBuilder(converted.getTextTrim());

        for (Element element : allTranslations) {
            builder.append(Xml.getString(element));
        }
        desc = builder.toString();
        return desc;
    }

    public static void resolveMultiLingualElements(Element elUser, final String[] elementsToResolve) throws IOException, JDOMException
    {
        boolean removeTranslation = (elUser.getChild("record")!=null && 
                elUser.getChild("record").getChild("profile") !=null && 
                !elUser.getChild("record").getChild("profile").getTextTrim().equals(Geocat.Profile.SHARED)) ||
                (elUser.getChild("profile") != null &&
                        !elUser.getChild("profile").getTextTrim().equals(Geocat.Profile.SHARED));
        
        Filter findMultilingualElements = new Filter()
        {
            private static final long serialVersionUID = 1L;
            Set<String>               toResolve        = new HashSet<String>(Arrays.asList(elementsToResolve));

            public boolean matches(Object arg0)
            {
                if (arg0 instanceof Element) {
                    Element elem = (Element) arg0;
                    return toResolve.contains(elem.getName());
                }
                return false;
            }
        };
        Iterator iter = elUser.getDescendants(findMultilingualElements);
        List<Element> toResolve = new ArrayList<Element>();
        while (iter.hasNext()) {
            toResolve.add((Element) iter.next());
        }

        for (Element elem : toResolve) {
            String text = elem.getText();
            elem.setText(null);
            List<Content> translationsAsSimpleXML = loadInternalMultiLingualElemCollection(text);
            if(removeTranslation) elem.setText(getSingleTranslation(translationsAsSimpleXML));
            else elem.addContent(translationsAsSimpleXML);
        }
    }

    private static String getSingleTranslation(List<Content> translationsAsSimpleXML) {

        for (Content content : translationsAsSimpleXML) {
            if (content instanceof Element) {
                Element e = (Element) content;
                if(!e.getTextTrim().isEmpty()) {
                    return e.getTextTrim();
                }
            }
        }
        return "";
    }

    public static String two2ThreeLangCode(String sLang)
    {
        // HACK needs to be more complete

        if( sLang.equalsIgnoreCase("en")){
            return "eng";
        }
        if( sLang.equalsIgnoreCase("de")){
            return "deu";
        }
        if( sLang.equalsIgnoreCase("fr")){
            return "fra";
        }
        if( sLang.equalsIgnoreCase("it")){
            return "ita";
        }
        if( sLang.equalsIgnoreCase("rm")){
            return "roh";
        }
        return sLang;
    }

    public static String loadString(String string, String appPath, String language) throws IOException, JDOMException {
        String[] keys = string.split(".",2);
        List<Element> strings = loadStrings(appPath,language);
        for (Element element : strings) {
            if(element.getName().equals(keys[0])) {
                if(keys.length > 1) {
                    String value = loadString(keys[1],appPath,language);
                    if(value != null) {
                        return value;
                    }
                } else {
                    return element.getText();
                }
            }
            if(element.getName().equals(string)) {
                return element.getText();
            }
        }
        
        if(!"eng".equalsIgnoreCase(language)) {
            return loadString(string, appPath, "eng");
        }
        
        return null;
    }

}
