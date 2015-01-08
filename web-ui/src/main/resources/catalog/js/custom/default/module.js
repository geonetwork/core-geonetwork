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

  module.controller('GnMdViewController', [
    '$scope', '$http', '$compile', 'gnSearchSettings',
    function($scope, $http, $compile, gnSearchSettings) {
      $scope.formatter = gnSearchSettings.formatter;
      $scope.usingFormatter = false;
      $scope.compileScope = $scope.$new();

      $scope.format = function(f) {
        $scope.usingFormatter = f !== undefined;
        $scope.currentFormatter = f;
        if (f) {
          $http.get(f.url + $scope.currentRecord.getUuid()).then(
              function(response) {
                var snippet = response.data.replace(
                    '<?xml version="1.0" encoding="UTF-8"?>', '');

                $('#gn-metadata-display').find('*').remove();

                $scope.compileScope.$destroy();

                // Compile against a new scope
                $scope.compileScope = $scope.$new();
                var content = $compile(snippet)($scope.compileScope);

                $('#gn-metadata-display').append(content);
              });
          }
        };

        // Reset current formatter to open the next record
        // in default mode.
        $scope.$watch('currentRecord', function () {
          $scope.usingFormatter = false;
          $scope.currentFormatter = null;
        });
    }]);

  module.controller('gnsDefault', [
    '$scope',
    '$location',
    'suggestService',
    '$http',
    'gnUtilityService',
    'gnSearchSettings',
    'gnViewerSettings',
    'gnMap',
    function($scope, $location, suggestService, $http, gnUtilityService,
             gnSearchSettings, gnViewerSettings, gnMap) {

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
        search: {
          title: 'view',
          titleInfo: '',
          active: false
        },
        map: {
          title: 'Map',
          active: false
        }};

      // TODO: Previous record should be stored on the client side
      var searchUrl = '';
      $scope.previousRecords = [];
      $scope.currentRecord = null;
      $scope.currentRecordIndex = null;
      $scope.openRecord = function(mdOrIndex, records) {
        $scope.records = records;
        var md = mdOrIndex;
        if (typeof mdOrIndex === 'number') {
          md = records[mdOrIndex];
          $scope.currentRecordIndex = mdOrIndex;
        } else {
          $scope.currentRecordIndex = this.$index;
        }
        $scope.currentRecord = md;

        //searchUrl = $location.search();
        //$location.search({uuid: md['geonet:info'].uuid});
        $scope.currentRecord.links = md.getLinksByType('LINK');
        $scope.currentRecord.downloads = md.getLinksByType('DOWNLOAD');
        $scope.currentRecord.layers = md.getLinksByType('OGC', 'kml');
        var thumbnails = md.getThumbnails();
        if (thumbnails) {
          $scope.currentRecord.overviews = thumbnails.list;
        }
        $scope.currentRecord.contacts = md.getContacts();
        $scope.currentRecord.encodedUrl = encodeURIComponent($location.absUrl());

        gnUtilityService.scrollTo();

        // TODO: do not add duplicates
        $scope.previousRecords.push($scope.currentRecord);
      };

      $scope.closeRecord = function(md) {
        $scope.currentRecord = null;
        //$location.search(searchUrl);
        $scope.mainTabs.search.active = true;
      };
      $scope.nextRecord = function () {
        $scope.openRecord($scope.currentRecordIndex + 1, $scope.records);
      }
      $scope.previousRecord = function () {
        $scope.openRecord($scope.currentRecordIndex - 1, $scope.records);
      }



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
