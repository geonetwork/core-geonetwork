(function() {

  goog.provide('gn_search_default_config');

  var module = angular.module('gn_search_default_config', []);

  module.value('gnTplResultlistLinksbtn',
      '../../catalog/views/default/directives/partials/linksbtn.html');

  module
      .run([
        'gnSearchSettings',
        'gnViewerSettings',
        'gnOwsContextService',
        'gnMap',
        function(searchSettings, viewerSettings, gnOwsContextService, gnMap) {
          // Load the context defined in the configuration
          viewerSettings.defaultContext =
            viewerSettings.mapConfig.viewerMap ||
            '../../map/config-viewer.xml';

          // Keep one layer in the background
          // while the context is not yet loaded.
          viewerSettings.bgLayers = [
            gnMap.createLayerForType('osm')
          ];

          viewerSettings.bingKey = 'AnElW2Zqi4fI-9cYx1LHiQfokQ9GrNzcjOh_' +
              'p_0hkO1yo78ba8zTLARcLBIf8H6D';

          viewerSettings.servicesUrl =
            viewerSettings.mapConfig.listOfServices || {};

          // WMS settings
          viewerSettings.singleTileWMS = true;

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

          // Object to store the current Map context
          viewerSettings.storage = 'sessionStorage';

          /*******************************************************************
             * Define maps
             */
          var mapsConfig = {
            center: [280274.03240585705, 6053178.654789996],
            zoom: 2
            //maxResolution: 9783.93962050256
          };

          var viewerMap = new ol.Map({
            controls: [],
            view: new ol.View(mapsConfig)
          });

          var searchMap = new ol.Map({
            controls:[],
            layers: viewerMap.getLayers(),
            view: new ol.View({
              center: mapsConfig.center,
              zoom: 2
            })
          });


          /** Facets configuration */
          searchSettings.facetsSummaryType = 'details';

          /*
             * Hits per page combo values configuration. The first one is the
             * default.
             */
          searchSettings.hitsperpageValues = [20, 50, 100];

          /* Pagination configuration */
          searchSettings.paginationInfo = {
            hitsPerPage: searchSettings.hitsperpageValues[0]
          };

          /*
             * Sort by combo values configuration. The first one is the default.
             */
          searchSettings.sortbyValues = [{
            sortBy: 'relevance',
            sortOrder: ''
          }, {
            sortBy: 'changeDate',
            sortOrder: ''
          }, {
            sortBy: 'title',
            sortOrder: 'reverse'
          }, {
            sortBy: 'rating',
            sortOrder: ''
          }, {
            sortBy: 'popularity',
            sortOrder: ''
          }, {
            sortBy: 'denominatorDesc',
            sortOrder: ''
          }, {
            sortBy: 'denominatorAsc',
            sortOrder: 'reverse'
          }];

          /* Default search by option */
          searchSettings.sortbyDefault = searchSettings.sortbyValues[0];

          /* Custom templates for search result views */
          searchSettings.resultViewTpls = [{
                  tplUrl: '../../catalog/components/search/resultsview/' +
                  'partials/viewtemplates/grid.html',
                  tooltip: 'Grid',
                  icon: 'fa-th'
                }];

          // For the time being metadata rendering is done
          // using Angular template. Formatter could be used
          // to render other layout

          // TODO: formatter should be defined per schema
          // schema: {
          // iso19139: 'md.format.xml?xsl=full_view&&id='
          // }
          searchSettings.formatter = {
            // defaultUrl: 'md.format.xml?xsl=full_view&id='
            defaultUrl: 'md.format.xml?xsl=xsl-view&uuid=',
            defaultPdfUrl: 'md.format.pdf?xsl=full_view&uuid=',
            list: [{
            //  label: 'inspire',
            //  url: 'md.format.xml?xsl=xsl-view' + '&view=inspire&id='
            //}, {
            //  label: 'full',
            //  url: 'md.format.xml?xsl=xsl-view&view=advanced&id='
            //}, {
              label: 'full',
              url: 'md.format.xml?xsl=full_view&uuid='
              /*
              // You can use a function to choose formatter
              url : function(md) {
                return 'md.format.xml?xsl=full_view&uuid=' + md.getUuid();
              }*/
            }]
          };

          // Mapping for md links in search result list.
          searchSettings.linkTypes = {
            links: ['LINK', 'kml'],
            downloads: ['DOWNLOAD'],
            //layers:['OGC', 'kml'],
            layers:['OGC'],
            maps: ['ows']
          };

          // Set the default template to use
          searchSettings.resultTemplate =
              searchSettings.resultViewTpls[0].tplUrl;

          // Set custom config in gnSearchSettings
          angular.extend(searchSettings, {
            viewerMap: viewerMap,
            searchMap: searchMap
          });
        }]);
})();
