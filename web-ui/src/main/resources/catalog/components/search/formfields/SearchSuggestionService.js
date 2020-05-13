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

  angular.module('gn_searchsuggestion_service', [
  ])

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
   */

      .service('suggestService', [
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
})();
