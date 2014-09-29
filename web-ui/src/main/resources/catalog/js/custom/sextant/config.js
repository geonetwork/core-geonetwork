(function() {

  goog.provide('gn_search_sextant_config');

  var module = angular.module('gn_search_sextant_config', []);

  module.constant('gnPanierSettings', {});

  module.config(['gnSearchSettings', 'gnViewerSettings', 'gnPanierSettings',

    function(searchSettings, viewerSettings, gnPanierSettings) {

      /** *************************************
       * Define mapviewer background layers
       */
      viewerSettings.bgLayers = [
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
      angular.forEach(viewerSettings.bgLayers, function(l) {
        l.displayInLayerManager = false;
        l.background = true;
      });

      /** *************************************
       * Define OWS services url for Import WMS
       */
      viewerSettings.servicesUrl = {
        wms: [
          'http://ids.pigma.org/geoserver/wms',
          'http://ids.pigma.org/geoserver/ign/wms',
          'http://www.ifremer.fr/services/wms/oceanographie_physique'
        ],
        wmts: [
          'http://sdi.georchestra.org/geoserver/gwc/service/wmts'
        ]
      };

      var bboxStyle = new ol.style.Style({
        stroke: new ol.style.Stroke({
          color: 'rgba(255,0,0,1)',
          width: 2
        }),
        fill: new ol.style.Fill({
          color: 'rgba(255,0,0,0.3)'
        })
      });
      searchSettings.olStyles = {
        drawBbox: bboxStyle,
        mdExtent: new ol.style.Style({
          stroke: new ol.style.Stroke({
            color: 'orange',
            width: 2
          })
        }),
        mdExtentHighlight:new ol.style.Style({
          stroke: new ol.style.Stroke({
            color: 'orange',
            width: 3
          }),
          fill: new ol.style.Fill({
            color: 'rgba(255,255,0,0.3)'
          })
        })

      }

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
          zoom: 0
        })
      });

      /** Facets configuration */
      searchSettings.facetsConfig = {
        keyword: 'keywords',
        orgName: 'orgNames',
        denominator: 'denominator',
        format: 'formats',
        createDateYear: 'createDateYears'
      };

      /* Pagination configuration */
      searchSettings.paginationInfo = {
        hitsPerPage: 20
      };

      /* Custom templates for search result views */
      searchSettings.resultViewTpls = [{
        tplUrl: '../../catalog/components/search/resultsview/partials/viewtemplates/thumb.html',
        tooltip: 'Simple',
        icon: 'fa-list'
      }, {
        tplUrl: '../../catalog/components/search/resultsview/partials/viewtemplates/sextant.html',
        tooltip: 'Thumbnails',
        icon: 'fa-th-list'
      }];

      /* Hits per page combo values configuration */
      searchSettings.hitsperpageValues = [3,10,20,50,100];

      /* Sort by combo values configuration */
      searchSettings.sortbyValues = ['relevance', 'title', 'rating'];

      // Set custom config in gnSearchSettings
      angular.extend(searchSettings, {
        viewerMap: viewerMap,
        searchMap: searchMap
      });

      gnPanierSettings.projs = [{
        value: '27571',
        label: 'Lambert Zone I - Datum NTF'
      },{
        value: '27572',
        label: 'Lambert Zone II - Datum NTF'
      },{
        value: '27573',
        label: 'Lambert Zone III - Datum NTF'
      },{
        value: '27574',
        label: 'Lambert Zone IV - Datum NTF'
      },{
        value: '27561',
        label: 'Lambert Nord France - Datum NTF'
      },{
        value: '27562',
        label: 'Lambert Centre France - Datum NTF'
      },{
        value: '27563',
        label: 'Lambert Sud France - Datum NTF'
      },{
        value: '27564',
        label: 'Lambert Corse - Datum NTF'
      },{
        value: '4326',
        label: 'Geographique - Datum WGS84'
      },{
        value: '32600',
        label: 'UTM - Datum WGS84'
      },{
        value: '4230',
        label: 'Geographique - Datum ED50'
      },{
        value: '4258',
        label: 'European Terrestrial Reference System 1989 (ETRS89)'
      },{
        value: '3395',
        label: 'Mercator - Datum WGS84'
      }];

      gnPanierSettings.formats = [{
        value: 'ESRI Shapefile',
        label: 'ESRI Shapefile'
      },{
        value: 'MapInfo File TAB',
        label: 'MapInfo File TAB'
      },{
        value: 'GML',
        label: 'GML'
      },{
        value: 'KML',
        label: 'KML'
      }];

      gnPanierSettings.defaults = {
        format: 'ESRI Shapefile',
        proj: '4326'
      }

    }]);
})();
