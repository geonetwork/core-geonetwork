package org.fao.geonet.kernel.search;

import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.util.spring.StringUtils;
import org.jdom.Element;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * Builds a Lucene query from a JDOM element representing a search request.
 *
 * @author heikki doeleman
 *
 */
public class LuceneQueryBuilder {

	HashSet<String> _tokenizedFieldSet;
	PerFieldAnalyzerWrapper _analyzer;
	
	// Bounding box constants
	static final String minBoundingLatitudeValue  = "270";  //  -90 + 360
	static final String maxBoundingLatitudeValue  = "450";  //   90 + 360
	static final String minBoundingLongitudeValue = "180";  // -180 + 360
	static final String maxBoundingLongitudeValue = "540";  //  180 + 360

	public LuceneQueryBuilder(HashSet<String> tokenizedFieldSet, PerFieldAnalyzerWrapper analyzer) {
		_tokenizedFieldSet = tokenizedFieldSet;
		_analyzer          = analyzer;
	}

	/**
	 * Creates a query for a string.
	 */
	private Query textFieldToken(String string, String luceneIndexField, String similarity) {
		// similarity is not set or is 1
		if(similarity == null || similarity.equals("1")) {
			TermQuery query = null;
			if(string != null) {
				query = new TermQuery(new Term(luceneIndexField, LuceneSearcher.analyzeQueryText(luceneIndexField, string, _analyzer, _tokenizedFieldSet)));
			}
			return query;
		}
		// similarity is not null and not 1
		else {
			FuzzyQuery query = null;
			if(string != null) {
				Float minimumSimilarity = Float.parseFloat(similarity);
				query = new FuzzyQuery(new Term(luceneIndexField, LuceneSearcher.analyzeQueryText(luceneIndexField, string, _analyzer, _tokenizedFieldSet)), minimumSimilarity);
			}
			return query;
		}
	}

	/**
	 * Creates a query for all tokens in the search param. The query must select only results
	 * where none of the tokens in the search param is present.
	 */
	private BooleanClause prohibitedTextField(String searchParam, String luceneIndexField, String similarity) {
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
					BooleanClause subClause = new BooleanClause(subQuery, dontOccur);
					booleanQuery.add(subClause);
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
					BooleanClause subClause = new BooleanClause(subQuery, tokenOccur);
					booleanQuery.add(subClause);
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
				    booleanClause = new BooleanClause(subQuery, occur);
			    }
			    else {
					BooleanQuery booleanQuery = new BooleanQuery();
				    while (st.hasMoreTokens()) {
				        String token = st.nextToken();
				        Query subQuery = textFieldToken(token, luceneIndexField, similarity);
						BooleanClause subClause = new BooleanClause(subQuery, occur);
						booleanQuery.add(subClause);
				    }
				    booleanClause = new BooleanClause(booleanQuery, occur);
			    }
			}
		}
		return booleanClause;
	}

	public Query build(Element request) {

		Log.debug(Geonet.SEARCH_ENGINE, "\n\nLuceneQueryBuilder: request is\n" + Xml.getString(request) + "\n\n");
//		DEBUG
//		System.out.println("\n\nLuceneQueryBuilder: request is\n" + Xml.getString(request) + "\n\n");

		// top query to hold all sub-queries for each search parameter
		BooleanQuery query = new BooleanQuery();

		//
		// hits per page
		//
		// nothing happens with it ?
		// String hitsPerPage = request.getChildText("hitsPerPage");

		//
		// attrset
		//
		// nothing happens with it ?
		//String attrset = request.getChildText("attrset");

		//
		// similarity
		//
		// this is passed to textfield-query-creating methods
		String similarity = request.getChildText("similarity");

		//
		// uuid
		//
		String uuidParam = request.getChildText("uuid");
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
		String any = request.getChildText("any");
		if(any != null) {
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
						BooleanClause subClause = new BooleanClause(subQuery, occur);
						if(subClause != null){
							booleanQuery.add(subClause);
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
		BooleanClause allQuery = requiredTextField(request.getChildText("all"), LuceneIndexField.ANY, similarity);
		if(allQuery != null) {
			query.add(allQuery);
		}

		//
		// or
		//
		BooleanClause orQuery = notRequiredTextField(request.getChildText("or"), LuceneIndexField.ANY, similarity);
		if(orQuery != null) {
			query.add(orQuery);
		}

		//
		// without
		//
		BooleanClause withoutQuery = prohibitedTextField(request.getChildText("without"), LuceneIndexField.ANY, similarity);
		if(withoutQuery != null) {
			query.add(withoutQuery);
		}

		//
		// phrase
		//
		String phrase = request.getChildText("phrase");
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
		@SuppressWarnings("unchecked")
		List<Element> isoTopicCategories = (List<Element>)request.getChildren("topic-category");
		if(isoTopicCategories != null && isoTopicCategories.size() > 0) {
			BooleanQuery isoTopicCategoriesQuery = new BooleanQuery();
			BooleanClause.Occur topicCategoryOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
			for(Iterator<Element> i = isoTopicCategories.iterator();i.hasNext();){
				String isoTopicCategory =  i.next().getText();
				isoTopicCategory = isoTopicCategory.trim();
				if(isoTopicCategory.length() > 0) {
					// some clients (like GN's GUI) stupidly append a * already. Prevent double stars here:
					if(isoTopicCategory.endsWith("*")) {
						isoTopicCategory = isoTopicCategory.substring(0, isoTopicCategory.length()-1);
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
        String download = request.getChildText("download");
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
        String dynamic = request.getChildText("dynamic");
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
		BooleanClause protocolClause = requiredTextField(request.getChildText("protocol"), LuceneIndexField.PROTOCOL, similarity);
		if(protocolClause != null) {
			query.add(protocolClause);
		}

		//
		// featured
		//
		String featured = request.getChildText("featured");
		if(featured != null && featured.equals("true")) {
			BooleanClause.Occur featuredOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
			TermQuery featuredQuery = new TermQuery(new Term(LuceneIndexField._OP6, "1"));
			BooleanClause featuredClause = new BooleanClause(featuredQuery, featuredOccur);
			query.add(featuredClause);
			TermQuery viewQuery = new TermQuery(new Term(LuceneIndexField._OP0, "1"));
			BooleanClause viewClause = new BooleanClause(viewQuery, featuredOccur);
			query.add(viewClause);
		}
		else {
			BooleanQuery groupsQuery = new BooleanQuery();
			boolean groupsQueryEmpty = true;
			@SuppressWarnings("unchecked")
			List<Element> groups = (List<Element>)request.getChildren("group");
			BooleanClause.Occur groupOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
			if(groups != null && groups.size() > 0) {
				for(Iterator<Element> i = groups.iterator(); i.hasNext();) {
					String group = i.next().getText();
					group = group.trim();
					if(group.length() > 0) {
						TermQuery groupQuery = new TermQuery(new Term(LuceneIndexField._OP0, group));
						BooleanClause groupClause = new BooleanClause(groupQuery, groupOccur);
						groupsQueryEmpty = false;
						groupsQuery.add(groupClause);
					}
				}
			}
			String reviewer = request.getChildText("isReviewer");
			if(reviewer != null) {
				if(groups != null && groups.size() > 0) {
					for(Iterator<Element> i = groups.iterator(); i.hasNext();) {
						String group = i.next().getText();
						group = group.trim();
						if(group.length() > 0) {
							TermQuery groupQuery = new TermQuery(new Term(LuceneIndexField.GROUP_OWNER, group));
							BooleanClause groupClause = new BooleanClause(groupQuery, groupOccur);
							groupsQueryEmpty = false;
							groupsQuery.add(groupClause);
						}
					}
				}
			}
			String userAdmin = request.getChildText("isUserAdmin");
			if(userAdmin != null) {
				if(groups != null && groups.size() > 0) {
					for(Iterator<Element> i = groups.iterator(); i.hasNext();) {
						String group = i.next().getText();
						group = group.trim();
						if(group.length() > 0) {
							TermQuery groupQuery = new TermQuery(new Term(LuceneIndexField.GROUP_OWNER, group));
							BooleanClause groupClause = new BooleanClause(groupQuery, groupOccur);
							groupsQueryEmpty = false;
							groupsQuery.add(groupClause);
						}
					}
				}
			}
			String owner = request.getChildText("owner");
			if(owner != null) {
				TermQuery ownerQuery = new TermQuery(new Term(LuceneIndexField.OWNER, owner));
				BooleanClause ownerClause = new BooleanClause(ownerQuery, groupOccur);
				groupsQueryEmpty = false;
				groupsQuery.add(ownerClause);
			}
			String admin = request.getChildText("isAdmin");
			if(admin != null) {
				TermQuery adminQuery = new TermQuery(new Term(LuceneIndexField.DUMMY, "0"));
				BooleanClause adminClause = new BooleanClause(adminQuery, groupOccur);
				groupsQueryEmpty = false;
				groupsQuery.add(adminClause);
			}
			if(groupsQueryEmpty == false) {
				BooleanClause.Occur groupsOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
				BooleanClause groupsClause = new BooleanClause(groupsQuery, groupsOccur);
				query.add(groupsClause);
			}

			@SuppressWarnings("unchecked")
			List<Element> groupOwners = (List<Element>)request.getChildren("groupOwner");
			if(groupOwners != null && groupOwners.size() > 0) {
				BooleanClause.Occur groupOwnerOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
				for(Iterator<Element> i = groupOwners.iterator();i.hasNext();) {
					String groupOwner = i.next().getText();
					groupOwner = groupOwner.trim();
					if(groupOwner.length() > 0) {
						TermQuery groupOwnerQuery = new TermQuery(new Term(LuceneIndexField.GROUP_OWNER, groupOwner));
						BooleanClause groupOwnerClause = new BooleanClause(groupOwnerQuery, groupOwnerOccur);
						query.add(groupOwnerClause);
					}
				}
			}
		}

		//
		// category
		//
		@SuppressWarnings("unchecked")
		List<Element> categories = (List<Element>)request.getChildren("category");
		if(categories != null && categories.size() > 0) {
			BooleanQuery categoriesQuery = new BooleanQuery();
			BooleanClause.Occur categoriesOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
			BooleanClause categoriesClause = null;
			for(Iterator<Element> i = categories.iterator(); i.hasNext();) {
				String category = i.next().getText();
				if(category != null){
					category = category.trim();
					if(category.length() > 0) {
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
		String isTemplate = request.getChildText("template");
		BooleanClause.Occur templateOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
		TermQuery templateQuery = null;
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

		// metadata date range
		addDateRangeQuery(query, 
				request.getChildText("dateTo"), 
				request.getChildText("dateFrom"), 
				LuceneIndexField.CHANGE_DATE);

		// Revision, publication and creation dates may
		// have been index as temporal extent also.
		// data revision date range
		addDateRangeQuery(query, 
				request.getChildText("revisionDateTo"), 
				request.getChildText("revisionDateFrom"), 
				LuceneIndexField.REVISION_DATE);

		// data publication date range
		addDateRangeQuery(query, 
				request.getChildText("publicationDateTo"), 
				request.getChildText("publicationDateFrom"), 
				LuceneIndexField.PUBLICATION_DATE);

		// data creation date range
		addDateRangeQuery(query, 
				request.getChildText("creationDateTo"), 
				request.getChildText("creationDateFrom"), 
				LuceneIndexField.CREATE_DATE);


        //
        // Temporal extent : finds records where temporal extent overlaps the search extent
        //
        String extTo = request.getChildText("extTo");
        String extFrom = request.getChildText("extFrom");


        if((extTo != null && extTo.length() > 0) || (extFrom != null && extFrom.length() > 0)) {
            BooleanQuery temporalExtentQuery = new BooleanQuery();
            BooleanClause.Occur temporalExtentOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            BooleanClause.Occur temporalRangeQueryOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);

			Term lowerTerm = null;
			Term upperTerm = null;
			RangeQuery temporalRangeQuery = null;

			// temporal extent start is within search extent
            if(extFrom != null) {
				lowerTerm = new Term(LuceneIndexField.TEMPORALEXTENT_BEGIN , extFrom);
			}
			if(extTo != null) {
				upperTerm = new Term(LuceneIndexField.TEMPORALEXTENT_BEGIN, extTo);
			}
			temporalRangeQuery = new RangeQuery(lowerTerm, upperTerm, true);
			BooleanClause temporalRangeQueryClause = new BooleanClause(temporalRangeQuery, temporalRangeQueryOccur);
			
            temporalExtentQuery.add(temporalRangeQueryClause);


            // or temporal extent end is within search extent
            lowerTerm = null;
			upperTerm = null;

            if(extFrom != null) {
				lowerTerm = new Term(LuceneIndexField.TEMPORALEXTENT_END , extFrom);
			}

			if(extTo != null) {
				upperTerm = new Term(LuceneIndexField.TEMPORALEXTENT_END, extTo);
			}
            temporalRangeQuery = new RangeQuery(lowerTerm, upperTerm, true);
            temporalRangeQueryClause = new BooleanClause(temporalRangeQuery, temporalRangeQueryOccur);

            temporalExtentQuery.add(temporalRangeQueryClause);

            //or temporal extent contains search extent
            if((extTo != null && extTo.length() > 0) && (extFrom != null && extFrom.length() > 0)) {
                BooleanQuery bq = new BooleanQuery();

                lowerTerm = null;
                upperTerm = null;
                RangeQuery rangeQuery = null;
                lowerTerm = new Term(LuceneIndexField.TEMPORALEXTENT_END , extTo);
                temporalRangeQuery = new RangeQuery(lowerTerm, null, true);
                temporalRangeQueryClause = new BooleanClause(temporalRangeQuery, temporalExtentOccur);

                bq.add(temporalRangeQueryClause);

                lowerTerm = null;
                upperTerm = new Term(LuceneIndexField.TEMPORALEXTENT_BEGIN, extFrom);

                temporalRangeQuery = new RangeQuery(lowerTerm, upperTerm, true);
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
		BooleanClause metadataStandardNameClause = requiredTextField(request.getChildText("metadataStandardName"), LuceneIndexField.METADATA_STANDARD_NAME, similarity);
		if(metadataStandardNameClause != null) {
			query.add(metadataStandardNameClause);
		}

        // schema
        //
        BooleanClause schemaClause = requiredTextField(request.getChildText("_schema"), LuceneIndexField.SCHEMA, similarity);
        if(schemaClause != null) {
           query.add(schemaClause);
        }

        // parentUuid
		//
		BooleanClause parentUuidClause = requiredTextField(request.getChildText("parentUuid"), LuceneIndexField.PARENTUUID, similarity);
		if(parentUuidClause != null) {
			query.add(parentUuidClause);
		}

        // operatesOn
		//
		BooleanClause operatesOnClause = requiredTextField(request.getChildText("operatesOn"), LuceneIndexField.OPERATESON, similarity);
		if(operatesOnClause != null) {
			query.add(operatesOnClause);
		}

		//
		// type
		//
		BooleanClause typeClause = requiredTextField(request.getChildText("type"), LuceneIndexField.TYPE, similarity);
		if(typeClause != null) {
			query.add(typeClause);
		}

        //
		// inspire
		//
		String inspire = request.getChildText("inspire");
		if(inspire != null) {
			TermQuery inspireQuery = new TermQuery(new Term(LuceneIndexField.INSPIRE_CAT, inspire));
            BooleanClause.Occur inspireOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);

			BooleanClause inspireClause = new BooleanClause(inspireQuery, inspireOccur);
			query.add(inspireClause);
		}

		//
		// inspireTheme
		//
		@SuppressWarnings("unchecked")
		List<Element> inspireThemes = (List<Element>)request.getChildren("inspiretheme");
		if(inspireThemes != null && inspireThemes.size() > 0) {
			BooleanQuery inspireThemesQuery = new BooleanQuery();
			BooleanClause.Occur inspireThemesOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
			for(Iterator<Element> i = inspireThemes.iterator(); i.hasNext();) {
				String inspireTheme = i.next().getText();
				inspireTheme = inspireTheme.trim();
				if(inspireTheme.length() > 0) {
					// some clients (like GN's GUI) stupidly append a * already. Prevent them here:
					if(inspireTheme.endsWith("*")) {
						inspireTheme = inspireTheme.substring(0, inspireTheme.length()-1);
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
				        phraseQuery.add(new Term(LuceneIndexField.INSPIRE_THEME, phraseElement));
				    }
				    inspireThemesQuery.add(phraseQuery, phraseOccur);
				}
			}
			query.add(inspireThemesQuery, inspireThemesOccur);
		}

		//
		// inspireannex
		//
		BooleanClause inspireannexQuery = requiredTextField(request.getChildText("inspireannex"), LuceneIndexField.INSPIRE_ANNEX, similarity);
		if(inspireannexQuery != null) {
			query.add(inspireannexQuery);
		}
        
		//
		// siteId / source
		//
		BooleanClause sourceQuery = requiredTextField(request.getChildText("siteId"), LuceneIndexField.SOURCE, similarity);
		if(sourceQuery != null) {
			query.add(sourceQuery);
		}

        //
        // themekey
        //
        @SuppressWarnings("unchecked")
        List<Element> themeKeys = (List<Element>)request.getChildren("themekey");
        if(themeKeys != null && themeKeys.size() > 0) {
            for(Iterator<Element> i = themeKeys.iterator(); i.hasNext();) {
                BooleanQuery allkeywordsQuery = new BooleanQuery();
                BooleanClause.Occur allKeywordsOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);

                String themeKey = i.next().getText();
                if (StringUtils.hasText(themeKey)) {
                    BooleanClause.Occur keywordOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
                    // TODO: Check separator
                    String[] tokens = StringUtils.delimitedListToStringArray(themeKey," or ");
                    for(int j = 0; j < tokens.length; j++) {
                        String token = tokens[j];
                        token = token.trim();
                        if(token.startsWith("\"")) {
                            token = token.substring(1);
                        }
                        if(token.endsWith("\"")) {
                            token = token.substring(0, token.length() - 1);
                        }
                        //
                        TermQuery keywordQuery = new TermQuery(new Term(LuceneIndexField.KEYWORD, token));
                        BooleanClause keywordClause = new BooleanClause(keywordQuery, keywordOccur);
                        allkeywordsQuery.add(keywordClause);
                    }
                }

                if (allkeywordsQuery.clauses().size() > 0) {
                    query.add(allkeywordsQuery, allKeywordsOccur);
                }
            }
        }

		//
		// digital and paper maps
		//
		String digital = request.getChildText("digital");
        String paper = request.getChildText("paper");

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
		BooleanClause titleQuery = requiredTextField(request.getChildText("title"), LuceneIndexField.TITLE, similarity);
		if(titleQuery != null) {
			query.add(titleQuery);
		}

		//
		// abstract
		//
		BooleanClause abstractQuery = requiredTextField(request.getChildText("abstract"), LuceneIndexField.ABSTRACT, similarity);
		if(abstractQuery != null) {
			query.add(abstractQuery);
		}

		//
		// bounding box
		//
		// TODO handle regions if set
		// Note that this has been removed from the NGR search options
		Element region = request.getChild("region");
		Element regionData = request.getChild("regions");


		String eastBL = request.getChildText("eastBL");
		String westBL = request.getChildText("westBL");
		String northBL = request.getChildText("northBL");
		String southBL = request.getChildText("southBL");
		String relation = request.getChildText("relation");

		addBoundingBoxQuery(query, relation, eastBL, westBL, northBL, southBL);

//		DEBUG
//		System.out.println("\n\nLuceneQueryBuilder: query is\n" + query + "\n\n");
		Log.debug(Geonet.SEARCH_ENGINE, "\n\nLuceneQueryBuilder: query is\n" + query + "\n\n");

		return query;
	}

	
	private void addDateRangeQuery(BooleanQuery query, String dateTo,
			String dateFrom, String luceneIndexField) {
		if((dateTo != null && dateTo.length() > 0) || (dateFrom != null && dateFrom.length() > 0)) {
			Term lowerTerm = null;
			Term upperTerm = null;
			RangeQuery rangeQuery = null;
			if(dateFrom != null) {
				lowerTerm = new Term(luceneIndexField, dateFrom);
			}
			if(dateTo != null) {
				// while the 'from' parameter can be short (like yyyy-mm-dd)
				// the 'until' parameter must be long to match
				if(dateTo.length() == 10) {
					dateTo = dateTo + "T23:59:59";
				}
				upperTerm = new Term(luceneIndexField, dateTo);
			}
			rangeQuery = new RangeQuery(lowerTerm, upperTerm, true);
			BooleanClause.Occur dateOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
			BooleanClause dateRangeClause = new BooleanClause(rangeQuery, dateOccur);
			query.add(dateRangeClause);
		}
	}
	
	/**
	 * Handle geographical search 
	 * FIXME : should be handle via spatial index search
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

		// ignore negative values
		if (eastBL != null) {
			eastBL = toPositiveValue(eastBL);
		}
		if (westBL != null) {
			westBL = toPositiveValue(westBL);
		}
		if (northBL != null) {
			northBL = toPositiveValue(northBL);
		}
		if (southBL != null) {
			southBL = toPositiveValue(southBL);
		}

		// Handle relation parameter
		if (relation == null)
			return;

		// Default inclusive value for TermRangeQuery (includeLower and includeUpper)
		boolean inclusive = true;

		// Default Occur value for BBox query
		BooleanClause.Occur defaultBBoxOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);

		//
		// overlaps (default value) : uses the equivalence
		// -(a + b + c + d) = -a * -b * -c * -d
		//
		if (relation.equals(Geonet.SearchResult.Relation.OVERLAPS)) {
			// eastBL
			if (westBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(westBL) + 1);
				String upperTerm = maxBoundingLongitudeValue;
				query.add(getBBoxTermRangeQuery(LuceneIndexField.EAST, lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// westBL
			if (eastBL != null) {
				String lowerTerm = minBoundingLongitudeValue;
				String upperTerm = Double.toString(Double.parseDouble(eastBL) - 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.WEST, lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// northBL
			if (southBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(southBL) + 1);
				String upperTerm = maxBoundingLatitudeValue;
				query.add(getBBoxTermRangeQuery(LuceneIndexField.NORTH, lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// southBL
			if (northBL != null) {
				String lowerTerm = minBoundingLatitudeValue;
				String upperTerm = Double.toString(Double.parseDouble(northBL) - 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.SOUTH, lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
		}
		//
		// equal: coordinates of the target rectangle within 1 degree from
		// corresponding ones of metadata rectangle
		//
		else if (relation.equals(Geonet.SearchResult.Relation.EQUAL)) {
			// eastBL
			if(eastBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(eastBL) - 1);
				String upperTerm = Double.toString(Double.parseDouble(eastBL) + 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.EAST,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// westBL
			if(westBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(westBL) - 1);
				String upperTerm = Double.toString(Double.parseDouble(westBL) + 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.WEST,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// northBL
			if(northBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(northBL) - 1);
				String upperTerm = Double.toString(Double.parseDouble(northBL) + 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.NORTH,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// southBL
			if(southBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(southBL) - 1);
				String upperTerm = Double.toString(Double.parseDouble(southBL) + 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.SOUTH,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
		}
		//
		// encloses: metadata rectangle encloses target rectangle shrunk by 1 degree
		//
		else if(relation.equals(Geonet.SearchResult.Relation.ENCLOSES)) {
			// eastBL
			if(eastBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(eastBL) - 1);
				String upperTerm = maxBoundingLongitudeValue;
				query.add(getBBoxTermRangeQuery(LuceneIndexField.EAST,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// westBL
			if(westBL != null) {
				String lowerTerm = minBoundingLongitudeValue;
				String upperTerm = Double.toString(Double.parseDouble(westBL) + 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.WEST,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// northBL
			if(northBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(northBL) - 1);
				String upperTerm = maxBoundingLatitudeValue;
				query.add(getBBoxTermRangeQuery(LuceneIndexField.NORTH,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// southBL
			if(southBL != null) {
				String lowerTerm = minBoundingLatitudeValue;
				String upperTerm = Double.toString(Double.parseDouble(southBL) + 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.SOUTH,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
		}
		//
		// fullyEnclosedWithin: metadata rectangle fully enclosed within target rectangle augmented by 1 degree
		//
		else if(relation.equals(Geonet.SearchResult.Relation.ENCLOSEDWITHIN)) {
			// eastBL
			if(eastBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(westBL) - 1);
				String upperTerm = Double.toString(Double.parseDouble(eastBL) + 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.EAST,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// westBL
			if(westBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(westBL) - 1);
				String upperTerm = Double.toString(Double.parseDouble(eastBL) + 1); 
				query.add(getBBoxTermRangeQuery(LuceneIndexField.WEST,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// northBL
			if(northBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(southBL) - 1);
				String upperTerm = Double.toString(Double.parseDouble(northBL) + 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.NORTH,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// southBL
			if(southBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(southBL) - 1);
				String upperTerm = Double.toString(Double.parseDouble(northBL) + 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.SOUTH,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
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
				String lowerTerm = minBoundingLongitudeValue;
				String upperTerm = Double.toString(Double.parseDouble(westBL) + 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.EAST,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// westBL
			if(eastBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(eastBL) - 1);
				String upperTerm = maxBoundingLongitudeValue; 
				query.add(getBBoxTermRangeQuery(LuceneIndexField.WEST,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// northBL
			if(southBL != null) {
				String lowerTerm = minBoundingLatitudeValue;
				String upperTerm = Double.toString(Double.parseDouble(southBL) + 1);
				query.add(getBBoxTermRangeQuery(LuceneIndexField.NORTH,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
			// southBL
			if(northBL != null) {
				String lowerTerm = Double.toString(Double.parseDouble(northBL) - 1);
				String upperTerm = maxBoundingLatitudeValue;
				query.add(getBBoxTermRangeQuery(LuceneIndexField.SOUTH,lowerTerm, upperTerm, inclusive), defaultBBoxOccur);
			}
		}
	}
	
	/**
	 * Build TermRangeQuery for bounding box values
	 * 
	 * @param field - The field that holds both lower and upper terms.
	 * @param lowerTerm - The term text at the lower end of the range
	 * @param upperTerm - The term text at the upper end of the range
	 * @param inclusive - If true, the lowerTerm and upperTerm are included in the range.
	 * @return
	 */
	private TermRangeQuery getBBoxTermRangeQuery(String field,
			String lowerTerm, String upperTerm, boolean inclusive) {

		TermRangeQuery bBoxValueQuery = new TermRangeQuery(field, lowerTerm,
				upperTerm, inclusive, inclusive);

		return bBoxValueQuery;
	}
	
	
	/**
	 * Ignore negative bounding box values 
	 * 
	 * @param boundingBoxValue
	 * @return String
	 */
	private String toPositiveValue (String boundingBoxValue) {
		double tmpBoundingBoxValue = Double.parseDouble(boundingBoxValue) ;
		boundingBoxValue = new Double(360 + tmpBoundingBoxValue).toString();
		return boundingBoxValue;
	}
}
