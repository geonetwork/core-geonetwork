(function () {
  goog.provide('gn_viewer');

  goog.require('gn');
  goog.require('gn_viewer_directive');
  goog.require('gn_viewer_service');
  goog.require('gn_wmsimport_directive');
  goog.require('gn_layermanager_directive');
  goog.require('gn_baselayerswitcher_directive');
  goog.require('gn_measure_directive');
  goog.require('gn_draw_directive');
  goog.require('gn_ows');
  goog.require('gn_popup');
  goog.require('gn_ncwms');
  goog.require('gn_localisation');

  var module = angular.module('gn_viewer', [
    'gn_ncwms',
    'gn_viewer_service',
    'gn_viewer_directive',
    'gn_wmsimport_directive',
    'gn_layermanager_directive',
    'gn_baselayerswitcher_directive',
    'gn_measure_directive',
    'gn_draw_directive',
    'gn_ows',
    'gn_localisation',
    'gn_popup',
    'gn'
  ]);

  module.controller('gnViewerController',
    ['$scope', 'gnNcWms', 'goDecorateLayer',
      function($scope, gnNcWms, goDecorateLayer) {

        /** Define object to receive measure info */
        $scope.measureObj = {};

        /** Define vector layer used for drawing */
        $scope.drawVector;

        /** print definition */
        $scope.printactive = true;

        // TODO : Move on layer load
        $scope.ncwmsLayer = gnNcWms.createNcWmsLayer();
        $scope.ncwmsLayer.displayInLayerManager = true;
        goDecorateLayer($scope.ncwmsLayer);

        $scope.map = new ol.Map({
          renderer: 'canvas',
          target: 'map',
          view: new ol.View({
            center: ol.proj.transform(
              [-1.99667, 49.0], 'EPSG:4326', 'EPSG:3857'),
            zoom: 6
          })
        });
        $scope.map.addLayer($scope.ncwmsLayer);
      }]);


  var bgLayer = new ol.layer.Tile({
    source: new ol.source.OSM(),
    title: 'OpenStreetMap'
  });
  bgLayer.displayInLayerManager = false;
  bgLayer.background = true;

  var bingSatellite = new ol.layer.Tile({
    preload: Infinity,
    source: new ol.source.BingMaps({
      key: 'Ak-dzM4wZjSqTlzveKz5u0d4IQ4bRzVI309GxmkgSVr1ewS6iPSrOvOKhA-CJlm3',
      imagerySet: 'Aerial'
    }),
    title: 'Bing Aerial'
  });
  bingSatellite.displayInLayerManager = false;
  bingSatellite.background = true;

  module.constant('gnBackgroundLayers', [bgLayer, bingSatellite]);

  module.controller('toolsController',
      ['$scope', 'gnMeasure',
        function($scope, gnMeasure) {
          $scope.mInteraction = gnMeasure.create($scope.map, $scope.measureObj, $scope);
          $scope.activeTools = {};
        }
      ]);

})();
