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
  goog.provide('gn_static_pages_directive');

  var module = angular.module('gn_static_pages_directive', []);

  module.directive(
      'gnStaticPagesViewer', ['$http', '$location',
        function($http, $location) {
        return {
          restrict: 'AEC',
          replace: true,
          scope: {
            language: '@language'
          },
          templateUrl: '../../catalog/components/pages/partials/content.html',
          link: function($scope) {
            $scope.loadPageContent = function() {
              var page = $location.search().page;

              $http({
                method: 'GET',
                url: '../api/pages/' + $scope.language + '/' + page + '/content'
              }).then(function mySuccess(response) {
                $scope.content = response.data;
              }, function myError(response) {
                $scope.content = 'Page not available';
                console.log(response.statusText);
              });

            };

            function reloadPageContent() {
              $scope.loadPageContent();
            }

            $scope.$on('$locationChangeSuccess', reloadPageContent);

            $scope.loadPageContent();
          }
        };
      }]);

  module.directive(
      'gnStaticPagesListViewer', ['$http', '$location',
        function($http, $location) {
        return {
          restrict: 'AEC',
          replace: true,
          scope: {
            language: '@language',
            section: '@section'
          },
          templateUrl: function(elem, attr) {
            return '../../catalog/components/pages/partials/' + attr.section + '.html';
          },
          link: function($scope) {

            $scope.loadPages = function() {
              $http({
                method: 'GET',
                url: '../api/pages/list?language=' + $scope.language + '&section=' + $scope.section.toUpperCase()
              }).then(function mySuccess(response) {
                  $scope.pagesList = response.data;
              }, function myError(response) {
                $scope.pagesList = null;
                console.log(response.statusText);
              });
            };

            $scope.loadPages();

          }
        };
      }]);
})();
