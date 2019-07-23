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

  module.service('gnESFacet', ['gnGlobalSettings', function(gnGlobalSettings) {

    this.configs = {
      search: gnGlobalSettings.gnCfg.mods.search.facetConfig,
      home: gnGlobalSettings.gnCfg.mods.home.facetConfig,
      editor: gnGlobalSettings.gnCfg.mods.editor.facetConfig,
    };

    this.addFacets = function(esParams, type) {
      esParams.aggregations = this.getAggregationFromConfig(type);
    };

    this.getAggregationFromConfig = function(typeOrConfig) {
      var aggs = typeof typeOrConfig === 'string' ?
        this.configs[typeOrConfig] : typeOrConfig;

      return aggs;
    };

    this.getUIModel = function(response, request) {
      var listModel;
      listModel = createFacetModel(request.aggregations, response.data.aggregations)
      response.data.facets = listModel;
      return response.data;
    };

    function createFacetModel(reqAggs, respAggs, isNested) {
      var listModel = [];
      for (var fieldId in respAggs) {
        var respAgg = respAggs[fieldId];
        var reqAgg = reqAggs[fieldId];

        var facetModel = {
          name: fieldId,
          items: []
        };

        if (reqAgg.hasOwnProperty('terms')) {
          facetModel.type = 'terms';
          facetModel.size = reqAgg.terms.size;
          respAgg.buckets.forEach(function (bucket) {
            if (bucket.key) {
              var facet = {
                name: bucket.key,
                count: bucket.doc_count
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
                facet.aggs = createFacetModel(reqAgg.aggs, nestAggs, true)
              }
              facetModel.items.push(facet);
            }
          });
        } else if (reqAgg.hasOwnProperty('filters')) {
          facetModel.type = 'filters';
          facetModel.size = DEFAULT_SIZE;
          for (var p in respAgg.buckets) {
            facetModel.items.push({
              name: p,
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
