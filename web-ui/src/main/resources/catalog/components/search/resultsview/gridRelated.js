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

  goog.provide('gn_gridrelated_directive');

  var module = angular.module('gn_gridrelated_directive', []);

  module.value('gnGridRelatedList', {
    list: {},
    promise: undefined,
    types: []
  });

  module.directive('gnGridRelatedQuery', [
    'gnGlobalSettings', 'gnRelatedService', 'gnGridRelatedList',
    function(gnGlobalSettings, gnRelatedService, gnGridRelatedList) {

      return {
        restrict: 'A',
        scope: {
          records: '<gnGridRelatedQuery'
        },
        link: function(scope, element, attrs) {

          var types = gnGlobalSettings.gnCfg.mods.search.grid.related;
          gnGridRelatedList.types = types;

          scope.$watch('records', function(mds) {
            var uuids = mds.map(function(md) {
              return md.getUuid();
            });
            if (uuids.length) {
              gnGridRelatedList.promise =
                  gnRelatedService.getMdsRelated(uuids, types).then(
                  function(response) {
                    gnGridRelatedList.list = response.data;
                  });
            }
          });
        }
      };
    }]);

  module.directive('gnGridRelated', [
    'gnGlobalSettings', 'gnRelatedService', 'gnGridRelatedList',
    function(gnGlobalSettings, gnRelatedService, gnGridRelatedList) {

      return {
        restrict: 'A',
        scope: {
          uuid: '<gnGridRelatedUuid'
        },
        templateUrl: function(elem, attrs) {
          return attrs.template ||
              '../../catalog/components/metadataactions/partials/related.html';
        },
        link: function(scope, element, attrs) {
          scope.location = window.location;
          scope.max = attrs['max'] || 5;
          scope.displayState = {};
          gnGridRelatedList.promise.then(function() {
            var related = gnGridRelatedList.list[scope.uuid];
            if (related) {
              scope.types = gnGridRelatedList.types;
              var hasProp = false;
              for (var p in related) {
                if (related[p]) {
                  hasProp = true;
                }
              }
              if (hasProp) {
                scope.relations = related;
              }
            }
          });
        }
      };
    }]);

})();
