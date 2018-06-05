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

  var MAX_ROWS = 10;

  module.service('gnESFacet', ['gnGlobalSettings', function(gnGlobalSettings) {

    this.configs = gnGlobalSettings.gnCfg.mods.search.facetConfig;

    this.addFacets = function(esParams, type) {
      esParams.aggregations = this.getAggs(type);
    };

    this.getAggs = function(type) {
      var config = this.configs[type];

      var aggs = {};
      var fieldNames = [
        'tag',
        'codelist_spatialRepresentationType'
        //'sourceCatalogue',
        //'docType',
        //'publicationYearForResource',
        //'topic'
      ];
      fieldNames.forEach(function(fieldName) {
        aggs[fieldName] = {
          terms: {
            field: fieldName,
            size: MAX_ROWS
          }
        };
      });
      return aggs;
    };

    this.getUIModel = function(response, request) {
      var listModel = [];
      for (var fieldId in response.data.aggregations) {
        var respAgg = response.data.aggregations[fieldId];
        var reqAgg = request.aggregations[fieldId];

        var facetModel = {
          name: fieldId,
          items: []
        };

        if(reqAgg.hasOwnProperty('terms')) {
          facetModel.type = 'terms';
          facetModel.size = reqAgg.terms.size;
          respAgg.buckets.forEach(function(bucket) {
            if(bucket.key) {
              facetModel.items.push({
                name: bucket.key,
                count: bucket.doc_count
              });
            }
          });
        }

        listModel.push(facetModel);

      }

      response.data.facets = listModel;
      return response.data;
    };


  }]);
})();
