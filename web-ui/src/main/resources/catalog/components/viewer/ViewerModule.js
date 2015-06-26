(function() {
  goog.provide('gn_viewer');














































  goog.require('gn_baselayerswitcher');
  goog.require('gn_draw');
  goog.require('gn_graticule');
  goog.require('gn_layermanager');
  goog.require('gn_localisation');
  goog.require('gn_measure');
  goog.require('gn_module');
  goog.require('gn_ncwms');
  goog.require('gn_ows');
  goog.require('gn_owscontext');
  goog.require('gn_popup');
  goog.require('gn_print');
  goog.require('gn_searchlayerformap_directive');
  goog.require('gn_terrainswitcher_directive');
  goog.require('gn_viewer_directive');
  goog.require('gn_viewer_service');
  goog.require('gn_wmsimport');

  /**
   * @ngdoc overview
   * @name gn_viewer
   *
   * @description
   * Main module for map viewer.
   */

  var module = angular.module('gn_viewer', [
    'gn_ncwms',
    'gn_viewer_service',
    'gn_viewer_directive',
    'gn_wmsimport',
    'gn_owscontext',
    'gn_layermanager',
    'gn_baselayerswitcher',
    'gn_measure',
    'gn_draw',
    'gn_ows',
    'gn_localisation',
    'gn_popup',
    'gn_print',
    'gn_module',
    'gn_graticule',
    'gn_searchlayerformap_directive',
    'gn_terrainswitcher_directive'
  ]);

  module.controller('gnViewerController', [
    '$scope',
    '$timeout',
    'gnViewerSettings',
    'gnOwsCapabilities',
    'gnMap',
    function($scope, $timeout, gnViewerSettings, gnOwsCapabilities, gnMap) {

      var map = $scope.searchObj.viewerMap;

      if (gnViewerSettings.wmsUrl && gnViewerSettings.layerName) {
        gnOwsCapabilities.getWMSCapabilities(gnViewerSettings.wmsUrl).
            then(function(capObj) {
              var layerInfo = gnOwsCapabilities.getLayerInfoFromCap(
                  gnViewerSettings.layerName, capObj);
              var layer = gnMap.addWmsToMapFromCap($scope.searchObj.viewerMap,
                  layerInfo);
            });
      }


      // Display pop up on feature over
      var div = document.createElement('div');
      div.className = 'overlay';
      var overlay = new ol.Overlay({
        element: div,
        positioning: 'bottom-left'
      });
      map.addOverlay(overlay);



      //TODO move it into a directive
      var hidetimer;
      var hovering = false;
      $(map.getViewport()).on('mousemove', function(e) {
        if (hovering) { return; }
        var f;
        var pixel = map.getEventPixel(e.originalEvent);
        var coordinate = map.getEventCoordinate(e.originalEvent);
        map.forEachFeatureAtPixel(pixel, function(feature, layer) {
          if (!layer) { return; }
          $timeout.cancel(hidetimer);
          if (f != feature) {
            f = feature;
            var html = '';
            if (feature.getKeys().indexOf('description') >= 0) {
              html = feature.get('description');
            } else {
              $.each(feature.getKeys(), function(i, key) {
                if (key == feature.getGeometryName() || key == 'styleUrl') {
                  return;
                }
                html += '<dt>' + key + '</dt>';
                html += '<dd>' + feature.get(key) + '</dd>';
              });
              html = '<dl class="dl-horizontal">' + html + '</dl>';
            }
            overlay.getElement().innerHTML = html;
          }
          overlay.setPosition(coordinate);
          $(overlay.getElement()).show();
        }, this, function(layer) {
          return !layer.get('temporary');
        });
        if (!f) {
          hidetimer = $timeout(function() {
            $(div).hide();
          }, 200);
        }
      });
      $(div).on('mouseover', function() {
        hovering = true;
      });
      $(div).on('mouseleave', function() {
        hovering = false;
      });
    }]);

  module.controller('toolsController',
      ['$scope', 'gnMeasure',
        function($scope, gnMeasure) {
          $scope.mInteraction = gnMeasure.create($scope.map,
              $scope.measureObj, $scope);
        }
      ]);

})();
