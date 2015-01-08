(function() {

  goog.provide('gn_search_sextant');











  goog.require('gn_search');
  goog.require('gn_search_sextant_config');
  goog.require('gn_thesaurus');
  goog.require('sxt_categorytree');
  goog.require('sxt_panier_directive');

  var module = angular.module('gn_search_sextant', [
    'gn_search',
    'gn_search_sextant_config',
    'sxt_panier_directive',
    'gn_thesaurus',
    'sxt_categorytree'
  ]);

  module.value('sxtGlobals', {});

  module.controller('gnsSextant', [
    '$scope',
    '$location',
    'suggestService',
    '$http',
    'gnSearchSettings',
    'gnViewerSettings',
    'gnMap',
    'gnThesaurusService',
    'sxtGlobals',
    'gnNcWms',
    function($scope, $location, suggestService, $http, gnSearchSettings,
        gnViewerSettings, gnMap, gnThesaurusService, sxtGlobals, gnNcWms) {

      var viewerMap = gnSearchSettings.viewerMap;
      var searchMap = gnSearchSettings.searchMap;

      $scope.mainTabs = {
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

      $scope.$on('addLayerFromMd', function(evt, getCapLayer) {
        gnMap.addWmsToMapFromCap(viewerMap, getCapLayer);
      });

      $scope.displayMapTab = function() {
        if (viewerMap.getSize()[0] == 0 || viewerMap.getSize()[1] == 0) {
          setTimeout(function() {
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
        $scope.$broadcast('renderPanierMap');
        $scope.mainTabs.panier.titleInfo = 0;
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
            ('STARTSWITHFIRST'));

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

          var label, theme = md.sextantTheme;
          for (var i = 0; i < sxtGlobals.sextantTheme.length; i++) {
            var t = sxtGlobals.sextantTheme[i];
            if (t.props.uri == theme) {
              label = t.label;
              break;
            }
          }
          gnOwsCapabilities.getWMSCapabilities(link.url).then(function(capObj) {
            var layerInfo = gnOwsCapabilities.getLayerInfoFromCap(
                link.name, capObj);
            layerInfo.group = label;
            var layer = gnMap.addWmsToMapFromCap($scope.searchObj.viewerMap,
                layerInfo);
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
    '$scope', 'suggestService', 'gnSearchSettings',
    function($scope, suggestService, searchSettings) {

      $scope.groupPublishedOptions = {
        mode: 'remote',
        remote: {
          url: suggestService.getUrl('QUERY', '_groupPublished',
              'STARTSWITHFIRST'),
          filter: suggestService.bhFilter,
          wildcard: 'QUERY'
        }
      };

      $scope.thesaurus = searchSettings.defaultListOfThesaurus;

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
