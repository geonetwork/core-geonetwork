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
  goog.provide('gn_es_facet');

  var module = angular.module('gn_es_facet', []);

  var DEFAULT_SIZE = 10;

  module.service('gnESFacet', ['gnGlobalSettings', 'gnTreeFromSlash', function(gnGlobalSettings, gnTreeFromSlash) {

    this.configs = {
      search: {
        facets: gnGlobalSettings.gnCfg.mods.search.facetConfig,
        source: {
          includes: [
            'uuid',
            'id',
            'creat*',
            'group*',
            'logo',
            'category',
            'topicCat',
            'inspire*',
            'resource*',
            'draft',
            'overview.*',
            'owner*',
            'link*',
            'image*',
            'status*',
            'rating',
            'tag*',
            'geom'
          ]
        }
      },
      home: {
        facets: gnGlobalSettings.gnCfg.mods.home.facetConfig,
        source: {
          includes: [
            'id',
            'uuid',
            'creat*',
            'topicCat',
            'inspire*',
            'resource*',
            'image*',
            'tag*'
          ]
        }
      },
      editor: {
        facets: gnGlobalSettings.gnCfg.mods.editor.facetConfig,
        source: {}
      }
    };

    this.addFacets = function(esParams, type) {
      var aggs = typeof type === 'string' ? this.configs[type].facets : type;
      esParams.aggregations = aggs;
    };

    this.addSourceConfiguration = function(esParams, type) {
      var source = typeof type === 'string' ? this.configs[type].source : type;
      esParams._source = source;
    };

    this.getFacetsFromPayloads = function(response, request) {
      return createFacetModel(request.aggregations, response.data.aggregations);
    };

    function createFacetModel(reqAggs, respAggs, isNested, path) {
      var listModel = [];
      for (var fieldId in respAggs) {
        var respAgg = respAggs[fieldId];
        var reqAgg = reqAggs[fieldId];

        var facetModel = {
          key: fieldId,
          items: []
        };

        if (reqAgg.hasOwnProperty('terms')) {

          if(fieldId.endsWith('_tree')) {
            facetModel.type = 'tree';
            var tree = gnTreeFromSlash.getTree(respAgg.buckets);
            facetModel.items = tree.items;
          } else {
            facetModel.type = 'terms';
            facetModel.size = reqAgg.terms.size;
            respAgg.buckets.forEach(function (bucket) {
              if (bucket.key) {
                var itemPath = (path || []).concat([fieldId, bucket.key + ''])
                var facet = {
                  value: bucket.key,
                  count: bucket.doc_count,
                  path: itemPath
                };
                // nesting
                if(isNested) {
                  facet.isNested = true
                }
                if(reqAgg.hasOwnProperty('aggs')) {
                  var nestAggs = {}
                  for (var indexKey in reqAgg.aggs) {
                    nestAggs[indexKey] = bucket[indexKey]
                  }
                  facet.aggs = createFacetModel(reqAgg.aggs, nestAggs, true, itemPath)
                }
                facetModel.items.push(facet);
              }
            });

          }

        } else if (reqAgg.hasOwnProperty('filters')) {
          facetModel.type = 'filters';
          facetModel.size = DEFAULT_SIZE;
          for (var p in respAgg.buckets) {
            facetModel.items.push({
              value: p,
              path: [fieldId, p],
              query_string: reqAgg.filters.filters[p],
              count: respAgg.buckets[p].doc_count
            });
          }
        }
        listModel.push(facetModel);
      }
      return listModel;
    }

  }]);
})();
