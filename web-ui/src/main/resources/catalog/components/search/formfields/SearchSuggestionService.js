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
  goog.provide('gn_searchsuggestion_service');

  var module = angular.module('gn_searchsuggestion_service', [
  ]);

  /**
   * @ngdoc service
   * @kind function
   * @name gn_searchsuggestion.service:suggestService
   * @requires gnHttpServices
   * @requires gnUrlUtils
   * @requires $http
   *
   * @description
   * The `suggestService` service provides all tools required to get
   * suggestions from the index.
   * TODO: SOLR-MIGRATION-TO-DELETE or substitute by spell checker
   */
  module.service('suggestService', [
    'gnHttpServices',
    'gnUrlUtils',
    '$http',
    function(gnHttpServices, gnUrlUtils, $http) {

      /**
           * @ngdoc method
           * @methodOf gn_searchsuggestion.service:suggestService
           * @name suggestService#getUrl
           *
           * @description
           * Build the suggestion url with given parameters.
           *
           * @param {string} filter the filter for search query.
           * @param {string} field index field you search on.
           * @param {string} sortBy option.
           * @return {string} service
           */
      this.getUrl = function(filter, field, sortBy) {
        return gnUrlUtils.append(gnHttpServices.suggest,
            gnUrlUtils.toKeyValue({
              field: field,
              sortBy: sortBy,
              q: filter || ''
            })
        );
      };

      /**
           * @ngdoc method
           * @methodOf gn_searchsuggestion.service:suggestService
           * @name suggestService#getInfoUrl
           *
           * @description
           * Return info service url, depending on the type.
           *
           * @param {string} type of info
           * @return {string} url
           */
      this.getInfoUrl = function(type) {
        return gnUrlUtils.append(gnHttpServices.info,
            gnUrlUtils.toKeyValue({
              type: type
            })
        );
      };

      /**
           * @ngdoc method
           * @methodOf gn_searchsuggestion.service:suggestService
           * @name suggestService#getAnySuggestions
           *
           * @description
           * Return suggestion for field 'any'
           *
           * @param {string} val any filter
           * @return {HttpPromise} promise
           */
      this.getAnySuggestions = function(val) {
        var url = this.getUrl(val, 'anylight',
            ('STARTSWITHFIRST'));

        return $http.get(url, {
        }).then(function(res) {
          return res.data[1];
        });
      };


      /**
           * @ngdoc method
           * @methodOf gn_searchsuggestion.service:suggestService
           * @name suggestService#filterResponse
           *
           * @description
           * Must return an array of datum. It is contained
           * in the second element of the json response.
           *
           * @param {Array} data needed to be filtered
           * @return {?Array} suggestions array
           */
      this.filterResponse = function(data) {
        return data[1];
      };

      /**
           * @ngdoc method
           * @methodOf gn_searchsuggestion.service:suggestService
           * @name suggestService#bhFilter
           *
           * @description
           * Must return an array of datum objects(id/name). It is contained
           * in the second element of the json response.
           *
           * @param {Array} data needed to be filtered
           * @return {?Array} suggestions array
           */
      this.bhFilter = function(data) {
        var datum = [];
        data[1].forEach(function(item) {
          datum.push({ id: item, name: item });
        });
        return datum;
      };

    }]);

  /**
   * Experiment SOLR spell check API
   */
  module.service('spellCheckService', [
    'gnHttpServices',
    'gnUrlUtils',
    '$http',
    function(gnHttpServices, gnUrlUtils, $http) {

      this.getUrl = function(filter, field, sortBy) {
        // http://localhost:8080/geonetwork/srv/api/0.1/search/records/spell?
        // q=spell:nort&rows=0&
        // wt=json&spellcheck=true&spellcheck.collateParam.q.op=AND&spellcheck.collate=true
        return gnUrlUtils.append('../api/0.1/search',
            gnUrlUtils.toKeyValue({
              q: filter,
              wt: 'json',
              rows: 0,
              spellcheck: 'true',
              'spellcheck.collateParam.q.op': 'AND',
              'spellcheck.collate': 'true',
              'suggest': 'true',
              'suggest.dictionary': 'mainSuggester',
              'suggest.count': 20
            })
        );
      };

      this.getAnySuggestions = function(val) {
        var url = this.getUrl(val);

        return $http.get(url, {
        }).then(function(res) {
          // TODO: We may have more than one suggestions
          if (!res.data.suggest) {
            return [];
          }
          var suggestions = res.data.suggest.mainSuggester,
              spellchecks = res.data.spellcheck.suggestions[1] &&
                            res.data.spellcheck.suggestions[1].suggestion,
              collations = res.data.spellcheck.collations,
              data = [];
          if (suggestions) {
            for (var p in suggestions) {
              if (suggestions.hasOwnProperty(p)) {
                var values = suggestions[p].suggestions;
                for (var i = 0; i < values.length; i++) {
                  data.push({
                    value: values[i].term,
                    label: values[i].term // TODO: Get count ?
                              // + ' (' + spellchecks[i].freq + ')'
                  });
                }
              }
            }
          }

          if (spellchecks) {
            for (var i = 0; i < spellchecks.length; i++) {
              data.push({
                value: spellchecks[i].word,
                label: spellchecks[i].word + ' (' + spellchecks[i].freq + ')'
              });
            }
          }
          if (collations) {
            for (var i = 0; i < collations.length; i++) {
              var c = collations[i];
              if (angular.isObject(c)) {
                data.push({
                  value: c.collationQuery,
                  label: c.collationQuery + ' (' + c.hits + ')'
                });
              }
            }
          }
          return data;
        });
      };
    }]);
})();
