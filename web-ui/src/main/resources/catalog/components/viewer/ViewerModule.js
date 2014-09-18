(function () {
  goog.provide('gn_viewer');

  goog.require('gn_module');
  goog.require('gn_viewer_directive');
  goog.require('gn_viewer_service');
  goog.require('gn_wmsimport_directive');
  goog.require('gn_owscontext');
  goog.require('gn_layermanager_directive');
  goog.require('gn_baselayerswitcher_directive');
  goog.require('gn_measure_directive');
  goog.require('gn_draw_directive');
  goog.require('gn_ows');
  goog.require('gn_popup');
  goog.require('gn_ncwms');
  goog.require('gn_localisation');
  goog.require('gn_print');

  var module = angular.module('gn_viewer', [
    'gn_ncwms',
    'gn_viewer_service',
    'gn_viewer_directive',
    'gn_wmsimport_directive',
    'gn_owscontext',
    'gn_layermanager_directive',
    'gn_baselayerswitcher_directive',
    'gn_measure_directive',
    'gn_draw_directive',
    'gn_ows',
    'gn_localisation',
    'gn_popup',
    'gn_print',
    'gn_module'
  ]);

  module.controller('gnViewerController', [
    '$scope',
    '$timeout',
    'gnNcWms',
    'goDecorateLayer',
    'gnMap',
    'gnMapConfig',
      function($scope, $timeout, gnNcWms, goDecorateLayer, gnMap, gnMapConfig) {

        var hidetimer;
        var hovering = false;
        $($scope.map.getViewport()).on('mousemove', function(e) {
          if (hovering) { return; }
          var f;
          var pixel = $scope.map.getEventPixel(e.originalEvent);
          var coordinate = $scope.map.getEventCoordinate(e.originalEvent);
          $scope.map.forEachFeatureAtPixel(pixel, function(feature, layer) {
            if (!layer) { return; }
            $timeout.cancel(hidetimer);
            if (f != feature) {
              f = feature;
              var html = '';
              if (feature.getKeys().indexOf('description') >=0 ) {
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
            hidetimer = $timeout(function(){
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

  var source = new ol.source.TileWMS({
    params: {
      LAYERS: 'ETOPO1_BATHY_R_3857,continent'
    },
    url: 'http://www.ifremer.fr/services/wms/wmsproxy_double.cgi?'
  });
  var sxtLayer = new ol.layer.Tile({
    type: 'WMS',
    source: source,
    title: 'Sextant'
  });

  module.controller('toolsController',
      ['$scope', 'gnMeasure',
        function($scope, gnMeasure) {
          $scope.mInteraction = gnMeasure.create($scope.map, $scope.measureObj, $scope);
        }
      ]);

})();
