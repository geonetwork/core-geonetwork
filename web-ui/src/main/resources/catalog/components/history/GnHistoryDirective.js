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
  goog.provide('gn_history_directive');

  var module = angular.module('gn_history_directive', []);

  /**
   * Send email to metadata contact or catalog administrator.
   */
  module
    .directive(
      'gnRecordHistory', [
        '$http', 'gnConfig', '$translate',
      function($http, gnConfig, $translate) {
        return {
          restrict: 'A',
          replace: true,
          scope: {
            md: '=gnRecordHistory'
          },
          templateUrl:
          '../../catalog/components/history/partials/recordHistory.html',
          link: function postLink(scope, element, attrs) {
            scope.types = [{'workflow': true}, {'task': true}, {'event': true}];
            scope.lang = scope.$parent.lang;
            scope.user = scope.$parent.user;
            scope.history = [];
            // History step removal is only allowed to admin
            // BTW allowRemoval attribute could control if remove button
            // is displayed or not.
            scope.allowRemoval =
              angular.isDefined(attrs.allowRemoval) ?
                attrs.allowRemoval == 'true' && scope.user.isAdministrator() :
                scope.user.isAdministrator();

            function loadHistory() {
              $http.get('../api/records/' + scope.md.getUuid() + '/status').
              then(function(r) {
                scope.history = r.data;
              });
            };

            scope.removeStep = function(s){
              $http.delete('../api/records/' + scope.md.getUuid() + '/status/' +
              s.id.statusId + '.' + s.id.userId + '.' + s.id.changeDate.dateAndTime).
              then(function(r) {
                loadHistory();
              });
            };

            loadHistory();
          }
        };
      }]);
})();
