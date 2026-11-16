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
  goog.provide("gn_searchsuggestion_service");

  angular
    .module("gn_searchsuggestion_service", [])

    /**
     * @ngdoc service
     * @kind function
     * @name gn_searchsuggestion.service:suggestService
     * @requires gnESClient
     *
     * @description
     * The `suggestService` service provides all tools required to get
     * suggestions from the index.
     */

    .service("suggestService", [
      "gnESClient",
      function (gnESClient) {
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
        this.getAnySuggestions = function (val, searchObj, field) {
          return gnESClient.suggest(field, val, searchObj);
        };
      }
    ]);
})();
