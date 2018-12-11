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
   * Display list of events, tasks and workflow status for a record.
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

            // Wait for metatada to be available
            scope.$watch('md', function(n, o) {
              if (angular.isDefined(n)) {
                loadHistory();
              }
             });

            // History step removal is only allowed to admin
            // BTW allowRemoval attribute could control if remove button
            // is displayed or not.
            scope.allowRemoval = false;
            if(scope.user) {
              scope.allowRemoval =
                angular.isDefined(attrs.allowRemoval) ?
                attrs.allowRemoval == 'true' && scope.user.isAdministrator() :
                scope.user.isAdministrator();
            }

            function loadHistory() {
              if (scope.md ) {
                $http.get('../api/records/' + scope.md.getUuid() + '/status').
                then(function(r) {
                  scope.history = r.data;
                });
              }
            };

            scope.removeStep = function(s){
              $http.delete('../api/records/' + scope.md.getUuid() + '/status/' +
              s.id.statusId + '.' + s.id.userId + '.' + s.id.changeDate.dateAndTime).
              then(function(r) {
                loadHistory();
              });
            };
          }
        };
      }]);


  /**
   * Manager
   */
  module
    .directive(
      'gnHistory', [
        '$http', '$filter', 'gnConfig', '$translate',
        'gnSearchManagerService',
        function($http, $filter, gnConfig, $translate, gnSearchManagerService) {
          return {
            restrict: 'A',
            replace: true,
            scope: {
            },
            templateUrl:
              '../../catalog/components/history/partials/history.html',
            link: function postLink(scope, element, attrs) {
              scope.types = {'workflow': true, 'task': true, 'event': true};
              scope.lang = scope.$parent.lang;
              scope.user = scope.$parent.user;
              scope.history = [];
              scope.ownerFilter = null;
              scope.authorFilter = null;
              scope.recordFilter = null;
              scope.dateFromFilter = null;
              scope.dateToFilter = null;
              var recordByPage = 20;
              scope.from = 0;
              scope.size = recordByPage;
              scope.hasMoreRecords = true;

              scope.getSuggestions = function(val) {
                return gnSearchManagerService.search('q?fast=index&_content_type=json&_isTemplate=y or n&title=' + (val || '*')).then(function(res) {
                  var listOfTitles = [];
                  angular.forEach(res.metadata, function(value, key) {
                    listOfTitles.push({id: value['geonet:info'].id, title: value.title});
                  });
                  return listOfTitles;
                });
              };

              scope.response = {
                doiCreationTask: {}
              };
              scope.doiCreationTask = {
                check: function (status) {
                  var key = status.id.metadataId + '-' + status.id.statusId;
                  scope.response.doiCreationTask[key] = {};
                  scope.response.doiCreationTask[key]['check'] = null;
                  $http.get('../api/records/' + status.id.metadataId + '/doi/checkPreConditions').
                  then(function(r) {
                    scope.response.doiCreationTask[key]['check'] = r;
                  }, function(r) {
                    scope.response.doiCreationTask[key]['check'] = r;
                  });
                },
                create: function (status) {
                  var key = status.id.metadataId + '-' + status.id.statusId;
                  scope.response.doiCreationTask[key]['create'] = null;
                  $http.put('../api/records/' + status.id.metadataId + '/doi').
                  then(function(r) {
                    scope.response.doiCreationTask[key]['create'] = r;
                    scope.closeTask(status);
                    then(function(r) {
                      loadHistory();
                    });
                  }, function(r) {
                    scope.response.doiCreationTask[key]['create'] = r;
                  });
                }
              };

              scope.closeTask = function(status) {
                // Close the related task
                $http.put('../api/records/' + status.id.metadataId + '/status/' +
                  status.id.statusId + '.' + status.id.userId + '.' +
                  status.id.changeDate.dateAndTime +
                  '/close?closeDate=' + moment().format('YYYY-MM-DDTHH:mm:ss')).then(function() {
                  loadHistory();
                });
              };

              function buildFilter() {
                var filters = [];
                angular.forEach(scope.types, function (v, k) {
                  if (v) {
                    filters.push('type=' + k);
                  }
                });
                if (scope.authorFilter && scope.authorFilter.id) {
                  filters.push('author=' + scope.authorFilter.id);
                }
                if (scope.ownerFilter && scope.ownerFilter.id) {
                  filters.push('owner=' + scope.ownerFilter.id);
                }
                if (scope.recordFilter) {
                  filters.push('record=' + scope.recordFilter);
                }
                if (scope.dateFromFilter) {
                  filters.push('dateFrom=' + $filter('date')(scope.dateFromFilter, 'yyyy-MM-dd'));
                }
                if (scope.dateToFilter) {
                  filters.push('dateTo=' + $filter('date')(scope.dateToFilter, 'yyyy-MM-dd'));
                }

                filters.push('from=' + scope.from);
                filters.push('size=' + scope.size);

                return filters.length > 0 ? '?' + filters.join('&') : '';
              }

              scope.more = function() {
                scope.size = scope.size + recordByPage;
                loadHistory();
              };

              function loadHistory() {
                $http.get('../api/status/search' + buildFilter()).
                then(function(r) {
                  scope.history = r.data;

                  scope.hasMoreRecords = r.data.length >= scope.size;
                });
              };

              var trigger = function(n, o) {
                if (n !== o) {
                  loadHistory();
                }
              };
              scope.$watchCollection('types', trigger);
              scope.$watchCollection('authorFilter', trigger);
              scope.$watchCollection('ownerFilter', trigger);
              scope.$watchCollection('recordFilter', trigger);
              scope.$watchCollection('dateFromFilter', trigger);
              scope.$watchCollection('dateToFilter', trigger);

              loadHistory();
            }
          };
        }]);
})();
