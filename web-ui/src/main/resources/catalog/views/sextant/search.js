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
  goog.require('sxt_interceptors');
  goog.require('sxt_mdactionmenu');
  goog.require('sxt_linksbtn');

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
    'sxt_interceptors',
    'sxt_mdactionmenu',
    'sxt_linksbtn'
  ]);

  $(document.body).append($('<div class="g"></div>'));

  if(typeof sxtSettings != 'undefined') {
    var catModule = angular.module('gn_cat_controller');
    catModule.config(['gnGlobalSettings',
      function(gnGlobalSettings) {
        var lang,
            lCfg = sxtSettings.langDetector;

        if(lCfg) {
          if(lCfg.fromHtmlTag) {
            lang = $('html').attr('lang').substr(0,2);
          }
          else if(lCfg.regexp) {
            var res = new RegExp(lCfg.regexp).exec(location.pathname);
            if(angular.isArray(res)) {
              lang = res[1];
            }
          }
          else if (lCfg.lang) {
            lang = lCfg.lang
          }
          if(lang == 'fr') lang = 'fre';
          else if(lang == 'en') lang = 'eng';
          else lang = '';
        }
        lang = lang || 'eng';
        gnGlobalSettings.locale = {
          lang: lang.substr(0,2)
        };

        // ${api.gn.url}
        if(sxtGnUrl) {
          gnGlobalSettings.gnUrl =
              sxtGnUrl + lang + '/';
        }
        else {
          console.error('The variable sxtGnUrl is not defined !');
        }
      }]);
  }


  module.value('sxtGlobals', {});

  module.config(['$LOCALES', 'gnGlobalSettings',
    function($LOCALES, gnGlobalSettings) {
    $LOCALES.push('sextant');

  }]);

  module.controller('gnsSextant', [
    '$scope',
    '$location',
    '$window',
    'suggestService',
    '$http',
    'gnSearchSettings',
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
    function($scope, $location, $window, suggestService,
             $http, gnSearchSettings,
        gnViewerSettings, gnMap, gnThesaurusService, sxtGlobals, gnNcWms,
        $timeout, gnMdView, mdView, gnSearchLocation, gnMetadataActions,
        $translate, $q, gnUrlUtils, gnGlobalSettings) {

      var viewerMap = gnSearchSettings.viewerMap;
      var searchMap = gnSearchSettings.searchMap;
      $scope.mainTabs = gnSearchSettings.mainTabs;
      $scope.layerTabs = gnSearchSettings.layerTabs;
      $scope.gnMetadataActions = gnMetadataActions;

      var localStorage = $window.localStorage || {};


      $scope.gnUrl = gnGlobalSettings.gnUrl || '';

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
                  ol.extent.extend(extent, waitingLayers[i].get('cextent'));
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
      viewerMap.getLayers().on('add', function(e) {
        var layer = e.element;
        if (layer.get('advanced') == true) {
          gnNcWms.feedOlLayer(layer);
        }
      });

      // Manage sextantTheme thesaurus translation
      gnThesaurusService.getKeywords(undefined, 'local.theme.sextant-theme',
          200, 1).then(function(data) {
        sxtGlobals.sextantTheme = data;
        $scope.$broadcast('sextantThemeLoaded');
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
          gnMap.feedLayerWithDownloads(l,linkGroup);
        }
      });

      $scope.addLayerPopover = function(to) {
        var pop = $('#sxt-tabswitcher');
        if(!pop) {
          return;
        }
        var content = $translate('addLayerPopover' + to);
        var opts = {
          content:content,
          placement:'bottom'
        };

        pop.popover(opts);
        pop.popover('show');
        $timeout(function() {
          pop.popover('destroy');
        }, 5000);
      };

      $scope.resultviewFns = {
        addMdLayerToMap: function(link, md) {

          if(gnSearchSettings.viewerUrl) {
            var url = gnSearchSettings.viewerUrl;
            url = url.replace('${wmsurl}', link.url);
            url = url.replace('${layername}', link.name);
            window.open(url, '_blank');
            return;
          }
          if(gnMap.isLayerInMap($scope.searchObj.viewerMap,
              link.name, link.url)) {
            return;
          }

          var group, theme = md.sextantTheme;
          if(angular.isArray(sxtGlobals.sextantTheme)) {
            for (var i = 0; i < sxtGlobals.sextantTheme.length; i++) {
              var t = sxtGlobals.sextantTheme[i];
              if (t.props.uri == theme) {
                group = t.label;
                break;
              }
            }
          }

          var loadLayerPromise = gnMap.addWmsFromScratch($scope.searchObj.viewerMap,
              link.url, link.name, undefined, md).then(function(layer) {
                layer.set('group', group);
                gnMap.feedLayerWithDownloads(layer, link.group);
                if(waitingLayers) waitingLayers.push(layer);
              });
          if (loadLayerPromises) loadLayerPromises.push(loadLayerPromise);

          if(gnSearchLocation.isMdView()) {
            angular.element($('[gn-metadata-display]')).scope().dismiss();
            $location.path('/map');
          }
          $scope.addLayerPopover('map');
          $scope.mainTabs.map.titleInfo += 1;

        },
        addAllMdLayersToMap: function (layers, md) {
          angular.forEach(layers, function (layer) {
            $scope.resultviewFns.addMdLayerToMap(layer, md);
          });
        },

        addMdLayerToPanier: function(link, md) {
          if(link.protocol == 'WWW:DOWNLOAD-1.0-link--download') {           //'WWW:DOWNLOAD-1.0-link--download') {
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
          $scope.addLayerPopover('panier');
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
                    layer.set('group', gnViewerSettings.layerGroup);
                    viewerMap.addLayer(layer);
                    if(waitingLayers) waitingLayers.push(layer);
                    $scope.addLayerPopover('map');
                    if (!$scope.mainTabs.map.active) {
                      $scope.mainTabs.map.titleInfo += 1;
                    }
                  });
          if (loadLayerPromises) loadLayerPromises.push(loadLayerPromise);
        }
      },0);


      // Manage tabs height for api
      $scope.tabOverflow = gnSearchSettings.tabOverflow;

      // Manage routing
      if (!$location.path()) {
        gnSearchLocation.setSearch();
      }

      gnMdView.initFormatter(gnSearchSettings.formatterTarget || '.gn');
      gnSearchLocation.initTabRouting($scope.mainTabs);

      $scope.gotoPanier = function() {
        $location.path('/panier');
      };

      angular.extend($scope.searchObj, {
        advancedMode: false,
        viewerMap: viewerMap,
        searchMap: searchMap,
        panier: [],
        hiddenParams: gnSearchSettings.hiddenParams
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
    }]);

  module.controller('gnsSextantSearch', [
    '$scope',
    'gnOwsCapabilities',
    'gnMap',
    'sxtGlobals',
    function($scope, gnOwsCapabilities, gnMap, sxtGlobals) {

    }]);

  module.controller('gnsSextantSearchForm', [
    '$scope', 'gnSearchSettings',
    function($scope, searchSettings) {

      $scope.isFacetsCollapse = function(facetKey) {
        return !$scope.searchObj.params[facetKey];
      };

      // Run search on bbox draw
      $scope.$watch('searchObj.params.geometry', function(v){
        if(angular.isDefined(v)) {
          $scope.triggerSearch();
        }
      });

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
          }
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

  module.directive('sxtLayerToolsToggle', [
    function() {

      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          var target = $(attrs['sxtLayerToolsToggle']);
          var fa = element.find('.fa');
          element.on('click', function() {
            scope.$apply(function(){
              element.toggleClass('active');
              fa.toggleClass('fa-plus');
              fa.toggleClass('fa-minus');
              target.toggleClass('sxt-maximize-layer-tools',
                element.hasClass('active'));
            });
          });
        }
      };
    }]);

  module.directive('sxtCustomScroll', [ function() {
      return {
        restrict: 'A',
        link: {
          post: function(scope, element, attrs) {
            var axis = attrs['axis'] || 'y';

            element.mCustomScrollbar({
              theme: 'dark-3',
              axis: axis,
              advanced:{
                updateOnContentResize: true,
                updateOnImageLoad: true
              },
              // alwaysShowScrollbar: 2,
              scrollButtons: {
                enable: true
              },
              callbacks: {
                onScrollStart: function() {
                  element.trigger('scroll');
                }
              }
            });

          }
        }
      };
    }]);


  module.service('sxtService', [ function() {

    this.feedMd = function(scope) {
      var md = scope.md;

      if(md.type.indexOf('dataset')>=0) {
        md.icon = {cls: 'fa-database', title: 'dataset'}
      }
      else if(md.type.indexOf('series')>=0) {
        md.icon = {cls: 'fa-database', title: 'series'}
      }
      else if(md.type.indexOf('software')>=0) {
        md.icon = {cls: 'fa-hdd-o', title: 'software'}
      }
      else if(md.type.indexOf('map')>=0) {
        md.icon = {cls: 'fa-globe', title: 'map'}
      }
      else if(md.type.indexOf('application')>=0) {
        md.icon = {cls: 'fa-hdd-o', title: 'application'}
      }
      else if(md.type.indexOf('basicgeodata')>=0) {
        md.icon = {cls: 'fa-globe', title: 'basicgeodata'}
      }
      else if(md.type.indexOf('service')>=0) {
        md.icon = {cls: 'fa-globe', title: 'service'}
      }

      var status = md.mdStatus;
      var user = scope.user;
      scope.cantStatus = ((status == 4 || status == 2 || status == 3)
          && !user.isReviewerOrMore());


      scope.links = md.getLinksByType('LINK');
      scope.downloads = [];
      scope.layers = [];

      var transferOpts = md.getLinksByType('OGC:WMS').length != 1;

      angular.forEach(md.linksTree, function(transferOptions, i) {

        // get all layers and downloads for this transferOptions
        var layers = md.getLinksByType(i+1, 'OGC:WMTS',
            'OGC:WMS', 'OGC:OWS-C');
        var downloads = md.getLinksByType(i+1, '#FILE', '#DB', '#COPYFILE',
           '#WWW:DOWNLOAD-1.0-link--download', '#WFS', 'WCS');

        if(downloads.length > 0) {
          // If only one layer, we get only one download (we bind them later)
          // We take the first one cause there is a priority on the types
          if(layers.length == 1) {
            scope.downloads.push(downloads[0]);
          }
          else {
            scope.downloads = scope.downloads.concat(downloads);
          }
        }
        scope.layers = scope.layers.concat(layers);
      });
    }
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
          var link = $filter('linky')(scope.text.replace(/&#182;/, '&para'));
          link = link.replace(/>(.)*</,' target="_blank">' + icon + '<');
          scope.text = $sce.trustAsHtml(link);
        }
      }
    }
  ]);

})();
