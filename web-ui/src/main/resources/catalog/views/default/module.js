/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {

  goog.provide('gn_search_default');




  goog.require('cookie_warning');
  goog.require('gn_mdactions_directive');
  goog.require('gn_related_directive');
  goog.require('gn_search');
  goog.require('gn_search_default_config');
  goog.require('gn_search_default_directive');

  var module = angular.module('gn_search_default',
      ['gn_search', 'gn_search_default_config',
       'gn_search_default_directive', 'gn_related_directive',
       'cookie_warning', 'gn_mdactions_directive']);


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
    'gnMdViewObj',
    'gnWmsQueue',
    'gnSearchLocation',
    'gnOwsContextService',
    'hotkeys',
    'gnGlobalSettings',
    function($scope, $location, suggestService, $http, $translate,
             gnUtilityService, gnSearchSettings, gnViewerSettings,
             gnMap, gnMdView, mdView, gnWmsQueue,
             gnSearchLocation, gnOwsContextService,
             hotkeys, gnGlobalSettings) {

      var viewerMap = gnSearchSettings.viewerMap;
      var searchMap = gnSearchSettings.searchMap;



      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
      $scope.modelOptionsForm = angular.copy(gnGlobalSettings.modelOptions);
      $scope.gnWmsQueue = gnWmsQueue;
      $scope.$location = $location;
      $scope.activeTab = '/home';
      $scope.resultTemplate = gnSearchSettings.resultTemplate;
      $scope.facetsSummaryType = gnSearchSettings.facetsSummaryType;
      $scope.location = gnSearchLocation;
      $scope.toggleMap = function () {
        $(searchMap.getTargetElement()).toggle();
        $('button.gn-minimap-toggle > i').toggleClass('fa-angle-double-left fa-angle-double-right');
      };
      hotkeys.bindTo($scope)
        .add({
            combo: 'h',
            description: $translate.instant('hotkeyHome'),
            callback: function(event) {
              $location.path('/home');
            }
          }).add({
            combo: 't',
            description: $translate.instant('hotkeyFocusToSearch'),
            callback: function(event) {
              event.preventDefault();
              var anyField = $('#gn-any-field');
              if (anyField) {
                gnUtilityService.scrollTo();
                $location.path('/search');
                anyField.focus();
              }
            }
          }).add({
            combo: 'enter',
            description: $translate.instant('hotkeySearchTheCatalog'),
            allowIn: 'INPUT',
            callback: function() {
              $location.search('tab=search');
            }
            //}).add({
            //  combo: 'r',
            //  description: $translate.instant('hotkeyResetSearch'),
            //  allowIn: 'INPUT',
            //  callback: function () {
            //    $scope.resetSearch();
            //  }
          }).add({
            combo: 'm',
            description: $translate.instant('hotkeyMap'),
            callback: function(event) {
              $location.path('/map');
            }
          });


      // TODO: Previous record should be stored on the client side
      $scope.mdView = mdView;
      gnMdView.initMdView();


      $scope.goToSearch = function (any) {
        $location.path('/search').search({'any': any});
      };
      $scope.canEdit = function(record) {
        // TODO: take catalog config for harvested records
        if (record && record['geonet:info'] &&
            record['geonet:info'].edit == 'true') {
          return true;
        }
        return false;
      };
      $scope.openRecord = function(index, md, records) {
        gnMdView.feedMd(index, md, records);
      };

      $scope.closeRecord = function() {
        gnMdView.removeLocationUuid();
      };
      $scope.nextRecord = function() {
        var nextRecordId = mdView.current.index + 1;
        if (nextRecordId === mdView.records.length) {
          // When last record of page reached, go to next page...
          // Not the most elegant way to do it, but it will
          // be easier using Solr search components
          $scope.$broadcast('nextPage');
        } else {
          $scope.openRecord(nextRecordId);
        }
      };
      $scope.previousRecord = function() {
        var prevRecordId = mdView.current.index - 1;
        if (prevRecordId === -1) {
          $scope.$broadcast('previousPage');
        } else {
          $scope.openRecord(prevRecordId);
        }
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

      // Set the default browse mode for the home page
      $scope.$watch('searchInfo', function (n, o) {
        if (angular.isDefined($scope.searchInfo.facet)) {
          if ($scope.searchInfo.facet['inspireThemes'].length > 0) {
            $scope.browse = 'inspire';
          } else if ($scope.searchInfo.facet['topicCats'].length > 0) {
            $scope.browse = 'topics';
          //} else if ($scope.searchInfo.facet['categories'].length > 0) {
          //  $scope.browse = 'cat';
          }
        }
      });

      $scope.$on('layerAddedFromContext', function(e,l) {
        var md = l.get('md');
        if(md) {
          var linkGroup = md.getLinkGroup(l);
          gnMap.feedLayerWithRelated(l,linkGroup);
        }
      });

      $scope.resultviewFns = {
        addMdLayerToMap: function (link, md) {

          if (gnMap.isLayerInMap(viewerMap,
              link.name, link.url)) {
            return;
          }
          gnMap.addWmsFromScratch(viewerMap, link.url, link.name, false, md).
          then(function(layer) {
            if(layer) {
              gnMap.feedLayerWithRelated(layer, link.group);
            }
          });
      },
        addAllMdLayersToMap: function (layers, md) {
          angular.forEach(layers, function (layer) {
            $scope.resultviewFns.addMdLayerToMap(layer, md);
          });
        },
        loadMap: function (map, md) {
          gnOwsContextService.loadContextFromUrl(map.url, viewerMap);
        }
      };

      // Manage route at start and on $location change
      if (!$location.path()) {
        $location.path('/home');
      }
      $scope.activeTab = $location.path().
          match(/^(\/[a-zA-Z0-9]*)($|\/.*)/)[1];

      $scope.$on('$locationChangeSuccess', function(next, current) {
        $scope.activeTab = $location.path().
            match(/^(\/[a-zA-Z0-9]*)($|\/.*)/)[1];

        // resize search map for any views exluding viewer
        if (!gnSearchLocation.isMap() && (!angular.isArray(
            searchMap.getSize()) || searchMap.getSize()[0] < 0)) {
          setTimeout(function() {
            searchMap.updateSize();

            // if an extent was obtained from a loaded context, apply it
            if(searchMap.get('lastExtent')) {
              searchMap.getView().fit(
                searchMap.get('lastExtent'),
                searchMap.getSize(), { nearest: true });
            }

            // TODO: load custom context to the search map
            //gnOwsContextService.loadContextFromUrl(
            //  gnViewerSettings.defaultContext,
            //  searchMap);

          }, 0);
        }

        // resize viewer map for corresponding view
        if (gnSearchLocation.isMap() && (!angular.isArray(
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

      angular.extend($scope.searchObj, {
        advancedMode: false,
        from: 1,
        to: 30,
        selectionBucket: 's101',
        viewerMap: viewerMap,
        searchMap: searchMap,
        mapfieldOption: {
          relations: ['within']
        },
        defaultParams: {
          'facet.q': '',
          resultType: gnSearchSettings.facetsSummaryType || 'details'
        },
        params: {
          'facet.q': '',
          resultType: gnSearchSettings.facetsSummaryType || 'details'
        }
      }, gnSearchSettings.sortbyDefault);

    }]);
})();
