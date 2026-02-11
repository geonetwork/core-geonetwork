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
  goog.provide("gn_anonymous_access_service");

  var module = angular.module("gn_anonymous_access_service", []);

  /**
   * @ngdoc service
   * @kind function
   * @name gn_anonymous_access.service:gnAnonymousAccessService
   * @requires $q
   * @requires $http
   *
   * @description
   * The `gnAnonymousAccessService` service provides all tools required to manage
   * anonymous access on metadata records. Anonymous access is given through
   * the creation of a link that enables viewing a metadata record when it's not yet published.
   */
  module.factory("gnAnonymousAccessService", [
    "$q",
    "$http",
    function ($q, $http) {
      return {
        /**
         * @ngdoc method
         * @methodOf gn_anonymous_access.service:gnAnonymousAccessService
         * @name gnAnonymousAccessService#create
         *
         * @description
         * Create the anonymous access link for a metadata record
         *
         * @param {string} The metadata identifier
         *
         * @return {HttpPromise} Future object which returns the hash string.
         */
        create: function (metadataUuid) {
          var defer = $q.defer();
          var url =
            "../api/anonymousAccessLink" +
            (angular.isDefined(metadataUuid) ? "/" + metadataUuid : "");

          $http.post(url).then(
            function (response) {
              defer.resolve(response.data.hash);
            },
            function (response) {
              defer.reject(response);
            }
          );

          return defer.promise;
        },

        /**
         * @ngdoc method
         * @methodOf gn_anonymous_access.service:gnAnonymousAccessService
         * @name gnAnonymousAccessService#delete
         *
         * @description
         * Delete the anonymous access link for a metadata record
         *
         * @param {string} The metadata identifier
         *
         * @return {HttpPromise} Future object which returns the error
         * if an error is received.
         */
        delete: function (metadataUuid) {
          var defer = $q.defer();
          var url =
            "../api/anonymousAccessLink" +
            (angular.isDefined(metadataUuid) ? "/" + metadataUuid : "");

          $http.delete(url).then(
            function (response) {
              defer.resolve();
            },
            function (response) {
              defer.reject(response);
            }
          );

          return defer.promise;
        }
      };
    }
  ]);
})();
