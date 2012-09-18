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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.util.StringUtils;

import java.util.*;

import jeeves.server.context.ServiceContext;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.httpclient.URIException;

/**
 * TODO javadoc.
 *
 */
public class KeywordBean {

	private int id;
	private String code;
	private String coordEast="";
	private String coordWest="";
	private String coordSouth="";
	private String coordNorth="";	
	private String thesaurusKey;	
	private boolean selected;
    private String thesaurusTitle;
	private String thesaurusDate;
	private String downloadUrl;
	private String keywordUrl;

    /**
     * A Hashmap of all the languages available in this keyword
     *
     * Parameters are:  Language, Label
     */
	private final Map<String, String> values = new LinkedHashMap<String,String>();
	private final Map<String, String> definitions = new LinkedHashMap<String,String>();
    private IsoLanguagesMapper isoLanguageMapper;
    private String defaultLang;
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
	 * @deprecated use the setters to construct the bean.  it is a fluent API so can do new KeywordBean().setCode("..").setValue("123","eng")
	 */
	public KeywordBean(int id, String value, String definition, String code, 
	        String coordEast, String coordWest, 
	        String coordSouth, String coordNorth, 
	        String thesaurus, boolean selected, String lang, 
	        String thesaurusTitle, String thesaurusDate, String downloadUrl, 
					String keywordUrl) {
	    this(id,value,definition,code,coordEast, coordWest, coordSouth, coordNorth, thesaurus,
	            selected, lang, thesaurusTitle, thesaurusDate, downloadUrl, keywordUrl, null);
	}
	
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
				String thesaurus, boolean selected, String lang, 
				String thesaurusTitle, String thesaurusDate, String downloadUrl,
				String keywordUrl, IsoLanguagesMapper isoLangMapper) {
		super();
		this.isoLanguageMapper = isoLangMapper;
		this.id = id;
        defaultLang = lang;
		setValue(value, lang);
		setDefinition(definition,lang);
		this.code = code;
		this.coordEast = coordEast;
		this.coordWest = coordWest;
		this.coordSouth = coordSouth;
		this.coordNorth = coordNorth;
		this.thesaurusKey = thesaurus;
		this.selected = selected;
        this.thesaurusTitle = thesaurusTitle;
        this.thesaurusDate = thesaurusDate;
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
	public KeywordBean(String value, String definition, boolean selected, String downloadUrl, String keywordUrl) {
		super();
		defaultLang = calculationDefaultLang();
        setValue(value, defaultLang);
        setDefinition(definition, defaultLang);
		this.selected = selected;
		this.downloadUrl = downloadUrl;
		this.keywordUrl = keywordUrl;
	}

	/**
	 * Create keyword bean with the default IsoLanguageMapper
	 */
    public KeywordBean() {
        this(null);
    }
    
    public KeywordBean(IsoLanguagesMapper isoLangMapper) {
        this.isoLanguageMapper = isoLangMapper;
    }

    private String calculationDefaultLang() {
	    String lang;
	    if(defaultLang != null) {
            lang = defaultLang;
	    } else if (ServiceContext.get() != null) {
	        lang = to3CharLang(ServiceContext.get().getLanguage());
	    } else {
	        lang = Geonet.DEFAULT_LANGUAGE;
	    }
        return lang;
    }

	public KeywordBean setThesaurusInfo(Thesaurus thesaurus) {
	    this.thesaurusKey = thesaurus.getKey();
	    this.thesaurusDate = thesaurus.getDate();
	    this.thesaurusTitle = thesaurus.getTitle();
	    
	    return this;
	}
	
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

	public String getThesaurusTitle() {
        return thesaurusTitle;
    }

    public void setThesaurusTitle(String thesaurusTitle) {
        this.thesaurusTitle = thesaurusTitle;
    }

    public String getThesaurusDate() {
        return thesaurusDate;
    }

    public void setThesaurusDate(String thesaurusDate) {
        this.thesaurusDate = thesaurusDate;
    }

    public KeywordBean setKeywordUrl(String keywordUrl) {
        this.keywordUrl = keywordUrl;
				return this;
    }

    public String getKeywordUrl() {
        return keywordUrl;
    }

    /**
	 * Return default language.  The default language is determined when creating the bean.
	 * In some cases the language is explicitly declared if not then it is the context language
	 * if there is no context available it is Geonet.DEFAULT_LANGUAGE
	 * 
	 * @return the default language
	 */
	public String getDefaultLang() {
        return defaultLang;
    }

    public KeywordBean setDefaultLang(String defaultLang) {
        this.defaultLang = defaultLang;
        return this;
    }

    /**
     * Get the "default" value. The default language is determined when creating the
     * bean.  Since often keyword beans have a single language this will return the only value
     * 
     * @return return default value
     */
	public String getDefaultValue() {
		return values.get(defaultLang);
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
     * @param value the new definition
     * @param lang the language to set, can be 2 or 3 letter language code
     * 
     * @return this keyword bean
     */
    public KeywordBean setValue(String value, String lang) {
        if(defaultLang == null) {
            defaultLang = to3CharLang(lang);
        }
        values.put(to3CharLang(lang), value);
        return this;
    }
	/**
	 * Get the "default" definition. The default language is determined when creating the
	 * bean.  Since often keyword beans have a single language this will return the only definition
	 * 
	 * @return return default definition
	 */
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
     * @param lang the language to set
     * 
     * @return this keyword
     */
    public KeywordBean setDefinition(String definition, String lang) {
        if(defaultLang == null) {
            defaultLang = to3CharLang(lang);
        }
        definitions.put(to3CharLang(lang), definition);
        return this;
    }

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
	public String getCode() {
		return code;
	}

    /**
     * TODO javadoc.
     *
     * @return
     */
	public String getRelativeCode() {
	    if(code == null) {
	        return "";
	    } else if (code.contains("#"))
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
	      if(code == null) {
	            return "#";
	        } else if (code.contains("#"))
			return code.split("#")[0] + "#";
		else
			return "#";
	}

	public KeywordBean setCode(String code) {
		this.code = code;
		return this;
	}

	private String asString(String s) {
	    return s == null?"":s;
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
     *
     * @return
     */
	public String getType() {
		int tmpDotIndex = thesaurusKey.indexOf('.');
		return thesaurusKey.substring(tmpDotIndex+1, thesaurusKey.indexOf(".",tmpDotIndex+1));
	}
	
	public String getThesaurusType() {
		return org.apache.commons.lang.StringUtils.substringBefore(thesaurusKey, ".");
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
				an.setText(getDefaultValue());
				an.setAttribute("href", URIUtil.encodeQuery(keywordUrl+getCode()), NS_XLINK);
				el.addContent(an);
			} catch (URIException e) { // what to do here? Just add the value
				cs.setText(getDefaultValue());
				el.addContent(cs); 
			}
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
		
		Element type = null;
		Element thesaurusName = null;
		
		for (KeywordBean kb : kbList) {
			Element keyword = new Element("keyword", NS_GMD);
			if (kb.getCode() != null && kb.getCode().length() != 0) {
				try {
					an.setText(kb.getDefaultValue());
					an.setAttribute("href", URIUtil.encodeQuery(kb.keywordUrl+kb.getCode()), NS_XLINK);
					keyword.addContent((Content) an.clone());
				} catch (URIException e) {
					cs.setText(kb.getDefaultValue());
					keyword.addContent((Content) cs.clone());
				}
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
		Element id = createIdentifier("geonetwork.thesaurus."+kb.thesaurusKey,kb.downloadUrl);

		citation.addContent(0,title);
		citation.addContent(1,date);
		citation.addContent(2,id);
		thesaurusName.addContent(citation);
		
		return thesaurusName;
	}

	/**
     * Create a xml node for the current Keyword
     *
     * @return
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

        for(String l : values.keySet()) {
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
            if(!prioritizedList.isEmpty() && ! prioritizedList.contains(language)) {
                continue;
            }

            Element elValue = new Element("value");
            elValue.addContent(values.get(language));
            elValue.setAttribute("lang", language.toUpperCase());
            elKeyword.addContent(elValue);
        }

        Element elDefiniton = new Element("definition");
        elDefiniton.addContent(getDefaultDefinition());
        Element elUri = new Element("uri");
        elUri.addContent(this.getCode());

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

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public KeywordBean setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
				return this;
    }

    public void setIsoLanguageMapper(IsoLanguagesMapper isoLanguageMapper) {
        this.isoLanguageMapper = isoLanguageMapper;
    }

    private String to3CharLang(String lang) {
        return getIsoLanguageMapper().iso639_1_to_iso639_2(lang.toLowerCase(), lang.toLowerCase());
    }

    public IsoLanguagesMapper getIsoLanguageMapper() {
        return isoLanguageMapper == null? IsoLanguagesMapper.getInstance() : isoLanguageMapper;
    }

    /**
     * Set the namespace portion of the code
     * 
     * @param namespace the new namespace
     * 
     * @return this bean
     */
    public KeywordBean setNamespaceCode(String namespace) {
        if (namespace.endsWith("#")) {
            this.code = namespace + getRelativeCode();
        } else {
            this.code = namespace+"#"+getRelativeCode();
        }
        return this;
    }
    
    /**
     * Set the id/relative portion of the code
     * 
     * @param newCode the new relative code
     * 
     * @return this bean
     */
    public KeywordBean setRelativeCode(String newCode) {
        this.code = getNameSpaceCode()+newCode;
        return this;
    }

}
