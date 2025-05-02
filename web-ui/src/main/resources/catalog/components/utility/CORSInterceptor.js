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
  goog.provide("gn_cors_interceptor");

  goog.require("gn_urlutils_service");
  goog.require("gn_map_service");

  var module = angular.module("gn_cors_interceptor", [
    "gn_urlutils_service",
    "gn_map_service"
  ]);

  /**
   * CORS Interceptor
   *
   * This interceptor checks if each AJAX call made in AngularJS needs a proxy
   * or not.
   */

  module.config([
    "$httpProvider",
    function ($httpProvider) {
      $httpProvider.interceptors.push([
        "$q",
        "$injector",
        "gnGlobalSettings",
        "gnLangs",
        "gnUrlUtils",
        "gnMapServicesCache",
        "$templateCache",
        function (
          $q,
          $injector,
          gnGlobalSettings,
          gnLangs,
          gnUrlUtils,
          gnMapServicesCache,
          $templateCache
        ) {
          return {
            request: function (config) {
              // Do not manipulate url which are available in the template
              // cache built by WRO4J.
              if ($templateCache.get(config.url) !== undefined) {
                return config;
              }
              var isGnUrl =
                config.url.indexOf(gnGlobalSettings.gnUrl) === 0 ||
                (config.url.indexOf("http") !== 0 && config.url.indexOf("//") !== 0);

              // If this is an authorized mapservice then we need to adjust the url or add auth headers
              // Only check http url and exclude any urls like data: which should not be changed. Also, there is not need to check prox urls.
              if (
                config.url !== null &&
                config.url.startsWith("http") &&
                !config.url.startsWith(gnGlobalSettings.proxyUrl)
              ) {
                var mapservice = gnMapServicesCache.getMapservice(config.url);
                if (mapservice !== null) {
                  if (mapservice.useProxy) {
                    // If we need to use the proxy then add it to requireProxy list.
                    if (
                      $.inArray(
                        config.url + "#" + config.method,
                        gnGlobalSettings.requireProxy
                      ) === -1
                    ) {
                      var url = config.url.split("/");
                      url = url[0] + "/" + url[1] + "/" + url[2] + "/";
                      gnGlobalSettings.requireProxy.push(url + "#" + config.method);
                    }
                  } else {
                    // If we are not using a proxy then add the headers.
                    // Note that is may still end up using the proxy if there is a cors issue.
                    if (gnMapServicesCache.getAuthorizationHeaderValue(mapservice)) {
                      config.headers["Authorization"] =
                        gnMapServicesCache.getAuthorizationHeaderValue(mapservice);
                    }
                  }
                }
              }

              //Add language headers manually or some servers fail
              if (isGnUrl && gnLangs.current && !config.headers["Accept-Language"]) {
                config.headers["Accept-Language"] = gnLangs.current;
              } else if (!config.headers["Accept-Language"]) {
                config.headers["Accept-Language"] = navigator.language;
              }
              // For HTTP url and those which
              // are not targeting the catalog
              // add proxy if needed
              if (
                config.url.indexOf("http", 0) === 0 &&
                config.url.indexOf(gnGlobalSettings.gnUrl) !== 0
              ) {
                var url = config.url.split("/");
                url = url[0] + "/" + url[1] + "/" + url[2] + "/";

                if (
                  $.inArray(url + "#" + config.method, gnGlobalSettings.requireProxy) !==
                  -1
                ) {
                  // require proxy
                  config.url = gnGlobalSettings.proxyUrl + encodeURIComponent(config.url);
                }
              } else if (gnGlobalSettings.gnUrl) {
                // Relative URL in API mode
                // are prefixed with catalog URL
                // console.log(config.url);
                config.url =
                  gnGlobalSettings.gnUrl + (gnLangs.current || "eng") + "/" + config.url;
              }

              /*
              Reset requireProxy array. This prevent same host of map server and catalouge deployment affecting together.
              For example: One online resource of wms https://example.com/mapserver/ows?service=WMS&request=GetCapabilities&layers=roads
              and another online resource of file store https://example.com/geonetwork/srv/api/records/c90c284d-171f-41a9-bc92-b2533970cdc8/attachments/sample.txt
              This will confuse the proxy to think the filestore has same hostname and will use proxy url. But due to the straight security measure the proxy url will not work for the backend store
              */
              gnGlobalSettings.requireProxy = [];

              return config;
            },
            responseError: function (response) {
              var config = response.config;

              if (config.nointercept) {
                if (response.status > 199 && response.status < 400) {
                  // let it pass
                  return $q.resolve(config);
                } else {
                  // return error
                  return $q.reject(config);
                }
              }

              //If we have no error status, the request didn't even make it to the server
              //Then, use proxy
              if (response.status === -1) {
                var defer = $q.defer();

                if (config.url.indexOf("http", 0) === 0) {
                  if (gnUrlUtils.urlIsSameOrigin(config.url)) {
                    // if the target URL is in the GN host,
                    // don't use proxy and reject the promise.
                    return $q.reject(response);
                  } else {
                    // if the target URL is in other site/protocol that GN,
                    // use the proxy to make the request.
                    var url = config.url.split("/");
                    url = url[0] + "/" + url[1] + "/" + url[2] + "/";

                    if (
                      $.inArray(
                        url + "#" + config.method,
                        gnGlobalSettings.requireProxy
                      ) === -1
                    ) {
                      gnGlobalSettings.requireProxy.push(url + "#" + config.method);
                    }

                    $injector.invoke([
                      "$http",
                      function ($http) {
                        // This modification prevents interception (infinite
                        // loop):
                        config.nointercept = true;

                        // retry again
                        $http(config).then(
                          function (resp) {
                            defer.resolve(resp);
                          },
                          function (resp) {
                            defer.reject(resp);
                          }
                        );
                      }
                    ]);
                  }
                } else {
                  //It is not an http request, it looks like an internal request
                  return $q.reject(response);
                }

                return defer.promise;
              } else {
                return $q.reject(response);
              }
            }
          };
        }
      ]);
    }
  ]);
})();
