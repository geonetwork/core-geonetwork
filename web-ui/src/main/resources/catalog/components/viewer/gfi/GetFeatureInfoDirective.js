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
  goog.provide('gn_gfi_directive');


  var module = angular.module('gn_gfi_directive', [
    'angular.filter'
  ]);

  var gfiTemplateURL = '../../catalog/components/viewer/gfi/partials/' +
      'gfi-popup.html';

  module.value('gfiTemplateURL', gfiTemplateURL);

  module.directive('gnVectorFeatureToolTip',
      ['ngeoDebounce', 'gnGlobalSettings', 'gfiTemplateURL',
      function(ngeoDebounce, gnGlobalSettings, gfiTemplateURL) {
        return {
          restrict: 'A',
          scope: {
            map: '=gnVectorFeatureToolTip'
          },
          templateUrl: gfiTemplateURL,
          link: function(scope, element, attrs) {

            var map = scope.map;
            var mapElement = $(map.getTarget());

            scope.features = [];
            
            var overlay = new ol.Overlay({
              positioning: 'center-center',
              position: undefined,
              element: $('<span class="marker">+</span>')[0]
            });
            map.addOverlay(overlay);

            scope.close = function() {
              overlay.setPosition(undefined);
            };

            map.on('singleclick', function(e) {
              for (var i = 0; i < map.getInteractions().getArray().length; i++) {
                var interaction = map.getInteractions().getArray()[i];
                if ((interaction instanceof ol.interaction.Draw ||
                    interaction instanceof ol.interaction.Select) &&
                    interaction.getActive()) {
                  return;
                }
              }
              overlay.setPosition(e.pixel);
            
              scope.map.forEachFeatureAtPixel(e.pixel,
                function(feature, layer) {
                  scope.features.push(feature);
              });
            });
          }
        };
      }]);
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
  module.directive('gnGfi', ['$http', 'gfiTemplateURL',
    function($http, gfiTemplateURL) {
      return {
        restrict: 'A',
        scope: {
          map: '='
        },
        controller: 'gnGfiController',
        templateUrl: gfiTemplateURL
      };
    }]);

  geonetwork.GnGfiController = function($scope, gnFeaturesTableManager) {

    this.gnFeaturesTableManager = gnFeaturesTableManager;

    this.$scope = $scope;
    this.map = $scope.map;
    var map = this.map;
    var coordinates;

    this.overlay = new ol.Overlay({
      positioning: 'center-center',
      position: undefined,
      element: $('<span class="marker">+</span>')[0]
    });
    map.addOverlay(this.overlay);

    map.on('singleclick', function(e) {
      this.$scope.$apply(function() {
        if (!this.canApply()) {
          return;
        }
        var layers = map.getLayers().getArray().filter(function(layer) {
          return (layer.getSource() instanceof ol.source.ImageWMS ||
              layer.getSource() instanceof ol.source.TileWMS) &&
              layer.getVisible();
        }).reverse();

        coordinates = e.coordinate;
        
        map.forEachFeatureAtPixel(e.pixel,
          function(feature, layer) {
            layers.push(layer)
        });
        
        this.registerTables(layers, e.coordinate);

      }.bind(this));
    }.bind(this));

    $scope.$watch(function() {
      return this.gnFeaturesTableManager.getCount();
    }.bind(this), function(newVal, oldVal) {
      this.overlay.setPosition(
          (newVal == 0) ? undefined : coordinates
      );
    }.bind(this));
  };

  geonetwork.GnGfiController.prototype.canApply = function() {
    var map = this.map;
    for (var i = 0; i < map.getInteractions().getArray().length; i++) {
      var interaction = map.getInteractions().getArray()[i];
      if ((interaction instanceof ol.interaction.Draw ||
          interaction instanceof ol.interaction.Select) &&
          interaction.getActive()) {
        return false;
      }
    }
    return true;
  };

  geonetwork.GnGfiController.prototype.registerTables =
      function(layers, coordinates) {

    this.gnFeaturesTableManager.clear();
    layers.forEach(function(layer) {

      var indexObject = layer.get('indexObject');
      var type = indexObject ? 'index' : 'gfi';

      this.gnFeaturesTableManager.addTable({
        name: layer.get('label') || layer.get('name'),
        type: type
      }, {
        map: this.map,
        indexObject: indexObject,
        layer: layer,
        coordinates: coordinates
      });
    }.bind(this));
  };


  module.controller('gnGfiController', [
    '$scope',
    'gnFeaturesTableManager',
    geonetwork.GnGfiController]);


})();
