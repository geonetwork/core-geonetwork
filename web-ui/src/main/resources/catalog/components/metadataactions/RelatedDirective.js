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

  goog.provide('gn_related_directive');
  goog.require('gn_relatedresources_service');

  var module = angular.module('gn_related_directive', [
    'gn_relatedresources_service'
  ]);

  /**
   * Shows a list of related records given an uuid with the actions defined in
   * config.js
   */
  module.service('gnRelatedService', ['$http', '$q', function($http, $q) {
    function get(uuid, types) {
      var canceller = $q.defer();
      var request = $http({
        method: 'get',
        url: '../api/records/' + uuid + '/related?' +
            (types ?
            'type=' + types.split('|').join('&type=') :
            ''),
        timeout: canceller.promise,
        cache: true
      });

      var promise = request.then(
          function(response) {
            return (response.data);
          },
          function() {
            return ($q.reject('Something went wrong loading ' +
            'related records of type ' + types));
          }
          );

      promise.abort = function() {
        canceller.resolve();
      };

      promise.finally(
          function() {
            promise.abort = angular.noop;
            canceller = request = promise = null;
          }
      );
      return (promise);
    }
    return {
      get: get
    };
  }]);
  module
      .directive(
          'gnRelated',
          [
        'gnRelatedService',
        'gnGlobalSettings',
        'gnRelatedResources',
        function(gnRelatedService, gnGlobalSettings, gnRelatedResources) {
          return {
            restrict: 'A',
            templateUrl: function(elem, attrs) {
              return attrs.template ||
                      '../../catalog/components/metadataactions/partials/related.html';
            },
            scope: {
              md: '=gnRelated',
              template: '@',
              types: '@',
              title: '@',
              list: '@',
              user: '=',
              hasResults: '=?'
            },
            link: function(scope, element, attrs, controller) {
              var promise;
              scope.updateRelations = function() {
                scope.relations = [];
                if (scope.uuid) {
                  scope.relationFound = false;
                  (promise = gnRelatedService.get(
                     scope.uuid, scope.types)
                  ).then(function(data) {
                       scope.relations = data;
                       angular.forEach(data, function(value) {
                         if (value) {
                           scope.relationFound = true;
                           scope.hasResults = true;
                         }
                       });
                     });
                }
              };

              scope.getTitle = function(link) {
                return link.title['#text'] || link.title;
              };
              scope.hasAction = function(mainType) {
                var fn = gnRelatedResources.map[mainType].action;
                // If function name ends with ToMap do not display the action
                if (fn.name.match(/.*ToMap$/) &&
                   gnGlobalSettings.isMapViewerEnabled === false) {
                  return false;
                }
                return angular.isFunction(fn);
              };
              scope.config = gnRelatedResources;

              scope.$watchCollection('md', function(n, o) {
                if (n && n !== o || angular.isUndefined(scope.uuid)) {
                  if (promise && angular.isFunction(promise.abort)) {
                    promise.abort();
                  }
                  if (scope.md != null) {
                    scope.uuid = scope.md.getUuid();
                  }
                  scope.updateRelations();
                }
              });
              //
              // /**
              //  * Return an array of all relations of the given types
              //  * @return {Array}
              //  */
              // scope.getByTypes = function() {
              //   var res = [];
              //   var types = Array.prototype.splice.call(arguments, 0);
              //   angular.forEach(scope.relations, function(rel) {
              //     if (types.indexOf(rel['@type']) >= 0) {
              //       res.push(rel);
              //     }
              //   });
              //   return res;
              // };
            }
          };
        }]);

  module.directive('relatedTooltip', function() {
    return function(scope, element, attrs) {
      for (var i = 0; i < element.length; i++) {
        element[i].title = scope.$parent.md['@type'];
      }
      element.tooltip();
    };
  });

})();
