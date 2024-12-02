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
  goog.provide("gn_searchlayerformap_directive");

  var module = angular.module("gn_searchlayerformap_directive", []);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnSearchLayerForMap
   *
   * @description
   * Panel to search for WMS layer describe in the catalog
   */
  module.directive("gnSearchLayerForMap", [
    "gnOwsCapabilities",
    "gnMap",
    "$translate",
    "Metadata",
    "gnRelatedResources",
    "gnSearchSettings",
    "gnGlobalSettings",
    function (
      gnOwsCapabilities,
      gnMap,
      $translate,
      Metadata,
      gnRelatedResources,
      gnSearchSettings,
      gnGlobalSettings
    ) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/viewer/searchlayerformap/" +
          "partials/searchlayerformap.html",
        scope: {
          map: "=gnSearchLayerForMap",
          mode: "@"
        },
        controller: [
          "$scope",
          function ($scope) {
            var sortConfig = gnSearchSettings.sortBy.split("#");
            $scope.searchObj = {
              permalink: false,
              hitsperpageValues: gnSearchSettings.hitsperpageValues,
              sortbyValues: gnSearchSettings.sortbyValues,
              filters: gnSearchSettings.filters,
              params: {
                sortBy: sortConfig[0] || "relevance",
                sortOrder: sortConfig[1] || ""
              }
            };
            if ($scope.mode === "map") {
              $scope.searchObj.params.type = "interactiveMap";
            } else {
              $scope.searchObj.params.linkProtocol = "OGC:WMS*";
            }
            $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);

            $scope.paginationInfo = {
              hitsPerPage: gnSearchSettings.hitsperpageValues[0]
            };
          }
        ],
        link: function (scope, element, attrs) {
          scope.filterTopic = function (topic) {
            delete scope.searchObj.params.topicCat;
            scope.searchObj.params.topicCat = topic;
          };
          scope.loadMap = function (map, md) {
            gnRelatedResources.getAction("MAP")(map, md);
          };
          scope.addToMap = function (link, md) {
            gnRelatedResources.getAction("WMS")(link, md);
          };
          scope.zoomToLayer = function (md) {
            var proj = scope.map.getView().getProjection();
            var feat = gnMap.getBboxFeatureFromMd(md, proj);
            var extent = feat.getGeometry().getExtent();
            if (extent) {
              extent = ol.extent.containsExtent(proj.getWorldExtent(), extent)
                ? ol.proj.transformExtent(extent, "EPSG:4326", proj)
                : proj.getExtent();
              scope.map.getView().fit(extent, scope.map.getSize());
            }
          };
          scope.getMetadata = function (md) {
            var m = new Metadata(md);
            m.relevantLinks = m.getLinksByType("WMS");
            m.relevantMapLinks = m.getLinksByType("OGC:OWS-C");
            return m;
          };
        }
      };
    }
  ]);
})();
