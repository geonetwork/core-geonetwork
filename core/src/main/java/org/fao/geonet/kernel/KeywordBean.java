//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jetty.util.URIUtil;
import org.fao.geonet.constants.Geonet.Namespaces;
import org.fao.geonet.exceptions.LabelNotFoundException;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Content;
import org.jdom.Element;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * TODO javadoc.
 */
public class KeywordBean {

    /**
     * A Hashmap of all the languages available in this keyword
     *
     * Parameters are:  Language, Label
     */
    private final Map<String, String> values = new LinkedHashMap<String, String>();
    private final Map<String, String> definitions = new LinkedHashMap<String, String>();
    private int id;
    private String code;
    private String coordEast = "";
    private String coordWest = "";
    private String coordSouth = "";
    private String coordNorth = "";
    private String thesaurusKey;
    private boolean selected;
    private String thesaurusTitle;
    private String thesaurusDate;
    private String downloadUrl;
    private String keywordUrl;
    private IsoLanguagesMapper isoLanguageMapper;
    private String defaultLang;

    public KeywordBean(IsoLanguagesMapper isoLangMapper) {
        this.isoLanguageMapper = isoLangMapper;
    }

    /**
     * Transforms a list of KeywordBean object into its iso19139 representation.
     *
     * <pre>
     *  <gmd:MD_Keywords>
     *  	<gmd:keyword>
     *  		<gmx:Anchor xlink:href="link_to_keyword_generator">Keyword 1</gmx:Anchor>
     *  	</gmd:keyword>
     * 		<gmd:keyword>
     *  		<gmx:Anchor xlink:href="link_to_keyword_generator">Keyword 2</gmx:Anchor>
     *  	</gmd:keyword>
     *  	<gmd:type>
     *  		<gmd:MD_KeywordTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode"
     * codeListValue="TYPE"/>
     *  	</gmd:type>
     *  	<gmd:thesaurusName>
     *  		<gmd:CI_Citation id="geonetwork.thesaurus.register.theme.bc44a748-f1a1-4775-9395-a4a6d8bb8df6">
     *  			<gmd:title>
     *  				<gco:CharacterString>THESAURUS NAME</gco:CharacterString>
     *  			</gmd:title>
     *  			<gmd:date gco:nilReason="unknown"/>
     * 				<gmd:identifier>
     *          <gmd:MD_Identifier>
     * 						<gmd:code>
     * 							<gmx:Anchor xlink:href="http://localhost:8080/geonetwork/srv/eng/metadata.show?uuid=bc44a748-f1a1-4775-9395-a4a6d8bb8df6">register.theme.bc44a748-f1a1-4775-9395-a4a6d8bb8df6</gmx:Anchor>
     * 						</gmd:code>
     *          </gmd:MD_Identifier>
     * 				</gmd:identifier>
     *  		</gmd:CI_Citation>
     *  	</gmd:thesaurusName>
     *  </gmd:MD_Keywords>
     *  </pre>
     *
     * @return a complex iso19139 representation of the keyword
     */
    @JsonIgnore
    public static Element getComplexIso19139Elt(List<KeywordBean> kbList) {
        Element root = new Element("MD_Keywords", Namespaces.GMD);

        Element cs = new Element("CharacterString", Namespaces.GCO);
        Element an = new Element("Anchor", Namespaces.GMX);

        List<Element> keywords = new ArrayList<Element>();

        Element type = null;
        Element thesaurusName = null;

        for (KeywordBean kb : kbList) {
            Element keyword = new Element("keyword", Namespaces.GMD);
            if (kb.getUriCode() != null && kb.getUriCode().length() != 0) {
                an.setText(kb.getDefaultValue());
                an.setAttribute("href", URIUtil.encodePath(kb.keywordUrl + kb.getUriCode()), Namespaces.XLINK);
                keyword.addContent((Content) an.clone());
            } else {
                cs.setText(kb.getDefaultValue());
                keyword.addContent((Content) cs.clone());
            }
            keywords.add((Element) keyword.detach());
            if (type == null)
                type = KeywordBean.createKeywordTypeElt(kb);
            if (thesaurusName == null)
                thesaurusName = KeywordBean.createThesaurusNameElt(kb);
        }

        // Add elements to the root MD_Keywords element.
        root.addContent(keywords);
        root.addContent(type);
        root.addContent(thesaurusName);

        return root;
    }

    /**
     * Creates keyword type element.
     */
    private static Element createKeywordTypeElt(KeywordBean kb) {
        Element type = new Element("type", Namespaces.GMD);
        Element keywordTypeCode = new Element("MD_KeywordTypeCode", Namespaces.GMD);
        keywordTypeCode.setAttribute("codeList", "http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode");
        keywordTypeCode.setAttribute("codeListValue", kb.getType());
        type.addContent(keywordTypeCode);

        return type;
    }

    /**
     * Create an identifier/MD_Identifier that describes the thesaurus
     */
    private static Element createIdentifier(String authority, String downloadUrl) {
        Element result = new Element("identifier", Namespaces.GMD);
        Element ident = new Element("MD_Identifier", Namespaces.GMD);
        Element code = new Element("code", Namespaces.GMD);
        Element gmxAnchor = new Element("Anchor", Namespaces.GMX).setText(authority);
        gmxAnchor.setAttribute("href", downloadUrl, Namespaces.XLINK);

        code.addContent(gmxAnchor);
        ident.addContent(code);
        result.addContent(ident);
        return result;
    }

    /**
     * Creates thesaurus name element.
     */
    private static Element createThesaurusNameElt(KeywordBean kb) {
        Element thesaurusName = new Element("thesaurusName", Namespaces.GMD);
        Element citation = new Element("CI_Citation", Namespaces.GMD);
        Element title = new Element("title", Namespaces.GMD);
        Element cs = new Element("CharacterString", Namespaces.GCO);
        Element date = new Element("date", Namespaces.GMD);

        cs.setText(kb.thesaurusTitle);

        if (StringUtils.hasLength(kb.thesaurusDate)) {
            Element ciDateEl = new Element("CI_Date", Namespaces.GMD);
            Element ciDateDateEl = new Element("date", Namespaces.GMD);
            Element ciDateDateGcoDateEl = new Element("Date", Namespaces.GCO);

            ciDateDateGcoDateEl.setText(kb.thesaurusDate);

            Element ciDateDatetypeEl = new Element("dateType", Namespaces.GMD);
            Element ciDateDatetypeCodeEl = new Element("CI_DateTypeCode", Namespaces.GMD);
            ciDateDatetypeCodeEl.setAttribute("codeList", "http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode");
            ciDateDatetypeCodeEl.setAttribute("codeListValue", "publication");

            ciDateDatetypeEl.addContent(ciDateDatetypeCodeEl);
            ciDateDateEl.addContent(ciDateDateGcoDateEl);
            ciDateEl.addContent(0, ciDateDateEl);
            ciDateEl.addContent(1, ciDateDatetypeEl);
            date.addContent(ciDateEl);

        } else {
            date.setAttribute("nilReason", "unknown", Namespaces.GCO);
        }


        title.addContent((Content) cs.clone());
        Element id = createIdentifier("geonetwork.thesaurus." + kb.thesaurusKey, kb.downloadUrl);

        citation.addContent(0, title);
        citation.addContent(1, date);
        citation.addContent(2, id);
        thesaurusName.addContent(citation);

        return thesaurusName;
    }

    public KeywordBean setThesaurusInfo(Thesaurus thesaurus) {
        this.thesaurusKey = thesaurus.getKey();
        this.thesaurusDate = thesaurus.getDate();
        this.thesaurusTitle = thesaurus.getTitle();

        return this;
    }

    @JsonIgnore
    public boolean isSelected() {
        return selected;
    }

    public KeywordBean setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public String getThesaurusKey() {
        return thesaurusKey;
    }

    public KeywordBean setThesaurusKey(String thesaurusKey) {
        this.thesaurusKey = thesaurusKey;
        return this;
    }

    @JsonIgnore
    public String getThesaurusTitle() {
        return thesaurusTitle;
    }

    public void setThesaurusTitle(String thesaurusTitle) {
        this.thesaurusTitle = thesaurusTitle;
    }

    @JsonIgnore
    public String getThesaurusDate() {
        return thesaurusDate;
    }

    public void setThesaurusDate(String thesaurusDate) {
        this.thesaurusDate = thesaurusDate;
    }

    @JsonIgnore
    public String getKeywordUrl() {
        return keywordUrl;
    }

    public KeywordBean setKeywordUrl(String keywordUrl) {
        this.keywordUrl = keywordUrl;
        return this;
    }

    /**
     * Return default language.  The default language is determined when creating the bean. In some
     * cases the language is explicitly declared if not then it is the context language if there is
     * no context available it is Geonet.DEFAULT_LANGUAGE
     *
     * @return the default language
     */
    @JsonIgnore
    public String getDefaultLang() {
        return defaultLang;
    }

    public KeywordBean setDefaultLang(String defaultLang) {
        this.defaultLang = defaultLang;
        return this;
    }

    /**
     * Get the "default" value. The default language is determined when creating the bean.  Since
     * often keyword beans have a single language this will return the only value
     *
     * @return return default value
     */
    @JsonProperty("value")
    public String getDefaultValue() {
        return values.get(defaultLang);
    }

    /**
     * Get the preferred label for a given language code
     *
     * @return preferredLabel
     */
    @JsonIgnore
    public String getPreferredLabel(String langCode) {
        String preferredLabel = values.get(langCode);

        if (hasPreferredLabel(preferredLabel)) {
            return preferredLabel;
        } else {
            throw new LabelNotFoundException(noPreferredLabelMessage(langCode));
        }
    }

    private String noPreferredLabelMessage(String langCode) {
        return "Could not find preferred label for language code " + langCode + " for the keyword uri " + getUriCode();
    }

    private boolean hasPreferredLabel(String preferredLabel) {
        return preferredLabel != null && !preferredLabel.isEmpty();
    }

    /**
     * Return an <em>unmodifiable</em> map of values.  Key is the 3 letter code language
     *
     * @return all definitions
     */
    public Map<String, String> getValues() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * Set a definition for the specified language
     *
     * @param value the new value
     * @param lang  the language to set, can be 2 or 3 letter language code
     * @return this keyword bean
     */
    public KeywordBean setValue(String value, String lang) {
        if (defaultLang == null) {
            defaultLang = to3CharLang(lang);
        }
        values.put(to3CharLang(lang), value);
        return this;
    }

    /**
     * Get the "default" definition. The default language is determined when creating the bean.
     * Since often keyword beans have a single language this will return the only definition
     *
     * @return return default definition
     */
    @JsonProperty("definition")
    public String getDefaultDefinition() {
        return definitions.get(defaultLang);
    }

    /**
     * Return an <em>unmodifiable</em> map of definitions.  Key is the 3 letter code language
     *
     * @return all definitions
     */
    public Map<String, String> getDefinitions() {
        return Collections.unmodifiableMap(definitions);
    }

    /**
     * Set a definition for the specified language
     *
     * @param definition the new definition
     * @param lang       the language to set
     * @return this keyword
     */
    public KeywordBean setDefinition(String definition, String lang) {
        if (defaultLang == null) {
            defaultLang = to3CharLang(lang);
        }
        definitions.put(to3CharLang(lang), definition);
        return this;
    }

    @JsonIgnore
    public int getId() {
        return id;
    }

    public KeywordBean setId(int id) {
        this.id = id;
        return this;
    }

    /**
     * Returns the URI of the keyword concept.
     */
    @JsonProperty("uri")
    public String getUriCode() {
        return code;
    }

    public KeywordBean setUriCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * TODO javadoc.
     */
    @JsonIgnore
    public String getRelativeCode() {
        if (code == null) {
            return "";
        } else if (code.contains("#"))
            return code.split("#")[1];
        else
            return code;
    }

    /**
     * Set the id/relative portion of the code
     *
     * @param newCode the new relative code
     * @return this bean
     */
    public KeywordBean setRelativeCode(String newCode) {
        this.code = getNameSpaceCode() + newCode;
        return this;
    }

    /**
     * TODO javadoc.
     */
    @JsonIgnore
    public String getNameSpaceCode() {
        if (code == null) {
            return "#";
        } else if (code.contains("#")) {
            String[] parts = code.split("#", 2);
            return parts[0] + "#";
        } else {
            return "#";
        }
    }

    private String asString(String s) {
        return s == null ? "" : s;
    }

    public String getCoordEast() {
        return asString(coordEast);
    }

    public KeywordBean setCoordEast(String coordEast) {
        this.coordEast = asString(coordEast);
        return this;
    }

    public String getCoordNorth() {
        return asString(coordNorth);
    }

    public KeywordBean setCoordNorth(String coordNorth) {
        this.coordNorth = asString(coordNorth);
        return this;
    }

    public String getCoordWest() {
        return asString(coordWest);
    }

    public KeywordBean setCoordWest(String coordWest) {
        this.coordWest = asString(coordWest);
        return this;
    }

    public String getCoordSouth() {
        return asString(coordSouth);
    }

    public KeywordBean setCoordSouth(String coordSouth) {
        this.coordSouth = asString(coordSouth);
        return this;
    }

    /**
     * TODO javadoc.
     */
    @JsonIgnore
    public String getType() {
        int tmpDotIndex = thesaurusKey.indexOf('.');
        return thesaurusKey.substring(tmpDotIndex + 1, thesaurusKey.indexOf(".", tmpDotIndex + 1));
    }

    @JsonIgnore
    public String getThesaurusType() {
        return org.apache.commons.lang.StringUtils.substringBefore(thesaurusKey, ".");
    }

    /**
     * Transforms a KeywordBean object into its iso19139 representation.
     *
     * <pre>
     * 		<gmd:keyword xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
     * xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="">
     * 	  		<gmx:Anchor xlink:href="link_to_keyword_generator">A KEYWORD GENERATED BY XLINK
     * SERVICE</gco:Anchor>
     * 		</gmd:keyword>
     *  	<gmd:type>
     *  		<gmd:MD_KeywordTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode"
     * codeListValue="TYPE"/>
     *  	</gmd:type>
     *  	<gmd:thesaurusName>
     *  		<gmd:CI_Citation>
     *  			<gmd:title>
     *  				<gco:CharacterString>THESAURUS NAME</gco:CharacterString>
     *  			</gmd:title>
     *  			<gmd:date gco:nilReason="unknown"/>
     * 				<gmd:identifier>
     *          <gmd:MD_Identifier>
     * 						<gmd:code>
     * 							<gmx:Anchor xlink:href="http://localhost:8080/geonetwork/srv/eng/metadata.show?uuid=bc44a748-f1a1-4775-9395-a4a6d8bb8df6">register.theme.bc44a748-f1a1-4775-9395-a4a6d8bb8df6</gmx:Anchor>
     * 						</gmd:code>
     *          </gmd:MD_Identifier>
     * 				</gmd:identifier>
     *  		</gmd:CI_Citation>
     *  	</gmd:thesaurusName>
     * </pre>
     *
     * @return an iso19139 representation of the keyword
     */
    @JsonIgnore
    public Element getIso19139() {
        Element ele = new Element("MD_Keywords", Namespaces.GMD);
        Element el = new Element("keyword", Namespaces.GMD);
        Element an = new Element("Anchor", Namespaces.GMX);
        Element cs = new Element("CharacterString", Namespaces.GCO);
        if (getUriCode() != null && getUriCode().length() != 0) {
            an.setText(getDefaultValue());
            an.setAttribute("href", URIUtil.encodePath(keywordUrl + getUriCode()), Namespaces.XLINK);
            el.addContent(an);
        } else {
            cs.setText(getDefaultValue());
            el.addContent(cs);
        }

        Element type = KeywordBean.createKeywordTypeElt(this);

        Element thesaurusName = KeywordBean.createThesaurusNameElt(this);

        ele.addContent(el);
        ele.addContent(type);
        ele.addContent(thesaurusName);

        return ele;
    }

    /**
     * Create a xml node for the current Keyword
     */
    public Element toElement(String defaultLang, String... langs) {
        defaultLang = to3CharLang(defaultLang);
        List<String> prioritizedList = new ArrayList<String>();
        prioritizedList.add(defaultLang);
        for (String s : langs) {
            s = to3CharLang(s);
            prioritizedList.add(s.toLowerCase());
        }
        TreeSet<String> languages = new TreeSet<String>(new PrioritizedLangComparator(defaultLang, prioritizedList));

        for (String l : values.keySet()) {
            l = to3CharLang(l);
            languages.add(l.toLowerCase());
        }

        Element elKeyword = new Element("keyword");

        Element elId = new Element("id");
        elId.addContent(Integer.toString(this.getId()));
        Element elCode = new Element("code");
        String code = this.getRelativeCode();
        elCode.setText(code);
        // TODO : Add Thesaurus name
        Element elSelected = new Element("selected");
        if (this.isSelected()) {
            elSelected.addContent("true");
        } else {
            elSelected.addContent("false");
        }

        elKeyword.addContent(elId);
        elKeyword.addContent(elCode);

        for (String language : languages) {
            if (!prioritizedList.isEmpty() && !prioritizedList.contains(language)) {
                continue;
            }

            Element elValue = new Element("value");
            elValue.addContent(values.get(language));
            elValue.setAttribute("lang", getIsoLanguageMapper().iso639_2_to_iso639_1(language, language.substring(2)).toUpperCase());
            elKeyword.addContent(elValue);
        }

        Element elDefiniton = new Element("definition");
        elDefiniton.addContent(getDefaultDefinition());
        Element elUri = new Element("uri");
        elUri.addContent(this.getUriCode());

        String thesaurusType = this.getThesaurusKey();
        thesaurusType = thesaurusType.replace('.', '-');
        if (thesaurusType.contains("-"))
            thesaurusType = thesaurusType.split("-")[1];
        elKeyword.setAttribute("type", thesaurusType);
        Element elthesaurus = new Element("thesaurus").setText(this.getThesaurusKey());

        // Geo attribute
        if (this.getCoordEast() != null && this.getCoordWest() != null
            && this.getCoordSouth() != null && this.getCoordNorth() != null) {
            Element elBbox = new Element("geo");
            Element elEast = new Element("east");
            elEast.addContent(this.getCoordEast());
            Element elWest = new Element("west");
            elWest.addContent(this.getCoordWest());
            Element elSouth = new Element("south");
            elSouth.addContent(this.getCoordSouth());
            Element elNorth = new Element("north");
            elNorth.addContent(this.getCoordNorth());
            elBbox.addContent(elEast);
            elBbox.addContent(elWest);
            elBbox.addContent(elSouth);
            elBbox.addContent(elNorth);
            elKeyword.addContent(elBbox);
        }

        elKeyword.addContent(elthesaurus);
        elKeyword.addContent(elDefiniton);
        elKeyword.addContent(elSelected);
        elKeyword.addContent(elUri);
        return elKeyword;
    }

    @JsonIgnore
    public String getDownloadUrl() {
        return downloadUrl;
    }

    public KeywordBean setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    private String to3CharLang(String lang) {
        return getIsoLanguageMapper().iso639_1_to_iso639_2(lang.toLowerCase(), lang.toLowerCase());
    }

    @JsonIgnore
    public IsoLanguagesMapper getIsoLanguageMapper() {
        return isoLanguageMapper;
    }

    /**
     * Set the namespace portion of the code
     *
     * @param namespace the new namespace
     * @return this bean
     */
    public KeywordBean setNamespaceCode(String namespace) {
        if (namespace.endsWith("#")) {
            this.code = namespace + getRelativeCode();
        } else {
            this.code = namespace + "#" + getRelativeCode();
        }
        return this;
    }

    /**
     * Remove the value with the provided language from this keyword
     *
     * @param lang language of value to remove
     * @return this
     */
    public KeywordBean removeValue(String lang) {
        values.remove(lang);
        return this;
    }

    /**
     * Remove the definition with the provided language from this keyword
     *
     * @param lang language of definition to remove
     * @return this
     */
    public KeywordBean removeDefinition(String lang) {
        definitions.remove(lang);
        return this;
    }

    @Override
    public String toString() {
        return getUriCode() + " : " + getDefaultValue();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeywordBean other = (KeywordBean) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		return true;
	}

}
