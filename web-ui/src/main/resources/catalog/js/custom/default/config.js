(function() {

  goog.provide('gn_search_default_config');

  var module = angular.module('gn_search_default_config', []);

  module.run(['gnSearchSettings', 'gnViewerSettings', 'gnMap',

    function(searchSettings, viewerSettings, gnMap) {

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
        center: [280274.03240585705, 6053178.654789996],
        zoom: 2,
        maxResolution: 9783.93962050256
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
          zoom: 2
        })
      });

      /** Facets configuration */
      searchSettings.facetsConfig = [{
        key: 'type',
        value: 'types'
      },{
        key: 'keyword',
        value: 'keywords'
      },{
        key: 'category',
        value: 'categories'
      },{
        key: 'orgName',
        value: 'orgNames'
      },{
        key: 'format',
        value: 'formats'
      },{
        key: 'spatialRepresentationType',
        value: 'spatialRepresentationTypes'
      },{
        key: 'denominator',
        value: 'denominators'
      },{
        key: 'format',
        value: 'formats'
      },{
        key: 'createDateYear',
        value: 'createDateYears'
      },{
        key: 'metadataPOC',
        value: 'metadataPOCs'
      },{
        key: 'serviceType',
        value: 'serviceTypes'
      }];


      /* Hits per page combo values configuration.
         The first one is the default. */
      searchSettings.hitsperpageValues = [20, 50, 100];

      /* Pagination configuration */
      searchSettings.paginationInfo = {
        hitsPerPage: searchSettings.hitsperpageValues[0]
      };

      /* Sort by combo values configuration.
         The first one is the default. */
      searchSettings.sortbyValues = [
        {sortBy: 'relevance', sortOrder: ''},
        {sortBy: 'changeDate', sortOrder: ''},
        {sortBy: 'title', sortOrder: 'reverse'},
        {sortBy: 'rating', sortOrder: ''},
        {sortBy: 'popularity', sortOrder: ''},
        {sortBy: 'denominatorDesc', sortOrder: ''},
        {sortBy: 'denominatorAsc', sortOrder: 'reverse'}];

      /* Default search by option */
      searchSettings.sortbyDefault = searchSettings.sortbyValues[0];

      /* Custom templates for search result views */
      searchSettings.resultViewTpls = [{
        tplUrl: '../../catalog/components/search/resultsview/' +
            'partials/viewtemplates/grid.html',
        tooltip: 'Grid',
        icon: 'fa-th'
      }, {
        tplUrl: '../../catalog/components/search/resultsview/' +
            'partials/viewtemplates/title.html',
        tooltip: 'List',
        icon: 'fa-list'
      }, {
        tplUrl: '../../catalog/components/search/resultsview/' +
            'partials/viewtemplates/list.html',
        tooltip: 'Complete',
        icon: 'fa-th-list'
      }];

      // For the time being metadata rendering is done
      // using Angular template. Formatter could be used
      // to render other layout
      searchSettings.formatter = {
        //defaultUrl: 'md.format.xml?xsl=full_view&id='
        defaultUrl: 'md.format.xml?xsl=xsl-view&id=',
        list: [
          {label: 'inspire', url: 'md.format.xml?xsl=xsl-view' +
              '&view=inspire&id='},
          {label: 'full', url: 'md.format.xml?xsl=xsl-view&view=advanced&id='},
          {label: 'groovy', url: 'md.format.xml?xsl=full_view&id='}
        ]
        // TODO: maybe formatter config should depends
        // on the metadata schema.
        //schema: {
        //  iso19139: 'md.format.xml?xsl=full_view&&id='
        //}
      };

      // Set the default template to use
      searchSettings.resultTemplate = searchSettings.resultViewTpls[0].tplUrl;

      // Set custom config in gnSearchSettings
      angular.extend(searchSettings, {
        viewerMap: viewerMap,
        searchMap: searchMap
      });
    }]);
})();
