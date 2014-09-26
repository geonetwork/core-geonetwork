(function() {

  goog.provide('gn_search_geocat');

  goog.require('gn_search');
  goog.require('gn_search_geocat_config');
  goog.require('gn_selection_directive');

  var module = angular.module('gn_search_geocat', [
    'gn_search',
    'gn_search_geocat_config',
    'gn_selection_directive'
  ]);

  /**
   * @ngdoc controller
   * @name gn_search_geocat.controller:gnsGeocat
   *
   * @description
   * Geocat view root controller
   * its $scope inherits from gnSearchController scope.
   *
   */
  module.controller('gnsGeocat', [
      '$scope',
      'gnSearchSettings',
    function($scope, gnSearchSettings) {

      angular.extend($scope.searchObj, {
        advancedMode: false,
        searchMap: gnSearchSettings.searchMap
      });
    }]);

    module.controller('gocatSearchFormCtrl', [
    '$scope',
    'gnHttp',
    'gnHttpServices',
    'gnRegionService',
    '$timeout',
    'suggestService',
    '$http',
    'gnSearchSettings',
    'goDecorateInteraction',
    'gnMap',

    function($scope, gnHttp, gnHttpServices, gnRegionService,
             $timeout, suggestService,$http, gnSearchSettings,
             goDecorateInteraction, gnMap) {

      // data store for types field
      $scope.types = ['any',
        'dataset',
        'basicgeodata',
        'basicgeodata-federal',
        'basicgeodata-cantonal',
        'basicgeodata-communal',
        'service',
        'service-OGC:WMS',
        'service-OGC:WFS'
      ];

      // data store for archives field
      $scope.archives = [{
        value: '',
        label: 'archiveincluded'
      }, {
        value: 'n',
        label: 'archiveexcluded'
      },{
        value: 'y',
        label: 'archiveonly'
      }];

      var map = $scope.searchObj.searchMap;

      var setSearchGeometry = function(geometry) {
        $scope.searchObj.params.geometry = format.writeGeometry(
          geometry.clone().transform('EPSG:3857', 'EPSG:4326')
        );
      };

      /** Manage draw area on search map */
      var feature = new ol.Feature();
      var featureOverlay = new ol.FeatureOverlay({
        style: gnSearchSettings.olStyles.drawBbox
      });
      featureOverlay.setMap(map);
      featureOverlay.addFeature(feature);

      var cleanDraw = function() {
        featureOverlay.getFeatures().clear();
        drawInteraction.active = false
      };

      var drawInteraction = new ol.interaction.Draw({
        features: featureOverlay.getFeatures(),
        type: 'Polygon',
        style: gnSearchSettings.olStyles.drawBbox
      });
      drawInteraction.on('drawend', function(){
        setSearchGeometry(featureOverlay.getFeatures().item(0).getGeometry());
        setTimeout(function() {
          drawInteraction.active = false;
        }, 0);
      });
      drawInteraction.on('drawstart', function(){
        featureOverlay.getFeatures().clear();
      });
      goDecorateInteraction(drawInteraction, map);

      $scope.$watch('restrictArea', function(v){
        if(angular.isDefined(v)) {
          if($scope.restrictArea == 'draw') {
            drawInteraction.active = true;
          }
          else {
            cleanDraw();
          }
        }
      });

      /** When we switch between simple and advanced form*/
      $scope.$watch('advanced', function(v){
        if(v == false) {
          $scope.restrictArea = '';
        }
      });

      /** Manage cantons selection (add feature to the map) */
      var nbCantons = 0;
      var addCantonFeature = function(id) {
        var url = 'http://www.geocat.ch/geonetwork/srv/eng/region.geom.wkt?id=kantone:'+id+'&srs=EPSG:3857';
        var proxyUrl = '../../proxy?url=' + encodeURIComponent(url);
        nbCantons++;

        return $http.get(proxyUrl).success(function(wkt) {
          var parser = new ol.format.WKT();
          var feature = parser.readFeature(wkt);
          featureOverlay.getFeatures().push(feature);
        });
      };

      // Request cantons geometry and zoom to extent when
      // all requests respond.
      $scope.$watch('searchObj.params.cantons', function(v){
        featureOverlay.getFeatures().clear();
        if(angular.isDefined(v) && v != '') {
          var cs = v.split(',');
          for(var i=0; i<cs.length;i++) {
            var id = cs[i].split('#')[1];
            addCantonFeature(Math.floor((Math.random() * 10) + 1)).then(function(){
              if(--nbCantons == 0) {
                var features = featureOverlay.getFeatures();
                var extent = features.item(0).getGeometry().getExtent();
                features.forEach(function(f) {
                  ol.extent.extend(extent, f.getGeometry().getExtent());
                });
                map.getView().fitExtent(extent, map.getSize());
              }
            });
          }
        }
      });

      var key;
      var format = new ol.format.WKT();
      var setSearchGeometryFromMapExtent = function() {
        var geometry = new ol.geom.Polygon(gnMap.getPolygonFromExtent(
            map.getView().calculateExtent(map.getSize())));
        setSearchGeometry(geometry);
      };
      $scope.$watch('restrictArea', function(v) {
        if (v == 'bbox') {
          setSearchGeometryFromMapExtent();
          key = map.getView().on('propertychange', setSearchGeometryFromMapExtent);
        } else {
          $scope.searchObj.params.geometry = '';
          map.getView().unByKey(key);
        }
      });

      $scope.searchObj.params.relation = 'within';

      $scope.scrollToBottom = function($event) {
        var elem = $($event.target).parents('.panel-body')[0];
        setTimeout(function() {
          elem.scrollTop = elem.scrollHeight;
        }, 0);
      };

/*
      $('#categoriesF').tagsinput({
        itemValue: 'id',
        itemText: 'label'
      });
      $('#categoriesF').tagsinput('input').typeahead({
        valueKey: 'label',
        prefetch: {
          url :suggestService.getInfoUrl('categories'),
          filter: function(data) {
            var res = [];
            for(var i=0; i<data.metadatacategory.length;i++) {
              res.push({
                id: data.metadatacategory[i]['@id'],
                label : data.metadatacategory[i].label.eng
              })
            }
            return res;
          }
        }
      }).bind('typeahead:selected', $.proxy(function (obj, datum) {
        this.tagsinput('add', datum);
        this.tagsinput('input').typeahead('setQuery', '');
      }, $('#categoriesF')));
*/


      // Keywords input list
      /*
       gnHttpServices.geocatKeywords = 'geocat.keywords.list';
       gnHttp.callService('geocatKeywords').success(function(data) {
       var xmlDoc = $.parseXML(data);
       var $xml = $(xmlDoc);
       var k = $xml.find('keyword');
       var n = $xml.find('name');
       });
       */

    }]);

})();
