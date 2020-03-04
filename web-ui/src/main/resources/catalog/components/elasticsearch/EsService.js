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
    'gnESFacet', 'gnEsLuceneQueryParser', 'gnGlobalSettings',
    function(
      gnESFacet, gnEsLuceneQueryParser, gnGlobalSettings) {


    this.facetsToLuceneQuery = function(facetsState) {
      return gnEsLuceneQueryParser.facetsToLuceneQuery(facetsState);
    }

    this.generateEsRequest = function(p, searchState, searchConfigId) {
      var params = {};
      var luceneQueryString = gnEsLuceneQueryParser.facetsToLuceneQuery(searchState.filters);
      var query = {
        "function_score": {
          "query": {
            bool: {
              must: []
            }
          },
          // Score experiments:
          // a)Score down old records
          // "gauss": {
          //   "dateStamp": {
          //     "scale":  "200d"
          //   }
          // }
          // b)Promote grids!
          // "boost": "5",
          // "functions": [
          //   {
          //     "filter": { "match": { "codelist_spatialRepresentationType": "vector" } },
          //     "random_score": {},
          //     "weight": 23
          //   },
          //   {
          //     "filter": { "match": { "codelist_spatialRepresentationType": "grid" } },
          //     "weight": 42
          //   }
          // ],
          // "max_boost": 42,
          // "score_mode": "max",
          // "boost_mode": "multiply",
          // "min_score" : 42
          "script_score" : {
            "script" : {
              "source": "Math.log(2 + doc['rating'].value)"
            }
          }
        }
      };
      var queryHook = query.function_score.query.bool.must;
      // var query = {
      //   bool: {
      //     must: []
      //   }
      // };
      // var queryHook = query.bool.must;

      var query_string;
      var excludeFields = ['_content_type', 'fast', 'from', 'to', 'bucket',
        'sortBy', 'sortOrder', 'resultType', 'facet.q', 'any', 'geometry', 'query_string',
        'creationDateFrom', 'creationDateTo', 'dateFrom', 'dateTo', 'geom', 'relation'];
      var mappingFields = {
        title: 'resourceTitle',
        abstract: 'resourceAbstract',
        type: 'resourceType',
        keyword: 'tag'
      };

      if(p.from) {
        params.from = p.from - 1;
      }
      if(p.to) {
        params.size = (p.to + 1) - p.from;
      }
      if(p.any || luceneQueryString) {
        var queryStringParams = [];
        if (p.any) {
          queryStringParams.push(p.any);
        }
        if (luceneQueryString) {
          queryStringParams.push(luceneQueryString);
        }
        query_string = {
          query: queryStringParams.join(' AND ').trim()
        };
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

      // ranges criteria (for dates)
      if (p.creationDateFrom || p.creationDateTo) {
        queryHook.push({
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
        queryHook.push({
          range: {
            changeDate : {
                gte: p.dateFrom || undefined,
                lte: p.dateTo || undefined,
                format: 'yyyy-MM-dd'
            }
          }
        });
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
        var value = termss[prop], isWildcard = value[0].indexOf('*') !== -1;
        var queryType = isWildcard ? 'wildcard' : 'terms';
        var clause = {};
        var field = {};
        if (isWildcard) {
          if (value.length > 1) {
            console.warn("Wildcard query not supported on array of values.",
              value);
          }
          field[prop] = value[0];
        } else {
          field[prop] = value;
        }
        clause[queryType] = field;
        queryHook.push(clause);
      }

      if(query_string) {
        queryHook.push({
          query_string: query_string
        });
      }
      if(p.geometry) {
        var geom = new ol.format.WKT().readGeometry(p.geometry)
        var extent = geom.getExtent()
        var coordinates = [
          [extent[0], extent[3]],
          [extent[2], extent[1]]
        ];

        queryHook.push({
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
    this.getSuggestParams = function(field, query) {
      var params = gnGlobalSettings.gnCfg.mods.search.autocompleteConfig;
      params.query.multi_match.query = query;
      return params;
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

    this.getMoreTermsParams = function(query, facetPath, newSize, facetConfig) {
      var params = {
        query: query || {bool: {must: []}},
        size: 0
      };
      var aggregations = params;
      for (var i = 0; i < facetPath.length; i++) {
        if ((i + 1) % 2 === 0) continue;
        var key = facetPath[i];
        aggregations.aggregations = {};
        aggregations.aggregations[key] = facetConfig[key];
        if (aggregations.aggregations[key].terms) {
          aggregations.aggregations[key].terms.size = newSize;
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
