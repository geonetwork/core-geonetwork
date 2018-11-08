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
  var STATUS_CANCELED = 3;  // unused for now

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
        scope: {
          taskInfo: '<'
        },
        templateUrl: '../../catalog/components/admin/index/partials/indexingstatus.html',
        link: function (scope, element, attrs) {
          scope.getStatusCode = function() {
            if (scope.taskInfo && scope.taskInfo.total > scope.taskInfo.processed) {
              return STATUS_INPROGRESS;
            }
            if (scope.taskInfo && scope.taskInfo.total === scope.taskInfo.processed && scope.taskInfo.errors > 0) {
              return STATUS_ERRORS;
            }
            if (scope.taskInfo && scope.taskInfo.total === scope.taskInfo.processed) {
              return STATUS_FINISHED;
            }
            return STATUS_UNDEFINED;
          }

          scope.getStatusLabel = function() {
            switch(scope.getStatusCode()) {
              case STATUS_UNDEFINED: return 'indexingTaskUndefined';
              case STATUS_INPROGRESS: return 'indexingTaskRunning';
              case STATUS_FINISHED: return 'indexingTaskFinished';
              case STATUS_ERRORS: return 'indexingTaskFinishedWithErrors';
              case STATUS_CANCELED: return 'indexingTaskCanceled';
            }
          }

          scope.getStatusIcon = function() {
            switch(scope.getStatusCode()) {
              case STATUS_UNDEFINED: return 'fa-question';
              case STATUS_INPROGRESS: return 'fa-spinner fa-spin';
              case STATUS_FINISHED: return 'fa-check';
              case STATUS_ERRORS: return 'fa-exclamation-triangle';
              case STATUS_CANCELED: return 'fa-ban';
            }
          }

          scope.getStatusClass = function(warningOnly) {
            switch(scope.getStatusCode()) {
              case STATUS_INPROGRESS: return '';
              case STATUS_FINISHED: return 'success';
              case STATUS_ERRORS: return 'warning';
              case STATUS_CANCELED: return '';
              default: return '';
            }
          }

          scope.getProcessRatio = function() {
            return Math.round(1000 * scope.taskInfo.processed / scope.taskInfo.total) * 0.001;
          }

          scope.getTaskInfo = function() {
            if (!scope.taskInfo) { return 'no task info available'; }
            return 'Task id: ' + scope.taskInfo.id;
          }
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
    '$http',
    function ($http) {
      return {
        restrict: 'E',
        scope: {},
        templateUrl: '../../catalog/components/admin/index/partials/indexingstatuscontainer.html',
        link: function (scope, element, attrs) {
        },
        controllerAs: 'ctrl',
        controller: [
          '$scope', '$element', '$attrs',
          function ($scope, $element, $attrs) {
            this.tasks = [];

            var me = this;

            this.refresh = function () {
              $http.get('../../jolokia/read/geonetwork:name=indexing-task,idx=*')
                .then(function (result) {
                  //console.log(result);
                  me.tasks.length = 0;

                  if (!result.data || !result.data.value) { return; }

                  var probes = result.data.value;
                  Object.keys(probes).forEach(function (probeName) {
                    var probe = probes[probeName];
                    me.tasks.push({
                      id: probeName,
                      errors: probe.InError,
                      processed: probe.Processed,
                      total: probe.ToProcessCount
                    });
                  })

                  // loop
                  setTimeout(me.refresh, 1000);
                });
            }

            this.refresh();
          }
        ]
      };
    }]
  );
})();
