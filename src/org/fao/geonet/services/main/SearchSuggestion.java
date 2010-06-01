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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.SearchManager;
import org.jdom.Element;

/**
 * Browse the index for a field and return a list of suggestion. Suggested terms
 * <b>contains</b> the query string.
 * 
 * The response body is converted to JSON using search-suggestion.xsl
 * 
 * To be improved : does not take care of privileges. Suggested terms could come
 * from private metadata records.
 * 
 * OpenSearch suggestion specification:
 * http://www.opensearch.org/Specifications/
 * OpenSearch/Extensions/Suggestions/1.0
 */
public class SearchSuggestion implements Service {
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

	/**
	 * Set default parameters
	 */
	public void init(String appPath, ServiceConfig config) throws Exception {
		_threshold = Integer.valueOf(config.getValue("threshold"));
		_maxNumberOfTerms = Integer.valueOf(config
				.getValue("max_number_of_terms"));
		_defaultSearchField = config.getValue("default_search_field");
	}

	/**
	 * Browse the index and return suggestion list.
	 */
	public Element exec(Element params, ServiceContext context)
			throws Exception {
		Element suggestions = new Element("items");

		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager sm = gc.getSearchmanager();

		String searchValue = Util.getParam(params, "q", "");
		String fieldName = Util.getParam(params, "field", _defaultSearchField);

		List<SearchManager.TermFrequency> termList = sm.getTermsFequency(
				fieldName, searchValue, _maxNumberOfTerms, _threshold);
		Collections.sort(termList);
		Collections.reverse(termList);

		Iterator<SearchManager.TermFrequency> iterator = termList.iterator();
		while (iterator.hasNext()) {
			SearchManager.TermFrequency freq = (SearchManager.TermFrequency) iterator
					.next();
			suggestions.addContent(new Element("item").setAttribute("term",
					freq.getTerm()).setAttribute("freq",
					String.valueOf(freq.getFrequency())));
		}

		// TODO : Could we output JSON directly from a Jeeves service
		// whithout having the XSL transformation ?
		return suggestions;
	}
}