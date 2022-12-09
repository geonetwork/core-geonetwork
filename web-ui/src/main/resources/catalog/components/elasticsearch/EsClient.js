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

(function () {
  goog.provide("gn_es_client");

  var module = angular.module("gn_es_client", []);

  var ES_API_URL = "../api/search/records/";

  module.service("gnESClient", [
    "$http",
    "Metadata",
    "gnESFacet",
    "gnESService",
    "gnGlobalSettings",
    function ($http, Metadata, gnESFacet, gnESService, gnGlobalSettings) {
      this.getUrl = function (service) {
        return ES_API_URL + service;
      };

      this.search = function (params, selectionBucket, configId, types) {
        return callApi("_search", params, selectionBucket, types).then(function (
          response
        ) {
          return gnESFacet.getUIModel(response, params, configId);
        });
      };

      this.suggest = function (field, query, searchObj) {
        var params = gnESService.getSuggestParams(field, query, searchObj);
        return callApi("_search", params).then(function (response) {
          var d = response.data.hits.hits.flatMap(function (md) {
            var md = new Metadata(md),
              that = md;
            // Suggestion on something else than the title is experimental.
            // It is probably better to use aggregation for it.
            // It is currently not used in the application.
            if (field) {
              var path = field.split("."),
                values = [],
                v = md;
              path.forEach(function (p) {
                if (v) {
                  v = angular.isArray(v)
                    ? v.map(function (o) {
                        return o[p];
                      })
                    : v[p];
                }
              });
              (v || []).forEach(function (value) {
                values.push({ label: value, record: md });
              });
              return values;
            } else {
              return { label: _.get(md, field) || md.resourceTitle, record: md };
            }
          });

          if (field) {
            var groups = _.groupBy(d, "label");
            return Object.keys(groups).map(function (g) {
              return { label: g, record: groups[g] };
            });
          } else {
            return d;
          }
        });
      };

      this.suggestAny = function (query) {
        var params = gnESService.getSuggestAnyParams(query);
        return callApi("_search", params).then(function (response) {
          return response.data.hits.hits.map(function (md) {
            return md._source[field];
          });
        });
      };

      this.getTermsParamsWithNewSizeOrFilter = function (
        query,
        key,
        facetConfig,
        newSize,
        include,
        exclude
      ) {
        var params = gnESService.getTermsParamsWithNewSizeOrFilter(
          query,
          key,
          facetConfig,
          newSize,
          include,
          exclude
        );
        return callApi("_search", params).then(function (response) {
          var model = gnESFacet.getUIModel(response, params);
          return model.facets[0];
        });
      };

      function callApi(service, params, selectionBucket, types) {
        var types = types || [];

        return $http.post(
          ES_API_URL +
            service +
            (selectionBucket ? "?bucket=" + selectionBucket : "") +
            (types.length > 0 ? [""].concat(types).join("&relatedType=") : ""),
          params
        );
      }
    }
  ]);
})();
