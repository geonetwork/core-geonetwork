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

  goog.provide('gn_selection_directive');

  var module = angular.module('gn_selection_directive', []);

  module.directive('gnSelectionWidget', [
    '$translate', 'hotkeys',
    'gnHttp', 'gnMetadataActions', 'gnConfig', 'gnConfigService',
    'gnSearchSettings', 'gnSearchManagerService',
    function($translate, hotkeys,
             gnHttp, gnMetadataActions, gnConfig, gnConfigService,
             gnSearchSettings, gnSearchManagerService) {

      return {
        restrict: 'A',
        scope: true,
        templateUrl: '../../catalog/components/search/resultsview/partials/' +
            'selection-widget.html',
        link: function(scope, element, attrs) {

          scope.customActions = gnSearchSettings.customSelectActions;
          var watchers = [];
          scope.checkAll = true;
          scope.excludePattern =
              new RegExp(attrs.excludeActionsPattern || '^$');
          scope.withoutActionMenu =
              angular.isDefined(attrs.withoutActionMenu) ? true : false;

          scope.mdService = gnMetadataActions;

          scope.operationOnSelectionInProgress = false;

          gnConfigService.load().then(function(c) {
            scope.isInspireValidationEnabled =
              gnConfig[gnConfig.key.isInspireEnabled] &&
              angular.isString(gnConfig['system.inspire.remotevalidation.url']);
          });

          scope.$on('operationOnSelectionStart', function() {
            scope.operationOnSelectionInProgress = true;
          });
          scope.$on('operationOnSelectionStop', function() {
            scope.operationOnSelectionInProgress = false;
          });

          // initial state
          gnSearchManagerService.selected(scope.searchResults.selectionBucket)
              .success(function(res) {
                if (angular.isArray(res)) {
                  scope.searchResults.selectedCount = res.length;
                }
              });

          var updateCkb = function(records) {
            var checked = true;
            records.forEach(function(md) {
              checked = checked && md['geonet:info'].selected;
            });
          };

          // set checkbox state on page change
          scope.$watchCollection('searchResults.records', function(records) {
            var w;
            while (w = watchers.pop()) { w(); }
            updateCkb(records);
            records.forEach(function(record, i) {
              watchers.push(scope.$watch(
                  'searchResults.records[' + i + ']["geonet:info"].selected',
                  function() { updateCkb(scope.searchResults.records); }
                  ));
            });
          });

          scope.select = function() {
            scope.checkAll = !scope.checkAll;
            if (scope.checkAll) {
              scope.selectAll(scope.searchResults.selectionBucket);
            } else {
              scope.unSelectAll(scope.searchResults.selectionBucket);
            }
          };

          scope.viewSelectionOnly = function() {
            return gnSearchManagerService.selected(
                scope.searchResults.selectionBucket)
                .then(function(res) {
                  if (angular.isArray(res.data)) {
                    scope.resetSearch({
                      _uuid: res.data.join(' or ')
                    });
                  }
                });
          };

          scope.getIcon = function() {
            if (scope.searchResults.selectedCount === 0) {
              return 'fa-square-o';
            } else if (scope.searchResults.selectedCount ==
                scope.searchResults.count) {
              return 'fa-check-square-o';
            } else {
              return 'fa-minus-square-o';
            }
          };

          scope.selectAllInPage = function(selected) {
            var uuids = [];
            scope.searchResults.records.forEach(function(record) {
              uuids.push(record.getUuid());
              record['geonet:info'].selected = selected;
            });

            gnSearchManagerService.select(uuids,
                scope.searchResults.selectionBucket)
                .success(function(res) {
                  scope.searchResults.selectedCount = parseInt(res, 10);
                });
          };

          scope.selectAll = function() {
            gnSearchManagerService.selectAll(
                scope.searchResults.selectionBucket)
                .success(function(res) {
                  scope.searchResults.selectedCount = parseInt(res, 10);
                  scope.searchResults.records.forEach(function(record) {
                    record['geonet:info'].selected = true;
                  });
                });
          };

          scope.unSelectAll = function() {
            gnSearchManagerService.selectNone(
                scope.searchResults.selectionBucket)
                .success(function(res) {
                  scope.searchResults.selectedCount = parseInt(res, 10);
                  scope.searchResults.records.forEach(function(record) {
                    record['geonet:info'].selected = false;
                  });
                });
          };
          hotkeys.bindTo(scope)
              .add({
                combo: 'a',
                description: $translate.instant('hotkeySelectAll'),
                callback: scope.selectAll
              }).add({
                combo: 'p',
                description: $translate.instant('hotkeySelectAllInPage'),
                callback: function() {
                  scope.selectAllInPage(true);
                }
              }).add({
                combo: 'n',
                description: $translate.instant('hotkeyUnSelectAll'),
                callback: scope.unSelectAll
              });

          scope.$on('mdSelectAll', scope.selectAll);
          scope.$on('mdSelectNone', scope.unSelectAll);

        }
      };

    }]);

  module.directive('gnSelectionMd', ['gnSearchManagerService',
    function(gnSearchManagerService) {

      return {
        restrict: 'A',
        link: function(scope, element, attrs) {

          scope.change = function() {
            var method = element[0].checked ? 'select' : 'unselect';
            gnSearchManagerService[method](
                scope.md.getUuid(), scope.searchResults.selectionBucket).
                success(function(res) {
                  scope.searchResults.selectedCount = parseInt(res, 10);
                });
          };

        }
      };
    }]);

  module.directive('gnContributeWidget', [function() {
    return {
      restrict: 'A',
      templateUrl: '../../catalog/components/search/resultsview/partials/' +
          'contribute-widget.html'
    };
  }]);
})();
