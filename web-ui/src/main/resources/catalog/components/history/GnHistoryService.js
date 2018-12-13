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
  goog.provide('gn_history_service');

  var module = angular.module('gn_history_service', []);

  /**
   * Service to deal with record history.
   */
  module
    .service(
      'gnRecordHistoryService', [
        '$http', '$filter',
      function($http, $filter) {
        this.delete = function (step) {
          return $http.delete('../api/records/' + step.id.metadataId + '/status/' +
            step.id.statusId + '.' + step.id.userId + '.' + step.id.changeDate.dateAndTime);
        };

        this.close = function (step, optionalDateOrNow) {
          return $http.put('../api/records/' + step.id.metadataId + '/status/' +
            step.id.statusId + '.' + step.id.userId + '.' +
            step.id.changeDate.dateAndTime +
            '/close?closeDate=' + (optionalDateOrNow || moment().format('YYYY-MM-DDTHH:mm:ss')));
        };


        function buildFilter(filter) {
          var filters = [];
          angular.forEach(filter.types, function (v, k) {
            if (v) {
              filters.push('type=' + k);
            }
          });
          if (filter.authorFilter && filter.authorFilter.id) {
            filters.push('author=' + filter.authorFilter.id);
          }
          if (filter.ownerFilter && filter.ownerFilter.id) {
            filters.push('owner=' + filter.ownerFilter.id);
          }
          if (filter.recordFilter) {
            filters.push('record=' + filter.recordFilter);
          }
          if (filter.dateFromFilter) {
            filters.push('dateFrom=' + $filter('date')(filter.dateFromFilter, 'yyyy-MM-dd'));
          }
          if (filter.dateToFilter) {
            filters.push('dateTo=' + $filter('date')(filter.dateToFilter, 'yyyy-MM-dd'));
          }

          filters.push('from=' + (filter.from || 0));
          filters.push('size=' + (filter.size || 20));

          return filters.length > 0 ? '?' + filters.join('&') : '';
        };

        this.search = function(filter) {
          return $http.get('../api/records/status/search' + buildFilter(filter));
        };
      }]);


  /**
   * Service to deal with tasks. For now only DOI related.
   */
  module
    .service(
      'gnRecordTaskService', [
        '$http', 'gnConfig', '$translate',
        function($http, gnConfig, $translate) {

          this.doiCreationTask = {
            check: function (status) {
              return $http.get('../api/records/' + status.id.metadataId + '/doi/checkPreConditions');
            },
            create: function (status) {
              return $http.put('../api/records/' + status.id.metadataId + '/doi');
            }
          };
        }]);
})();
