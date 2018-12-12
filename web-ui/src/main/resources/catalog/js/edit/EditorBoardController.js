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
      $scope.onlyMyRecord = {
        is: gnGlobalSettings.gnCfg.mods.editor.isUserRecordsOnly
      };
      $scope.isFilterTagsDisplayed =
          gnGlobalSettings.gnCfg.mods.editor.isFilterTagsDisplayed;
      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
      $scope.defaultSearchObj = {
        permalink: true,
        sortbyValues: gnSearchSettings.sortbyValues,
        hitsperpageValues: gnSearchSettings.hitsperpageValues,
        selectionBucket: 'e101',
        params: {
          sortBy: 'changeDate',
          _isTemplate: 'y or n',
          resultType: $scope.facetsSummaryType,
          from: 1,
          to: 20
        },
        defaultParams: {
          sortBy: 'changeDate',
          _isTemplate: 'y or n',
          resultType: $scope.facetsSummaryType,
          from: 1,
          to: 20
        }
      };
      angular.extend($scope.searchObj, $scope.defaultSearchObj);


      $scope.toggleOnlyMyRecord = function(callback) {
        $scope.onlyMyRecord.is ? setOwner() : unsetOwner();
        callback();
      };

      var setOwner = function() {
        $scope.searchObj.params['_owner'] = $scope.user.id;
      };

      var unsetOwner = function() {
        delete $scope.searchObj.params['_owner'];
      };

      $scope.$watch('user.id', function(newId) {
        if (angular.isDefined(newId) && $scope.onlyMyRecord.is) {
          setOwner();
        }
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
    '$rootScope',
    '$location',
    'gnSearchSettings',
    function($scope, $rootScope, $location, gnSearchSettings) {

      // Refresh list when privileges are updated
      $scope.$on('PrivilegesUpdated', function(event, data) {
        if(data && data===true) {
          $rootScope.$broadcast('search');
        }
      });

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


  module.controller('GnEditorHotKeyController', [
    '$scope',
    '$location',
    'gnSearchSettings',
    'gnUtilityService',
    '$timeout',
    'hotkeys',
    '$translate',
    function($scope, $location, gnSearchSettings, gnUtilityService,
             $timeout, hotkeys, $translate) {

      $timeout(function() {
        hotkeys.bindTo($scope)
          .add({
            combo: 'd',
            description: $translate.instant('hotkeyDirectory'),
            callback: function(event) {
              $location.path('/directory');
            }
          }).add({
          combo: 'i',
          description: $translate.instant('hotkeyImportRecord'),
          callback: function(event) {
            $location.path('/import');
          }
        }).add({
          combo: 'r',
          description: $translate.instant('hotkeyAccessManager'),
          callback: function(event) {
            $location.path('/accessManager');
          }
        }).add({
          combo: 'h',
          description: $translate.instant('hotkeyEditorBoard'),
          callback: function(event) {
            $location.path('/board');
          }
        }).add({
          combo: '+',
          description: $translate.instant('hotkeyAddRecord'),
          callback: function(event) {
            $location.path('/create');
          }
        }).add({
          combo: 't',
          description: $translate.instant('hotkeyFocusToSearch'),
          callback: function(event) {
            event.preventDefault();
            var anyField = $('#gn-any-field');
            if (anyField) {
              gnUtilityService.scrollTo();
              $location.path('/board');
              anyField.focus();
            }
          }
        }).add({
          combo: 'enter',
          description: $translate.instant('hotkeySearchTheCatalog'),
          allowIn: ['INPUT'],
          callback: function() {
            angular.element($('#gn-any-field'))
              .scope().triggerSearch()
          }
        }).add({
          combo: 'b',
          description: $translate.instant('hotkeyBatchEdit'),
          callback: function(event) {
            $location.path('/batchedit');
          }
        });
      }, 500);
    }
  ]);


})();
