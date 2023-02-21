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
  goog.provide("gn_heatmap_directive");

  var module = angular.module("gn_heatmap_directive", []);

  /**
   * @ngdoc directive
   * @name gn_heatmap.directive:gnHeatmap
   *
   * @description
   * Given a feature type, this directive will query the ElasticSearch backend
   * to render a heatmap of features on the map.
   * The heatmap is actually several box features which gives info when
   * hovered (feature count, etc.). These features are redrawn on every map
   * move.
   */
  module.directive("gnHeatmap", [
    "gnHeatmapService",
    "$translate",
    function (gnHeatmapService, $translate) {
      return {
        restrict: "E",
        scope: {
          map: "<",
          featureType: "<",
          enabled: "<",
          filter: "<"
        },
        bindToController: true,
        controllerAs: "ctrl",
        controller: [
          "$scope",
          function ($scope) {
            this.$onInit = function () {
              var ctrl = this;

              // create a vector layer to hold the features
              ctrl.source = new ol.source.Vector({
                features: []
              });
              ctrl.layer = new ol.layer.Vector({
                source: ctrl.source,
                style: gnHeatmapService.getCellStyle()
              });
              ctrl.map.addLayer(ctrl.layer);

              // add an interaction for cell hovering
              ctrl.hoverInteration = new ol.interaction.Select({
                condition: ol.events.condition.pointerMove,
                style: gnHeatmapService.getCellHoverStyle(),
                layers: [ctrl.layer]
              });
              ctrl.map.addInteraction(ctrl.hoverInteration);

              // add popover for feature info
              ctrl.overlay = new ol.Overlay({
                element: $('<div class="heatmap-overlay"></div>')[0],
                positioning: "bottom-center",
                stopEvent: false,
                offset: [0, -2]
              });
              ctrl.map.addOverlay(ctrl.overlay);
              ctrl.hoverInteration.on("select", function (event) {
                var selected = event.selected[0];

                // hide if no feature hovered; else move overlay on hovered feature
                if (!selected) {
                  ctrl.overlay.setPosition();
                } else {
                  var center = ol.extent.getCenter(selected.getGeometry().getExtent());
                  var topleft = ol.extent.getTopLeft(selected.getGeometry().getExtent());
                  ctrl.overlay.setPosition([center[0], topleft[1]]);
                  ctrl.overlay.getElement().innerText =
                    $translate.instant("featureCount") + ": " + selected.get("count");
                }
              });

              // this will refresh the heatmap
              ctrl.refresh = function () {
                gnHeatmapService
                  .requestHeatmapData(ctrl.featureType, ctrl.map, ctrl.filter)
                  .then(function (cells) {
                    // add cells as features
                    ctrl.source.clear();
                    ctrl.hoverInteration.getFeatures().clear();
                    ctrl.overlay.setPosition();
                    ctrl.source.addFeatures(cells);
                  });
              };

              // watch "enabled" param to show/hide layer
              $scope.$watch("ctrl.enabled", function (newValue, oldValue) {
                ctrl.layer.setVisible(!!newValue);
                ctrl.hoverInteration.setActive(!!newValue);

                // the heatmap was enabled: refresh data
                // else: clear select interaction
                if (newValue) {
                  ctrl.refresh();
                } else {
                  ctrl.hoverInteration.getFeatures().clear();
                }
              });

              // refresh features on map move
              var mapEventKey = ctrl.map.on("moveend", function () {
                if (!ctrl.enabled) {
                  return;
                }
                ctrl.refresh();
              });

              // unbind event & remove layer on destroy
              $scope.$on("$destroy", function () {
                ctrl.map.un(mapEventKey);
                ctrl.map.removeLayer(ctrl.layer);
                ctrl.map.removeInteraction(ctrl.hoverInteration);
                ctrl.map.removeInteraction(ctrl.overlay);
              });

              // adjust ES request based on current filters
              // (skip the initial watch trigger)
              function reload(newValue, oldValue) {
                if (!ctrl.enabled || oldValue === undefined) {
                  return;
                }
                ctrl.refresh();
              }

              $scope.$watch("ctrl.filter", reload, true);
            };
          }
        ],
        link: function (scope, element, attrs) {
          // destroy scope on element removal
          element.on("$destroy", function () {
            scope.$destroy();
          });
        }
      };
    }
  ]);
})();
