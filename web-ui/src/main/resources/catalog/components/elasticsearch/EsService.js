/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {
  goog.provide('gn_es_service');

  var module = angular.module('gn_es_service', []);

  module.service('gnESService', [
    'gnESFacet', 'gnEsLuceneQueryParser', 'gnGlobalSettings', '$rootScope',
    function(
      gnESFacet, gnEsLuceneQueryParser, gnGlobalSettings, $rootScope) {

      var mappingFields = {
        title: 'resourceTitle',
        abstract: 'resourceAbstract',
        type: 'resourceType',
        keyword: 'tag'
      };

      // https://lucene.apache.org/core/3_4_0/queryparsersyntax.html#Escaping%20Special%20Characters
      function escapeSpecialCharacters(luceneQueryString) {
        return luceneQueryString.replace(
          /(\+|-|&&|\|\||!|\{|\}|\[|\]\^|\~|\*|\?|:|\\{1}|\(|\))/g,
          '\\$1');
      };

      this.facetsToLuceneQuery = function(facetsState) {
        return gnEsLuceneQueryParser.facetsToLuceneQuery(facetsState);
      }


      /**
       * Build all clauses to be added to the Elasticsearch
       * query from current parameters.
       *
       * @param queryHook
       * @param p
       */
      this.buildQueryClauses = function(queryHook, p, luceneQueryString, exactMatch) {
        var excludeFields = ['_content_type', 'fast', 'from', 'to', 'bucket',
          'sortBy', 'sortOrder', 'resultType', 'facet.q', 'any', 'geometry', 'query_string',
          'creationDateFrom', 'creationDateTo', 'dateFrom', 'dateTo', 'geom', 'relation',
          'editable'];
        if(p.any || luceneQueryString) {
          var queryStringParams = [];
          if (p.any) {
            p.any = p.any.toString();
            var queryExpression = p.any.match(/^q\((.*)\)$/);
            if (queryExpression == null) {
              // var queryBase = '${any} resourceTitleObject.default:(${any})^2',
              var queryBase = '(' + gnGlobalSettings.gnCfg.mods.search.queryBase + ')',
                  defaultQuery = '${any}';
              if (queryBase.indexOf(defaultQuery) === -1) {
                console.warn('Check your configuration. Query base \'' +
                  queryBase + '\' MUST contains a \'${any}\' token ' +
                  'to be replaced by the search text. ' +
                  'See mods.search.queryBase property. ' +
                  'Using default value \'${any}\'.');
                queryBase = defaultQuery;
              }
              var searchString = escapeSpecialCharacters(p.any),
                q = queryBase.replace(
                      /\$\{any\}/g,
                      exactMatch === true ? '\"' + searchString + '\"' : searchString);
              queryStringParams.push(q);
            } else {
              queryStringParams.push(queryExpression[1]);
            }

          }
          if (luceneQueryString) {
            queryStringParams.push(luceneQueryString);
          }
          queryHook.must.push({
            query_string: {
              query: queryStringParams.join(' AND ').trim()
            }
          });
        }

        // ranges criteria (for dates)
        if (p.creationDateFrom || p.creationDateTo) {
          queryHook.must.push({
            range: {
              createDate : {
                gte: p.creationDateFrom || undefined,
                lte: p.creationDateTo || undefined,
                format: 'yyyy-MM-dd'
              }
            }
          });
        }
        if (p.dateFrom || p.dateTo) {
          queryHook.must.push({
            range: {
              changeDate : {
                gte: p.dateFrom || undefined,
                lte: p.dateTo || undefined,
                format: 'yyyy-MM-dd'
              }
            }
          });
        }
        if (p.editable == 'true') {
          if ($rootScope.user.isEditorOrMore() && !$rootScope.user.isAdmin()) {
            // Append user group query
            if ($rootScope.user.groupsWithEditor.length > 0) {
              queryHook.must.push({
                terms: {
                  "op2" : $rootScope.user.groupsWithEditor
                }
              });
            }
          }
        }


        var termss = Object.keys(p).reduce(function(output, current) {
          var value = p[current];
          if(excludeFields.indexOf(current) < 0) {
            var newName = mappingFields[current] || current;
            if(!angular.isArray(value)) {
              value = [value];
            }
            output[newName] = value;
          }
          return output;
        }, {});

        for (var prop in termss) {
          var value = termss[prop],
            isNegative = prop.startsWith('-'),
            fieldName = isNegative ? prop.slice(1) : prop,
            isRange = value[0] && value[0].range !== undefined,
            isWildcard = value[0] && value[0].indexOf
              && value[0].indexOf('*') !== -1 || false,
            queryType = isWildcard ? 'wildcard' : 'terms',
            clause = null,
            field = {};
          if (isRange) {
            // "range" : {
            //   "resourceTemporalDateRange" : {
            //     "gte" : null,
            //     "lte" : null,
            //     "relation" : "intersects" // within, contains
            //   }
            // }
            var r = value[0].range[Object.keys(value[0].range)[0]],
              rangeBoundsDefined = r.gte != null && r.lte != null
            if(rangeBoundsDefined) {
              clause = value[0];
            }
          } else {
            if (isWildcard) {
              if (value.length > 1) {
                console.warn("Wildcard query not supported on array of values.",
                  value);
              }
              field[fieldName] = value[0];
            } else {
              field[fieldName] = value;
            }
            clause = {};
            clause[queryType] = field;
          }

          if(clause != null) {
            var condition = isNegative ? 'must_not' : 'must';
            if (!queryHook[condition]) {
              queryHook[condition] = [];
            }
            queryHook[condition].push(clause);
          }
        }

        if(p.geometry) {
          var geom = new ol.format.WKT().readGeometry(p.geometry)
          var extent = geom.getExtent()
          var coordinates = [
            [extent[0], extent[3]],
            [extent[2], extent[1]]
          ];

          queryHook.must.push({
            'geo_shape': {
              'geom': {
                'shape': {
                  'type': 'envelope',
                  'coordinates': coordinates
                },
                'relation': p.relation || 'intersects'
              }
            }
          });
        }
      };

      this.generateEsRequest = function(p, searchState, searchConfigId, filters) {
        var params = {};
        var luceneQueryString = gnEsLuceneQueryParser.facetsToLuceneQuery(searchState.filters);

        // A query with no score
        // var query = {
        //   bool: {
        //     must: []
        //   }
        // };
        // var queryHook = query.bool;
        var query = {}, defaultScore = {
          "script_score" : {
            "script" : {
              "source": "_score"
            }
          }
        };
        angular.copy({
          "function_score": gnGlobalSettings.gnCfg.mods.search.scoreConfig ?
            gnGlobalSettings.gnCfg.mods.search.scoreConfig : defaultScore
        }, query);
        query.function_score['query'] = {
          bool: {
            must: []
          }
        };

        if (angular.isArray(filters)) {
          query.function_score['query'].bool.filter = filters;
        }

        var queryHook = query.function_score.query.bool;
        this.buildQueryClauses(queryHook, p, luceneQueryString, searchState.exactMatch);

        if(p.from) {
          params.from = p.from - 1;
        }
        if(p.to) {
          params.size = (p.to + 1) - p.from;
        }
        if(p.sortBy) {
          var sort = {};
          params.sort = [];
          if(p.sortBy != 'relevance') {
            sort[getFieldName(mappingFields, p.sortBy)] = p.sortOrder || 'asc';
            params.sort.push(sort);
          }
          params.sort.push('_score');
        }

        params.query = query;

        // Collapse could be an option to group
        // features related to a record.
        // params.collapse = {
        //   "field": "recordGroup",
        //     "inner_hits": {
        //     "name": "others",
        //       "size": 30
        //   },
        //   "max_concurrent_group_searches": 4
        // };
        gnESFacet.addFacets(params, searchConfigId);
        gnESFacet.addSourceConfiguration(params, searchConfigId);


        return params;
      };

      /**
       * Return suggestion from a field, like title, while making a search
       * on the field and return the field value (value should be unique for
       * each document).
       *
       * @param field document field
       * @param query Completion query
       * @returns es request params
       */
      this.getSuggestParams = function(field, query, searchObj) {

        var params = {}, defaultScore = {
          "script_score" : {
            "script" : {
              "source": "_score"
            }
          }
        }, autocompleteQuery = {};
        angular.copy(gnGlobalSettings.gnCfg.mods.search.autocompleteConfig.query, autocompleteQuery);
        angular.copy({"query": {
          "function_score": gnGlobalSettings.gnCfg.mods.search.scoreConfig ?
            gnGlobalSettings.gnCfg.mods.search.scoreConfig : defaultScore
        }}, params);
        params.query.function_score['query'] = autocompleteQuery;


        var currentSearch = {};
        angular.copy(searchObj, currentSearch);

        // The multi_match will take care of the any filter.
        currentSearch.params.any = undefined;

        try {
          params.query.function_score.query.bool.must[0].multi_match.query = query;

          // Inject current search to contextualize suggestions
          var queryHook = params.query.function_score.query.bool;
          var luceneQueryString = currentSearch.state && currentSearch.state.filters ? gnEsLuceneQueryParser.facetsToLuceneQuery(currentSearch.state.filters) : undefined;

          this.buildQueryClauses(queryHook, currentSearch.params, luceneQueryString);

          return params;
        } catch (e) {
          console.warn("Suggestion query error. Could not find a query.bool.must[0].multi_match.query or query.bool.must in your autocompleteConfig query. Check your configuration.", e)
        }
      };

      /**
       * Get completion using the index type `completion` for a field
       * @param field
       * @param query
       * @returns {{suggest: {}, _source: *}}
       */
      this.getCompletion = function(field, query) {
        var suggest = {};
        suggest['completion'/*field.split('.')[0]*/] = {
          prefix : query,
          completion : {
            field : field
          }
        };
        return {
          suggest: suggest,
          _source: ''
        };
      };

      /**
       * Par es completion field response to match typeahead input format
       * @param response
       */
      this.parseCompletionResponse = function(response) {
        return response.suggest.completion[0].options.map(function(sugg) {
          return {
            name: sugg.text,
            id: sugg.text
          }
        });
      };

      // Using trigram
      // GET /records/_search
      // {
      //   "suggest": {
      //   "text": "espese",
      //     "simple_phrase": {
      //     "phrase": {
      //       "field": "tag.trigram",
      //         "direct_generator": [ {
      //         "field": "tag.trigram",
      //         "suggest_mode": "always"
      //       } ],
      //         "highlight": {
      //         "pre_tag": "<em>",
      //           "post_tag": "</em>"
      //       }
      //     }
      //   }
      // }
      // }


      this.getSuggestAnyParams = function(query) {

        var anyFields = ['resourceTitle', 'resourceAbstract'];
        var params = {
          query: {
            multi_match: {
              fields: anyFields,
              query: query,
              type: 'phrase_prefix'
            }
          },
          _source: anyFields
        };

        return params;
      };

      function getFieldName(mapping, name) {
        return mapping[name] || name;
      }

      this.getTermsParamsWithNewSizeOrFilter = function(query, facetPath,
                                         newSize,
                                         include,
                                         exclude,
                                         facetConfig) {
        var params = {
          query: query || {bool: {must: []}},
          size: 0
        };
        var aggregations = params;
        for (var i = 0; i < facetPath.length; i++) {
          if ((i + 1) % 2 === 0) continue;
          var key = facetPath[i],
              isFilter = angular.isDefined(include) || angular.isDefined(exclude);
          aggregations.aggregations = {};
          // Work on a copy of facetConfig to not alter main search
          // aggregations.aggregations[key] = facetConfig[key];
          aggregations.aggregations[key] = isFilter ?
            angular.copy(facetConfig[key], {}) :
            facetConfig[key];
          if (aggregations.aggregations[key].terms) {
            if (Number.isInteger(newSize)) {
              aggregations.aggregations[key].terms.size = newSize;
            }
            if (angular.isDefined(include)){
              var isARegex = include.match(/^\/.*\/$/) != null,
                  filter = '';

              // Note that ES filter on terms can not be case insensitive
              // See https://discuss.elastic.co/t/terms-aggregation-with-include-filter/50976/10
              // but we can still build a case insensitive regex.
              if (facetConfig[key].meta && facetConfig[key].meta.caseInsensitiveInclude) {
                filter = '.*' + include
                  .split('')
                  .map(function(l) {return '['+ l.toLowerCase() + l.toUpperCase() + ']'})
                  .join('') + '.*';
              } else {
                filter = isARegex ?
                          include.substr(1, include.length - 2) :
                          '.*' + include + '.*'
              }
              aggregations.aggregations[key].terms.include = filter;
            }
            if (angular.isDefined(exclude)){
              aggregations.aggregations[key].terms.exclude = exclude;
            }
          } else {
            console.warn(
              'Loading more results of a none terms directive is not supported',
              aggregations.aggregations[key]);
          }
          aggregations = aggregations.aggregations[key];
        }
        return params;
      };
  }]);
})();
