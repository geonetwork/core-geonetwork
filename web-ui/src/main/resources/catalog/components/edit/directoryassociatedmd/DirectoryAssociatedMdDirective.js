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
  goog.provide('gn_directoryassociatedmd_directive');


  goog.require('gn_mdtypewidget');
  goog.require('gn_mdtypeinspirevalidationwidget');
  goog.require('gn_draftvalidationwidget');
  goog.require('gn_batchtask');

  var module = angular.module('gn_directoryassociatedmd_directive', [
    'gn_mdtypewidget', 'gn_mdtypeinspirevalidationwidget',
    'gn_draftvalidationwidget', 'gn_batchtask'
  ]);

  module.directive('gnDirectoryAssociatedMd', [
    'gnGlobalSettings',
    function() {
      return {
        restrict: 'E',
        scope: {
          entryUuid: '='
        },
        templateUrl: '../../catalog/components/edit/directoryassociatedmd/' +
            'partials/associated-results.html',
        controller: ['$scope', 'gnGlobalSettings',
          function($scope, gnGlobalSettings) {
            $scope.searchObj = {
              params: {
                _isTemplate: 'y or n',
                any: '',
                sortBy: 'title',
                _xlink: '*'
              },
              internal: true,
              sortbyValues: [
                {
                  sortBy: 'title'
                },
                {
                  sortBy: 'owner'
                },
                {
                  sortBy: 'changeDate',
                  sortOrder: 'reverse'
                }
              ]
            };
            $scope.paginationInfo = {
              pages: -1,
              currentPage: 1,
              hitsPerPage: 8
            };
            $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);

            $scope.$watch('entryUuid', function(v) {
              if (v) {
                $scope.searchObj.params._xlink =
                    '*local://srv/api/registries/entries/' + v + '*';
                $scope.$broadcast('clearResults');
                $scope.$broadcast('search');
              }
            });
          }]
      };
    }
  ]);

})();
