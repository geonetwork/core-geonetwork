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


package org.fao.geonet.kernel.search;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Element;
import org.openrdf.model.Value;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.query.QueryResultsTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * Select entries from SKOS thesauri.
 *
 */
public class KeywordsSearcher {
	private ThesaurusManager _thesaurusManager;
	private String _query;
    private String _lang;
    private List<KeywordBean> _results = new ArrayList<KeywordBean>();

    private int _maxResults = 100000;

    /**
     * TODO javadoc.
     *
     * @param tm thesaurusmanager
     */
	public KeywordsSearcher(ThesaurusManager tm) {
		_thesaurusManager = tm;
	}

    /**
     * TODO javadoc.
     *
     * @param id id
     * @param sThesaurusName thesaurus name
     * @param lang language
     * @return keywordbean
     * @throws Exception hmm
     */
	public KeywordBean searchById(String id, String sThesaurusName, String lang) throws Exception {
        //System.out.println("KeywordsSearcher searchById");
		_query = "SELECT prefLab, note, id, lowc, uppc "
			+ " FROM {id} rdf:type {skos:Concept}; "
			+ " skos:prefLabel {prefLab};"
			+ " [skos:scopeNote {note} WHERE lang(note) LIKE \""+lang+"*\"]; "
			+ " [gml:BoundedBy {} gml:lowerCorner {lowc}]; "
			+ " [gml:BoundedBy {} gml:upperCorner {uppc}] "
			+ " WHERE lang(prefLab) LIKE \""+lang+"*\" "
			+ " AND id LIKE \""+id+"\" "
			+ " IGNORE CASE "
			+ " USING NAMESPACE skos=<http://www.w3.org/2004/02/skos/core#>, gml=<http://www.opengis.net/gml#> ";

			Thesaurus thesaurus = _thesaurusManager.getThesaurusByName(sThesaurusName);

			// Perform request
			QueryResultsTable resultsTable = thesaurus.performRequest(_query);
			int rowCount = resultsTable.getRowCount();
			int idKeyword = 0;

			if (rowCount == 0){
				return null;
			}
            else{
				// MUST be one because search by ID

				// preflab
				Value value = resultsTable.getValue(0, 0);
				String sValue = "";
				if (value != null) {
					sValue = value.toString();
				}

//				 uri (= id in RDF file != id in list)
				Value uri = resultsTable.getValue(0, 2);
				String sUri = "";
				if (uri != null) {
					sUri = uri.toString();
				}

				KeywordBean kb = new KeywordBean(idKeyword, sValue, "", sUri, "", "", "", "", sThesaurusName, false, _lang, thesaurus.getTitle(), thesaurus.getDate(), thesaurus.getVersion(), thesaurus.getDownloadUrl(), thesaurus.getKeywordUrl());
				idKeyword++;

				return kb;
			}

	}

    /**
     * TODO javadoc.
     *
     * @param srvContext servicecontext
     * @param params params
     * @throws Exception hmm
     */
	public void search(ServiceContext srvContext, Element params) throws Exception {
        //System.out.println("KeywordsSearcher search");
		// Get params from request and set default
		String sKeyword = Util.getParam(params, "pKeyword");
		
		// Get max results number or set default one.
		_maxResults = Util.getParam(params, "maxResults", _maxResults);

		// Type of search
		int pTypeSearch;
        // if param pTypeSearch not here
		if (params.getChild("pTypeSearch") != null){
            pTypeSearch = Util.getParamAsInt(params, "pTypeSearch");

            // Thesaurus to search in
            List<Element> listThesauri = new ArrayList<Element>();
            // Type of thesaurus to search in
            String pTypeThesaurus = null;
            // if param pType not here
            if (params.getChild("pType") != null) {
                pTypeThesaurus = Util.getParam(params, "pType");
            }
            // whether to search in all thesauri
            boolean bAll = true;

            // if param pThesauri not here
		    if (params.getChild("pThesauri") != null){
			    listThesauri = params.getChildren("pThesauri");
			    bAll = false;

			    // Check empty child and remove empty ones.
			    for (Iterator<Element> it = listThesauri.iterator(); it.hasNext();) {
				    Element th = it.next();
				    if ("".equals(th.getTextTrim())) {
					    it.remove();
                    }
			    }
			
			    if (listThesauri.size() == 0) {
				    bAll = true;
                }
		    }

		    //	If no thesaurus search in all.
		    if (bAll){
			    ConcurrentHashMap<String, Thesaurus> tt = _thesaurusManager.getThesauriMap();

			    Enumeration<String> e = tt.keys();
			    boolean add = true;
                // Fill the list with all thesauri available
		        while (e.hasMoreElements()) {
		    	    Thesaurus thesaurus = tt.get(e.nextElement());
		    	    if (pTypeThesaurus != null){
                        add = thesaurus.getDname().equals(pTypeThesaurus);
		    	    }

		    	    if (add){
		    		    Element el = new Element("pThesauri");
			    	    el.addContent(thesaurus.getKey());
			    	    listThesauri.add(el);
		    	    }
		        }
		    }
            // Keyword to look for
            if (!sKeyword.equals("")) {
                String lang = IsoLanguagesMapper.getInstance().iso639_2_to_iso639_1(srvContext.getLanguage());
                createQuery(lang, sKeyword, pTypeSearch);
            }
            search(listThesauri);
            List<KeywordBean> resultsWithLanguage = _results;

            // repeat search for language = "#default"
            // #default doesn't work in sesame search -- replaced it by 00
            if (!sKeyword.equals("")) {
                String lang = "00";
                createQuery(lang, sKeyword, pTypeSearch);
            }
            search(listThesauri);
            List<KeywordBean> resultsWithoutLanguage = _results;

            // end results are results without language except for those that also do have a language
            List<KeywordBean> endResults  = resultsWithLanguage;

            for(KeywordBean keywordBeanWithoutLanguage : resultsWithoutLanguage) {
                boolean keywordWithLanguagefound = false;
                for(KeywordBean keywordBeanWithLanguage: resultsWithLanguage) {
                    // keyword found with language : no need to add default
                    if(keywordBeanWithoutLanguage.getValue().equals(keywordBeanWithLanguage.getValue())) {
                        keywordWithLanguagefound = true;
                        break;
                    }
                }
                // keyword with language not found : use default
                if(!keywordWithLanguagefound){
                    endResults.add(keywordBeanWithoutLanguage);
                }
            }
             _results = endResults;

		}
	}

    /**
     * Searches. Global _query must have been created before calling this.
     *
     * @param listThesauri list of thesauri
     * @throws MalformedQueryException hmm
     * @throws IOException hmm
     * @throws QueryEvaluationException hmm
     * @throws AccessDeniedException hmm
     */
    private void search(List<Element> listThesauri) throws MalformedQueryException, IOException, QueryEvaluationException, AccessDeniedException {
        // For each thesaurus, search for keywords in _results
        _results = new ArrayList<KeywordBean>();
        int idKeyword = 0;

        // Search in all Thesaurus if none selected
        for (Element thesaurusName : listThesauri) {
            Thesaurus thesaurus = _thesaurusManager.getThesaurusByName(thesaurusName.getTextTrim());

            // Perform request
            QueryResultsTable resultsTable = thesaurus.performRequest(_query);

            int rowCount = resultsTable.getRowCount();

            for (int row = 0; row < rowCount; row++) {
                // preflab
                Value value = resultsTable.getValue(row, 0);
                String sValue = "";
                if (value != null) {
                    sValue = value.toString();
                }
                // definition
                Value definition = resultsTable.getValue(row, 1);
                String sDefinition = "";
                if (definition != null) {
                    sDefinition = definition.toString();
                }
                // uri (= id in RDF file != id in list)
                Value uri = resultsTable.getValue(row, 2);
                String sUri = "";
                if (uri != null) {
                    sUri = uri.toString();
                }

                Value lowCorner = resultsTable.getValue(row, 3);
                Value upperCorner = resultsTable.getValue(row, 4);

                String sUpperCorner;
                String sLowCorner;

                String sEast = "";
                String sSouth = "";
                String sWest = "";
                String sNorth = "";

                // lowcorner
                if (lowCorner != null) {
                    sLowCorner = lowCorner.toString();
                    sWest = sLowCorner.substring(0, sLowCorner.indexOf(' ')).trim();
                    sSouth = sLowCorner.substring(sLowCorner.indexOf(' ')).trim();
                }

                // uppercorner
                if (upperCorner != null) {
                    sUpperCorner = upperCorner.toString();
                    sEast = sUpperCorner.substring(0, sUpperCorner.indexOf(' ')).trim();
                    sNorth = sUpperCorner.substring(sUpperCorner.indexOf(' ')).trim();
                }

                KeywordBean kb = new KeywordBean(idKeyword, sValue, sDefinition, sUri, sEast, sWest, sSouth, sNorth, thesaurusName.getTextTrim(),
                        false, _lang, thesaurus.getTitle(), thesaurus.getDate(), thesaurus.getVersion(), thesaurus.getDownloadUrl(), thesaurus.getKeywordUrl());
                _results.add(kb);
                idKeyword++;
            }
        }
        //System.out.println("KeywordsSearcher search found # " + _results.size() + " results");
    }
    /**
     * Creates query for keyword in a language.
     *
     * @param _lang language
     * @param sKeyword keyword
     * @param pTypeSearch type of search
     */
    private void createQuery(String _lang, String sKeyword, int pTypeSearch){
        // FIXME : Where to search ? only on term having GUI language or in all ?
        // Should be
        // - look for a term in all language
        // - get prefLab in GUI lang
        // This will cause multilingual metadata search quite complex !!
        // Quid Lucene and thesaurus ?
        _query = "SELECT prefLab, note, id, lowc, uppc "
            + " FROM {id} rdf:type {skos:Concept}; "
            + " skos:prefLabel {prefLab};"
            + " [skos:scopeNote {note} WHERE lang(note) LIKE \""+_lang+"*\"]; "
            + " [gml:BoundedBy {} gml:lowerCorner {lowc}]; "
            + " [gml:BoundedBy {} gml:upperCorner {uppc}] "
            + " WHERE lang(prefLab) LIKE \""+_lang+"*\""
            + " AND prefLab LIKE ";

        switch (pTypeSearch) {
        case 0: // Start with
            _query += "\""+ sKeyword+ "*\" ";
            break;
        case 1: // contains
            _query += "\"*"+ sKeyword+ "*\" ";
            break;
        case 2: // exact match
            _query += "\""+ sKeyword+ "\" ";
            break;
        default:
            break;
        }
        _query 	+= " IGNORE CASE "
                + " LIMIT "+ _maxResults
                + " USING NAMESPACE skos=<http://www.w3.org/2004/02/skos/core#>, gml=<http://www.opengis.net/gml#> ";
    }

    /**
     * TODO javadoc.
     *
     * @param srvContext servicecontext
     * @param params parameters
     * @param request request
     * @throws Exception hmm
     */
	public void searchBN(ServiceContext srvContext, Element params, String request) throws Exception {
        //System.out.println("KeywordsSearcher searchBN");
		// TODO : Add geonetinfo elements.
		String id = Util.getParam(params, "id");
		String sThesaurusName = Util.getParam(params, "thesaurus");

        String _lang = IsoLanguagesMapper.getInstance().iso639_2_to_iso639_1(srvContext.getLanguage());

		searchBN(id, sThesaurusName, request, _lang);
	}

    /**
     * TODO javadoc.
     *
     * @param id id
     * @param sThesaurusName thesaurus name
     * @param request request
     * @param _lang language
     * @throws Exception hmm
     */
	public void searchBN(String id, String sThesaurusName, String request, String _lang) throws Exception {
        //System.out.println("KeywordsSearcher searchBN 2");

		Thesaurus thesaurus = _thesaurusManager.getThesaurusByName(sThesaurusName);
		_results = new ArrayList<KeywordBean>();
		String _query = "SELECT prefLab, note, id "
			+ " from {id} rdf:type {skos:Concept};"
			+ " skos:prefLabel {prefLab};"
			+ " [skos:"+request+" {b}];"
			+ " [skos:scopeNote {note} WHERE lang(note) LIKE \""+_lang+"*\"] "
			+ " WHERE lang(prefLab) LIKE \""+_lang+"*\""
			+ " AND b LIKE \"*"+id+"\""
			+ " IGNORE CASE "
			+ " USING NAMESPACE skos=<http://www.w3.org/2004/02/skos/core#>, gml=<http://www.opengis.net/gml#> ";

		//	Perform request
		QueryResultsTable resultsTable = thesaurus.performRequest(_query);

		int rowCount = resultsTable.getRowCount();
		int idKeyword = 0;

		for (int row = 0; row < rowCount; row++) {

			// preflab
			Value value = resultsTable.getValue(row, 0);
			String sValue = "";
			if (value != null) {
				sValue = value.toString();
			}

//			 uri (= id in RDF file != id in list)
			Value uri = resultsTable.getValue(row, 2);
			String sUri = "";
			if (uri != null) {
				sUri = uri.toString();
			}

			KeywordBean kb = new KeywordBean(idKeyword, sValue, "", sUri, "", "", "", "", sThesaurusName, false, _lang, thesaurus.getTitle(), thesaurus.getDate(), thesaurus.getVersion(), thesaurus.getDownloadUrl(), thesaurus.getKeywordUrl());
			_results.add(kb);
			idKeyword++;
		}
	}

    /**
     *
     * @return size of results
     */
	public int getNbResults() {
		return _results.size();
	}

    /**
     * TODO javadoc.
     *
     * @param tri direction
     */
	public void sortResults(String tri) {
        if ("label".equals(tri)) {
			// sort by label
			Collections.sort(_results, new Comparator() {
				public int compare(final Object o1, final Object o2) {
					final KeywordBean kw1 = (KeywordBean) o1;
					final KeywordBean kw2 = (KeywordBean) o2;
					return kw1.getValue().compareToIgnoreCase(kw2.getValue());
				}
			});
		}
		if ("definition".equals(tri)) {
			// sort by def
			Collections.sort(_results, new Comparator() {
				public int compare(final Object o1, final Object o2) {
					final KeywordBean kw1 = (KeywordBean) o1;
					final KeywordBean kw2 = (KeywordBean) o2;
					return kw1.getDefinition().compareToIgnoreCase(
							kw2.getDefinition());
				}
			});
		}
	}

    /**
     * TODO javadoc.
     *
     * @return element
     * @throws Exception hmm
     */
	public Element getResults() throws Exception {

		Element elDescKeys = new Element("descKeys");

		int nbResults = (this.getNbResults()<=_maxResults?this.getNbResults():_maxResults);

		//for (int i = from; i <= to; i++) {
		for (int i = 0; i <= nbResults - 1; i++) {
			KeywordBean kb = _results.get(i);
            toRawElement(elDescKeys, kb);
		}

		return elDescKeys;
	}

    public static Element toRawElement(Element elDescKeys, KeywordBean kb) {
        Element elKeyword = new Element("keyword");
        Element elSelected = new Element("selected");
        // TODO : Add Thesaurus name

        if (kb.isSelected()) {
        	elSelected.addContent("true");
        } else {
        	elSelected.addContent("false");
        }
        Element elId = new Element("id");
        elId.addContent(Integer.toString(kb.getId()));
        Element elValue = new Element("value");
        elValue.addContent(kb.getValue());
        Element elDefiniton = new Element("definition");
        elDefiniton.addContent(kb.getDefinition());
        Element elTh = new Element("thesaurus");
        elTh.addContent(kb.getThesaurus());
        Element elUri = new Element("uri");
        elUri.addContent(kb.getCode());
        
        addBbox(kb, elKeyword);
        
        elKeyword.addContent(elSelected);
        elKeyword.addContent(elId);
        elKeyword.addContent(elValue);
        elKeyword.addContent(elDefiniton);
        elKeyword.addContent(elTh);
        elKeyword.addContent(elUri);
        elDescKeys.addContent(elKeyword);
        
        return elDescKeys;
    }

    /**
     * TODO javadoc.
     *
     * @param params parameters
     */
	public void selectUnselectKeywords(Element params) {
		List listIdKeywordsSelected = params.getChildren("pIdKeyword");
        for (Object aListIdKeywordsSelected : listIdKeywordsSelected) {
            Element el = (Element) aListIdKeywordsSelected;
            int keywordId = Integer.decode(el.getTextTrim());
            for (KeywordBean _result : _results) {
                if (( _result).getId() == keywordId) {
                    ( _result)
                            .setSelected(!( _result)
                                    .isSelected());
                }
            }
        }
	}

	/**
     * TODO javadoc.
     *
	 * @return an element describing the list of selected keywords
	 */
	public Element getSelectedKeywords() {
		Element elDescKeys = new Element("descKeys");
		int nbSelectedKeywords = 0;
		for (int i = 0; i < this.getNbResults(); i++) {
			KeywordBean kb = _results.get(i);
			if (kb.isSelected()) {
				Element elKeyword = new Element("keyword");
				// keyword type
				String thesaurusType = kb.getThesaurus();
				thesaurusType = thesaurusType.replace('.', '-');
				thesaurusType =  thesaurusType.split("-")[1];
				elKeyword.setAttribute("type", thesaurusType);
				Element elValue = new Element("value");
				elValue.addContent(kb.getValue());
				Element elCode = new Element("code");
				String code=kb.getRelativeCode();
				//code = code.split("#")[1];
				elCode.addContent(code);
				addBbox(kb, elKeyword);
				elKeyword.addContent(elCode);
				elKeyword.addContent(elValue);
				elDescKeys.addContent(elKeyword);
				nbSelectedKeywords++;
			}
		}
		Element elNbTot = new Element("nbtot");
		elNbTot.addContent(Integer.toString(nbSelectedKeywords));
		elDescKeys.addContent(elNbTot);

		return elDescKeys;
	}

	/**
	 * Adds bounding box of keyword if one available.
	 * 
	 * @param kb	The keyword to analyze.
	 * @param elKeyword	The XML fragment to update.
	 */
	private static void addBbox(KeywordBean kb, Element elKeyword) {
		if (kb.getCoordEast() != null && kb.getCoordWest() != null
				&& kb.getCoordSouth() != null
				&& kb.getCoordNorth() != null && !kb.getCoordEast().equals("") 
				&& !kb.getCoordWest().equals("")
				&& !kb.getCoordSouth().equals("")
				&& !kb.getCoordNorth().equals("")) {
			Element elBbox = new Element("geo");
			Element elEast = new Element("east");
			elEast.addContent(kb.getCoordEast());
			Element elWest = new Element("west");
			elWest.addContent(kb.getCoordWest());
			Element elSouth = new Element("south");
			elSouth.addContent(kb.getCoordSouth());
			Element elNorth = new Element("north");
			elNorth.addContent(kb.getCoordNorth());
			elBbox.addContent(elEast);
			elBbox.addContent(elWest);
			elBbox.addContent(elSouth);
			elBbox.addContent(elNorth);
			elKeyword.addContent(elBbox);
		}
	}

    /**
     * TODO javadoc.
     *
     * @return list of keywordbeans
     */
	public List<KeywordBean> getSelectedKeywordsInList() {
		List<KeywordBean> keywords = new ArrayList<KeywordBean>();
		for (int i = 0; i < this.getNbResults(); i++) {
			KeywordBean kb = _results.get(i);
			if (kb.isSelected()) {
					keywords.add(kb);
				}
			}
		return keywords;
	}

    /**
     * TODO javadoc.
     *
     * @param id id
     * @return keywordbean
     */
	public KeywordBean existsResult(String id) {
		KeywordBean keyword = null;
		for (int i = 0; i < this.getNbResults(); i++) {
			KeywordBean kb = _results.get(i);
			if (kb.getId() == Integer.parseInt(id)) {
					keyword = kb;
					break;
				}
			}
		return keyword;
	}

}
