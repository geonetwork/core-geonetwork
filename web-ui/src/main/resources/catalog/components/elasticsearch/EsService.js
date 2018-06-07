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

  module.service('gnESService', ['gnESFacet', function(gnESFacet) {

    this.convertLuceneParams = function(p) {
      var params = {};
      var query = {
        bool: {
          must: []
        }
      };
      var query_string;

      var excludeFields = ['_content_type', 'fast', 'from', 'to', 'bucket',
        'sortBy', 'resultType', 'facet.q', 'any',
        'creationDateFrom', 'creationDateTo', 'dateFrom', 'dateTo'];
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
        params.size = p.to - p.from;
      }
      if(p.any) {
        query_string = {
          query: p.any
        };
      }
      if(p.sortBy) {
        var sort = {};
        params.sort = [];
        if(p.sortBy != 'relevance') {
          sort[getFieldName(mappingFields, p.sortBy)] = 'asc';
          params.sort.push(sort);
        }
        params.sort.push('_score');
      }

      // ranges criteria (for dates)
      if (p.creationDateFrom || p.creationDateTo) {
        query.bool.must.push({
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
        query.bool.must.push({
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
        var terms = {};
        terms[prop] = termss[prop];
        query.bool.must.push({
          terms: terms
        });
      }

      if(query_string) {
        query.bool.must.push({
          query_string: query_string
        });
      }

      params.query = query;
      gnESFacet.addFacets(params, 'mainsearch');
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
      var phrase = {
        query: query,
        max_expansions: 1
      };
      var match = {};
      match[field] = phrase;
      var params = {
        query: {
          match_phrase_prefix: match
        },
        _source: [field]
      };
      return params;
    };

    this.getSuggestParamsTrigram = function(field, query) {
      var params = {
        suggest: {
          text: query,
          simple_phrase: {
            phrase: {
              field: field + '.trigram',
              direct_generator: [ {
                field: field + '.trigram',
                suggest_mode: 'always'
              } ],
              highlight: {
                pre_tag: '<em>',
                post_tag: '</em>'
              }
            }
          }
        }
      };
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

  }]);
})();
