(function() {

  goog.provide('gn_search_sextant');

  goog.require('gn_search');
  goog.require('gn_search_sextant_config');
  goog.require('gn_thesaurus');
  goog.require('gn_related_directive');
  goog.require('gn_search_default_directive');
  goog.require('gn_legendpanel_directive');
  goog.require('gn_wps');
  goog.require('gn_userfeedback');
  goog.require('sxt_directives');
  goog.require('sxt_services');
  goog.require('sxt_panier');
  goog.require('sxt_mdactionmenu');
  goog.require('sxt_linksbtn');
  goog.require('gn_sxt_utils');
  goog.require('gn_gridrelated_directive');
  goog.require('sxt_annotations');

  var module = angular.module('gn_search_sextant', [
    'gn_search',
    'gn_search_sextant_config',
    'gn_related_directive',
    'gn_search_default_directive',
    'gn_legendpanel_directive',
    'gn_thesaurus',
    'gn_wps',
    'gn_userfeedback',
    'sxt_directives',
    'sxt_services',
    'sxt_panier',
    'sxt_mdactionmenu',
    'sxt_linksbtn',
    'gn_sxt_utils',
    'gn_gridrelated_directive',
    'sxt_annotations'
  ]);

  $(document.body).append($('<div class="g"></div>'));

  // this will loop until the loading screen has been removed
  function removeLoadingScreen() {
    if (!$('.gn .sxt-loading').length) {
      setTimeout(removeLoadingScreen, 500);
      return;
    }
    $('.gn .sxt-loading').remove();
    $('.gn').css({
      'overflow-y': 'auto',
      'overflow-x': 'hidden'
    });
  }

  if(typeof sxtSettings != 'undefined') {
    var catModule = angular.module('gn_cat_controller');
    catModule.config(['gnGlobalSettings', 'gnLangs',
      function (gnGlobalSettings, gnLangs) {

        if (typeof sxtSettings != 'undefined') {
          gnGlobalSettings.gnCfg.mods.search.resultTemplate =
            '../../catalog/views/sextant/templates/mdview/grid.html';
          gnGlobalSettings.gnCfg.langDetector = sxtSettings.langDetector;

        }
      }]);

    // init gn_config
    var cfgModule = angular.module('gn_config', []);
    cfgModule.config(['gnViewerSettings', 'gnSearchSettings', 'gnGlobalSettings',
      function (gnViewerSettings, gnSearchSettings, gnGlobalSettings) {
        gnGlobalSettings.init({}, null, gnViewerSettings, gnSearchSettings);
      }
    ]);

    // avoid FOUC (flash of unstyled content)
    $('.gn').css({
      'overflow': 'hidden',
      'position': 'relative'
    });

    // api css loading: add a <link> element to the correct stylesheet
    // also add a preload link to inform the browser of the priority of the css
    // when the css is loaded, hide the loading screen & restore the general overflow attribute
    var theme = sxtSettings.theme || 'default';
    var stylesheetUrl = sxtGnUrl + '../static/api-' + theme + '.css';

    var preloadLink = document.createElement("link");
    preloadLink.href = stylesheetUrl;
    preloadLink.rel = "preload";
    preloadLink.as = "style";

    var link = document.createElement("link");
    link.href = stylesheetUrl;
    link.rel = "stylesheet";
    link.media = "screen";
    link.addEventListener('load', removeLoadingScreen);

    var head = document.querySelector('head');
    head.appendChild(preloadLink);
    head.appendChild(link);
  } else {
    window.addEventListener('load', removeLoadingScreen);
  }

  module.value('sxtGlobals', {
    keywords: {}
  });

  module.config(['$LOCALES', 'gnGlobalSettings',
    function($LOCALES, gnGlobalSettings) {
      $LOCALES.push('/../api/i18n/packages/search');
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
    'sxtEmodnetDownload',
    'gnConfigService',
    function($rootScope, $scope, $location, $window, suggestService,
             $http, gnSearchSettings, sxtService,
             gnViewerSettings, gnMap, gnThesaurusService, sxtGlobals, gnNcWms,
             $timeout, gnMdView, mdView, gnSearchLocation, gnMetadataActions,
             $translate, $q, gnUrlUtils, gnGlobalSettings, sxtPanierService,
             gnOwsContextService, gnConfig, sxtEmodnetDownload, gnConfigService) {

      var viewerMap = gnSearchSettings.viewerMap;
      var searchMap = gnSearchSettings.searchMap;
      $scope.mainTabs = gnSearchSettings.mainTabs;
      $scope.layerTabs = gnSearchSettings.layerTabs;
      $scope.gnMetadataActions = gnMetadataActions;

      gnViewerSettings.storage = 'sessionStorage';

      gnConfigService.load().then(function(c) {
        $scope.isRecordHistoryEnabled = gnConfig['system.metadata.history.enabled'];

        var statusSystemRating =
          gnConfig['system.localrating.enable'];

        if (statusSystemRating == 'advanced') {
          $scope.isUserFeedbackEnabled = true;
        }
        if (statusSystemRating == 'basic') {
          $scope.isRatingEnabled = true;
        }
      });

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
      // note: this is required in sextant because of API mode; should be handled in a cleaner way eventually
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

      // extent of the layers in the viewer (default: empty)
      viewerMap.set('addedExtent', ol.extent.createEmpty());

      $scope.displayMapTab = function() {
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

      $scope.getAnySuggestions = function(val, searchObj) {
        return suggestService.getAnySuggestions(val, searchObj);
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
          content: content,
          placement: 'bottom',
          trigger: 'manual'
        };

        pop.popover(opts);
        $timeout(function() { pop.popover('show'); });
        $timeout(function() {
          pop.popover('hide');
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
              viewerMap);

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
              viewerMap,
              link.url, link.name, undefined, md);
          } else {
            loadLayerPromise = gnMap.addWmsFromScratch(
              viewerMap,
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

              // add layer extent to added layers extent for the viewer
              if (layer.get('cextent') && viewerMap.get('addedExtent')) {
                viewerMap.set('addedExtent', ol.extent.extend(
                  layer.get('cextent'),
                  viewerMap.get('addedExtent')
                ));
              }

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
          // special case for Emodnet Chemistry (Matomo stat tracking)
          if (sxtEmodnetDownload.requiresDownloadForm(link)) {
            sxtEmodnetDownload.openDownloadForm([link.url], md.uuid);
            return;
          }

          if(link.protocol.match(
              "WWW:FTP|WWW:DOWNLOAD-1.0-link--download|WWW:DOWNLOAD-1.0-http--download|WWW:OPENDAP|MYO:MOTU-SUB") != null) {
            if (link.protocol == 'MYO:MOTU-SUB') {
              link.url = link.url.replace('action=describeproduct', 'action=productdownloadhome');
            }
            window.open(link.url);
            return;
          }

          // make sure the layer is not already there
          for (var i = 0; i < $scope.searchObj.panier.length; i++) {
            if ($scope.searchObj.panier[i].link.name === link.name &&
                $scope.searchObj.panier[i].link.url === link.url &&
                $scope.searchObj.panier[i].link.protocol === link.protocol) {
              return;
            }
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
          // first separate layers between the ones which need a download form
          // and the others
          var layersToForm = [];
          var otherLayers = [];
          layers.forEach(function(layer) {
            if(sxtEmodnetDownload.requiresDownloadForm(layer)) {
              layersToForm.push(layer);
            } else {
              otherLayers.push(layer);
            }
          });

          if (layersToForm.length) {
            var urls = layersToForm.map(function(layer) { return layer.url; });
            sxtEmodnetDownload.openDownloadForm(urls, md.uuid);
          }

          angular.forEach(otherLayers, function (layer) {
            $scope.resultviewFns.addMdLayerToPanier(layer, md);
          });
        }
      };

      // Share map loading functions
      gnViewerSettings.resultviewFns = $scope.resultviewFns;

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

      $scope.gotoPanier = function() {
        $location.path('/panier');
      };
      // In order to add all layers to the map at least two layers (and not maps) must exist
      $scope.getMapLayersCount = function(layers) {
        return layers.filter(
          function (layer) {return layer.contentType!=='OGC:OWS-C'}).length
      };
      $scope.hasExternalViewer = function() {
        return !!gnSearchSettings.viewerUrl;
      };

      var sortConfig = gnSearchSettings.sortBy.split('#');
      angular.extend($scope.searchObj, {
        advancedMode: false,
        viewerMap: viewerMap,
        searchMap: searchMap,
        panier: [],
        filters: gnSearchSettings.filters,
        defaultParams: {
          isTemplate: 'n',
          sortBy: sortConfig[0] || 'relevance',
          sortOrder: sortConfig[1] || ''
        },
        params: {
          isTemplate: 'n',
          sortBy: sortConfig[0] || 'relevance',
          sortOrder: sortConfig[1] || ''
        },
        sortbyValues: gnSearchSettings.sortbyValues
      });


      $scope.$watch('mainTabs.panier.active', function(a) {
        if(a === true) {
          sxtPanierService.bindPanierWithLayers($scope.searchObj.panier,
            viewerMap);
          sxtPanierService.addProcessesToItems($scope.searchObj.panier);
        }
        else if (a === false){
        }
      });

      // attempt to update map sizes
      // note: this is required in sextant because of API mode; should be handled in a cleaner way eventually
      $(window).load(function() {
        setTimeout(function () {
          viewerMap.updateSize();
          searchMap.updateSize();
        }, 500);
      });

      gnSearchSettings.mapProtocols = {
        layers: [
          ],
        services: [
          ]
      };

      // login button management
      $scope.allowLogin = typeof sxtSettings !== 'undefined' ?
      sxtSettings.allowLogin : true;

      // signin url is https://host/geonetwork/cas/login?service=<currentUrl>
      // following #41968, we make sure there is always a query string in currentUrl
      $scope.getSignInUrl = function() {
        var currentUrl = window.location.origin + window.location.pathname
            + (window.location.search || '?') + window.location.hash;
        return gnGlobalSettings.gnUrl + '../../cas/login?service=' +
          encodeURIComponent(currentUrl);
      };

      // signout url is https://host/geonetwork/logout?service=<currentUrl>
      $scope.getSignOutUrl = function() {
        var currentUrl = window.location.origin + window.location.pathname
          + (window.location.search || '?') + window.location.hash;
        return gnGlobalSettings.gnUrl + '../../signout?redirect=' +
          encodeURIComponent(currentUrl);
      };
    }]);

  module.controller('gnsSextantSearch', [
    '$scope',
    'gnSearchSettings',
    function($scope, gnSearchSettings) {
      $scope.ctrl = {};
      $scope.facetConfig = gnSearchSettings.facetConfig;
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
        $scope.searchObj.hiddenParams = searchSettings.filters;
      }

      // Get Thesaurus config and set first one as active
      $scope.thesaurus = searchSettings.defaultListOfThesaurus;

      $scope.mapfieldOpt = {
        relations: ['within']
      };

      $scope.$watch('searchObj.params',
        function(newFilters, oldVal) {
        var currentFilters = [], EXCLUDED_PARAMS = [
          'bboxes',
          'bucket',
          'editable',
          'fast',
          'from',
          'ownRecords',
          'resultType',
          'sortBy',
          'sortOrder',
          'to',
          'isTemplate',
          'owner'];
        for (var filterKey in newFilters) {
          // filter param is excluded from summary
          if (EXCLUDED_PARAMS.indexOf(filterKey) > -1) {
            continue;
          }

          var value = newFilters[filterKey];

          // value is empty/undefined
          if (!value || typeof value !== 'string') {
            continue;
          }
          $scope.searchObj.canReset = true;
          return;
        }
        $scope.searchObj.canReset = false;
      });

      $scope.facetConfig = searchSettings.facetConfig;

      $scope.titleSearchOnly = $scope.searchObj.params.hasOwnProperty('title');
      $scope.titleSearchToggleVisible = false;

      var dropDownCheckboxElement;
      var inputSearchElement;

      $scope.setDropDownSelectors = function(){
        dropDownCheckboxElement = $('.dropdown-menu.type-ahead-dropdown');
        inputSearchElement = $('.input-group.gn-search-input input');
      };

      $scope.isInputActive = function () {
        return inputSearchElement.is(':focus') ||
          dropDownCheckboxElement.is(':active');
      };

      // $scope.toggleTitleSearchOnly = function() {
      //   inputSearchElement.focus();
      //   $scope.titleSearchOnly = !$scope.titleSearchOnly;
      //   if ($scope.titleSearchOnly) {
      //     $scope.searchObj.params.title = $scope.searchObj.params.any;
      //     delete $scope.searchObj.params.any;
      //   } else {
      //     $scope.searchObj.params.any = $scope.searchObj.params.title;
      //     delete $scope.searchObj.params.title;
      //   }
      // }
      // $scope.isTitleSearchEnabled = function() {
      //   return $scope.titleSearchOnly;
      // }
      // $scope.showTitleSearchToggle = function(visible) {
      //   $scope.titleSearchToggleVisible = visible;
      // }

      // function addWildcardOnWords(searchString) {
      //   return searchString && searchString.split(/\s+/).map(function(part) {
      //     // do not add wildcards on uui
      //     return part.match(/[a-f0-9]{8}(?:-[a-f0-9]{4}){4}[a-f0-9]{8}/) ? part : part + '*';
      //   }).join(' ')
      // }
      // function removeWildcardOnWords(searchString) {
      //   return searchString && searchString.split(/\s+/).map(function(part) {
      //     return part.replace(/\*$/, '');
      //   }).join(' ')
      // }

      // $scope.searchInput = {};
      // Object.defineProperty($scope.searchInput, 'model', {
      //   set: function(newValue) {
      //     var key = $scope.titleSearchOnly ? 'title' : 'any';
      //     $scope.searchObj.params[key] = addWildcardOnWords(newValue);
      //   },
      //   get: function() {
      //     var key = $scope.titleSearchOnly ? 'title' : 'any';
      //     return removeWildcardOnWords($scope.searchObj.params[key]);
      //   }
      // });
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
            if (htmlS.height=='') {
              htmlS.height = '100%';
            }
            if (bodyS.height=='') {
              bodyS.minHeight = '100%';
              bodyS.height = '100%';
            }
            var fitHeight = function() {
              var height = $(document.body).height() - $(element).offset().top;
              if (attrs['sxtSizeDiff']) {
                height -= parseInt(attrs['sxtSizeDiff'], 10);
              }

              // use an arbitrary minimal height
              height = Math.max(600, height);

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

})();
