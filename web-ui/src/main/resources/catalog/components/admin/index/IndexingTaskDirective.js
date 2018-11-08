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

(function () {
  goog.provide('gn_indexingtask_directive');

  var module = angular.module('gn_indexingtask_directive', []);

  var STATUS_UNDEFINED = -1;
  var STATUS_INPROGRESS = 0;
  var STATUS_FINISHED = 1;
  var STATUS_ERRORS = 2;
  var STATUS_CANCELED = 3;

  /**
   * Renders the status of a given indexing task (queries are made to update it)
   *
   * Usage:
   * <gn-indexing-task-status task-id="123456" />
   */
  module.directive('gnIndexingTaskStatus', [
    '$http',
    function ($http) {
      return {
        restrict: 'E',
        scope: {},
        templateUrl: '../../catalog/components/admin/index/partials/indexingstatus.html',
        link: function (scope, element, attrs) {
          scope.taskId = attrs['taskId'];
        },
        controllerAs: 'ctrl',
        controller: function ($scope, $element, $attrs) {
          var taskId = $scope.taskId;
          this.statusCode = STATUS_UNDEFINED;
          this.status = 'indexing...';
        }
      };
    }]
  );

  /**
   * Will query the backend to display all currently running tasks.
   *
   * Usage:
   * <gn-indexing-tasks-container />
   */
  module.directive('gnIndexingTasksContainer', [
      function () {
        return {
          restrict: 'E',
          scope: {},
          templateUrl: '../../catalog/components/admin/index/partials/indexingstatuscontainer.html',
          link: function (scope, element, attrs) {
          },
          controllerAs: 'ctrl',
          controller: function ($scope, $element, $attrs) {
            this.tasks = [{
              id: '1234'
            }, {
              id: '6789'
            },{
              id: 'abcdefg'
            }];
          }
        };
      }
    ]
  );
})();
