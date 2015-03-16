(function() {

  goog.provide('gn_search_sextant');

  goog.require('gn_search');
  goog.require('gn_search_sextant_config');
  goog.require('gn_thesaurus');
  goog.require('gn_related_directive');
  goog.require('gn_search_default_directive');
  goog.require('gn_legendpanel_directive');
  goog.require('sxt_directives');
  goog.require('sxt_panier_directive');

  var module = angular.module('gn_search_sextant', [
    'gn_search',
    'gn_search_sextant_config',
    'gn_related_directive',
    'gn_search_default_directive',
    'gn_legendpanel_directive',
    'gn_thesaurus',
    'sxt_directives',
    'sxt_panier_directive'
  ]);

  module.value('sxtGlobals', {});

  module.config(['$LOCALES', function($LOCALES) {
    //$LOCALES.push('sextant');
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
    function($scope, $location, $window, suggestService,
             $http, gnSearchSettings,
        gnViewerSettings, gnMap, gnThesaurusService, sxtGlobals, gnNcWms,
        $timeout, gnMdView, mdView, gnSearchLocation, gnMetadataActions) {

      var viewerMap = gnSearchSettings.viewerMap;
      var searchMap = gnSearchSettings.searchMap;
      $scope.mainTabs = gnSearchSettings.mainTabs;
      $scope.gnMetadataActions = gnMetadataActions;

      var localStorage = $window.localStorage || {};

      // Manage routing
      if (!$location.path()) {
        gnSearchLocation.setSearch();
      }
      gnSearchLocation.initTabRouting($scope.mainTabs);

      $scope.gotoPanier = function() {
        $location.path('/panier');
      };

      $scope.locService = gnSearchLocation;

      // Manage the collapsed search panel
      $scope.collapsed = localStorage.searchWidgetCollapsed ?
          JSON.parse(localStorage.searchWidgetCollapsed) :
          { search: true};

      $scope.toggleSearch = function() {
        $scope.collapsed.search = !$scope.collapsed.search;
        $timeout(function() {
          gnSearchSettings.searchMap.updateSize();
        }, 300);
      };

      var storeCollapsed = function() {
        localStorage.searchWidgetCollapsed = JSON.stringify($scope.collapsed);
      };
      $scope.$watch('collapsed.search', storeCollapsed);

      $scope.$on('addLayerFromMd', function(evt, getCapLayer) {
        gnMap.addWmsToMapFromCap(viewerMap, getCapLayer);
      });

      $scope.displayMapTab = function() {
        if (angular.isUndefined(viewerMap.getSize()) ||
            viewerMap.getSize()[0] == 0 ||
            viewerMap.getSize()[1] == 0) {
          $timeout(function() {
            viewerMap.updateSize();
            if (gnViewerSettings.initialExtent) {
              viewerMap.getView().fitExtent(gnViewerSettings.initialExtent,
                  viewerMap.getSize());
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
        if (layer.get('isNcwms') == true) {
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

      $scope.$watch('searchObj.advancedMode', function(val) {
        if (val && (searchMap.getSize()[0] == 0 ||
            searchMap.getSize()[1] == 0)) {
          setTimeout(function() {
            searchMap.updateSize();
          }, 0);
        }
      });

      /** Manage metadata view */
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

      ///////////////////////////////////////////////////////////////////

      angular.extend($scope.searchObj, {
        advancedMode: false,
        viewerMap: viewerMap,
        searchMap: searchMap,
        panier: []
      });
    }]);

  module.controller('gnsSextantSearch', [
    '$scope',
    'gnOwsCapabilities',
    'gnMap',
    'sxtGlobals',
    function($scope, gnOwsCapabilities, gnMap, sxtGlobals) {

      $scope.resultviewFns = {
        addMdLayerToMap: function(link, md) {

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
          gnOwsCapabilities.getWMSCapabilities(link.url).then(function(capObj) {

            if(gnMap.isLayerInMap($scope.searchObj.viewerMap,
                link.name, link.url)) {
              return;
            }

            var layerInfo = gnOwsCapabilities.getLayerInfoFromCap(
                link.name, capObj);
            layerInfo.group = group;
            var layer = gnMap.addWmsToMapFromCap($scope.searchObj.viewerMap,
                layerInfo);
            layer.set('md', md);

          }, function(response) {
            console.warn('Error loading: ' + link.url);
          });
          $scope.mainTabs.map.titleInfo += 1;

        },
        addMdLayerToPanier: function(link, md) {
          $scope.searchObj.panier.push({
            link: link,
            md: md
          });
          $scope.mainTabs.panier.titleInfo += 1;
        }
      };
    }]);

  module.controller('gnsSextantSearchForm', [
    '$scope', 'gnSearchSettings',
    function($scope, searchSettings) {

      $scope.categorytreeCollapsed = true;

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

  module.directive('sxtFixMdlinks', [
    function() {

      return {
        restrict: 'A',
        scope: false,
        link: function(scope) {
          scope.links = scope.md.getLinksByType('LINK');
          scope.downloads = scope.md.getLinksByType('DOWNLOAD', '#FILE',
              '#DB', '#COPYFILE', 'WFS');
          scope.layers = scope.md.getLinksByType('OGC');

        }
      };
    }]);

})();
