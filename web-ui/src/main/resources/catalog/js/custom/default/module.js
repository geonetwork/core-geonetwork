(function() {

  goog.provide('gn_search_default');

  goog.require('gn_search');
  goog.require('gn_search_default_config');
  goog.require('gn_search_default_directive');

  var module = angular.module('gn_search_default',
      ['gn_search', 'gn_search_default_config', 'gn_search_default_directive']);



  module.controller('gnsSearchPopularController', [
    '$scope', 'gnSearchSettings',
    function($scope, gnSearchSettings) {
      $scope.searchObj = {
        permalink: false,
        params: {
          sortBy: 'popularity',
          from: 1,
          to: 9
        }
      };
    }]);


  module.controller('gnsSearchLatestController', [
    '$scope',
    function($scope) {
      $scope.searchObj = {
        permalink: false,
        params: {
          sortBy: 'changeDate',
          from: 1,
          to: 9
        }
      };
    }]);

  module.controller('gnsDefault', [
    '$scope',
    '$location',
    'suggestService',
    '$http',
    '$translate',
    'gnUtilityService',
    'gnSearchSettings',
    'gnViewerSettings',
    'gnMap',
    'gnMdView',
    'hotkeys',
    function($scope, $location, suggestService, $http, $translate,
             gnUtilityService, gnSearchSettings, gnViewerSettings,
             gnMap, gnMdView, hotkeys) {

      var viewerMap = gnSearchSettings.viewerMap;
      var searchMap = gnSearchSettings.searchMap;
      $scope.$location = $location;
      $scope.resultTemplate = gnSearchSettings.resultTemplate;

      $scope.mainTabs = {
        home: {
          title: 'Home',
          titleInfo: '',
          active: true
        },
        search: {
          title: 'Search',
          titleInfo: '',
          active: false
        },
        view: {
          title: 'view',
          titleInfo: '',
          active: false
        },
        map: {
          title: 'Map',
          active: false
        }};

      hotkeys.bindTo($scope)
        .add({
            combo: 'h',
            description: $translate('hotkeyHome'),
            callback: function(event) {
              $scope.mainTabs.home.active = true;
            }
          }).add({
            combo: 't',
            description: $translate('hotkeyFocusToSearch'),
            callback: function(event) {
              event.preventDefault();
              var anyField = $('#gn-any-field');
              if (anyField) {
                gnUtilityService.scrollTo();
                $scope.mainTabs.search.active = true;
                anyField.focus();
              }
            }
          }).add({
            combo: 'enter',
            description: $translate('hotkeySearchTheCatalog'),
            allowIn: 'INPUT',
            callback: function() {
              $location.search('tab=search');
            }
            //}).add({
            //  combo: 'r',
            //  description: $translate('hotkeyResetSearch'),
            //  allowIn: 'INPUT',
            //  callback: function () {
            //    $scope.resetSearch();
            //  }
          }).add({
            combo: 'm',
            description: $translate('hotkeyMap'),
            callback: function(event) {
              $scope.mainTabs.map.active = true;
            }
          });


      // TODO: Previous record should be stored on the client side
      var mdView = {
        previousRecords: [],
        current: {
          record: null,
          index: null
        }
      };
      $scope.mdView = mdView;

      $scope.openRecord = function(index, md, records) {
        gnMdView.feedMd(index, md, records, mdView);
        gnUtilityService.scrollTo();
      };

      $scope.closeRecord = function() {
        mdView.current.record = null;
        //$location.search(searchUrl);
        $scope.mainTabs.search.active = true;
      };
      $scope.nextRecord = function() {
        // TODO: When last record of page reached, go to next page...
        $scope.openRecord(mdView.current.index + 1);
      };
      $scope.previousRecord = function() {
        $scope.openRecord(mdView.current.index - 1);
      };

      $scope.infoTabs = {
        lastRecords: {
          title: 'lastRecords',
          titleInfo: '',
          active: true
        },
        preferredRecords: {
          title: 'preferredRecords',
          titleInfo: '',
          active: false
        }};
      $scope.addLayerToMap = function(number) {
        $scope.mainTabs.map.titleInfo = '+' + number;
      };

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
        $scope.mainTabs.map.titleInfo = '';
      };

      // Switch to the location requested tab.
      $scope.$on('$locationChangeStart', function(next, current) {
        var params = $location.search();
        if (params.tab) {
          var tab = $scope.mainTabs[params.tab];
          if (tab && tab.active === false) {
            tab.active = true;
          }
        }
      });

      $scope.$watch('searchObj.advancedMode', function(val) {
        if (val && (searchMap.getSize()[0] == 0 ||
            searchMap.getSize()[1] == 0)) {
          setTimeout(function() {
            searchMap.updateSize();
          }, 0);
        }
      });

      angular.extend($scope.searchObj, {
        advancedMode: false,
        from: 1,
        to: 30,
        viewerMap: viewerMap,
        searchMap: searchMap
      }, gnSearchSettings.sortbyDefault);
    }]);
})();
