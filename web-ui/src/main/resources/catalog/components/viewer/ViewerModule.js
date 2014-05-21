(function () {
  goog.provide('gn_viewer');

  goog.require('gn');
  goog.require('gn_viewer_directive');
  goog.require('gn_viewer_service');
  goog.require('gn_wmsimport_directive');
  goog.require('gn_layermanager_directive');
  goog.require('gn_measure_directive');
  goog.require('gn_draw_directive');
  goog.require('gn_ows');
  goog.require('gn_popup');

  var module = angular.module('gn_viewer', [
    'gn_viewer_service',
    'gn_viewer_directive',
    'gn_wmsimport_directive',
    'gn_layermanager_directive',
    'gn_measure_directive',
    'gn_draw_directive',
    'gn_ows',
    'gn_popup',
    'gn'
  ]);

  module.controller('gnViewerController',
    ['$scope', 'gnConfig',
      function($scope, gnConfig) {
        $scope.gnConfig = gnConfig;
        var bgLayer = new ol.layer.Tile({
          source: new ol.source.OSM()
        });
        bgLayer.displayInLayerManager = false;
        bgLayer.background = true;

        $scope.map = new ol.Map({
          renderer: 'canvas',
          layers: [
/*
            new ol.layer.Tile({
              source: new ol.source.Stamen({
                layer: 'watercolor'
              })
            }),
            new ol.layer.Tile({
              source: new ol.source.Stamen({
                layer: 'terrain-labels'
              })
            }),
*/
            bgLayer
          ],
          target: 'map',
          view: new ol.View2D({
            center: ol.proj.transform(
              [0.416667, 44.6], 'EPSG:4326', 'EPSG:3857'),
            zoom: 8
          })
        });
      }]);

  // Define the translation files to load
  module.constant('$LOCALES', ['core', 'editor']);

  module.config(['$translateProvider', '$LOCALES',
    function ($translateProvider, $LOCALES) {
/*
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: '../../catalog/locales/',
        suffix: '.json'
      });

      var lang = location.href.split('/')[5].substring(0, 2) || 'en';
      $translateProvider.preferredLanguage(lang);
      moment.lang(lang);
*/
    }]);

})();
