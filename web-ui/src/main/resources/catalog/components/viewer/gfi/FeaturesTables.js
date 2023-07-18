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
  goog.provide("gn_featurestables_directive");

  var module = angular.module("gn_featurestables_directive", []);

  module.directive("gnFeaturesTables", [
    function () {
      return {
        restrict: "E",
        scope: {
          map: "<gnFeaturesTablesMap",
          active: "<gnActive",
          showClose: "=",
          showExport: "=",
          height: "="
        },
        controllerAs: "ctrl",
        bindToController: true,
        controller: "gnFeaturesTablesController",
        templateUrl: "../../catalog/components/viewer/gfi/partials/featurestables.html",
        link: function (scope, element, attrs, ctrl) {
          ctrl.addLayers();
        }
      };
    }
  ]);

  var GnFeaturesTablesController = function (gnFeaturesTableManager, gnSearchSettings) {
    this.tm = gnFeaturesTableManager;
    this.tables = gnFeaturesTableManager.tables;
    this.gnSearchSettings = gnSearchSettings;
  };

  GnFeaturesTablesController.prototype.addLayers = function () {
    this.featuresOverlay = new ol.layer.Vector({
      source: new ol.source.Vector({
        useSpatialIndex: false
      }),
      style: this.gnSearchSettings.olStyles.mdExtent,
      updateWhileAnimating: true,
      updateWhileInteracting: true,
      map: this.map
    });
    this.highlightOverlay = new ol.layer.Vector({
      source: new ol.source.Vector({
        useSpatialIndex: false
      }),
      style: this.gnSearchSettings.olStyles.mdExtentHighlight,
      updateWhileAnimating: true,
      updateWhileInteracting: true,
      map: this.map
    });
  };

  GnFeaturesTablesController.prototype.clear = function () {
    this.tm.clear();
  };

  module.controller("gnFeaturesTablesController", [
    "gnFeaturesTableManager",
    "gnSearchSettings",
    GnFeaturesTablesController
  ]);
})();
