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
  goog.provide("gn_atom_service");
  goog.require("gn_urlutils_service");

  var module = angular.module("gn_atom_service", ["gn_urlutils_service"]);

  module.service("gnAtomService", [
    "$http",
    "$q",
    "gnUrlUtils",
    "$cacheFactory",
    function ($http, $q, gnUrlUtils, $cacheFactory) {
      var feedsCache = $cacheFactory("gnAtomService");
      var isPromiseLike = function (obj) {
        return obj && typeof obj.then === "function";
      };

      /**
       * Do a getCapabilities request to the URL given in parameter.
       * @param {string} url WMS service URL.
       * @return {Promise} a promise that resolves into the parsed
       * capabilities document.
       */
      this.parseFeed = function (url) {
        var defer = $q.defer();
        var cachedFeed = feedsCache.get(url);
        if (cachedFeed) {
          if (isPromiseLike(cachedFeed.data)) {
            return cachedFeed.data;
          } else {
            if (cachedFeed.status === "success") {
              defer.resolve(cachedFeed.data);
            } else if (cachedFeed.status === "fail") {
              defer.reject(cachedFeed.data);
            }
          }
        } else {
          // not in cache

          if (gnUrlUtils.isValid(url)) {
            var feedPromise = $http.get(url, {
              cache: true
            });
            feedsCache.put(url, {
              data: defer.promise,
              status: "pending",
              deferred: defer
            });
            feedPromise.then(
              function (response) {
                var currentDefer = feedsCache.get(url).deferred;
                try {
                  var xmlDoc = $.parseXML(response.data);
                  feedsCache.put(url, { data: $(xmlDoc), status: "success" });
                  currentDefer.resolve($(xmlDoc));
                } catch (e) {
                  feedsCache.put(url, { data: e, status: "fail" });
                  currentDefer.reject(e);
                }
              },
              function (response) {
                var currentDefer = feedsCache.get(url).deferred;
                feedsCache.put(url, { data: "url_unavailable", status: "fail" });
                currentDefer.reject("url_unavailable");
              }
            );
          } else {
            feedsCache.put(url, { data: "invalid_url", status: "fail" });
            defer.reject("invalid_url");
          }
        }
        return defer.promise;
      };
    }
  ]);
})();
