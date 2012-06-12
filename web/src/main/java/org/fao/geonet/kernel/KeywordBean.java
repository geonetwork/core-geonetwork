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

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.httpclient.URIException;

/**
 * TODO javadoc.
 *
 */
public class KeywordBean {
	private int id;
	private String value;
	private String lang;
	private String definition;
	private String code;
	private String coordEast;
	private String coordWest;
	private String coordSouth;
	private String coordNorth;	
	private String thesaurus;	
	private boolean selected;
    private String thesaurusTitle;
	private String thesaurusDate;
	private String downloadUrl;
	private String keywordUrl;

	private static final Namespace NS_GMD = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
	private static final Namespace NS_GCO = Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
	private static final Namespace NS_GMX = Namespace.getNamespace("gmx", "http://www.isotc211.org/2005/gmx");
	private static final Namespace NS_XLINK = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
	
	/**
     * TODO javadoc.
     *
	 * @param id
	 * @param value
	 * @param definition
	 * @param code
	 * @param coordEast
	 * @param coordWest
	 * @param coordSouth
	 * @param coordNorth
	 * @param thesaurus
	 * @param selected
   * @param lang
   * @param thesaurusTitle
   * @param thesaurusDate
	 * @param downloadUrl
	 * @param keywordUrl
	 */
	public KeywordBean(int id, String value, String definition, String code, 
				String coordEast, String coordWest, 
				String coordSouth, String coordNorth, 
				String thesaurus, boolean selected, String lang, String thesaurusTitle, String thesaurusDate, String downloadUrl, String keywordUrl) {
		super();
		this.id = id;
		this.value = value;
		this.lang = lang;
		this.definition = definition;
		this.code = code;
		this.coordEast = coordEast;
		this.coordWest = coordWest;
		this.coordSouth = coordSouth;
		this.coordNorth = coordNorth;
		this.thesaurus = thesaurus;
		this.selected = selected;
        this.thesaurusTitle = thesaurusTitle;
        this.thesaurusDate = thesaurusDate;
		this.downloadUrl = downloadUrl;
		this.keywordUrl = keywordUrl;
	}

	/**
     * TODO javadoc.
     *
	 * @param id
	 * @param value
	 * @param definition
	 * @param thesaurus
	 * @param selected
	 * @param downloadUrl
	 * @param keywordUrl
	 */
	public KeywordBean(int id, String value, String definition, String thesaurus, boolean selected, String downloadUrl, String keywordUrl) {
		super();
		this.id = id;
		this.value = value;
		this.definition = definition;
		this.thesaurus = thesaurus;
		this.selected = selected;
		this.downloadUrl = downloadUrl;
		this.keywordUrl = keywordUrl;
	}
	
	/**
     * TODO javadoc.
     *
	 * @param value
	 * @param definition
	 * @param thesaurus
	 * @param selected
	 * @param downloadUrl
	 * @param keywordUrl
	 */
	public KeywordBean(String value, String definition, String thesaurus, boolean selected, String downloadUrl, String keywordUrl) {
		super();
		this.value = value;
		this.definition = definition;
		this.thesaurus = thesaurus;
		this.selected = selected;
		this.downloadUrl = downloadUrl;
		this.keywordUrl = keywordUrl;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getThesaurus() {
		return thesaurus;
	}

	public void setThesaurus(String thesaurus) {
		this.thesaurus = thesaurus;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the URI of the keyword concept.
	 */
	public String getCode() {
		return code;
	}

    /**
     * TODO javadoc.
     *
     * @return
     */
	public String getRelativeCode() {
		if (code.contains("#"))
		    return code.split("#")[1];
		else
			return code;
	}

    /**
     * TODO javadoc.
     *
     * @return
     */
	public String getNameSpaceCode() {
		if (code.contains("#"))
			return code.split("#")[0] + "#";
		else
			return "";
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCoordEast() {
		return coordEast;
	}

	public void setCoordEast(String coordEast) {
		this.coordEast = coordEast;
	}

	public String getCoordNorth() {
		return coordNorth;
	}

	public void setCoordNorth(String coordNorth) {
		this.coordNorth = coordNorth;
	}

	public String getCoordWest() {
		return coordWest;
	}

	public void setCoordWest(String coordWest) {
		this.coordWest = coordWest;
	}

	public String getCoordSouth() {
		return coordSouth;
	}

	public void setCoordSouth(String coordSouth) {
		this.coordSouth = coordSouth;
	}

    /**
     * TODO javadoc.
     *
     * @return
     */
	public String getType() {
		int tmpDotIndex = thesaurus.indexOf('.');
		return thesaurus.substring(tmpDotIndex+1, thesaurus.indexOf(".",tmpDotIndex+1));
	}
	
	public String getThesaurusType() {
		return org.apache.commons.lang.StringUtils.substringBefore(thesaurus, ".");
	}
	
	/**
	 * Transforms a KeywordBean object into its iso19139 representation.
	 * 
	 * <pre>
	 * 		<gmd:keyword xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="">
	 *	  		<gmx:Anchor xlink:href="link_to_keyword_generator">A KEYWORD GENERATED BY XLINK SERVICE</gco:Anchor>
	 *		</gmd:keyword>
	 *  	<gmd:type>
	 *  		<gmd:MD_KeywordTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode" codeListValue="TYPE"/>
	 *  	</gmd:type>
	 *  	<gmd:thesaurusName>
	 *  		<gmd:CI_Citation>
	 *  			<gmd:title>
	 *  				<gco:CharacterString>THESAURUS NAME</gco:CharacterString>
	 *  			</gmd:title>
	 *  			<gmd:date gco:nilReason="unknown"/>
	 *				<gmd:identifier>
	 *          <gmd:MD_Identifier>
	 *						<gmd:code>
	 *							<gmx:Anchor xlink:href="http://localhost:8080/geonetwork/srv/eng/metadata.show?uuid=bc44a748-f1a1-4775-9395-a4a6d8bb8df6">register.theme.bc44a748-f1a1-4775-9395-a4a6d8bb8df6</gmx:Anchor>
	 *						</gmd:code>
	 *          </gmd:MD_Identifier>
	 *				</gmd:identifier>
	 *  		</gmd:CI_Citation>
	 *  	</gmd:thesaurusName>
	 * </pre>
	 * 
	 * @return an iso19139 representation of the keyword
	 */
	public Element getIso19139 () {
		Element ele = new Element("MD_Keywords", NS_GMD);
		Element el = new Element("keyword", NS_GMD);
		Element an = new Element("Anchor", NS_GMX);
		Element cs = new Element("CharacterString", NS_GCO);
		if (getCode() != null && getCode().length() != 0) {
			try {
				an.setText(getValue());
				an.setAttribute("href", URIUtil.encodeQuery(keywordUrl+getCode()), NS_XLINK);
				el.addContent(an);
			} catch (URIException e) { // what to do here? Just add the value
				cs.setText(getValue());
				el.addContent(cs); 
			}
		} else {
			cs.setText(getValue());
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
	 * Transforms a list of KeywordBean object into its iso19139 representation.
	 *  
	 *  <pre>
	 *  <gmd:MD_Keywords>
	 *  	<gmd:keyword>
	 *  		<gmx:Anchor xlink:href="link_to_keyword_generator">Keyword 1</gmx:Anchor>
	 *  	</gmd:keyword>
	 * 		<gmd:keyword>
	 *  		<gmx:Anchor xlink:href="link_to_keyword_generator">Keyword 2</gmx:Anchor>
	 *  	</gmd:keyword>
	 *  	<gmd:type>
	 *  		<gmd:MD_KeywordTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode" codeListValue="TYPE"/>
	 *  	</gmd:type>
	 *  	<gmd:thesaurusName>
	 *  		<gmd:CI_Citation id="geonetwork.thesaurus.register.theme.bc44a748-f1a1-4775-9395-a4a6d8bb8df6">
	 *  			<gmd:title>
	 *  				<gco:CharacterString>THESAURUS NAME</gco:CharacterString>
	 *  			</gmd:title>
	 *  			<gmd:date gco:nilReason="unknown"/>
	 *				<gmd:identifier>
	 *          <gmd:MD_Identifier>
	 *						<gmd:code>
	 *							<gmx:Anchor xlink:href="http://localhost:8080/geonetwork/srv/eng/metadata.show?uuid=bc44a748-f1a1-4775-9395-a4a6d8bb8df6">register.theme.bc44a748-f1a1-4775-9395-a4a6d8bb8df6</gmx:Anchor>
	 *						</gmd:code>
	 *          </gmd:MD_Identifier>
	 *				</gmd:identifier>
	 *  		</gmd:CI_Citation>
	 *  	</gmd:thesaurusName>
	 *  </gmd:MD_Keywords>
	 *  </pre>
	 *       
	 *  
	 * @param kbList
	 * @return a complex iso19139 representation of the keyword
	 */
	public static Element getComplexIso19139Elt(List<KeywordBean> kbList) {
		Element root = new Element("MD_Keywords", NS_GMD);
		
		Element cs = new Element("CharacterString", NS_GCO);
		Element an = new Element("Anchor", NS_GMX);
		
		List<Element> keywords = new ArrayList<Element>();
		
		String thName = "";
		Element type = null;
		Element thesaurusName = null;
		
		for (KeywordBean kb : kbList) {
			Element keyword = new Element("keyword", NS_GMD);
			if (kb.getCode() != null && kb.getCode().length() != 0) {
				try {
					an.setText(kb.getValue());
					an.setAttribute("href", URIUtil.encodeQuery(kb.keywordUrl+kb.getCode()), NS_XLINK);
					keyword.addContent((Content) an.clone());
				} catch (URIException e) {
					cs.setText(kb.getValue());
					keyword.addContent((Content) cs.clone());
				}
			} else {
				cs.setText(kb.getValue());
				keyword.addContent((Content) cs.clone());
			}
			keywords.add((Element) keyword.detach());
			thName = kb.getThesaurus();
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
	 * 
	 * @param kb
	 * @return
	 */
	private static Element createKeywordTypeElt(KeywordBean kb) {
		Element type = new Element("type", NS_GMD);
		Element keywordTypeCode = new Element("MD_KeywordTypeCode", NS_GMD);
		keywordTypeCode.setAttribute("codeList", "http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode");
		keywordTypeCode.setAttribute("codeListValue", kb.getType());
		type.addContent(keywordTypeCode);
		
		return type;
	}

	/**
	 * Create an identifier/MD_Identifier that describes the thesaurus
	 * 
	 * @return
	 */
	private static Element createIdentifier(String authority, String downloadUrl) {
		Element result = new Element("identifier", NS_GMD);
		Element ident  = new Element("MD_Identifier", NS_GMD);
		Element code = new Element("code", NS_GMD);
		Element gmxAnchor = new Element("Anchor", NS_GMX).setText(authority);
		gmxAnchor.setAttribute("href", downloadUrl, NS_XLINK);

		code.addContent(gmxAnchor);
		ident.addContent(code);
		result.addContent(ident);
		return result;
	}

	/**
	 * Creates thesaurus name element.
	 * 
	 * @param kb
	 * @return
	 */
	private static Element createThesaurusNameElt (KeywordBean kb) {
		Element thesaurusName = new Element("thesaurusName", NS_GMD);
		Element citation = new Element("CI_Citation", NS_GMD);
		Element title = new Element("title", NS_GMD);
		Element cs = new Element("CharacterString", NS_GCO);
		Element date = new Element("date", NS_GMD);

        cs.setText(kb.thesaurusTitle);

        if (StringUtils.hasLength(kb.thesaurusDate)) {
            Element ciDateEl = new Element("CI_Date", NS_GMD);
            Element ciDateDateEl = new Element("date", NS_GMD);
            Element ciDateDateGcoDateEl = new Element("Date", NS_GCO);

            ciDateDateGcoDateEl.setText(kb.thesaurusDate);

            Element ciDateDatetypeEl = new Element("dateType", NS_GMD);
            Element ciDateDatetypeCodeEl = new Element("CI_DateTypeCode", NS_GMD);
            ciDateDatetypeCodeEl.setAttribute("codeList","http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_DateTypeCode");
            ciDateDatetypeCodeEl.setAttribute("codeListValue", "publication");

            ciDateDatetypeEl.addContent(ciDateDatetypeCodeEl);
            ciDateDateEl.addContent(ciDateDateGcoDateEl);
            ciDateEl.addContent(0, ciDateDateEl);
            ciDateEl.addContent(1, ciDateDatetypeEl);
            date.addContent(ciDateEl);

        } else {
            date.setAttribute("nilReason", "unknown",NS_GCO);
        }


		title.addContent((Content) cs.clone());
		Element id = createIdentifier("geonetwork.thesaurus."+kb.thesaurus,kb.downloadUrl);

		citation.addContent(0,title);
		citation.addContent(1,date);
		citation.addContent(2,id);
		thesaurusName.addContent(citation);
		
		return thesaurusName;
	}
}
