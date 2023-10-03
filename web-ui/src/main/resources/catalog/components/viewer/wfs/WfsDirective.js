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
  goog.provide("gn_wfs_directive");

  var module = angular.module("gn_wfs_directive", []);

  module.directive("gnWfsDownload", [
    "gnWfsService",
    function (gnWfsService) {
      return {
        restrict: "A",
        scope: {},
        templateUrl: "../../catalog/components/viewer/wfs/partials/download.html",
        link: function (scope, element, attrs) {
          scope.initOnDemand = attrs["initOnDemand"] == "true" || false;
          scope.isWfsAvailable = false;
          scope.isInitialized = false;
          scope.mode = "dropdown";

          scope.init = function () {
            // Get WFS URL from attrs or try by substituting WFS in WMS URLs.
            scope.url = attrs["url"].replace(/wms/i, "wfs");
            scope.typenames = attrs["typename"].split(",");
            scope.formats = [];
            scope.checkWFSUrl();
          };

          scope.downloadFeatureType = function (
            featureType,
            featureTypeName,
            format,
            mapExtentOnly
          ) {
            if (mapExtentOnly) {
              var extent = map
                ? scope.map.getView().calculateExtent(scope.map.getSize())
                : [-90, -180, 90, 180];
              // Use layer default SRS
              var p = featureType.defaultSRS;
              var e = map
                ? ol.proj.transformExtent(
                    extent,
                    scope.map.getView().getProjection().getCode(),
                    p
                  )
                : "epsg:4326";
              gnWfsService.download(
                scope.url,
                scope.capabilities.version,
                featureTypeName,
                format,
                e[1] + "," + e[0] + "," + e[3] + "," + e[2],
                p
              );
            } else {
              gnWfsService.download(
                scope.url,
                scope.capabilities.version,
                featureTypeName,
                format
              );
            }
          };

          /**
           * Check if the WFS url provided return a response.
           */
          scope.checkWFSUrl = function () {
            return gnWfsService.getCapabilities(scope.url).then(
              function (capabilities) {
                scope.isInitialized = true;
                scope.isWfsAvailable = true;
                scope.capabilities = capabilities;
                scope.featureTypes = [];
                scope.typenames.forEach(function (typename) {
                  var type = gnWfsService.getTypeName(capabilities, typename);
                  if (type) {
                    scope.featureTypes.push({ label: typename, type: type });
                  }
                });
                scope.formats = gnWfsService.getOutputFormat(capabilities);
              },
              function (r) {
                console.warn(r);
                scope.isInitialized = true;
                scope.isWfsAvailable = false;
              }
            );
          };

          if (!scope.initOnDemand) {
            scope.init();
          }
        }
      };
    }
  ]);
  module.directive("gnWFS", [
    function () {
      var inputTypeMapping = {
        string: "text",
        float: "number"
      };

      var defaultValue = function (literalData) {
        var value = undefined;
        if (literalData.defaultValue != undefined) {
          value = literalData.defaultValue;
        }
        if (literalData.dataType.value == "float") {
          value = parseFloat(value);
        }
        if (literalData.dataType.value == "string") {
          value = value || "";
        }
        return value;
      };

      return {
        restrict: "AE",
        scope: {
          uri: "=",
          processId: "="
        },
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/viewer/wps/partials/processform.html"
          );
        },

        link: function (scope, element, attrs) {}
      };
    }
  ]);
})();
