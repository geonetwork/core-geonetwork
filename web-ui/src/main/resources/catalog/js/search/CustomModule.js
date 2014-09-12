(function() {

  goog.provide('gn_search_customui');
  var module = angular.module('gn_search_customui', []);


  module.constant('gnBackgroundLayers', []);
  module.constant('gnSearchConfig', {});


  module.config(['gnSearchConfig', 'gnBackgroundLayers',

    function(config, gnBackgroundLayers) {

      /** *************************************
       * Define mapviewer background layers
       */
      var bgLayers = [
        new ol.layer.Tile({
          style: 'Road',
          source: new ol.source.MapQuest({layer: 'osm'}),
          title: 'MapQuest'
        }),
        new ol.layer.Tile({
          source: new ol.source.OSM(),
          title: 'OpenStreetMap'
        }),
        new ol.layer.Tile({
          preload: Infinity,
          source: new ol.source.BingMaps({
            key: 'Ak-dzM4wZjSqTlzveKz5u0d4IQ4bRzVI309GxmkgSVr1ewS6iPSrOvOKhA-CJlm3',
            imagerySet: 'Aerial'
          }),
          title: 'Bing Aerial'
        })
      ];
      angular.forEach(bgLayers, function(l) {
        l.displayInLayerManager = false;
        l.background = true;
        gnBackgroundLayers.push(l);
      });

      /** *************************************
       * Define OWS services url for Import WMS
       */
      var servicesUrl = {
        wms: [
          'http://ids.pigma.org/geoserver/wms',
          'http://ids.pigma.org/geoserver/ign/wms',
          'http://www.ifremer.fr/services/wms/oceanographie_physique'
        ],
        wmts: [
          'http://sdi.georchestra.org/geoserver/gwc/service/wmts'
        ]
      };

      /** *************************************
       * Define maps
       */
      var mapsConfig = {
        center: [280274.03240585705, 6053178.654789996],
        zoom: 2,
        maxResolution: '9783.93962050256'
      };

      var viewerMap = new ol.Map({
        view: new ol.View(mapsConfig)
      });

      var searchMap = new ol.Map({
        layers: [
          new ol.layer.Tile({
            source: new ol.source.OSM()
          })
        ],
        view: new ol.View({
          center: mapsConfig.center,
          zoom: 0,
          maxResolution: mapsConfig.maxResolution
        })
      });

      // Set custom config in gnSearchConfig
      angular.extend(config, {
        viewerMap: viewerMap,
        searchMap: searchMap,
        servicesUrl: servicesUrl

      });
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

      var map = $scope.searchObj.searchMap;

      gnRegionService.loadRegion('ocean', 'fre').then(
          function (data) {
            $scope.cantons = data;
          });

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
      map.addLayer(cantonVector);

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
                map.getView().fitExtent(cantonSource.getExtent(), map.getSize());
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
