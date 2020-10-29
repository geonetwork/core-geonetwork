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
  goog.provide('gn_es_client');

  var module = angular.module('gn_es_client', []);


  var ES_API_URL = '../api/search/records/';

  module.service('gnESClient', [
    '$http',
    'Metadata',
    'gnESFacet',
    'gnESService',
    function($http, Metadata, gnESFacet, gnESService) {

    this.getUrl = function(service) {
      return ES_API_URL + service;
    };

    this.search = function(params, selectionBucket, configId) {
      return callApi('_search', params, selectionBucket).then(
        function(response) {
          return gnESFacet.getUIModel(response, params, configId);
        }
      );
    };

    this.suggest = function(field, query, searchObj) {
      var params = gnESService.getSuggestParams(field, query, searchObj);
      return callApi('_search', params).then(
        function(response) {
          var d = response.data.hits.hits.flatMap(function(md) {
            if (field) {
              return new Metadata(md)[field];
            } else {
              var values = [];
              for (p in new Metadata(md)) {
                values = values.concat(md[p]);
              };
              return values;
            }
          });
          return d;
        }
      );
    };

    this.suggestAny = function(query) {
      var params = gnESService.getSuggestAnyParams(query);
      return callApi('_search', params).then(
        function(response) {
          return response.data.hits.hits.map(function(md) {
            return md._source[field];
          });
        }
      );
    };

    this.getTermsParamsWithNewSizeOrFilter = function(
      query, facetPath, newSize, include, exclude, facetConfig) {
      var params = gnESService.getTermsParamsWithNewSizeOrFilter(
        query, facetPath, newSize, include, exclude, facetConfig);
      return callApi('_search', params).then(
        function(response) {
          var model = gnESFacet.getUIModel(response, params);
          return model.facets[0];
        }
      );
    };

    function callApi(service, params, selectionBucket) {
      return $http.post(ES_API_URL + service + (selectionBucket ? '?bucket=' + selectionBucket : ''), params);
    }

  }]);
})();
