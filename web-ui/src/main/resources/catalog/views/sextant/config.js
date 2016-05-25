(function() {

  goog.provide('gn_search_sextant_config');

  var module = angular.module('gn_search_sextant_config', []);

  module.constant('gnPanierSettings', {});

  var gfiTemplateURL = '../../catalog/views/sextant/templates/gfi.html';

  module.value('gfiTemplateURL', gfiTemplateURL);

  module.value('baselayerTemplateURL', '../../catalog/views/sextant/templates/baselayer.html');
  module.value('kmlimportTemplateURL', '../../catalog/views/sextant/templates/kmlimport.html');

  module.run(['gnSearchSettings', 'gnViewerSettings', 'gnPanierSettings',
    'gnGlobalSettings', 'gnMap',

    function(searchSettings, viewerSettings, gnPanierSettings,
             gnGlobalSettings, gnMap) {

      gnGlobalSettings.isMapViewerEnabled =
          gnGlobalSettings.isMapViewerEnabled || true;

      /** *************************************
       * Define mapviewer background layers
       */
      viewerSettings.bgLayers = [
        gnMap.createLayerForType('mapquest'),
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

      viewerSettings.singleTileWMS = true;

      viewerSettings.bingKey = 'AplOhn33DW5iCpDv0bY-CzSriFoi6GE2r' +
          '5cY94SqSi47koQ1s4XlylK8DUB7NZFZ';

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

      viewerSettings.localisations = [{
        name: 'France',
        extent: [-817059, 4675034, 1719426, 7050085]
      }];

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
        zoom: 0,
        maxResolution: '39135.75848201024'
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

      /** Facets configuration */
      searchSettings.facetsSummaryType = 'hits';

      /* Custom templates for search result views */
      searchSettings.resultTemplate = '../../catalog/views/sextant/' +
          'templates/mdview/grid.html';

      searchSettings.formatter = {
        defaultUrl: function(md) {
          var url;
          if(md.getSchema() == 'iso19139.sdn-product') {
            url = 'md.format.xml?xsl=sdn-emodnet&uuid=' + md.getUuid();
          } else if(md.getSchema() == 'iso19115-3') {
            var view =
              md.standardName === 'ISO 19115-3 - Emodnet Checkpoint' ? 'medsea' :
                (md.standardName ===
                  'ISO 19115-3 - Emodnet Checkpoint - Targeted Product' ?
                  'medsea-targeted-product' : 'default'
                );
            url = 'md.format.xml?xsl=xsl-view&view=' + view +
                    '&uuid=' + md.getUuid();
          } else {
            url = 'md.format.xml?xsl=sxt_view&uuid=' + md.getUuid();
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

      /* Hits per page combo values configuration */
      searchSettings.hitsperpageValues = [20, 50, 100];

      searchSettings.paginationInfo = {
        hitsPerPage: searchSettings.hitsperpageValues[0]
      };
      /* Sort by combo values configuration */
      searchSettings.sortbyValues = [
        {sortBy: 'popularity', sortOrder: ''},
        {sortBy: 'title', sortOrder: 'reverse'},
        {sortBy: 'changeDate', sortOrder: ''}];
      searchSettings.sortbyDefault = searchSettings.sortbyValues[0];

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
          value: 'Jpeg2000',
          label: 'Jpeg2000'
        }]
      };

      searchSettings.tabOverflow = {
        search: false,
        panier: false,
        map: false
      };

      viewerSettings.menuExpanded = true;

      // Define if children button must be displayed in search results.
      searchSettings.displayChildrenBtn = true;
      // Define if search should exclude children
      searchSettings.excludeChildrenFromSearch = false;

      searchSettings.facetConfig = [{
        key: 'publishedForGroup',
        index: '_groupPublished'
      }, {
        key: 'category'
      }, {
        langs: {
          eng: 'inspireTheme_en',
          fre: 'inspireTheme_fr'
        }
      }, {
        key: 'keyword'
      },{
        key: 'orgName'
      },{
        key: 'createDateYear'
      }];

      if(typeof sxtSettings != 'undefined') {
        angular.extend(searchSettings, sxtSettings);
        angular.extend(gnPanierSettings, sxtSettings.panier);

        if(sxtSettings.servicesUrl) {
          viewerSettings.servicesUrl = sxtSettings.servicesUrl;
        }
        if(sxtSettings.localisations) {
          viewerSettings.localisations = sxtSettings.localisations;
          viewerSettings.localisations.forEach(function(loc) {
            loc.extent = ol.proj.transformExtent(loc.extent,
                'EPSG:4326', viewerMap.getView().getProjection());
          });
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
      }

      searchSettings.hiddenParams = {
        type: 'dataset or series or publication or nonGeographicDataset or ' +
            'feature or featureCatalog or map'
      };
      searchSettings.configWho = searchSettings.configWho || '';
      if(searchSettings.configWho) {
        angular.extend(searchSettings.hiddenParams, {
          orgName: searchSettings.configWho.replace(/,/g, ' or ')
        })
      }
      if(searchSettings.excludeChildrenFromSearch) {
        searchSettings.hiddenParams.isChild = 'false';
      }

      searchSettings.configWhat = searchSettings.configWhat || '';
      if(searchSettings.configWhat) {
        angular.extend(searchSettings.hiddenParams, {
          _groupPublished: searchSettings.configWhat.replace(/,/g, ' or ')
        })
      }
    }]);
})();
