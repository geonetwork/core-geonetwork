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
  goog.provide('gn_category_directive');

  var module = angular.module('gn_category_directive', []);

  /**
     * Provide a list of categories if at least one
     * exist in the catalog
     *
     */
  module.directive('gnCategory', ['$http', '$translate',
    function($http, $translate) {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          element: '=gnCategory',
          lang: '@lang',
          label: '@label'
        },
        templateUrl: '../../catalog/components/category/partials/' +
            'category.html',
        link: function(scope, element, attrs) {
          $http.get('../api/tags', {cache: true}).
              success(function(data) {
                scope.categories = data;
              });
        }
      };
    }]);

  module.directive('gnBatchCategories', [
    'gnUtilityService', '$http', '$translate', '$q',
    function(gnUtilityService, $http, $translate, $q) {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: '../../catalog/components/category/partials/' +
            'batchcategory.html',
        link: function(scope, element, attrs) {
          scope.report = null;

          $http.get('../api/tags', {cache: true}).
              success(function(data) {
                scope.categories = data;
              });

          scope.save = function(replace) {
            scope.report = null;
            var defer = $q.defer();
            var params = [];
            var url = '../api/records/tags?' +
                        '&bucket=' +
                (attrs.selectionBucket || 'metadata') + '&' +
                        (replace ? 'clear=true&id=' : 'id=');
            angular.forEach(scope.categories, function(c) {
              if (c.checked === true) {
                params.push(c.id);
              }
            });
            $http.put(url + params.join('&id='))
                .success(function(data) {
                  scope.processReport = data;

                  gnUtilityService.openModal({
                    title: $translate.instant('categoriesUpdated'),
                    content: '<div gn-batch-report="processReport"></div>',
                    className: 'gn-category-popup',
                    onCloseCallback: function() {
                      scope.processReport = null;
                    }
                  }, scope, 'CategoryUpdated');

                  scope.report = data;
                  defer.resolve(data);
                }).error(function(data) {
                  scope.processReport = data;

                  gnUtilityService.openModal({
                    title: $translate.instant('categoriesUpdated'),
                    content: '<div gn-batch-report="processReport"></div>',
                    className: 'gn-category-popup',
                    onCloseCallback: function() {
                      scope.processReport = null;
                    }
                  }, scope, 'CategoryUpdated');

                  defer.reject(data);
                });
            return defer.promise;
          };
        }
      };
    }]);
})();
