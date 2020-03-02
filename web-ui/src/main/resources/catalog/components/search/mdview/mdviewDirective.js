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
  goog.provide('gn_mdview_directive');

  var module = angular.module('gn_mdview_directive', [
    'ui.bootstrap.tpls',
    'ui.bootstrap.rating']);

  module.directive('gnMetadataOpen', [
    '$http',
    '$sanitize',
    '$compile',
    'gnSearchSettings',
    '$sce',
    'gnMdView',
    function($http, $sanitize, $compile, gnSearchSettings, $sce, gnMdView) {
      return {
        restrict: 'A',
        scope: {
          md: '=gnMetadataOpen',
          selector: '@gnMetadataOpenSelector'
        },

        link: function(scope, element, attrs, controller) {

          element.on('click', function(e) {
            e.preventDefault();
            gnMdView.setLocationUuid(scope.md.getUuid());
            gnMdView.setCurrentMdScope(scope.$parent);
            scope.$apply();
          });
        }
      };
    }]
  );

  module.directive('gnMetadataDisplay', [
    'gnMdView', 'gnSearchSettings', function(gnMdView, gnSearchSettings) {
      return {
        templateUrl: '../../catalog/components/search/mdview/partials/' +
            'mdpanel.html',
        scope: true,
        link: function(scope, element, attrs, controller) {

          var unRegister;

          element.find('.panel-body').append(scope.fragment);
          scope.dismiss = function() {
            unRegister();
            // Do not close parent mdview
            if ($('[gn-metadata-display] ~ [gn-metadata-display]')
                .length == 0) {
              gnMdView.removeLocationUuid();
            }
            element.remove();
            //TODO: is the scope destroyed ?
          };

          if (gnSearchSettings.dismissMdView) {
            scope.dismiss = gnSearchSettings.dismissMdView;
          }
          unRegister = scope.$on('locationBackToSearchFromMdview', function() {
            scope.dismiss();
          });
        }
      };
    }]);

  module.directive('gnMetadataRate', [
    '$http',
    function($http) {
      return {
        templateUrl: '../../catalog/components/search/mdview/partials/' +
            'rate.html',
        restrict: 'A',
        scope: {
          md: '=gnMetadataRate',
          readonly: '@readonly'
        },

        link: function(scope, element, attrs, controller) {
          scope.$watch('md', function() {
            scope.rate = scope.md ? scope.md.rating : null;
          });


          scope.rateForRecord = function() {
            return $http.put('../api/records/' + scope.md['geonet:info'].uuid +
                             '/rate', scope.rate).success(function(data) {
              scope.rate = data;
            });
          };
        }
      };
    }]
  );
})();
