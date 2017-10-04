(function() {

  goog.provide('gn_search_sextant');

  goog.require('gn_search');
  goog.require('gn_search_sextant_config');
  goog.require('gn_thesaurus');
  goog.require('gn_related_directive');
  goog.require('gn_search_default_directive');
  goog.require('gn_legendpanel_directive');
  goog.require('gn_wps');
  goog.require('sxt_directives');
  goog.require('sxt_panier');
  goog.require('sxt_mdactionmenu');
  goog.require('sxt_linksbtn');
  goog.require('gn_sxt_utils');
  goog.require('gn_gridrelated_directive');

  var module = angular.module('gn_search_sextant', [
    'gn_search',
    'gn_search_sextant_config',
    'gn_related_directive',
    'gn_search_default_directive',
    'gn_legendpanel_directive',
    'gn_thesaurus',
    'gn_wps',
    'sxt_directives',
    'sxt_panier',
    'sxt_mdactionmenu',
    'sxt_linksbtn',
    'gn_sxt_utils',
    'gn_gridrelated_directive'
  ]);

  $(document.body).append($('<div class="g"></div>'));

  if(typeof sxtSettings != 'undefined') {
    var catModule = angular.module('gn_cat_controller');
    catModule.config(['gnGlobalSettings', 'gnLangs',
      function(gnGlobalSettings, gnLangs) {

        if(typeof sxtSettings != 'undefined') {
          gnGlobalSettings.gnCfg.mods.search.resultTemplate =
            '../../catalog/views/sextant/templates/mdview/grid.html';
          gnGlobalSettings.gnCfg.langDetector = sxtSettings.langDetector;

        }
      }]);

    // api css loading
    var theme = sxtSettings.theme || 'default';
    var link = document.createElement("link");
    link.href = sxtGnUrl + '../static/api-' + theme + '.css';
    link.rel = "stylesheet";
    link.media = "screen";
    document.querySelector('head').appendChild(link);
  }


  module.value('sxtGlobals', {
    keywords: {}
  });

  module.config(['$LOCALES', 'gnGlobalSettings',
    function($LOCALES, gnGlobalSettings) {
      $LOCALES.push('sextant');

    }]);

  module.controller('gnsSextant', [
    '$rootScope',
    '$scope',
    '$location',
    '$window',
    'suggestService',
    '$http',
    'gnSearchSettings',
    'sxtService',
    'gnViewerSettings',
    'gnMap',
    'gnThesaurusService',
    'sxtGlobals',
    'gnNcWms',
    '$timeout',
    'gnMdView',
    'gnMdViewObj',
    'gnSearchLocation',
    'gnMetadataActions',
    '$translate',
    '$q',
    'gnUrlUtils',
    'gnGlobalSettings',
    'sxtPanierService',
    'gnOwsContextService',
    'gnConfig',
    function($rootScope, $scope, $location, $window, suggestService,
             $http, gnSearchSettings, sxtService,
             gnViewerSettings, gnMap, gnThesaurusService, sxtGlobals, gnNcWms,
             $timeout, gnMdView, mdView, gnSearchLocation, gnMetadataActions,
             $translate, $q, gnUrlUtils, gnGlobalSettings, sxtPanierService,
             gnOwsContextService, gnConfig) {

      // merge config in viewer settings
      if (gnConfig['ui.config']) {
        angular.merge(gnViewerSettings.mapConfig,
          gnConfig['ui.config'].mods.map);
      }
      gnViewerSettings.bingKey = gnViewerSettings.mapConfig.bingKey;

      var viewerMap = gnSearchSettings.viewerMap;
      var searchMap = gnSearchSettings.searchMap;
      $scope.mainTabs = gnSearchSettings.mainTabs;
      $scope.layerTabs = gnSearchSettings.layerTabs;
      $scope.gnMetadataActions = gnMetadataActions;

      gnViewerSettings.storage = 'sessionStorage';

      if(angular.isDefined(gnSearchSettings.tabOverflow.search)) {
        var updateTabVisibility = function() {
          if(gnSearchLocation.isMdView()) {
            $scope.inMdView = true;
          }
          else {
            $scope.inMdView = false;
          }
        };
        updateTabVisibility();
        $scope.$on('$locationChangeSuccess', updateTabVisibility);
      }

      $scope.locService = gnSearchLocation;


      // make sure search map is correctly rendered
      var unregisterMapsize = $scope.$on('locationBackToSearch', function() {
        if (angular.isUndefined(searchMap.getSize()) ||
            searchMap.getSize()[0] == 0 ||
            searchMap.getSize()[1] == 0) {
          $timeout(function() {searchMap.updateSize()}, 100);
        }
        unregisterMapsize();
      });

      var mapVisited = false; // Been once in mapviewer
      var waitingLayers = []; // Layers added from catalog but not visited yet
      var loadLayerPromises = []; // Promises to know when all layers are loaded

      $scope.displayMapTab = function() {

        // Make sure viewer map is correctly rendered
        if (angular.isUndefined(viewerMap.getSize()) ||
            viewerMap.getSize()[0] == 0 ||
            viewerMap.getSize()[1] == 0) {
          $timeout(function() {
            viewerMap.updateSize();

            // Zoom to last added layer on first visit to viewer map
            if(loadLayerPromises) {
              $q.all(loadLayerPromises).finally(function() {
                var extent = ol.extent.createEmpty();
                for(var i=0;i<waitingLayers.length;++i) {
                  var layerExtent = gnMap.reprojExtent(
                    waitingLayers[i].get('cextent'),
                    'EPSG:3857', viewerMap.getView().getProjection()
                  );
                  ol.extent.extend(extent, layerExtent);
                }
                if (!ol.extent.isEmpty(extent)) {
                  viewerMap.getView().fit(extent, viewerMap.getSize());
                }
                if (loadLayerPromises) delete loadLayerPromises;
                if (waitingLayers) delete waitingLayers;
              });
            }
          }, 0);
        }
        $scope.mainTabs.map.titleInfo = 0;
      };

      $scope.displayPanierTab = function() {
        $timeout(function() {
          $scope.$broadcast('renderPanierMap');
        },0)
      };

      //Check if a added layer is NcWMS
      gnViewerSettings.getPreAddLayerPromise =
        gnNcWms.feedOlLayer.bind(gnNcWms);

      // Manage sextantTheme thesaurus translation
      sxtGlobals.keywords['sextantThemePromise'] =
      gnThesaurusService.getKeywords(undefined, 'local.theme.sextant-theme',
        gnGlobalSettings.locale.iso3lang, 200).then(function(data) {
            sxtGlobals.keywords.sextantTheme = data;
            return data;
          });

      ///////////////////////////////////////////////////////////////////
      ///////////////////////////////////////////////////////////////////
      $scope.getAnySuggestions = function(val) {
        var url = suggestService.getUrl(val, 'anylight',
            ('STARTSWITHONLY'));

        return $http.get(url, {
        }).then(function(res) {
          return res.data[1];
        });
      };

      /** Manage metadata view */
      /*
       $scope.mdView = mdView;
       gnMdView.initMdView();

       $scope.openRecord = function(index, md, records) {
       gnMdView.feedMd(index, md, records);
       };

       $scope.closeRecord = function() {
       gnMdView.removeLocationUuid();
       };
       $scope.nextRecord = function() {
       // TODO: When last record of page reached, go to next page...
       $scope.openRecord(mdView.current.index + 1);
       };
       $scope.previousRecord = function() {
       $scope.openRecord(mdView.current.index - 1);
       };
       */

      ///////////////////////////////////////////////////////////////////

      $scope.$on('layerAddedFromContext', function(e,l) {
        var md = l.get('md');
        if(md) {
          var linkGroup = md.getLinkGroup(l);
          gnMap.feedLayerWithRelated(l,linkGroup);
          var downloads = l.get('downloads');
          if( downloads && downloads.length > 1) {
            var d = sxtService.getTopPriorityDownload(downloads);
            if(d) {
              l.set('downloads', [d]);
            }
          }
        }
      });

      $scope.addLayerPopover = function(to, opt_el) {
        var pop = opt_el || $('#sxt-tabswitcher');
        if(!pop) {
          return;
        }
        var content = $translate.instant('addLayerPopover' + to);
        var opts = {
          content:content,
          placement:'bottom'
        };

        pop.popover(opts);
        $timeout(function() { pop.popover('show'); });
        $timeout(function() {
          pop.popover('destroy');
        }, 5000);
      };

      $scope.resultviewFns = {
        addMdLayerToMap: function(link, md, $event) {

          if(gnSearchSettings.viewerUrl) {
            var url = gnSearchSettings.viewerUrl;
            url = url.replace('${wmsurl}', link.url);
            url = url.replace('${layername}', link.name);
            window.open(url, '_blank');
            return;
          }

          // if this is a context: handle it differently
          if (link.protocol.indexOf('OGC:OWS-C') > -1) {
            gnOwsContextService.loadContextFromUrl(link.url,
              $scope.searchObj.viewerMap);

            // clear md scope
            if(gnSearchLocation.isMdView()) {
              angular.element($('[gn-metadata-display]')).scope().dismiss();
            }

            // switch to map
            gnSearchLocation.setMap();
            return;
          }

          var loadLayerPromise;

          // handle WMTS layer info
          if (link.protocol.indexOf('WMTS') > -1) {
            loadLayerPromise = gnMap.addWmtsFromScratch(
              $scope.searchObj.viewerMap,
              link.url, link.name, undefined, md);
          } else {
            loadLayerPromise = gnMap.addWmsFromScratch(
              $scope.searchObj.viewerMap,
              link.url, link.name, undefined, md);
          }

          loadLayerPromise.then(function(layer) {
            if(layer) {
              var group, theme = md.sextantTheme;
              var themes = sxtGlobals.keywords.sextantTheme;
              if(angular.isArray(themes)) {
                for (var i = 0; i < themes.length; i++) {
                  var t = themes[i];
                  if (t.props.uri == theme) {
                    group = t.label;
                    break;
                  }
                }
              }
              layer.set('group', group);
              gnMap.feedLayerWithRelated(layer, link.group);
              if(link.extra && link.extra.downloads) {
                layer.set('downloads', link.extra.downloads);
              }
              if(waitingLayers) waitingLayers.push(layer);
              if (loadLayerPromises) loadLayerPromises.push(loadLayerPromise);

              if(gnSearchLocation.isMdView()) {
                angular.element($('[gn-metadata-display]')).scope().dismiss();
                $location.path('/map');
              }
              var el;
              if ($event) {
                el = $($event.currentTarget).parents('.gn-grid-item');
              }
              $scope.addLayerPopover('map', el);
              $scope.mainTabs.map.titleInfo += 1;
            }
          });


        },
        addAllMdLayersToMap: function (layers, md, $event) {
          angular.forEach(layers, function (layer) {
            $scope.resultviewFns.addMdLayerToMap(layer, md, $event);
          });
        },

        addMdLayerToPanier: function(link, md, $event) {
          if(link.protocol.match(
                  "WWW:FTP|WWW:DOWNLOAD-1.0-link--download|WWW:OPENDAP|MYO:MOTU-SUB") != null) {
            if (link.protocol == 'MYO:MOTU-SUB') {
              link.url = link.url.replace('action=describeproduct', 'action=productdownloadhome');
            }
            window.open(link.url);
            return;
          }
          $scope.searchObj.panier.push({
            link: link,
            md: md
          });
          if(gnSearchLocation.isMdView()) {
            angular.element($('[gn-metadata-display]')).scope().dismiss();
            $location.path('/panier');
          }
          var el;
          if ($event) {
            el = $($event.currentTarget).parents('.gn-grid-item');
          }
          $scope.addLayerPopover('panier', el);
        },

        addAllMdLayersToPanier: function (layers, md) {
          angular.forEach(layers, function (layer) {
            $scope.resultviewFns.addMdLayerToPanier(layer, md);
          });
        }
      };

      // Manage layer url parameters
      $timeout(function() {
        if (gnViewerSettings.wmsUrl && gnViewerSettings.layerName) {
          var loadLayerPromise =
              gnMap.addWmsFromScratch(viewerMap, gnViewerSettings.wmsUrl,
                  gnViewerSettings.layerName, true).

                  then(function(layer) {
                    if(layer) {
                      layer.set('group', gnViewerSettings.layerGroup);
                      layer.set('fromUrlParams', true);
                      viewerMap.addLayer(layer);
                      if(waitingLayers) waitingLayers.push(layer);
                      $scope.addLayerPopover('map');
                      if (!$scope.mainTabs.map.active) {
                        $scope.mainTabs.map.titleInfo += 1;
                      }
                    }
                  });
          if (loadLayerPromises) loadLayerPromises.push(loadLayerPromise);
        }
      },0);


      // Manage tabs height for api
      $scope.tabOverflow = gnSearchSettings.tabOverflow;

      // Manage routing
      if (!$location.path()) {
        if (!$scope.tabOverflow.search && $scope.tabOverflow.map) {
          gnSearchLocation.setMap();
        } else {
          gnSearchLocation.setSearch();
        }
      }

      gnMdView.initFormatter(gnSearchSettings.formatterTarget || '.gn');
      gnSearchLocation.initTabRouting($scope.mainTabs);
      $rootScope.$on('$locationChangeSuccess', function(){
        var tab = $location.path().match(/^\/([a-zA-Z0-9]*)($|\/.*)/)[1];

        // resize search map for any views exluding viewer
        if (tab == 'search' && (!angular.isArray(
            searchMap.getSize()) || searchMap.getSize()[0] < 0)) {
          setTimeout(function() {
            searchMap.updateSize();

            // if an extent was obtained from a loaded context, apply it
            if(searchMap.get('lastExtent')) {
              searchMap.getView().fit(
                searchMap.get('lastExtent'),
                searchMap.getSize(), { nearest: true });
            }
          }, 0);
        }

        // resize viewer map for corresponding view
        if (tab == 'map' && (!angular.isArray(
            viewerMap.getSize()) || viewerMap.getSize().indexOf(0) >= 0)) {
          setTimeout(function() {
            viewerMap.updateSize();

            // if an extent was obtained from a loaded context, apply it
            if(viewerMap.get('lastExtent')) {
              viewerMap.getView().fit(
                viewerMap.get('lastExtent'),
                viewerMap.getSize(), { nearest: true });
            }
          }, 0);
        }
      });


      $scope.gotoPanier = function() {
        $location.path('/panier');
      };

      angular.extend($scope.searchObj, {
        advancedMode: false,
        viewerMap: viewerMap,
        searchMap: searchMap,
        panier: [],
        filters: gnSearchSettings.filters
      });

      /**
       * API, get the url params to get layers or OWC
       */
      if(typeof sxtSettings != 'undefined') {
        var params = gnUrlUtils.parseKeyValue(window.location.search.
            replace(/^\?/, ''));
        gnViewerSettings.owsContext = params.owscontext &&
            decodeURIComponent(params.owscontext);
        gnViewerSettings.wmsUrl = params.wmsurl;
        gnViewerSettings.layerName = params.layername;
        gnViewerSettings.layerGroup = params.layergroup;
      }

      $scope.$watch('mainTabs.panier.active', function(a) {
        if(a === true) {
          sxtPanierService.bindPanierWithLayers($scope.searchObj.panier,
            viewerMap);
        }
        else if (a === false){
        }
      });

      // avoid FOUC (see end of file)
      $('.gn, .g').css({
        'maxHeight': '',
        'overflow': 'auto'
      });

      viewerMap.updateSize();
      searchMap.updateSize();
    }]);

  module.controller('gnsSextantSearch', [
    '$scope',
    'gnOwsCapabilities',
    'gnMap',
    'sxtGlobals',
    function($scope, gnOwsCapabilities, gnMap, sxtGlobals) {
      $scope.ctrl = {};
    }]);

  module.controller('gnsSextantSearchForm', [
    '$scope', 'gnSearchSettings',
    function($scope, searchSettings) {

      $scope.isFacetsCollapse = function(facetKey) {
        return !$scope.searchObj.params[facetKey];
      };
      $scope.treeFacetCollapsed = $scope.isFacetsCollapse('sextantTheme');

      // Run search on bbox draw
      $scope.$watch('searchObj.params.geometry', function(v){
        if(angular.isDefined(v)) {
          $scope.triggerSearch();
        }
      });


      if (searchSettings.filters) {
        $scope.searchObj.filters = searchSettings.filters;
      }

      // Get Thesaurus config and set first one as active
      $scope.thesaurus = searchSettings.defaultListOfThesaurus;

      $scope.mapfieldOpt = {
        relations: ['within']
      };

      // Disable/enable reset button
      var defaultSearchParams = ['sortBy', 'from', 'to', 'fast',
        '_content_type'];
      $scope.$watch('searchObj.params', function(v) {
        for (var p in v) {
          if(defaultSearchParams.indexOf(p) < 0) {
            $scope.searchObj.canReset = true;
            return;
          }
        }
        $scope.searchObj.canReset = false;
      });

      $scope.facetConfig = searchSettings.facetConfig;

    }]);

  module.directive('sxtFixMdlinks', [ 'sxtService',
    function(sxtService) {

      return {
        restrict: 'A',
        scope: false,
        link: function(scope) {
          sxtService.feedMd(scope);
          scope.getScope = function() {
            return scope;
          };
          scope.mdUrl = location.origin + location.pathname + '#/metadata/' + scope.md.getUuid();
        }
      };
    }]);

  module.directive('sxtSize', [
    function() {

      return {
        restrict: 'A',
        scope: {
          size: '@sxtSize'
        },
        link: function(scope, element, attrs) {
          if (scope.size == 'auto') {
            var htmlS = document.documentElement.style;
            var bodyS = document.body.style;
            if (htmlS.height=='' && htmlS.height=='') {
              htmlS.height = '100%';
            }
            if (bodyS.height=='' && bodyS.height=='') {
              bodyS.minHeight = '100%';
            }
            var fitHeight = function() {
              var height = $(document.body).height() - $(element).offset().top;
              if (attrs['sxtSizeDiff']) {
                height -= parseInt(attrs['sxtSizeDiff'], 10);
              }
              element.css('height', height+'px');
            };
            $(window).on('resize', fitHeight);
            fitHeight();
          } else if (parseInt(scope.size, 10) != NaN) {
            var height = parseInt(scope.size, 10) + 'px';
            element.css('height', height);
          }

        }
      };
    }]);

  module.directive('sxtLayerToolsToggle', [ '$rootScope', function($rootScope) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        scope.active.maximized = false;
        element.on('click', function() { scope.$apply(function(){
          scope.active.maximized = !scope.active.maximized;
        }); });
        $rootScope.$on('owsContextReseted', function() {
          scope.active.maximized = false;
          scope.active.layersTools = false;
        });

      }
    };
  }]);

  // fix angularjs bug fixed in v1.5.0-beta.1 : some html special char are
  // interpreted: &param => %B6m
  module.directive('sxtFixLinks', [ '$filter', '$sce',
    function($filter, $sce) {
      var icon = '<span class="fa-stack fa-lg">' +
          '<i class="fa fa-square fa-stack-2x"></i>' +
          '<i class="fa fa-link fa-stack-1x fa-inverse"></i>' +
          '</span>';
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          scope.text = scope.result.get(scope.attr) || '';
          if (scope.text.indexOf('http') < 0) {
            return;
          }
          var link = $filter('linky')(scope.text);
          link = link.replace(/>(.)*</,' target="_blank">' + icon + '<')
          scope.text = $sce.trustAsHtml(link.replace(/&#182;/, '&para'));
        }
      }
    }
  ]);

  module.directive('sxtTruncate', [ '$timeout', function($timeout) {
    return {
      restrict: 'A',
      link: {
        post:function(scope, element, attrs) {
          var source;
          var PX_PER_CHAR_AVG = 6.2;
          var resize = function() {
            var len = Math.round(element.parent().width()*2 / PX_PER_CHAR_AVG);
            var text = source.substr(0, len) + ((source.length > len) ? '…' : '');
            element.text(text);
          };
          $timeout(function() {
            source = element.text();
            resize();
          }, 0, false);
          $(window).on('resize', resize);
          scope.$on('$destroy', function() {
            $(window).off('resize', resize);
          });
        }
      }
    }
  }]);

  // avoid FOUC (flash of unstyled content)
  $('.gn, .g').css({
    'maxHeight': '0px',
    'overflow': 'hidden'
  });

})();
