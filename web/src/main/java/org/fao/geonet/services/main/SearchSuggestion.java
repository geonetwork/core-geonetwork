//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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

package org.fao.geonet.services.main;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Return a list of suggestion for a field. The values could be filtered and only 
 * values contained in field values are returned. 
 * 
 * Two modes are defined. First one is browse the index for a field and return a list of suggestion.
 * For this origin should be set to INDEX_TERM_VALUES. The second mode,
 * using origin RECORDS_FIELD_VALUES, perform a search and extract the field for
 * all docs and return the matching values.
 * 
 * If no origin is set, a RECORDS_FIELD_VALUES mode is done and if no suggestion
 * found, a INDEX_TERM_VALUES mode is done.
 * 
 * 
 * The response body is converted to JSON using search-suggestion.xsl
 * 
 * 
 * OpenSearch suggestion specification:
 * http://www.opensearch.org/Specifications/
 * OpenSearch/Extensions/Suggestions/1.0
 */
public class SearchSuggestion implements Service {
	private static final String RECORDS_FIELD_VALUES = "RECORDS_FIELD_VALUES";

	private static final String INDEX_TERM_VALUES = "INDEX_TERM_VALUES";

	private static final String ORIGIN_ATTR = "origin";

	/**
	 * Max number of term's values to look in the index. For large catalogue
	 * this value should be increased in order to get better results. If this
	 * value is too high, then looking for terms could take more times. The use
	 * of good analyzer should allow to reduce the number of useless values like
	 * (a, the, ...).
	 */
	private Integer _maxNumberOfTerms;

	/**
	 * Minimum frequency for a term value to be proposed in suggestion.
	 */
	private Integer _threshold;

	/**
	 * Default field to search in. any is full-text search field.
	 */
	private static String _defaultSearchField = "any";

	private ServiceConfig _config;

	/**
	 * Set default parameters
	 */
	public void init(String appPath, ServiceConfig config) throws Exception {
		_threshold = Integer.valueOf(config.getValue("threshold"));
		_maxNumberOfTerms = Integer.valueOf(config
				.getValue("max_number_of_terms"));
		_defaultSearchField = config.getValue("default_search_field");
		_config = config;
	}

	/**
	 * Browse the index and return suggestion list.
	 */
	public Element exec(Element params, ServiceContext context)
			throws Exception {
		// The field to search in
		String fieldName = Util.getParam(params, "field", _defaultSearchField);
		// The value to search for
		String searchValue = Util.getParam(params, "q", "");
		// Search index term and/or index records
		String origin = Util.getParam(params, "origin", "");
		// The max number of terms to return - only apply while searching terms
		int maxNumberOfTerms = Util.getParam(params, "maxNumberOfTerms",
				_maxNumberOfTerms);
		// The minimum frequency for a term value to be proposed in suggestion -
		// only apply while searching terms
		int threshold = Util.getParam(params, "threshold", _threshold);

		if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) {
			Log.debug(Geonet.SEARCH_ENGINE, "Autocomplete on field: '"
					+ fieldName + "'" + "\tsearching: '" + searchValue + "'"
					+ "\tthreshold: '" + threshold + "'"
					+ "\tmaxNumberOfTerms: '" + maxNumberOfTerms + "'"
					+ "\tfrom: '" + origin + "'"
					);
		}



		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager sm = gc.getSearchmanager();
		// The response element
		Element suggestionsResponse = new Element("items");
		List<String> listOfSuggestions = new ArrayList<String>();

		// If a field is stored, field values could be retrieved from the index
		// The main advantage is that only values from records visible to the
		// user are returned, because the search filter the results first.
		if (origin.equals("") || origin.equals(RECORDS_FIELD_VALUES)) {
			LuceneSearcher searcher = (LuceneSearcher) sm.newSearcher(
					SearchManager.LUCENE, Geonet.File.SEARCH_LUCENE);

			Collection<String> result = searcher.getSuggestionForFields(context,
					fieldName, searchValue, _config, maxNumberOfTerms, threshold);

			listOfSuggestions.addAll(result);
		}

		// No values found from the index records field value ...
		if (origin.equals(INDEX_TERM_VALUES) 
				|| (listOfSuggestions.size() == 0 && origin.equals(""))) {
			// If a field is not stored, field values could not be retrieved
			// In that cas search the index
			List<SearchManager.TermFrequency> termList = sm.getTermsFequency(
					fieldName, searchValue, maxNumberOfTerms, threshold);
			Collections.sort(termList);
			Collections.reverse(termList);

			Iterator<SearchManager.TermFrequency> iterator = termList
					.iterator();
			while (iterator.hasNext()) {
				SearchManager.TermFrequency freq = (SearchManager.TermFrequency) iterator
						.next();
				listOfSuggestions.add(freq.getTerm());
				// Term frequency not returned :
				// String.valueOf(freq.getFrequency());
			}
		}
		
		if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) {
			Log.debug(Geonet.SEARCH_ENGINE, "  Found: "
					+ listOfSuggestions.size() + " suggestions from " + origin + ".");
		}
		
		
		// TODO : test sorting with accent, numbers, capital letter
		Collections.sort(listOfSuggestions, Collator.getInstance());
		suggestionsResponse.setAttribute(new Attribute(ORIGIN_ATTR,
				origin));
		
		for (String suggestion : listOfSuggestions) {
			Element md = new Element("item");
			// md.setAttribute("term", suggestion.replaceAll("\"",""));
			md.setAttribute("term", suggestion);
			md.setAttribute("freq", "");
			suggestionsResponse.addContent(md);
		}

		return suggestionsResponse;

	}
}