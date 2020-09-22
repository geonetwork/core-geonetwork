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
  goog.provide('gn_localisation_directive');

  var module = angular.module('gn_localisation_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnLocalisationInput
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive('gnLocalisationInput', [
    '$timeout',
    'gnGlobalSettings',
    'gnViewerSettings',
    'gnGazetteerProvider',
    function($timeout, gnGlobalSettings, gnViewerSettings, gnGazetteerProvider) {
      return {
        restrict: 'A',
        require: 'gnLocalisationInput',
        replace: true,
        templateUrl: '../../catalog/components/viewer/localisation/' +
            'partials/localisation.html',
        scope: {
          map: '='
        },
        controllerAs: 'locCtrl',
        controller: ['$scope', '$http', 'gnGetCoordinate',
          function($scope, $http, gnGetCoordinate) {

            var parent = $scope.$parent;

            $scope.modelOptions =
                angular.copy(gnGlobalSettings.modelOptions);

            var zoomTo = function(extent, map) {
              map.getView().fit(extent, map.getSize());
            };
            this.onClick = function(loc, map) {
              return gnGazetteerProvider.onClick($scope, loc, map);
            }

            $scope.zoomToYou = function(map) {
              if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(function(pos) {
                  var position = new ol.geom.Point([
                    pos.coords.longitude,
                    pos.coords.latitude]);
                  map.getView().setCenter(
                      position.transform(
                      'EPSG:4326',
                      map.getView().getProjection()).getFirstCoordinate()
                  );
                });
              } else {

              }
            };

            /**
             * Request geonames search. Trigger when user changes
             * the search input.
             *
             * @param {string} query string value of the search input
             */
            this.search = function (loc, query) {
              return gnGazetteerProvider.search($scope, loc, $scope.query);
            }
          }],
        link: function(scope, element, attrs, ctrl) {
          scope.locToolDisabled = gnGlobalSettings.gnCfg.mods.geocoder.enabled === false;

          /** localisation text query */
          scope.query = '';

          scope.collapsed = true;

          /** default localisation */
          scope.localisations = gnViewerSettings.localisations;

          /** Clear input and search results */
          scope.clearInput = function() {
            scope.query = '';
            scope.results = [];

          };

          // Bind events to display the dropdown menu
          element.find('input').bind('focus', function(evt) {
            scope.$apply(function() {
              ctrl.search(scope.query);
              scope.collapsed = false;
            });
          });

          element.on('keydown', 'input', function(e) {
            if (e.keyCode === 40) {
              $(this).parents('.search-container')
                  .find('.dropdown-menu a').first().focus();
            }
          });

          element.on('keydown', 'a', function(e) {
            if (e.keyCode === 40) {
              var links = $(this).parents('.search-container')
                  .find('.dropdown-menu a');
              $(links[links.index(this)]).focus();
            }
          });

          scope.map.on('click', function() {
            scope.$apply(function() {
              $(':focus').blur();
              scope.collapsed = true;
            });
          });

          $('body').on('click', function(e) {
            if (!$.contains(element[0], e.target)) { return; }
            if ((element.find('input')[0] != e.target) &&
                ($(e.target).parents('.dropdown-menu')[0] !=
                element.find('.dropdown-menu')[0])) {
              scope.$apply(function() {
                $(':focus').blur();
                scope.collapsed = true;
              });
            }
          });

        }
      };
    }]);
})();
