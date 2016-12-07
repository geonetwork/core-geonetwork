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
  goog.provide('gn_pagination_directive');

  var module = angular.module('gn_pagination_directive', []);

  module.directive('gnPagination', ['hotkeys', '$translate',
                                    function(hotkeys, $translate) {

      return {
        restrict: 'A',
        replace: true,
        require: '^ngSearchForm',
        scope: {
          config: '=gnPagination',
          values: '=hitsValues'
        },
        templateUrl: '../../catalog/components/search/pagination/partials/' +
            'pagination.html',
        link: function(scope, element, attrs, controller) {

          // Init config from default and eventual given one
          var defaultConfig = {
            pages: -1,
            currentPage: 1,
            hitsPerPage: 10
          };
          angular.extend(defaultConfig, scope.config);
          scope.config = defaultConfig;
          delete defaultConfig;
          /**
           * If an object {paginationInfo} is defined inside the
           * SearchFormController, then add from and to  params
           * to the search.
           */
          var getPaginationParams = function(customPageOptions) {
            var pageOptions = scope.config;
            angular.extend(pageOptions, customPageOptions);
            return {
              from: (pageOptions.currentPage - 1) * pageOptions.hitsPerPage + 1,
              to: pageOptions.currentPage * pageOptions.hitsPerPage
            };
          };
          controller.getPaginationParams = getPaginationParams;

          scope.updateSearch = function(hitsPerPage) {
            if (hitsPerPage) {
              scope.config.hitsPerPage = hitsPerPage;
            }
            controller.updateSearchParams(getPaginationParams());
            controller.triggerSearch(true);
          };

          scope.previous = function() {
            if (scope.config.currentPage > 1) {
              scope.config.currentPage -= 1;
              scope.updateSearch();
            }
          };
          scope.next = function() {
            if (scope.config.currentPage < scope.config.pages) {
              scope.config.currentPage += 1;
              scope.updateSearch();
            }
          };
          scope.first = function() {
            scope.config.currentPage = 1;
            scope.updateSearch();
          };
          scope.last = function() {
            scope.config.currentPage = scope.config.pages;
            scope.updateSearch();
          };

          if (angular.isDefined(attrs.enableEvents)) {
            var events = ['first', 'previous', 'next', 'last'];
            angular.forEach(events, function(key) {
              scope.$on(key + 'Page', function(evt, cbFn) {
                scope[key]();
                if (angular.isFunction(cbFn)) {
                  cbFn();
                }
              });
            });
          }
          controller.activatePagination();

          if (angular.isDefined(attrs.enableHotKeys)) {
            hotkeys.bindTo(scope)
                .add({
                  combo: 'ctrl+left',
                  description: $translate.instant('hotkeyFirstPage'),
                  callback: scope.first
                }).add({
                  combo: 'left',
                  description: $translate.instant('hotkeyPreviousPage'),
                  callback: scope.previous
                }).add({
                  combo: 'right',
                  description: $translate.instant('hotkeyNextPage'),
                  callback: scope.next
                }).add({
                  combo: 'ctrl+right',
                  description: $translate.instant('hotkeyLastPage'),
                  callback: scope.last
                });
          }
        }
      };
    }]);
})();
