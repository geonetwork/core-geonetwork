(function() {

  goog.provide('gn_search_sextant_config');

  goog.require('gn_sxt_utils');

  var module = angular.module('gn_search_sextant_config', ['gn_sxt_utils']);

  module.constant('gnPanierSettings', {});

  var gfiTemplateURL = '../../catalog/views/sextant/templates/gfi.html';

  module.value('gfiTemplateURL', gfiTemplateURL);

  module.value('baselayerTemplateURL', '../../catalog/views/sextant/templates/baselayer.html');
  module.value('kmlimportTemplateURL', '../../catalog/views/sextant/templates/kmlimport.html');

  module.value('gnTplResultlistLinksbtn',
    '../../catalog/views/default/directives/partials/linksbtn.html');

  module.run([
    'gnSearchSettings',
    'gnViewerSettings',
    'gnPanierSettings',
    'gnGlobalSettings',
    'gnMap',
    'gnConfigService',
    'gnMapsManager',
    'gnUrlUtils',
    'sxtService',
    function(searchSettings, viewerSettings, gnPanierSettings,
             gnGlobalSettings, gnMap, gnConfigService, gnMapsManager,
             gnUrlUtils, sxtService) {

      if(typeof sxtSettings != 'undefined') {
        gnGlobalSettings.init({},
          (typeof sxtGnUrl != 'undefined') ? sxtGnUrl : undefined,
          viewerSettings, searchSettings);

        // use bing key from settings (this should be integrated into a larger
        // refactoring of the way settings are loaded in API mode)
        gnConfigService.loadPromise.then(function(data) {
          viewerSettings.bingKey = data['ui.config'].mods.map.bingKey;
        });

        // get url params for backwards compatibility
        // (angular params should be used from now on)
        var params = gnUrlUtils.parseKeyValue(window.location.search.
          replace(/^\?/, ''));
        viewerSettings.owscontext = params.owscontext &&
          decodeURIComponent(params.owscontext);
        viewerSettings.wmsurl = params.wmsurl;
        viewerSettings.layername = params.layername;
        viewerSettings.layergroup = params.layergroup;
      }


      gnGlobalSettings.isMapViewerEnabled =
          gnGlobalSettings.isMapViewerEnabled || true;

      /** *************************************
       * Define mapviewer background layers
       */
      viewerSettings.bgLayers = [
        new ol.layer.Tile()
      ];

      angular.forEach(viewerSettings.bgLayers, function(l) {
        l.displayInLayerManager = false;
        l.background = true;
        l.set('group', 'Background layers');
      });

      viewerSettings.defaultContext = '../../catalog/views/sextant/data/' +
          'defaultContext.xml';

      // WMS settings
      // If 3D mode is activated, single tile WMS mode is
      // not supported by ol3cesium, so force tiling.
      if (viewerSettings.mapConfig.is3DModeAllowed) {
        viewerSettings.singleTileWMS = false;
        // Configure Cesium to use a proxy. This is required when
        // WMS does not have CORS headers. BTW, proxy will slow
        // down rendering.
        viewerSettings.cesiumProxy = true;
      } else {
        viewerSettings.singleTileWMS = true;
      }

      /** *************************************
       * Define OWS services url for Import WMS
       */
      viewerSettings.servicesUrl = {
        wms: [{
          name: 'Ifremer - Biologie',
          url: 'http://www.ifremer.fr/services/wms/biologie?'
        }, {
          name: 'Ifremer - Océanographie physique',
          url: 'http://www.ifremer.fr/services/wms/oceanographie_physique?service=WMS&request=GetCapabilities'
        }],
        wmts: [{
            name: 'Ifremer - maps.ngdc.noaa.gov',
            url: 'http://maps.ngdc.noaa.gov/arcgis/rest/services/web_mercator/etopo1_hillshade/MapServer/WMTS/1.0.0/WMTSCapabilities.xml'
        }]
      };

      viewerSettings.localisations = [];

      proj4.defs('EPSG:2154', '+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 ' +
          '+lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,' +
          '0,0' +
          ' +units=m +no_defs');
      proj4.defs('http://www.opengis.net/gml/srs/epsg.xml#2154', '+' +
          'proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 ' +
          '+y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs');

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
        mdExtentHighlight: new ol.style.Style({
          stroke: new ol.style.Stroke({
            color: 'orange',
            width: 3
          }),
          fill: new ol.style.Fill({
            color: 'rgba(255,255,0,0.3)'
          })
        })
      };

      /** *************************************
       * Define maps
       */
      var mapsConfig = {
        zoom: 2,
        maxResolution: 78271.51696402048
      };

      if(typeof sxtSettings != 'undefined') {
        if(sxtSettings.olView && sxtSettings.olView.extent) {
          sxtSettings.olView.extent = ol.proj.transformExtent(
              sxtSettings.olView.extent,
              'EPSG:4326', 'EPSG:3857');
        }
        angular.extend(mapsConfig, sxtSettings.olView);
      }


      /** Main tabs configuration */
      searchSettings.mainTabs = {
        search: {
          title: 'Search',
          titleInfo: 0,
          active: false
        },
        map: {
          title: 'Map',
          active: false,
          titleInfo: 0

        },
        panier: {
          title: 'Panier',
          active: false,
          titleInfo: 0
        }};

      /** Layers tabs configuration */
      searchSettings.layerTabs = {
        legend: {
          active: false
        },
        sort: {
          active: false
        },
        source: {
          active: false
        },
        ncwms: {
          active: false
        },
        wps: {
          active: false
        },
        wfsfilter: {
          active: false
        },
        annotations: {
          active: false
        }
      };

      /* thesaurus definition */
      searchSettings.defaultListOfThesaurus = [{
        id: 'local.theme.sextant-theme',
        labelFromThesaurus: true,
        field: 'sextantTheme',
        tree: true,
        label: {eng: 'Sextant', fre: 'Sextant'}
      }];

      /* Use name or labels for the columns */
      if (typeof sxtSettings !== 'undefined' && sxtSettings.useFacetLabelsInFeatureTable) {
        searchSettings.useFacetLabelsInFeatureTable = true
      }

      gnPanierSettings.projs = [{
        value: '4326',
        label: 'Geographique - Datum WGS84'
      },{
        value: '2154',
        label: 'Lambert - 93'
      },{
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

      gnPanierSettings.formats = {
        vector:[{
          value: 'ESRI Shapefile',
          label: 'Shapefile'
        },{
          value: 'MapInfo File',
          label: 'MapInfo'
        },{
          value: 'GML',
          label: 'GML'
        },{
          value: 'KML',
          label: 'KML'
        }, {
          value: 'GeoJSON',
          label: 'GeoJSON'
        }],
        raster:[{
          value: 'GTiff',
          label: 'GeoTiff'
        },{
          value: 'ECW',
          label: 'ECW'
        },{
          value: 'JPEG2000',
          label: 'JPEG2000'
        }]
      };

      searchSettings.tabOverflow = {
        search: false,
        panier: false,
        map: false
      };

      // overwrite settings
      searchSettings.grid.related = ['parent', 'children', 'sources', 'hassources'];

      viewerSettings.menuCollapsed = false;
      viewerSettings.menuHidden = false;

      // Define if children button must be displayed in search results.
      searchSettings.displayChildrenBtn = true;
      // Define if search should exclude children
      searchSettings.excludeChildrenFromSearch = false;

      // Define if the 'related' dropdown should show source/hassource relations
      searchSettings.hideSourceRelations = false;

      if(typeof sxtSettings != 'undefined') {
        angular.extend(searchSettings, sxtSettings);
        angular.extend(gnPanierSettings, sxtSettings.panier);

        if(sxtSettings.servicesUrl) {
          viewerSettings.servicesUrl = sxtSettings.servicesUrl;
        }
        if(sxtSettings.layerFilter)  {
          viewerSettings.layerFilter = sxtSettings.layerFilter;
        }
        if(sxtSettings.defaultContext)  {
          viewerSettings.defaultContext = sxtSettings.defaultContext;
        }
        if(sxtSettings.menuCollapsed)  {
          viewerSettings.menuCollapsed = sxtSettings.menuCollapsed;
        }
        if(sxtSettings.menuHidden)  {
          viewerSettings.menuHidden = sxtSettings.menuHidden;
        }
        if(sxtSettings.displayChildrenBtn)  {
          searchSettings.displayChildrenBtn = sxtSettings.displayChildrenBtn;
        }
        if(sxtSettings.excludeChildrenFromSearch)  {
          searchSettings.excludeChildrenFromSearch =
            sxtSettings.excludeChildrenFromSearch;
        }
        if(sxtSettings.hideSourceRelations)  {
          searchSettings.hideSourceRelations = sxtSettings.hideSourceRelations;
        }
        if(sxtSettings.processes)  {
          viewerSettings.processes = sxtSettings.processes;
        }
        if(sxtSettings.profileTool) {
          viewerSettings.profileTool = sxtSettings.profileTool;
        }
        if(sxtSettings.graticule)  {
          viewerSettings.mapConfig.graticuleOgcService = sxtSettings.graticule;
        }
        if(angular.isUndefined(searchSettings.tabOverflow.search)) {
          delete searchSettings.mainTabs.search;
        }
        if(angular.isUndefined(searchSettings.tabOverflow.map) ||
            searchSettings.viewerUrl) {
          delete searchSettings.mainTabs.map;
        }
        if(angular.isUndefined(searchSettings.tabOverflow.panier)) {
          delete searchSettings.mainTabs.panier;
        }
        if(sxtSettings.facetOrdering)  {
          searchSettings.facetOrdering = sxtSettings.facetOrdering;
        }
        if(sxtSettings.sortBy) {
          searchSettings.sortbyValues = sxtSettings.sortBy.map(
            function (criteria) {
              var name = criteria;
              var direction = '';
              if (name.substring(0, 1) == '+') {
                name = name.substring(1);
              } else if (name.substring(0, 1) == '-') {
                name = name.substring(1);
                direction = 'desc';
              }
              return {
                sortBy: name,
                sortOrder: direction
              }
            }
          );
        }
        if(sxtSettings.metadataFormatter)  {
          searchSettings.metadataFormatter = sxtSettings.metadataFormatter;
        }
        if(sxtSettings.metadataType)  {
          searchSettings.filters.push({
            "query_string": {
              "query": "+type:" + sxtSettings.metadataType
            }
          });
        }

        var esFacetConfig = gnGlobalSettings.gnCfg.mods.search.facetConfig;

        // legacy Sextant facets are specified as array, so in that case we know we have to migrate them
        if (Array.isArray(sxtSettings.facetConfig)) {
          sxtService.migrateLegacyFacetConfigToEs(esFacetConfig, sxtSettings.facetConfig);
        }
        // otherwise, let's just assume the intent was to specify v7-compatible configuration
        else if (typeof sxtSettings.facetConfig === 'object') {
          angular.extend(esFacetConfig, sxtSettings.facetConfig);
        }
      }

      searchSettings.sortbyDefault = searchSettings.sortbyValues[0];

      searchSettings.configWho = searchSettings.configWho || '';
      if(searchSettings.configWho) {
        searchSettings.filters.push({
            "query_string": {
              "query": "+orgName:(\"" + searchSettings.configWho.replace(/,/g, '\" OR \"') + "\")"
            }
          });
      }
      if(searchSettings.excludeChildrenFromSearch) {
        searchSettings.filters.push({
          "query_string": {
            "query": "!_exists_:parentUuid"
          }
        });
      }

      if (searchSettings.hideSourceRelations) {
        searchSettings.grid.related = searchSettings.grid.related.filter(
          function(type) {
            return type !== 'sources' && type !== 'hassources';
          }
        );
      }

      searchSettings.configWhat = searchSettings.configWhat || '';
      if(searchSettings.configWhat) {
        searchSettings.filters.push({
          "query_string": {
            "query": "+groupPublished:(\"" + searchSettings.configWhat.replace(/,/g, '\" OR \"') + "\")"
          }
        });
      }

      var searchMap = gnMapsManager.createMap(gnMapsManager.SEARCH_MAP);
      var viewerMap = gnMapsManager.createMap(gnMapsManager.VIEWER_MAP);

      searchMap.get('creationPromise').then(function() {
        searchMap.getView().setZoom(0);
        searchMap.getView().setCenter([280274.03240585705, 6053178.654789996]);
      });

      viewerMap.get('creationPromise').then(function() {
        // apply extent constraint if any
        if(mapsConfig.extent) {
          var view = viewerMap.getView();
          viewerMap.setView(new ol.View({
            center: view.getCenter(),
            resolution: view.getResolution(),
            projection: view.getProjection(),
            extent: mapsConfig.extent
          }));
        }
      });

      // Set custom config in gnSearchSettings
      angular.extend(searchSettings, {
        viewerMap: viewerMap,
        searchMap: searchMap
      });

      if(typeof sxtSettings != 'undefined') {
        if(angular.isDefined(sxtSettings.localisations)) {
          viewerSettings.localisations = sxtSettings.localisations;
          if(angular.isArray(viewerSettings.localisations)){
            viewerSettings.localisations.forEach(function(loc) {
              loc.extent = ol.proj.transformExtent(loc.extent,
                'EPSG:4326', viewerMap.getView().getProjection());
            });
          }
        }
      }
    }]);
})();
