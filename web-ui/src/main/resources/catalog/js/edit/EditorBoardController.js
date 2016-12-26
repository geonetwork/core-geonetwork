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
  goog.provide('gn_editorboard_controller');



  goog.require('gn_mdactions_service');
  goog.require('gn_search');
  goog.require('gn_search_form_controller');

  var module = angular.module('gn_editorboard_controller',
      ['gn_search', 'gn_search_form_controller', 'gn_mdactions_service']);


  module.controller('GnEditorBoardSearchController', [
    '$scope',
    '$location',
    '$rootScope',
    '$translate',
    '$q',
    'gnSearchSettings',
    'gnMetadataActions',
    'gnGlobalSettings',
    function($scope, $location, $rootScope, $translate, $q,
        gnSearchSettings, gnMetadataActions, gnGlobalSettings) {
      $scope.onlyMyRecord = false;
      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
      $scope.defaultSearchObj = {
        permalink: false,
        sortbyValues: gnSearchSettings.sortbyValues,
        hitsperpageValues: gnSearchSettings.hitsperpageValues,
        selectionBucket: 'e101',
        params: {
          sortBy: 'changeDate',
          _isTemplate: 'y or n or s',
          resultType: $scope.facetsSummaryType,
          from: 1,
          to: 20
        }
      };
      angular.extend($scope.searchObj, $scope.defaultSearchObj);

      $scope.toggleOnlyMyRecord = function() {
        $scope.onlyMyRecord = !$scope.onlyMyRecord;
      };
      var setOwner = function() {
        $scope.searchObj.params['_owner'] = $scope.user.id;
      };
      var unsetOwner = function() {
        delete $scope.searchObj.params['_owner'];
      };
      $scope.$watch('onlyMyRecord', function(value) {
        if (!$scope.searchObj) {
          return;
        }

        value ? setOwner() : unsetOwner();
      });

      $scope.deleteRecord = function(md) {
        var deferred = $q.defer();

        gnMetadataActions.deleteMd(md).
            then(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('metadataRemoved',
                    {title: md.title || md.defaultTitle}),
                timeout: 2
              });
              deferred.resolve(data);
            }, function(reason) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant(reason.data.error.message),
                timeout: 0,
                type: 'danger'
              });
              deferred.reject(reason);
            });

        return deferred.promise;
      };
    }
  ]);
  module.controller('GnEditorBoardController', [
    '$scope',
    '$location',
    'gnSearchSettings',
    function($scope, $location, gnSearchSettings) {

      gnSearchSettings.resultViewTpls = [{
        tplUrl: '../../catalog/components/search/resultsview/' +
            'partials/viewtemplates/editor.html',
        tooltip: 'List',
        icon: 'fa-list'
      }];

      gnSearchSettings.resultTemplate =
          gnSearchSettings.resultViewTpls[0].tplUrl;

      $scope.facetsSummaryType = gnSearchSettings.facetsSummaryType = 'manager';

      gnSearchSettings.sortbyValues = [{
        sortBy: 'relevance',
        sortOrder: ''
      }, {
        sortBy: 'changeDate',
        sortOrder: ''
      }, {
        sortBy: 'title',
        sortOrder: 'reverse'
      }];

      gnSearchSettings.hitsperpageValues = [20, 50, 100];

      gnSearchSettings.paginationInfo = {
        hitsPerPage: gnSearchSettings.hitsperpageValues[0]
      };

    }
  ]);
})();
