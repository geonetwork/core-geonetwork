//==============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search;

import jeeves.utils.Log;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.util.spring.CollectionUtils;
import org.fao.geonet.util.spring.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * Class to create a Lucene query from a {@link LuceneQueryInput} representing a search request.
 *
 * @author heikki doeleman
 *
 */
public class LuceneQueryBuilder {

	private Set<String> _tokenizedFieldSet;
	private PerFieldAnalyzerWrapper _analyzer;
	private Map<String, LuceneConfig.LuceneConfigNumericField> _numericFieldSet;
	
	// Lat long bounding box constants
	static final Double minBoundingLatitudeValue  = -90.0;
	static final Double maxBoundingLatitudeValue  = 90.0;
	static final Double minBoundingLongitudeValue = -180.0;
	static final Double maxBoundingLongitudeValue = 180.0;

	public LuceneQueryBuilder(Set<String> tokenizedFieldSet, Map<String, LuceneConfig.LuceneConfigNumericField> numericFieldSet, PerFieldAnalyzerWrapper analyzer) {
		_tokenizedFieldSet = tokenizedFieldSet;
		_numericFieldSet = numericFieldSet;
		_analyzer          = analyzer;
	}

    /**
     * Creates a query for a string. If the string contains a wildcard, similarity is ignored.
     *
     * @param string string
     * @param luceneIndexField index field
     * @param similarity fuzziness
     * @return query
     */
	private Query textFieldToken(String string, String luceneIndexField, String similarity) {
        if(string == null) {
            throw new IllegalArgumentException("Cannot create Lucene query for null string");
        }
        Query query = null;
        String analyzedString = LuceneSearcher.analyzeQueryText(luceneIndexField, string, _analyzer, _tokenizedFieldSet);
        if(StringUtils.hasLength(analyzedString)) {
        // no wildcards
        if(string.indexOf('*') < 0 && string.indexOf('?') < 0) {
            // similarity is not set or is 1
            if(similarity == null || similarity.equals("1")) {
                    query = new TermQuery(new Term(luceneIndexField, analyzedString));
            }
            // similarity is not null and not 1
            else {
                Float minimumSimilarity = Float.parseFloat(similarity);
                    query = new FuzzyQuery(new Term(luceneIndexField, analyzedString), minimumSimilarity);
            }
        }
        // wildcards
        else {
                query = new WildcardQuery(new Term(luceneIndexField, analyzedString));
            }
        }		
        return query;
	}

	/**
	 * Creates a query for all tokens in the search param. The query must select only results
	 * where none of the tokens in the search param is present.
     *
     * @param searchParam search param
     * @param luceneIndexField index field
     * @return boolean clause
	 */
	private BooleanClause prohibitedTextField(String searchParam, String luceneIndexField) {
		BooleanClause booleanClause  = null;
		BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
		BooleanClause.Occur dontOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, true);
		if(searchParam != null) {
			searchParam = searchParam.trim();
			if(searchParam.length() > 0) {
				BooleanQuery booleanQuery = new BooleanQuery();
				MatchAllDocsQuery matchAllDocsQuery = new MatchAllDocsQuery();
				BooleanClause matchAllDocsClause = new BooleanClause(matchAllDocsQuery, occur);
				booleanQuery.add(matchAllDocsClause);
				// tokenize searchParam
			    StringTokenizer st = new StringTokenizer(searchParam);
			    while (st.hasMoreTokens()) {
			        String token = st.nextToken();
			        // ignore fuzziness in without-queries
			        Query subQuery = textFieldToken(token, luceneIndexField, null);
					if(subQuery != null) {
					BooleanClause subClause = new BooleanClause(subQuery, dontOccur);
					booleanQuery.add(subClause);
			    }
			    }
			    booleanClause = new BooleanClause(booleanQuery, occur);
			}
		}
		return booleanClause;
	}

	/**
	 * Creates a query for all tokens in the search param. 'Not required' does not mean that this is
	 * not a required search parameter; rather it means that if this parameter is present, the query
	 * must select results where at least one of the tokens in the search param is present.
     *
     * @param searchParam search param
     * @param luceneIndexField index field
     * @param similarity fuzziness
     * @return boolean clause
	 */
	private BooleanClause notRequiredTextField(String searchParam, String luceneIndexField, String similarity) {
		BooleanClause booleanClause  = null;
		BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
		BooleanClause.Occur tokenOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
		if(searchParam != null) {
			searchParam = searchParam.trim();
			if(searchParam.length() > 0) {
				// tokenize searchParam
			    StringTokenizer st = new StringTokenizer(searchParam);
				BooleanQuery booleanQuery = new BooleanQuery();
			    while (st.hasMoreTokens()) {
			        String token = st.nextToken();
			        Query subQuery = textFieldToken(token, luceneIndexField, similarity);
                    if(subQuery != null) {
					BooleanClause subClause = new BooleanClause(subQuery, tokenOccur);
					booleanQuery.add(subClause);
			    }
			    }
			    booleanClause = new BooleanClause(booleanQuery, occur);
			}
		}
		return booleanClause;
	}


	/**
	 * Creates a query for all tokens in the search param. 'Required' does not mean that this is
	 * a required search parameter; rather it means that if this parameter is present, the query
	 * must select only results where each of the tokens in the search param is present.
     *
     * @param searchParam search parameter
     * @param luceneIndexField index field
     * @param similarity fuzziness
     * @return boolean clause
	 */
	private BooleanClause requiredTextField(String searchParam, String luceneIndexField, String similarity) {
		BooleanClause booleanClause  = null;
		BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
		if(searchParam != null) {
			searchParam = searchParam.trim();
			if(searchParam.length() > 0) {
				// tokenize searchParam
			    StringTokenizer st = new StringTokenizer(searchParam);
			    if(st.countTokens() == 1) {
			        String token = st.nextToken();
			        Query subQuery = textFieldToken(token, luceneIndexField, similarity);
                    if(subQuery != null) {
				    booleanClause = new BooleanClause(subQuery, occur);
			    }
			    }
			    else {
					BooleanQuery booleanQuery = new BooleanQuery();
				    while (st.hasMoreTokens()) {
				        String token = st.nextToken();
				        Query subQuery = textFieldToken(token, luceneIndexField, similarity);
						if(subQuery != null) {
						BooleanClause subClause = new BooleanClause(subQuery, occur);
						booleanQuery.add(subClause);
				    }
				    }
				    booleanClause = new BooleanClause(booleanQuery, occur);
			    }
			}
		}
		return booleanClause;
	}

    /**
     *
     * @param selectors
     * @param luceneIndexField
     * @param query
     * @return
     */
	private BooleanQuery selectorTextField(Collection<String> selectors, String luceneIndexField, BooleanQuery query) {
		if (selectors != null && selectors.size() > 0) {
			for (String selector : selectors) {
				BooleanQuery allSelectorsQuery = new BooleanQuery();
				BooleanClause.Occur allSelectorsOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);

				if (StringUtils.hasText(selector)) {
     			BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
       		// TODO: Check separator
       		String[] tokens = StringUtils.delimitedListToStringArray(selector, " or ");
       		for (String token : tokens) {
       			token = token.trim();
        		if (token.startsWith("\"")) {
        			token = token.substring(1);
        		}
        		if (token.endsWith("\"")) {
          		token = token.substring(0, token.length() - 1);
        		}
        		//
        		TermQuery termQuery = new TermQuery(new Term(luceneIndexField, token));
        		BooleanClause clause = new BooleanClause(termQuery, occur);
        		allSelectorsQuery.add(clause);
       		}
				}
				if (allSelectorsQuery.clauses().size() > 0) {
					query.add(allSelectorsQuery, allSelectorsOccur);
				}
			}
		}
		return query;
	}

	public Query build(LuceneQueryInput luceneQueryInput) {

		Log.debug(Geonet.SEARCH_ENGINE, "\n\nLuceneQueryBuilder: luceneQueryInput is\n" + luceneQueryInput.toString() + "\n\n");
//		DEBUG
		//System.out.println("\n\n** LuceneQueryBuilder: luceneQueryInput is\n" + luceneQueryInput.toString() + "\n\n");

		// top query to hold all sub-queries for each search parameter
		BooleanQuery query = new BooleanQuery();

		//
		// similarity
		//
		// this is passed to textfield-query-creating methods
		String similarity = luceneQueryInput.getSimilarity();

		//
		// uuid
		//
		String uuidParam = luceneQueryInput.getUuid();
		if(uuidParam != null) {
			uuidParam = uuidParam.trim();
			if(uuidParam.length() > 0) {
				// the uuid param is an 'or' separated list. Remove the 'or's and handle like an 'or' query:
				uuidParam = uuidParam.replaceAll("\\sor\\s", " ");
				BooleanClause uuidQuery = notRequiredTextField(uuidParam, LuceneIndexField.UUID, similarity);
				if(uuidQuery != null) {
					query.add(uuidQuery);
				}
			}
		}

		//
		// any
		//
		BooleanClause anyClause  = null;
		BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
		String any = luceneQueryInput.getAny();
		if(any != null && !onlyWildcard(any)) {
			any = any.trim();
			if(any.length() > 0) {
				// tokenize searchParam
			    StringTokenizer st = new StringTokenizer(any);
			    if(st.countTokens() == 1) {
			        String token = st.nextToken();
			        Query subQuery = textFieldToken(token, LuceneIndexField.ANY, similarity);
			        if(subQuery != null) {
			        	anyClause = new BooleanClause(subQuery, occur);
			        }
			    }
			    else {
					BooleanQuery booleanQuery = new BooleanQuery();
				    while (st.hasMoreTokens()) {
				        String token = st.nextToken();
				        Query subQuery = textFieldToken(token, LuceneIndexField.ANY, similarity);
						if(subQuery != null) {
						BooleanClause subClause = new BooleanClause(subQuery, occur);
						if(subClause != null){
							booleanQuery.add(subClause);
						}
				    }
				    }
				    anyClause = new BooleanClause(booleanQuery, occur);
			    }
			}
		}
		if(anyClause != null) {
			query.add(anyClause);
		}

		//
		// all -- mapped to same Lucene field as 'any'
		//
		BooleanClause allQuery = requiredTextField(luceneQueryInput.getAll(), LuceneIndexField.ANY, similarity);
		if(allQuery != null) {
			query.add(allQuery);
		}

		//
		// or
		//
		BooleanClause orQuery = notRequiredTextField(luceneQueryInput.getOr(), LuceneIndexField.ANY, similarity);
		if(orQuery != null) {
			query.add(orQuery);
		}

		//
		// without
		//
		BooleanClause withoutQuery = prohibitedTextField(luceneQueryInput.getWithout(), LuceneIndexField.ANY);
		if(withoutQuery != null) {
			query.add(withoutQuery);
		}

		//
		// phrase
		//
		String phrase = luceneQueryInput.getPhrase();
		if(phrase != null) {
			phrase = phrase.trim();
			if(phrase.length() > 0) {
				PhraseQuery phraseQuery = new PhraseQuery();
				BooleanClause.Occur phraseOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
				// tokenize phrase
			    StringTokenizer st = new StringTokenizer(phrase);
			    while (st.hasMoreTokens()) {
			        String phraseElement = st.nextToken();
			        phraseElement = phraseElement.trim().toLowerCase();
			        phraseQuery.add(new Term(LuceneIndexField.ANY, phraseElement));
			    }
				query.add(phraseQuery, phraseOccur);
			}
		}

		//
		// ISO topic category
		//
		Set<String> isoTopicCategories = luceneQueryInput.getTopicCategories();
		if(!CollectionUtils.isEmpty(isoTopicCategories )) {
			BooleanQuery isoTopicCategoriesQuery = new BooleanQuery();
			BooleanClause.Occur topicCategoryOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
            for (String isoTopicCategory : isoTopicCategories) {
                isoTopicCategory = isoTopicCategory.trim();
                if (isoTopicCategory.length() > 0) {
                    // some clients (like GN's GUI) stupidly append a * already. Prevent double stars here:
                    if (isoTopicCategory.endsWith("*")) {
                        isoTopicCategory = isoTopicCategory.substring(0, isoTopicCategory.length() - 1);
                    }
                    PrefixQuery topicCategoryQuery = new PrefixQuery(new Term(LuceneIndexField.TOPIC_CATEGORY, isoTopicCategory));
                    BooleanClause topicCategoryClause = new BooleanClause(topicCategoryQuery, topicCategoryOccur);
                    isoTopicCategoriesQuery.add(topicCategoryClause);
                }
            }
			BooleanClause.Occur isoTopicCategoriesOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
			BooleanClause isoTopicCategoriesClause = new BooleanClause(isoTopicCategoriesQuery, isoTopicCategoriesOccur);
			query.add(isoTopicCategoriesClause);
		}

		//
		// download
		//
        String download = luceneQueryInput.getDownload();
        if (StringUtils.hasText(download) && download.equals("on")) {
            BooleanQuery downloadQuery = new BooleanQuery();

            WildcardQuery downloadQueryProtocol = new WildcardQuery(new Term(LuceneIndexField.PROTOCOL, "WWW:DOWNLOAD-*--download"));
            BooleanClause.Occur  downloadOccurProtocol = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
            downloadQuery.add(downloadQueryProtocol, downloadOccurProtocol);

            BooleanClause.Occur  downloadOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            BooleanClause downloadClause = new BooleanClause(downloadQuery, downloadOccur);
            query.add(downloadClause);
        }


		//
		// dynamic
		//
        String dynamic = luceneQueryInput.getDynamic();
        if (StringUtils.hasText(dynamic) && dynamic.equals("on")) {
            BooleanQuery dynamicQuery = new BooleanQuery();

            BooleanClause.Occur  dynamicProtocolOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);

            WildcardQuery dynamicQueryGetMap = new WildcardQuery(new Term(LuceneIndexField.PROTOCOL, "OGC:WMS-*-get-map"));
            dynamicQuery.add(dynamicQueryGetMap, dynamicProtocolOccur);

            WildcardQuery dynamicQueryGetCapabilities = new WildcardQuery(new Term(LuceneIndexField.PROTOCOL, "OGC:WMS-*-get-capabilities"));
            dynamicQuery.add(dynamicQueryGetCapabilities, dynamicProtocolOccur);

            WildcardQuery dynamicQueryEsriAims = new WildcardQuery(new Term(LuceneIndexField.PROTOCOL, "ESRI:AIMS-*-get-image"));
            dynamicQuery.add(dynamicQueryEsriAims, dynamicProtocolOccur);

            BooleanClause.Occur  dynamicOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            BooleanClause downloadClause = new BooleanClause(dynamicQuery, dynamicOccur);
            query.add(downloadClause);

        }

		//
		// protocol
		//
		BooleanClause protocolClause = requiredTextField(luceneQueryInput.getProtocol(), LuceneIndexField.PROTOCOL, similarity);
		if(protocolClause != null) {
			query.add(protocolClause);
		}

		//
		// featured
		//
		String featured = luceneQueryInput.getFeatured();
		if(featured != null && featured.equals("true")) {
			BooleanClause.Occur featuredOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
			TermQuery featuredQuery = new TermQuery(new Term(LuceneIndexField._OP6, "1"));
			BooleanClause featuredClause = new BooleanClause(featuredQuery, featuredOccur);
			query.add(featuredClause);
            // featured needs to be visible to all.
			TermQuery viewQuery = new TermQuery(new Term(LuceneIndexField._OP0, "1"));
			BooleanClause viewClause = new BooleanClause(viewQuery, featuredOccur);
			query.add(viewClause);
		}

        //
        // groups
        //

        Set<String> groups = luceneQueryInput.getGroups();
        String editable$ = luceneQueryInput.getEditable();
        boolean editable = StringUtils.hasText(editable$) && editable$.equals("true");

        BooleanQuery groupsQuery = new BooleanQuery();
        boolean groupsQueryEmpty = true;
        BooleanClause.Occur groupOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
        if(!CollectionUtils.isEmpty(groups)) {
            for (String group : groups) {
                group = group.trim();
                if (group.length() > 0) {
                    if(! editable) {
                        // add to view
                        TermQuery viewQuery = new TermQuery(new Term(LuceneIndexField._OP0, group));
                        BooleanClause viewClause = new BooleanClause(viewQuery, groupOccur);
                        groupsQueryEmpty = false;
                        groupsQuery.add(viewClause);
                    }
                    // add to edit
                    TermQuery editQuery = new TermQuery(new Term(LuceneIndexField._OP2, group));
                    BooleanClause editClause = new BooleanClause(editQuery, groupOccur);
                    groupsQueryEmpty = false;
                    groupsQuery.add(editClause);
                }
            }
        }

        //
        // owner: this goes in groups query. This way if you are logged in you can retrieve the results you are allowed
        // to see by your groups, plus any that you own not assigned to any group.
        //
        String owner = luceneQueryInput.getOwner();
			if(owner != null) {
				TermQuery ownerQuery = new TermQuery(new Term(LuceneIndexField.OWNER, owner));
            BooleanClause.Occur ownerOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
            BooleanClause ownerClause = new BooleanClause(ownerQuery, ownerOccur);
				groupsQueryEmpty = false;
				groupsQuery.add(ownerClause);
			}

        //
        // "dummy" -- to go in groups query, to retrieve everything for Administrator users.
        //
        boolean admin = luceneQueryInput.getAdmin();
        if(admin) {
				TermQuery adminQuery = new TermQuery(new Term(LuceneIndexField.DUMMY, "0"));
				BooleanClause adminClause = new BooleanClause(adminQuery, groupOccur);
				groupsQueryEmpty = false;
				groupsQuery.add(adminClause);
			}


			if(!groupsQueryEmpty) {
				BooleanClause.Occur groupsOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
				BooleanClause groupsClause = new BooleanClause(groupsQuery, groupsOccur);
				query.add(groupsClause);
			}

		//
		// category
		//
		Set<String> categories = luceneQueryInput.getCategories();
		if(!CollectionUtils.isEmpty(categories)) {
			BooleanQuery categoriesQuery = new BooleanQuery();
			BooleanClause.Occur categoriesOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
			BooleanClause categoriesClause = null;
            for (String category : categories) {
				if(category != null){
					category = category.trim();
					if(category.length() > 0) {
                        // TODO check: should this be handled using that subQuery ?
			            //Query subQuery = textFieldToken(category, LuceneIndexField.CAT, similarity);
						//BooleanClause categoryClause = new BooleanClause(subQuery, categoriesOccur);
                        BooleanClause categoryClause = notRequiredTextField(category, LuceneIndexField.CAT, similarity);

						if(categoryClause != null) {
							if(categoriesClause == null) {
								categoriesClause = new BooleanClause(categoriesQuery, categoriesOccur);
							}
							categoriesQuery.add(categoryClause);
						}
					}
				}
			}
			if(categoriesClause != null) {
				query.add(categoriesClause);
			}
		}

		//
		// template
		//
		String isTemplate = luceneQueryInput.getTemplate();
		BooleanClause.Occur templateOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
		TermQuery templateQuery;
		if(isTemplate != null && isTemplate.equals("y")) {
			templateQuery = new TermQuery(new Term(LuceneIndexField.IS_TEMPLATE, "y"));
		}
		else if(isTemplate != null && isTemplate.equals("s")) {
			templateQuery = new TermQuery(new Term(LuceneIndexField.IS_TEMPLATE, "s"));
		}
		else {
			templateQuery = new TermQuery(new Term(LuceneIndexField.IS_TEMPLATE, "n"));
		}
		query.add(templateQuery, templateOccur);

		String orgName = luceneQueryInput.getOrgName();
		BooleanClause.Occur orgNameOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
		TermQuery orgNameQuery;
		if(orgName != null) {
			orgNameQuery = new TermQuery(new Term(LuceneIndexField.ORG_NAME, orgName));
			query.add(orgNameQuery, orgNameOccur);
		}
		
		String spatialRepType = luceneQueryInput.getSpatialRepresentationType();
		BooleanClause.Occur spatialRepTypeOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
		TermQuery spatialRepTypeQuery;
		if(spatialRepType != null) {
			spatialRepTypeQuery = new TermQuery(new Term(LuceneIndexField.SPATIALREPRESENTATIONTYPE, spatialRepType));
			query.add(spatialRepTypeQuery, spatialRepTypeOccur);
		}
		
		
		// metadata date range
		addRangeQuery(query, 
				luceneQueryInput.getDateTo(), 
				luceneQueryInput.getDateFrom(), 
				LuceneIndexField.CHANGE_DATE);

		// Revision, publication and creation dates may have been indexed as temporal extent also.
		// data revision date range
		addRangeQuery(query, 
				luceneQueryInput.getRevisionDateTo(), 
				luceneQueryInput.getRevisionDateFrom(), 
				LuceneIndexField.REVISION_DATE);

		// data publication date range
		addRangeQuery(query, 
				luceneQueryInput.getPublicationDateTo(), 
				luceneQueryInput.getPublicationDateFrom(), 
				LuceneIndexField.PUBLICATION_DATE);

		// data creation date range
		addRangeQuery(query, 
				luceneQueryInput.getCreationDateTo(), 
				luceneQueryInput.getCreationDateFrom(), 
				LuceneIndexField.CREATE_DATE);


        //
        // Temporal extent : finds records where temporal extent overlaps the search extent
        //
        String extTo = luceneQueryInput.getExtTo();
        String extFrom = luceneQueryInput.getExtFrom();


        if((extTo != null && extTo.length() > 0) || (extFrom != null && extFrom.length() > 0)) {
            BooleanQuery temporalExtentQuery = new BooleanQuery();
            BooleanClause.Occur temporalExtentOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            BooleanClause.Occur temporalRangeQueryOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);

						TermRangeQuery temporalRangeQuery;

						// temporal extent start is within search extent
						temporalRangeQuery = new TermRangeQuery(LuceneIndexField.TEMPORALEXTENT_BEGIN, extFrom, extTo, true, true);
						BooleanClause temporalRangeQueryClause = new BooleanClause(temporalRangeQuery, temporalRangeQueryOccur);
						
       			temporalExtentQuery.add(temporalRangeQueryClause);

            // or temporal extent end is within search extent
            temporalRangeQuery = new TermRangeQuery(LuceneIndexField.TEMPORALEXTENT_END, extFrom, extTo, true, true);
            temporalRangeQueryClause = new BooleanClause(temporalRangeQuery, temporalRangeQueryOccur);

            temporalExtentQuery.add(temporalRangeQueryClause);

            //or temporal extent contains search extent
            if((extTo != null && extTo.length() > 0) && (extFrom != null && extFrom.length() > 0)) {
                BooleanQuery bq = new BooleanQuery();

                temporalRangeQuery = new TermRangeQuery(LuceneIndexField.TEMPORALEXTENT_END, extTo, null, true, true);
                temporalRangeQueryClause = new BooleanClause(temporalRangeQuery, temporalExtentOccur);

                bq.add(temporalRangeQueryClause);

                temporalRangeQuery = new TermRangeQuery(LuceneIndexField.TEMPORALEXTENT_BEGIN, null, extFrom, true, true);
                temporalRangeQueryClause = new BooleanClause(temporalRangeQuery, temporalExtentOccur);
                bq.add(temporalRangeQueryClause);

                temporalExtentQuery.add(bq, temporalRangeQueryOccur);
            }

            if (temporalExtentQuery.clauses().size() > 0) {
                temporalRangeQueryClause = new BooleanClause(temporalExtentQuery, temporalExtentOccur);
                query.add(temporalRangeQueryClause);
            }
        }



		// metadataStandardName
		//
		BooleanClause metadataStandardNameClause = requiredTextField(luceneQueryInput.getMetadataStandardName(), LuceneIndexField.METADATA_STANDARD_NAME, similarity);
		if(metadataStandardNameClause != null) {
			query.add(metadataStandardNameClause);
		}

        // schema
        //
        BooleanClause schemaClause = requiredTextField(luceneQueryInput.get_schema(), LuceneIndexField.SCHEMA, similarity);
        if(schemaClause != null) {
           query.add(schemaClause);
        }

        // parentUuid
		//
		BooleanClause parentUuidClause = requiredTextField(luceneQueryInput.getParentUuid(), LuceneIndexField.PARENTUUID, similarity);
		if(parentUuidClause != null) {
			query.add(parentUuidClause);
		}

        // operatesOn
		//
		BooleanClause operatesOnClause = requiredTextField(luceneQueryInput.getOperatesOn(), LuceneIndexField.OPERATESON, similarity);
		if(operatesOnClause != null) {
			query.add(operatesOnClause);
		}

		// serviceType
		//
		BooleanClause serviceTypeClause = requiredTextField(luceneQueryInput.getServiceType(), LuceneIndexField.SERVICE_TYPE, similarity);
		if(serviceTypeClause != null) {
			query.add(serviceTypeClause);
		}
		
		//
		// type
		//
		BooleanClause typeClause = requiredTextField(luceneQueryInput.getType(), LuceneIndexField.TYPE, similarity);
		if(typeClause != null) {
			query.add(typeClause);
		}

		
		addRangeQuery(query,
				luceneQueryInput.getDenominator(),
                luceneQueryInput.getDenominator(),
				LuceneIndexField.DENOMINATOR);
		
		addRangeQuery(query, 
                luceneQueryInput.getDenominatorFrom(),
                luceneQueryInput.getDenominatorTo(),
				LuceneIndexField.DENOMINATOR);
		
		
        //
		// inspire
		//
		String inspire = luceneQueryInput.getInspire();
		if(inspire != null) {
			TermQuery inspireQuery = new TermQuery(new Term(LuceneIndexField.INSPIRE_CAT, inspire));
            BooleanClause.Occur inspireOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);

			BooleanClause inspireClause = new BooleanClause(inspireQuery, inspireOccur);
			query.add(inspireClause);
		}

		//
		// inspireTheme
		//
		Set<String> inspireThemes = luceneQueryInput.getInspireThemes();

        //System.out.println("*** inspireThemes size: " + inspireThemes.size());

		if(!CollectionUtils.isEmpty(inspireThemes)) {
			BooleanQuery inspireThemesQuery = new BooleanQuery();
			BooleanClause.Occur inspireThemesOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            for (String inspireTheme : inspireThemes) {
                inspireTheme = inspireTheme.trim();
                if (inspireTheme.length() > 0) {
                    // some clients (like GN's GUI) stupidly append a * already. Prevent them here:
                    if (inspireTheme.endsWith("*")) {
                        inspireTheme = inspireTheme.substring(0, inspireTheme.length() - 1);
                    }
                    // NOTE if we want to support a combined phrase/prefix query we should (instead) create a MultiPhraseQuery here.
                    // but  think that may be slow.
                    PhraseQuery phraseQuery = new PhraseQuery();
                    BooleanClause.Occur phraseOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
                    // tokenize phrase
                    StringTokenizer st = new StringTokenizer(inspireTheme);
                    while (st.hasMoreTokens()) {
                        String phraseElement = st.nextToken();
                        phraseElement = phraseElement.trim().toLowerCase();
                        phraseQuery.add(new Term(LuceneIndexField.INSPIRE_THEME, LuceneSearcher.analyzeQueryText(LuceneIndexField.INSPIRE_THEME, phraseElement, _analyzer, _tokenizedFieldSet)));
                    }
                    inspireThemesQuery.add(phraseQuery, phraseOccur);
                }
            }
			query.add(inspireThemesQuery, inspireThemesOccur);
		}

		//
		// inspireannex
		//
		BooleanClause inspireannexQuery = requiredTextField(luceneQueryInput.getInspireAnnex(), LuceneIndexField.INSPIRE_ANNEX, similarity);
		if(inspireannexQuery != null) {
			query.add(inspireannexQuery);
		}
        
		//
		// siteId / source
		//
		BooleanClause sourceQuery = requiredTextField(luceneQueryInput.getSiteId(), LuceneIndexField.SOURCE, similarity);
		if(sourceQuery != null) {
			query.add(sourceQuery);
		}

        //
        // themekey
        //
				query = selectorTextField(luceneQueryInput.getThemeKeys(), LuceneIndexField.KEYWORD, query);

		//
		// digital and paper maps
		//
		String digital = luceneQueryInput.getDigital();
        String paper = luceneQueryInput.getPaper();

        // if both are off or both are on then no clauses are added
        if (StringUtils.hasText(digital) && digital.equals("on") && (!StringUtils.hasText(paper) || paper.equals("off"))) {
            TermQuery digitalQuery = new TermQuery(new Term(LuceneIndexField.DIGITAL, "true"));
            BooleanClause.Occur digitalOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            BooleanClause digitalClause = new BooleanClause(digitalQuery, digitalOccur);
            query.add(digitalClause);
        }

        if (StringUtils.hasText(paper) && paper.equals("on") && (!StringUtils.hasText(digital)|| digital.equals("off"))) {
            TermQuery paperQuery = new TermQuery(new Term(LuceneIndexField.PAPER, "true"));
            BooleanClause.Occur paperOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            BooleanClause paperClause = new BooleanClause(paperQuery,paperOccur);
            query.add(paperClause);
        }

		//
		// title
		//
		BooleanClause titleQuery = requiredTextField(luceneQueryInput.getTitle(), LuceneIndexField.TITLE, similarity);
		if(titleQuery != null) {
			query.add(titleQuery);
		}

		//
		// abstract
		//
		BooleanClause abstractQuery = requiredTextField(luceneQueryInput.getAbstrakt(), LuceneIndexField.ABSTRACT, similarity);
		if(abstractQuery != null) {
			query.add(abstractQuery);
		}

		//
		// taxon
		//
		query = selectorTextField(luceneQueryInput.getTaxons(), LuceneIndexField.TAXON, query);

		//
		// credit
		//
		query = selectorTextField(luceneQueryInput.getCredits(), LuceneIndexField.CREDIT, query);

		//
		// dataparam
		//
		query = selectorTextField(luceneQueryInput.getDataparams(), LuceneIndexField.DATAPARAM, query);

		//
		// bounding box
		//
		String eastBL = luceneQueryInput.getEastBL();
		String westBL = luceneQueryInput.getWestBL();
		String northBL = luceneQueryInput.getNorthBL();
		String southBL = luceneQueryInput.getSouthBL();
		String relation = luceneQueryInput.getRelation();

		if (!("".equals(eastBL) ||
		      "".equals(westBL) ||
					"".equals(southBL) ||
					"".equals(northBL)) ) {
			addBoundingBoxQuery(query, relation, eastBL, westBL, northBL, southBL);
		}

//		DEBUG
		//System.out.println("\n\n** LuceneQueryBuilder: query is\n" + query + "\n\n");
		Log.debug(Geonet.SEARCH_ENGINE, "\n\nLuceneQueryBuilder: query is\n" + query + "\n\n");

		return query;
	}

	
	/**
	 * Add a range query according to field type. If field type is numeric,
	 * then a numeric range query is used. If not a default range query is uses.
	 * 
	 * Range query include lower and upper bounds by default.
	 * 
	 * @param query
	 * @param from
	 * @param to
	 * @param luceneIndexField
	 */
	private void addRangeQuery(BooleanQuery query, String from,
			String to, String luceneIndexField) {
		if (from == null && to == null)
			return;
		
		LuceneConfig.LuceneConfigNumericField type = _numericFieldSet.get(luceneIndexField);
		if (type == null) {
			addTextRangeQuery(query, from, to, luceneIndexField);
		} else {
			addNumericRangeQuery(query, from, to, true, true, luceneIndexField, true);
		}
	}
	
	/**
	 * Add a numeric range query according to field numeric type.
	 * 
	 * @param query
	 * @param min
	 * @param max
	 * @param minInclusive 
	 * @param maxExclusive 
	 * @param luceneIndexField
	 * @param required TODO
	 */
	private void addNumericRangeQuery(BooleanQuery query, String min,
			String max, boolean minInclusive, boolean maxExclusive, String luceneIndexField, boolean required) {
		if (min != null && max != null) {
			String type = _numericFieldSet.get(luceneIndexField).getType();
			
			NumericRangeQuery rangeQuery = buildNumericRangeQueryForType(luceneIndexField,
					min, max, minInclusive, maxExclusive, type);
			
			BooleanClause.Occur denoOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(required, false);
			BooleanClause rangeClause = new BooleanClause(rangeQuery, denoOccur);
			
			query.add(rangeClause);	
		}
	}

	public static NumericRangeQuery buildNumericRangeQueryForType(String fieldName,
			String min, String max, boolean minInclusive, boolean maxInclusive, String type) {
		NumericRangeQuery rangeQuery;
		if ("double".equals(type)) {
			rangeQuery = NumericRangeQuery.newDoubleRange(fieldName, 
					 (min == null?Double.MIN_VALUE:Double.valueOf(min)), 
					 (max == null?Double.MAX_VALUE:Double.valueOf(max)),
					 true, true);
			
		} else if ("float".equals(type)) {
			rangeQuery = NumericRangeQuery.newFloatRange(fieldName, 
					 (min == null?Float.MIN_VALUE:Float.valueOf(min)), 
					 (max == null?Float.MAX_VALUE:Float.valueOf(max)), 
					 true, true);
		} else if ("long".equals(type)) {
			rangeQuery = NumericRangeQuery.newLongRange(fieldName, 
					 (min == null?Long.MIN_VALUE:Long.valueOf(min)), 
					 (max == null?Long.MAX_VALUE:Long.valueOf(max)), 
					 true, true);
		} else {
			rangeQuery = NumericRangeQuery.newIntRange(fieldName, 
					 (min == null?Integer.MIN_VALUE:Integer.valueOf(min)), 
					 (max == null?Integer.MAX_VALUE:Integer.valueOf(max)), 
					 true, true);
		}
		return rangeQuery;
	}
	
	/**
	 * Add a date range query for a text field type.
	 *  
	 * @param query
	 * @param dateTo
	 * @param dateFrom
	 * @param luceneIndexField
	 */
	private void addTextRangeQuery(BooleanQuery query, String dateTo,
			String dateFrom, String luceneIndexField) {
		if((dateTo != null && dateTo.length() > 0) || (dateFrom != null && dateFrom.length() > 0)) {
			TermRangeQuery rangeQuery;
			if(dateTo != null) {
				// while the 'from' parameter can be short (like yyyy-mm-dd)
				// the 'until' parameter must be long to match
				if(dateTo.length() == 10) {
					dateTo = dateTo + "T23:59:59";
				}
			}
			rangeQuery = new TermRangeQuery(luceneIndexField, dateFrom, dateTo, true, true);
			BooleanClause.Occur dateOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
			BooleanClause dateRangeClause = new BooleanClause(rangeQuery, dateOccur);
			query.add(dateRangeClause);
		}
	}
	
	
	/**
	 *  Handle geographical search using Lucene.
	 * 
	 *  East, North, South and West bounds are indexed as numeric in Lucene.
	 *  
	 *  Lucene bounding box searches are probably faster than using spatial 
	 *  index using geometry criteria. It does not support complex geometries 
	 *  and all type of relation. 
	 *  
	 *  If metadata contains multiple bounding boxes invalid results may appear.  
	 * 
	 *  If relation is null or is not a known relation type (See {@link org.fao.geonet.constants.Geonet.SearchResult.Relation}),
	 *  overlap is used.
	 * 
	 * @param query
	 * @param relation
	 * @param eastBL
	 * @param westBL
	 * @param northBL
	 * @param southBL
	 */
	private void addBoundingBoxQuery(BooleanQuery query, String relation,
			String eastBL, String westBL, String northBL, String southBL) {
		
		// Default inclusive value for RangeQuery (includeLower and includeUpper)
		boolean inclusive = true;
		
		
		if (relation == null || relation.equals(Geonet.SearchResult.Relation.OVERLAPS)){

			//
			// overlaps (default value) : uses the equivalence
			// -(a + b + c + d) = -a * -b * -c * -d
			//
			// eastBL
			if (westBL != null) {
				addNumericRangeQuery(query, westBL, String.valueOf(maxBoundingLongitudeValue), inclusive, inclusive, LuceneIndexField.EAST, true);
			}
			// westBL
			if (eastBL != null) {
				addNumericRangeQuery(query, String.valueOf(minBoundingLongitudeValue), eastBL, inclusive, inclusive, LuceneIndexField.WEST, true);
			}
			// northBL
			if (southBL != null) {
				addNumericRangeQuery(query, southBL, String.valueOf(maxBoundingLatitudeValue), inclusive, inclusive, LuceneIndexField.NORTH, true);
			}
			// southBL
			if (northBL != null) {
				addNumericRangeQuery(query, String.valueOf(minBoundingLatitudeValue), northBL, inclusive, inclusive, LuceneIndexField.SOUTH, true);
			}
		}
		//
		// equal: coordinates of the target rectangle within 1 degree from
		// corresponding ones of metadata rectangle
		//
		else if (relation.equals(Geonet.SearchResult.Relation.EQUAL)) {
			// eastBL
			if(eastBL != null) {
				addNumericRangeQuery(query, eastBL, eastBL, inclusive, inclusive, LuceneIndexField.EAST, true);
			}
			// westBL
			if(westBL != null) {
				addNumericRangeQuery(query, westBL, westBL, inclusive, inclusive, LuceneIndexField.WEST, true);
			}
			// northBL
			if(northBL != null) {
				addNumericRangeQuery(query, northBL, northBL, inclusive, inclusive, LuceneIndexField.NORTH, true);
			}
			// southBL
			if(southBL != null) {
				addNumericRangeQuery(query, southBL, southBL, inclusive, inclusive, LuceneIndexField.SOUTH, true);
			}
		}
		//
		// encloses: metadata rectangle encloses target rectangle
		//
		else if(relation.equals(Geonet.SearchResult.Relation.ENCLOSES)) {
			// eastBL
			if(eastBL != null) {
				addNumericRangeQuery(query, eastBL, String.valueOf(maxBoundingLongitudeValue), inclusive, inclusive, LuceneIndexField.EAST, true);
			}
			// westBL
			if(westBL != null) {
				addNumericRangeQuery(query, String.valueOf(minBoundingLongitudeValue), westBL, inclusive, inclusive, LuceneIndexField.WEST, true);
			}
			// northBL
			if(northBL != null) {
				addNumericRangeQuery(query, northBL, String.valueOf(maxBoundingLatitudeValue), inclusive, inclusive, LuceneIndexField.NORTH, true);
			}
			// southBL
			if(southBL != null) {
				addNumericRangeQuery(query, String.valueOf(minBoundingLatitudeValue), southBL, inclusive, inclusive, LuceneIndexField.SOUTH, true);
			}
		}
		//
		// fullyEnclosedWithin: metadata rectangle fully enclosed within target rectangle
		//
		else if(relation.equals(Geonet.SearchResult.Relation.ENCLOSEDWITHIN)) {
			// eastBL
			if(eastBL != null) {
				addNumericRangeQuery(query, westBL, eastBL, inclusive, inclusive, LuceneIndexField.EAST, true);
			}
			// westBL
			if(westBL != null) {
				addNumericRangeQuery(query, westBL, eastBL, inclusive, inclusive, LuceneIndexField.WEST, true);
			}
			// northBL
			if(northBL != null) {
				addNumericRangeQuery(query, southBL, northBL, inclusive, inclusive, LuceneIndexField.NORTH, true);
			}
			// southBL
			if(southBL != null) {
				addNumericRangeQuery(query, southBL, northBL, inclusive, inclusive, LuceneIndexField.SOUTH, true);
			}
		}
		//
		// fullyOutsideOf: one or more of the 4 forbidden halfplanes contains the metadata
		// rectangle, that is, not true that all the 4 forbidden halfplanes do not contain
		// the metadata rectangle
		//
		else if(relation.equals(Geonet.SearchResult.Relation.OUTSIDEOF)) {
			// eastBL
			if(westBL != null) {
				addNumericRangeQuery(query, String.valueOf(minBoundingLongitudeValue), westBL, inclusive, inclusive, LuceneIndexField.EAST, false);
			}
			// westBL
			if(eastBL != null) {
				addNumericRangeQuery(query, eastBL, String.valueOf(maxBoundingLongitudeValue), inclusive, inclusive, LuceneIndexField.WEST, false);
			}
			// northBL
			if(southBL != null) {
				addNumericRangeQuery(query, String.valueOf(minBoundingLatitudeValue), southBL, inclusive, inclusive, LuceneIndexField.NORTH, false);
			}
			// southBL
			if(northBL != null) {
				addNumericRangeQuery(query, northBL, String.valueOf(maxBoundingLatitudeValue), inclusive, inclusive, LuceneIndexField.SOUTH, false);
			}
		}
	}
	
    private boolean onlyWildcard(String s) {
        return s != null && s.trim().equals("*");
    }
}
