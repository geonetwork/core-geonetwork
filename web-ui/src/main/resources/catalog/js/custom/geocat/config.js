(function() {

  goog.provide('gn_search_geocat_config');

  var module = angular.module('gn_search_geocat_config', []);

  module.run(['gnSearchSettings',

    function(searchSettings) {

      proj4.defs('EPSG:21781', '+proj=somerc +lat_0=46.95240555555556' +
          ' +lon_0=7.439583333333333 +k_0=1 +x_0=600000' +
          ' +y_0=200000 +ellps=bessel +towgs84=660.077,13.551,' +
          '369.344,2.484,1.783,2.939,5.66 +units=m +no_defs');

      ol.proj.get('EPSG:21781').setExtent([420000, 30000, 900000, 350000]);
      ol.proj.get('EPSG:21781').setWorldExtent(
          [5.9700, 45.8300, 10.4900, 47.8100]);

      searchSettings.olStyles = {
        drawBbox: new ol.style.Style({
          stroke: new ol.style.Stroke({
            color: 'rgba(255,0,0,1)',
            width: 2
          }),
          fill: new ol.style.Fill({
            color: 'rgba(255,0,0,0.3)'
          })
        }),
        mdExtent: new ol.style.Style({
          stroke: new ol.style.Stroke({
            color: 'black',
            width: 2
          })
        }),
        mdExtentHighlight: new ol.style.Style({
          stroke: new ol.style.Stroke({
            color: 'black',
            width: 3
          }),
          fill: new ol.style.Fill({
            color: 'rgba(0,0,0,0.3)'
          })
        })
      };

      var resolutions = [
        4000, 3750, 3500, 3250, 3000, 2750, 2500, 2250, 2000, 1750, 1500, 1250,
        1000, 750, 650, 500, 250, 100, 50, 20, 10, 5, 2.5, 2, 1.5, 1, 0.5
      ];

      var matrixIds = [];
      for (var i = 0; i < resolutions.length; i++) {
        matrixIds.push(i);
      }

      var tileGrid = new ol.tilegrid.WMTS({
        origin: [420000, 350000],
        resolutions: resolutions,
        matrixIds: matrixIds
      });

      var defaultUrl = 'http://wmts{5-9}.geo.admin.ch/1.0.0/{Layer}/default/' +
          '20140520/21781/' +
          '{TileMatrix}/{TileRow}/{TileCol}.jpeg';
      var url = angular.isDefined(searchSettings.mapConfig) ?
          searchSettings.mapConfig.url || defaultUrl : defaultUrl;

      var chLayer = new ol.layer.Tile({
        source: new ol.source.WMTS(({
          crossOrigin: 'anonymous',
          url: url,
          tileGrid: tileGrid,
          layer: 'ch.swisstopo.pixelkarte-farbe',
          requestEncoding: 'REST',
          projection: 'EPSG:21781'
        })),
        extent: [434250, 37801.909073720046, 894750, 337801.90907372005],
        useInterimTilesOnError: false
      });
      searchSettings.searchMap = new ol.Map({
        layers: [chLayer],
        view: new ol.View({
          resolutions: [1250, 1000, 750, 650, 500, 250, 100, 50, 20,
            10, 5, 2.5, 2, 1, 0.5, 0.25, 0.1],
          extent: [420000, 30000, 900000, 350000],
          projection: 'EPSG:21781',
          center: [660000, 190000],
          zoom: 2
        })
      });

      /** Facets configuration */
      searchSettings.facetsConfig = [{
        key: 'topicCat',
        value: 'topicCats'
      },{
        key: 'spatialRepresentationType',
        value: 'spatialRepresentationTypes'
      },{
        key: 'keyword',
        value: 'keywords'
      },{
        key: 'denominator',
        value: 'denominators'
      },{
        key: 'serviceType',
        value: 'serviceTypes'
      },{
        key: 'type',
        value: 'types'
      },{
        key: 'orgName',
        value: 'orgNames'
      }];



      /* Hits per page combo values configuration */
      searchSettings.hitsperpageValues = [3, 10, 20, 50, 100];

      /* Pagination configuration */
      searchSettings.paginationInfo = {
        hitsPerPage: searchSettings.hitsperpageValues[1]
      };

      /* Sort by combo values configuration */
      searchSettings.sortbyValues = [
        {sortBy: 'relevance', sortOrder: ''},
        {sortBy: 'title', sortOrder: 'reverse'},
        {sortBy: 'rating', sortOrder: ''}];
      searchSettings.sortbyDefault = searchSettings.sortbyValues[0];

      /* Custom templates for search result views */
      searchSettings.resultViewTpls = [{
        tplUrl: '../../catalog/components/search/resultsview/' +
            'partials/viewtemplates/title.html',
        tooltip: 'Simple',
        icon: 'fa-list'
      }, {
        tplUrl: '../../catalog/components/search/resultsview/' +
            'partials/viewtemplates/geocat.html',
        tooltip: 'Geocat',
        icon: 'fa-th-list'
      }];

      searchSettings.formatter = {
        defaultUrl: 'md.format.xml?xsl=full_view&id='
      };

    }]);
})();
