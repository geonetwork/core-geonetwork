(function() {

  goog.provide('gn_search_sextant_config');

  var module = angular.module('gn_search_sextant_config', []);

  module.constant('gnPanierSettings', {});

  var gfiTemplateURL = '../../catalog/views/sextant/templates/gfi.html';

  module.value('gfiTemplateURL', gfiTemplateURL);

  module.value('baselayerTemplateURL', '../../catalog/views/sextant/templates/baselayer.html');
  module.value('kmlimportTemplateURL', '../../catalog/views/sextant/templates/kmlimport.html');

  module.run([
    'gnSearchSettings',
    'gnViewerSettings',
    'gnPanierSettings',
    'gnGlobalSettings',
    'gnMap',
    'gnConfigService',
    function(searchSettings, viewerSettings, gnPanierSettings,
             gnGlobalSettings, gnMap, gnConfigService) {

      if(typeof sxtSettings != 'undefined') {
        gnGlobalSettings.init({},
          (typeof sxtGnUrl != 'undefined') ? sxtGnUrl : undefined,
          viewerSettings, searchSettings);

        // use bing key from settings (this should be integrated into a larger
        // refactoring of the way settings are loaded in API mode)
        gnConfigService.loadPromise.then(function(data) {
          viewerSettings.bingKey = data['ui.config'].mods.map.bingKey;
        });
      }


      gnGlobalSettings.isMapViewerEnabled =
          gnGlobalSettings.isMapViewerEnabled || true;

      /** *************************************
       * Define mapviewer background layers
       */
      viewerSettings.bgLayers = (typeof sxtGnUrl != 'undefined') ?  [
        new ol.layer.Tile()
      ] : [
        gnMap.createLayerForType('osm'),
        gnMap.createLayerForType('bing_aerial')
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
          name: 'Pigma - Central WMS Service',
          url: 'http://ids.pigma.org/geoserver/wms'
        }, {
          name: 'Pigma - IGN',
          url: 'http://ids.pigma.org/geoserver/ign/wms'
        }, {
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

      var viewerMap = new ol.Map({
        view: new ol.View(mapsConfig),
        controls: []
      });

      var searchMap = new ol.Map({
        layers: [
          new ol.layer.Tile({
            source: new ol.source.OSM()
          })
        ],
        controls: [],
        view: new ol.View({
          center: [280274.03240585705, 6053178.654789996],
          zoom: 0
        })
      });

      $(window).load(function() {
        viewerMap.updateSize();
        searchMap.updateSize();
      });

      //Fix change view contraints
      //TODO: Wait for fix in ol3: https://github.com/openlayers/ol3/issues/4265
      if(mapsConfig.extent) {
        var view = viewerMap.getView();
        view.on('change:center', function(e) {
          if(!ol.extent.containsCoordinate(mapsConfig.extent, view.getCenter())) {
            view.setCenter(view.constrainCenter(view.getCenter()));
          }
        });
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
        }
      };

      searchSettings.formatter = {
        defaultUrl: function(md) {
          var url;
          if(md.getSchema() == 'iso19139.sdn-product') {
            url = 'md.format.xml?xsl=sdn-emodnet&uuid=' + md.getUuid();
          } else if(md.getSchema() == 'iso19139.emodnet.hydrography') {
            url = 'md.format.xml?xsl=emodnet&uuid=' + md.getUuid();
          } else if(md.getSchema() == 'iso19115-3') {
            var view =
              md.standardName ===
                'ISO 19115-3 - Emodnet Checkpoint - Upstream Data' ? 'medsea' :
                (md.standardName === 'ISO 19115-3 - Emodnet Checkpoint - Targeted Data Product' ?
                  'checkpoint-tdp' :
                  (md.standardName === 'ISO 19115-3 - Emodnet Checkpoint - Data Product Specification' ?
                  'checkpoint-dps' : 'default'
                ));
            url = 'md.format.xml?root=div&css=checkpoint&xsl=xsl-view&view=' + view +
                    '&uuid=' + md.getUuid();
          } else {
            if (md.standardName === 'ISO 19115:2003/19139 - EMODNET - BATHYMETRY' ||
                md.standardName === 'ISO 19115:2003/19139 - EMODNET - HYDROGRAPHY') {
              url = 'md.format.xml?root=div&xsl=xsl-view&view=emodnetHydrography&uuid=' + md.getUuid();
            } else {
              url = 'md.format.xml?xsl=sxt_view&uuid=' + md.getUuid();
            }
          }
          return url;
        },
        defaultPdfUrl: 'md.format.pdf?xsl=full_view&uuid=',
        list: [
          {label: 'fullView', url: 'md.format.xml?xsl=full_view&uuid='}
        ]
        // TODO: maybe formatter config should depends
        // on the metadata schema.
        //schema: {
        //  iso19139: 'md.format.xml?xsl=full_view&id='
        //}
      };

      /* thesaurus definition */
      searchSettings.defaultListOfThesaurus = [{
        id: 'local.theme.sextant-theme',
        labelFromThesaurus: true,
        field: 'sextantTheme',
        tree: true,
        label: {eng: 'Sextant', fre: 'Sextant'}
      }];

      // Set custom config in gnSearchSettings
      angular.extend(searchSettings, {
        viewerMap: viewerMap,
        searchMap: searchMap
      });

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

      viewerSettings.menuExpanded = true;

      // Define if children button must be displayed in search results.
      searchSettings.displayChildrenBtn = true;
      // Define if search should exclude children
      searchSettings.excludeChildrenFromSearch = false;


      // TODO: Too many changes made in sextant
      // compared to GeoNetwork facet config
      searchSettings.facetConfig = [{
        key: 'publishedForGroup',
        index: '_groupPublished',
        filter: true
      }, {
        key: 'sextantTheme',
        tree: true
      }, {
        langs: {
          eng: 'inspireTheme_en',
          fre: 'inspireTheme_fr'
        }
      }, {
        key: 'keyword'
      }, {
        key: 'orgName'
      }, {
        key: 'createDateYear'
      }];

      if(typeof sxtSettings != 'undefined') {
        angular.extend(searchSettings, sxtSettings);
        angular.extend(gnPanierSettings, sxtSettings.panier);

        if(sxtSettings.servicesUrl) {
          viewerSettings.servicesUrl = sxtSettings.servicesUrl;
        }
        if(angular.isDefined(sxtSettings.localisations)) {
          viewerSettings.localisations = sxtSettings.localisations;
          if(angular.isArray(viewerSettings.localisations)){
            viewerSettings.localisations.forEach(function(loc) {
              loc.extent = ol.proj.transformExtent(loc.extent,
                'EPSG:4326', viewerMap.getView().getProjection());
            });
          }
        }
        if(sxtSettings.layerFilter)  {
          viewerSettings.layerFilter = sxtSettings.layerFilter;
        }
        if(sxtSettings.defaultContext)  {
          viewerSettings.defaultContext = sxtSettings.defaultContext;
        }
        if(sxtSettings.menuExpanded)  {
          viewerSettings.menuExpanded = sxtSettings.menuExpanded;
        }
        if(sxtSettings.displayChildrenBtn)  {
          searchSettings.displayChildrenBtn = sxtSettings.displayChildrenBtn;
        }
        if(sxtSettings.excludeChildrenFromSearch)  {
          searchSettings.excludeChildrenFromSearch =
            sxtSettings.excludeChildrenFromSearch;
        }
        if(sxtSettings.processes)  {
          viewerSettings.processes = sxtSettings.processes;
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
        if(sxtSettings.sortBy)  {
          searchSettings.sortbyValues = sxtSettings.sortBy.map(
            function(criteria) {
              var name = criteria;
              var direction = '';
              if (name.substring(0, 1) == '+') {
                name = name.substring(1);
              } else if (name.substring(0, 1) == '-') {
                name = name.substring(1);
                direction = 'reverse';
              }
              return {
                sortBy: name,
                sortOrder: direction
              }
            }
          );
        }
      }

      searchSettings.sortbyDefault = searchSettings.sortbyValues[0];

      // searchSettings.hiddenParams = {
      //   type: 'dataset or series or publication or nonGeographicDataset or ' +
      //       'feature or featureCatalog or map'
      // };
      searchSettings.configWho = searchSettings.configWho || '';
      if(searchSettings.configWho) {
        angular.extend(searchSettings.filters, {
          orgName: searchSettings.configWho.replace(/,/g, ' or ')
        })
      }
      if(searchSettings.excludeChildrenFromSearch) {
        searchSettings.filters.isChild = 'false';
      }

      searchSettings.configWhat = searchSettings.configWhat || '';
      if(searchSettings.configWhat) {
        angular.extend(searchSettings.filters, {
          _groupPublished: searchSettings.configWhat.replace(/,/g, ' or ')
        })
      }
    }]);
})();
