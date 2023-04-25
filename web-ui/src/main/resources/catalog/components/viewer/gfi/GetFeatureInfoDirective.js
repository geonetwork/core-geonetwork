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
  goog.provide("gn_gfi_directive");

  var module = angular.module("gn_gfi_directive", ["angular.filter"]);

  var gfiTemplateURL = "../../catalog/components/viewer/gfi/partials/gfi-popup.html";

  module.value("gfiTemplateURL", gfiTemplateURL);

  module.directive("gnVectorFeatureToolTip", [
    "gnDebounce",
    function (gnDebounce) {
      return {
        restrict: "A",
        scope: {
          map: "=gnVectorFeatureToolTip"
        },
        link: function (scope, element, attrs) {
          $("body").append(
            '<div id="feature-info" data-content=""' +
              'style="position: absolute; z-index: 100;"/>'
          );
          var info = $("#feature-info");
          info.popover({
            animation: false,
            trigger: "manual",
            placement: "top",
            html: true,
            title: "Feature info"
          });

          var displayFeatureInfo = function (pixel) {
            var mapTop = scope.map.getTarget().getBoundingClientRect().top;
            info.css({
              left: pixel[0] + "px",
              top: pixel[1] + mapTop + "px"
            });

            var feature = scope.map.forEachFeatureAtPixel(
              pixel,
              function (feature, layer) {
                if (layer && layer.get("featureTooltip")) {
                  return feature;
                }
              }.bind(this),
              {
                layerFilter: function (layer) {
                  return layer instanceof ol.layer.Vector;
                }
              }
            );
            if (feature) {
              var props = feature.getProperties();
              var tooltipContent = "<ul>";
              $.each(props, function (key, values) {
                if (typeof values !== "object") {
                  tooltipContent += "<li>" + key + ": " + values + "</li>";
                }
              });
              tooltipContent += "</ul>";
              info.popover("hide");
              info.data("bs.popover").options.content = tooltipContent;
              info.popover("show");
            } else {
              info.popover("hide");
            }
          };

          scope.map.on(
            "pointermove",
            gnDebounce(function (evt) {
              if (evt.dragging) {
                //info.hide();
                info.popover("hide");
                return;
              }

              displayFeatureInfo(scope.map.getEventPixel(evt.originalEvent));
            }, 500)
          );
        }
      };
    }
  ]);
  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnGfi
   *
   * @description
   * This directive manage the getFeatureInfo process. If added in the
   * map viewer template, the directive listen to mapclick and display the
   * GFI results as an array in a popup.
   * The template could be overriden using `gfiTemplateURL` constant.
   */
  module.directive("gnGfi", [
    "$http",
    "gfiTemplateURL",
    function ($http, gfiTemplateURL) {
      return {
        restrict: "A",
        scope: {
          map: "="
        },
        controller: "gnGfiController",
        templateUrl: gfiTemplateURL
      };
    }
  ]);

  geonetwork.GnGfiController = function ($scope, gnFeaturesTableManager) {
    this.gnFeaturesTableManager = gnFeaturesTableManager;

    this.$scope = $scope;
    this.map = $scope.map;
    var map = this.map;
    var coordinates;

    this.overlay = new ol.Overlay({
      positioning: "center-center",
      position: undefined,
      element: $('<span class="marker">+</span>')[0]
    });
    map.addOverlay(this.overlay);

    map.on(
      "singleclick",
      function (e) {
        if (!this.canApply()) {
          return;
        }
        this.$scope.$apply(
          function () {
            var layers = map
              .getLayers()
              .getArray()
              .filter(function (layer) {
                return (
                  layer.background != true &&
                  (layer.getSource() instanceof ol.source.ImageWMS ||
                    layer.getSource() instanceof ol.source.TileWMS ||
                    layer.getSource() instanceof ol.source.ImageArcGISRest) &&
                  layer.getVisible()
                );
              })
              .reverse();

            coordinates = e.coordinate;
            this.registerTables(layers, e.coordinate);
          }.bind(this)
        );
      }.bind(this)
    );

    $scope.$watch(
      function () {
        return this.gnFeaturesTableManager.getCount();
      }.bind(this),
      function (newVal, oldVal) {
        this.overlay.setPosition(newVal == 0 ? undefined : coordinates);
      }.bind(this)
    );
  };

  geonetwork.GnGfiController.prototype.canApply = function () {
    var map = this.map;
    if (map.get("disable-gfi")) {
      return;
    }
    for (var i = 0; i < map.getInteractions().getArray().length; i++) {
      var interaction = map.getInteractions().getArray()[i];
      if (
        (interaction instanceof ol.interaction.Draw ||
          interaction instanceof ol.interaction.Select) &&
        interaction.getActive()
      ) {
        return false;
      }
    }
    return true;
  };

  geonetwork.GnGfiController.prototype.registerTables = function (layers, coordinates) {
    this.gnFeaturesTableManager.clear();
    layers.forEach(
      function (layer) {
        var indexObject = layer.get("indexObject");
        var isArcGis = layer.getSource() instanceof ol.source.ImageArcGISRest;
        var type = "gfi";
        if (!!indexObject) {
          type = "index";
        } else if (isArcGis) {
          type = "esri";
        }

        this.gnFeaturesTableManager.addTable(
          {
            name:
              (layer.get("label") || layer.get("name")) +
              (layer.get("currentStyle")
                ? " (" +
                  (layer.get("currentStyle").Title || layer.get("currentStyle").Name) +
                  ")"
                : ""),
            type: type
          },
          {
            map: this.map,
            indexObject: indexObject,
            layer: layer,
            coordinates: coordinates
          }
        );
      }.bind(this)
    );
  };

  module.controller("gnGfiController", [
    "$scope",
    "gnFeaturesTableManager",
    geonetwork.GnGfiController
  ]);
})();
