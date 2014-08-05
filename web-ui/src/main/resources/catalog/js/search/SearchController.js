(function() {

  goog.provide('gn_search_controller');

  goog.require('gn_searchsuggestion_service');


  var module = angular.module('gn_search_controller',
      [
        'bootstrap-tagsinput',
          'gn_searchsuggestion_service'
      ]);

  module.constant('gnOlStyles',{
    bbox: new ol.style.Style({
      stroke: new ol.style.Stroke({
        color: 'rgba(255,0,0,1)',
        width: 2
      }),
      fill: new ol.style.Fill({
        color: 'rgba(255,0,0,0.3)'
      }),
      image: new ol.style.Circle({
        radius: 7,
        fill: new ol.style.Fill({
          color: 'rgba(255,0,0,1)'
        })
      })
    })
  });

  /**
   * Main search controller attached to the first element of the
   * included html file from the base-layout.xsl output.
   */
  module.controller('GnSearchController', [
    '$scope',
    '$location',
    function($scope, $location) {

      /** Object to be shared through directives and controllers */
      $scope.searchObj = {
        params: {},
        permalink: true
      };

      /** Define in the controller scope a reference to the map */
      $scope.map = new ol.Map({
        layers: [
          new ol.layer.Tile({
            source: new ol.source.OSM()
          })
        ],
        view: new ol.View({
          center: [-10997148, 4569099],
          zoom: 1
        })
      });

      /** Facets configuration */
      $scope.facetsConfig = {
        keyword: 'keywords',
        orgName: 'orgNames',
        denominator: 'denominator',
        format: 'formats',
        createDateYear: 'createDateYears'
      };

      /* Pagination configuration */
      $scope.paginationInfo = {
        hitsPerPage: 3
      };
      $scope.resultTemplate = '../../catalog/components/search/resultsview/partials/viewtemplates/thumb.html';
    }]);

  /**
   * Specific geocat search form controller, attached to the root node of the search form,
   * in the searchForm.html view.
   */
  module.controller('gocatSearchFormCtrl', [
    '$scope',
      'gnHttp',
      'gnHttpServices',
      'gnRegionService',
      '$timeout',
      'suggestService',
      '$http',
      'gnOlStyles',
      '$location',

    function($scope, gnHttp, gnHttpServices, gnRegionService,
             $timeout, suggestService,$http, gnOlStyles, $location) {

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

      /** Manage cantons selection (add feature to the map) */
      var cantonSource = new ol.source.Vector();
      var nbCantons = 0;
      var cantonVector = new ol.layer.Vector({
        source: cantonSource,
        style: gnOlStyles.bbox
      });
      var addCantonFeature = function(id) {
        var url = 'http://www.geocat.ch/geonetwork/srv/eng/region.geom.wkt?id=kantone:'+id+'&srs=EPSG:3857';
        var proxyUrl = '../../proxy?url=' + encodeURIComponent(url);
        nbCantons++;

        return $http.get(proxyUrl).success(function(wkt) {
          var parser = new ol.format.WKT();
          var feature = parser.readFeature(wkt);
          cantonSource.addFeature(feature);
        });
      };
      $scope.map.addLayer(cantonVector);

      // Request cantons geometry and zoom to extent when
      // all requests respond.
      $scope.$watch('searchObj.params.cantons', function(v){
        cantonSource.clear();
        if(angular.isDefined(v) && v != '') {
          var cs = v.split(',');
          for(var i=0; i<cs.length;i++) {
            var id = cs[i].split('#')[1];
            addCantonFeature(Math.floor((Math.random() * 10) + 1)).then(function(){
              if(--nbCantons == 0) {
                $scope.map.getView().fitExtent(cantonSource.getExtent(), $scope.map.getSize());
              }
            });
          }
        }
      });


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
