(function() {

  goog.provide('gn_search_default');

  goog.require('gn_search');
  goog.require('gn_search_default_config');

  var module = angular.module('gn_search_default',
      ['gn_search', 'gn_search_default_config']);



  module.controller('gnsSearchPopularController', [
    '$scope',
    function($scope) {
      $scope.searchObj = {
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
    'gnSearchSettings',
    'gnViewerSettings',
    'gnMap',
    function($scope, $location, suggestService, $http, gnSearchSettings,
        gnViewerSettings, gnMap) {

      var viewerMap = gnSearchSettings.viewerMap;
      var searchMap = gnSearchSettings.searchMap;
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
        map: {
          title: 'Map',
          active: false
        }};
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
      });
    }]);
})();
