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
  goog.provide('gn_admintools_controller');


  goog.require('gn_search');
  goog.require('gn_search_form_controller');

  var module = angular.module('gn_admintools_controller',
      ['gn_search', 'gn_search_form_controller']);


  module.controller('GnAdminToolsSearchController', [
    '$scope', 'gnSearchSettings',
    function($scope, gnSearchSettings) {

      var defaultSearchObj = {
        permalink: false,
        sortbyValues: gnSearchSettings.sortbyValues,
        hitsperpageValues: gnSearchSettings.hitsperpageValues,
        selectionBucket: 'b101',
        filters: gnSearchSettings.filters,
        params: {
          sortBy: 'changeDate',
          _isTemplate: 'y or n',
          from: 1,
          to: 20
        }
      };
      angular.extend($scope.searchObj, defaultSearchObj);

      $scope.setTemplate = function(params) {
        var values = [];
        if ($('#batchSearchTemplateY')[0].checked) values.push('y');
        if ($('#batchSearchTemplateN')[0].checked) values.push('n');
        if ($('#batchSearchTemplateS')[0].checked) values.push('s');
        $scope.searchObj.params._isTemplate = values.join(' or ');
      };


    }]);

  /**
   * GnAdminToolsController provides administration tools
   */
  module.controller('GnAdminToolsController', [
    '$scope', '$http', '$rootScope', '$translate', '$compile',
    '$q', '$timeout', '$routeParams', '$location',
    'gnSearchManagerService', 'gnConfigService',
    'gnUtilityService', 'gnSearchSettings', 'gnGlobalSettings',

    function($scope, $http, $rootScope, $translate, $compile,
             $q, $timeout, $routeParams, $location,
             gnSearchManagerService, gnConfigService,
             gnUtilityService, gnSearchSettings, gnGlobalSettings) {
      $scope.modelOptions =
          angular.copy(gnGlobalSettings.modelOptions);

      $scope.pageMenu = {
        folder: 'tools/',
        defaultTab: 'index',
        tabs:
            [{
              type: 'index',
              label: 'catalogueAdminTools',
              icon: 'fa-search',
              href: '#/tools/index'
            },{
              type: 'batch',
              label: 'batchProcess',
              icon: 'fa-medkit',
              href: '#/tools/batch'
            },{
              type: 'transferownership',
              label: 'transfertPrivs',
              icon: 'fa-user',
              href: '#/tools/transferownership'
            }]
      };

      /**
       * True if currently processing records
       */
      $scope.processing = false;

      /**
       * The full text search filter
       * TODO : could be nice to filter by type of record
       * and schema
       */
      $scope.recordsToProcessFilter = '';

      /**
       * The process report returned by the batch
       * processing service with information about the
       * number of records processed, or not, process not found, ...
       */
      $scope.processReport = null;

      /**
       * The list of records to be processed
       */
      $scope.recordsToProcess = null;
      $scope.numberOfRecordsProcessed = null;

      /**
       * The selected process
       */
      $scope.selectedProcess = null;
      $scope.data = {};

      /**
       * The list of batch process available.
       * Stored in batch-process-cfg.json.
       * TODO: Move to server side configuration.
       * TODO: i18n should not be shared in en-admin.json
       *
       */
      $scope.batchProcesses = null;

      /**
       * Search filter selected for metadata and template type
       */
      $scope.batchSearchTemplateY = true;
      $scope.batchSearchTemplateN = true;
      $scope.batchSearchTemplateS = false;
      $scope.batchSearchGroups = [];
      $scope.batchSearchUsers = [];
      $scope.batchSearchCategories = [];

      $scope.editors = [];
      $scope.editorSelectedId = null;
      $scope.editorGroups = {};



      gnSearchSettings.resultViewTpls = [{
        tplUrl: '../../catalog/components/search/resultsview/' +
            'partials/viewtemplates/titlewithselection.html',
        tooltip: 'List',
        icon: 'fa-list'
      }];

      gnSearchSettings.resultTemplate =
          gnSearchSettings.resultViewTpls[0].tplUrl;

      $scope.facetsSummaryType = gnSearchSettings.facetsSummaryType = 'manager';

      gnSearchSettings.sortbyValues = [{
        sortBy: 'relevance',
        sortOrder: ''
      }, {
        sortBy: 'changeDate',
        sortOrder: ''
      }, {
        sortBy: 'title',
        sortOrder: 'reverse'
      }];

      gnSearchSettings.hitsperpageValues = [20, 50, 100];

      gnSearchSettings.paginationInfo = {
        hitsPerPage: gnSearchSettings.hitsperpageValues[0]
      };

      function loadEditors() {
        $http.get('../api/users/owners')
            .success(function(data) {
              $scope.editors = data;
            });
        $http.get('../api/users/groups')
            .success(function(data) {
              var uniqueUserGroups = {};
              angular.forEach(data, function(g) {
                var key = g.groupId + '-' + g.userId;
                if (!uniqueUserGroups[key]) {
                  uniqueUserGroups[key] = g;
                  uniqueUserGroups[key].groupNameTranslated = g.groupName === 'allAdmins' ?
                    $translate.instant(g.groupName) :
                    $translate.instant('group-' + g.groupId);
                }
              });
              $scope.userGroups = uniqueUserGroups;
            });
      }
      $scope.selectUser = function(id) {
        $scope.editorSelectedId = id;
        $http.get('../api/users/' + id + '/groups')
            .success(function(data) {
              var uniqueGroup = {};
              angular.forEach(data, function(g) {
                if (!uniqueGroup[g.group.id]) {
                  uniqueGroup[g.group.id] = g.group;
                }
              });
              $scope.editorGroups = uniqueGroup;
            });
      };
      $scope.transfertList = {};

      $scope.tranferOwnership = function(sourceGroup) {
        var params = $scope.transfertList[sourceGroup];

        params.running = true;
        return $http.put('../api/users/owners', {
          sourceUser: parseInt($scope.editorSelectedId),
          sourceGroup: parseInt(sourceGroup),
          targetUser: params.targetGroup.userId,
          targetGroup: params.targetGroup.groupId
        }).success(function(data) {
          $rootScope.$broadcast('StatusUpdated', {
            msg: $translate.instant('transfertPrivilegesFinished',
                {
                  privileges: data.privileges,
                  metadata: data.metadata}),
            timeout: 2,
            type: 'success'});
          params.running = false;
        }).error(function(data) {
          // TODO
          params.running = false;
        });
      };
      $scope.isRunning = function(sourceGroup) {
        return $scope.transfertList[sourceGroup].running;
      };

      function loadProcessConfig() {
        $http.get(gnGlobalSettings.gnUrl +
            '../catalog/config/batch-process-cfg.json')
            .success(function(data) {
              $scope.batchProcesses = data.config;

              $timeout(initProcessByRoute);
            });
      }

      function loadGroups() {
        $http.get('../api/groups').
            success(function(data) {
              $scope.batchSearchGroups = data;
            }).error(function(data) {
              // TODO
            });
      }
      function loadUsers() {
        $http.get('../api/users').
            success(function(data) {
              $scope.batchSearchUsers = data;
            }).error(function(data) {
              // TODO
            });
      }

      function loadCategories() {
        $http.get('../api/tags').
            success(function(data) {
              $scope.batchSearchCategories = data;
            }).error(function(data) {
              // TODO
            });
      }

      /**
       * Check process progress every ...
       */
      var processCheckInterval = 1000;


      function checkLastBatchProcessReport() {
        // Check if processing
        return $http.get('../api/processes/reports').
            success(function(data, status) {
              // TODO: Assume one process is running
              // Should use the process ID to register and retrieve a process
              $scope.processReport = data[0];
              $scope.numberOfRecordsProcessed =
                 $scope.processReport.numberOfRecordsProcessed;
              if ($scope.processReport &&
                 $scope.processReport.running) {
                $timeout(checkLastBatchProcessReport, processCheckInterval);
              }
            });
      }

      $scope.runProcess = function(formId) {
        $scope.runProcess(formId, undefined, undefined);
      };

      $scope.runProcess = function(formId, process, testMode) {

        var formParams = $(formId).serialize();
        if (testMode != undefined) {
          formParams += '&isTesting=' + testMode;
        }
        formParams += '&bucket=b101';

        var service = '../api/processes/' +
                      (process != undefined ?
                       process : $scope.data.selectedProcess.key);

        $scope.processing = true;
        $scope.processReport = null;
        $http.post(service + '?' +
                   formParams)
            .success(function(data) {
              $scope.processReport = data;
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('processFinished'),
                timeout: 2,
                type: 'success'});
              $scope.processing = false;

              // Turn off batch report checking for search and replace mode
              // AFA as report is not properly set in session
              // https://github.com/geonetwork/core-geonetwork/issues/828
              // if (service.indexOf('search-and-replace') === -1) {
              //   checkLastBatchProcessReport();
              // }
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('processError'),
                error: data,
                timeout: 0,
                type: 'danger'});
              $scope.processing = false;
            });

        // gnUtilityService.scrollTo('#gn-batch-process-report');
        // FIXME
        // if (service.indexOf('search-and-replace') === -1) {
        //   $timeout(checkLastBatchProcessReport, processCheckInterval);
        // }
      };

      loadGroups();
      loadUsers();
      loadCategories();
      loadEditors();

      // TODO: Should only do that if batch process is the current page
      loadProcessConfig();
      // checkLastBatchProcessReport();

      var initProcessByRoute = function() {
        if ($routeParams.tab === 'batch') {
          // Check if we should select all record
          if ($routeParams.selectAll) {
            // Check if we should select all record
            $scope.$broadcast('resetSearch');
          }
          if ($routeParams.processId) {
            // Select a process defined in the route
            angular.forEach($scope.batchProcesses, function(p) {
              if (p.key === $routeParams.processId) {
                // For each process parameter check if param
                // defined in the location search section
                angular.forEach(p.params, function(param) {
                  var urlParam = $location.search()[param.name];
                  if (urlParam) {
                    param.value = urlParam;
                  }
                });
                $scope.selectedProcess = p;
              }
            });
          }
        }
      };


      // Indexing management

      /**
       * Inform if indexing is ongoing or not
       */
      $scope.isIndexing = false;

      /**
       * Number of records in the index
       */
      $scope.numberOfIndexedRecords = null;

      /**
       * Check index every ...
       */
      var indexCheckInterval = 5000;

      /**
       * Get number of record in the index and
       * then check if indexing is ongoing or not every
       * indexCheckInterval. Stop when not indexing.
       *
       * TODO: Could we kill the check when switching to somewhere else?
       */
      function checkIsIndexing() {
        // Check if indexing
        return $http.get('../api/site/indexing').
            success(function(data, status) {
              $scope.isIndexing = data;
              if ($scope.isIndexing) {
                $timeout(checkIsIndexing, indexCheckInterval);
              }
              // Get the number of records (template, records, subtemplates)
              $http.get('qi?_content_type=json&' +
                 'template=y or n or s&summaryOnly=true').
                 success(function(data, status) {
                   $scope.numberOfIndexedRecords = data[0]['@count'];
                 });
            });
      }

      checkIsIndexing();

      $scope.rebuildIndex = function() {
        return $http.get('admin.index.rebuild?reset=yes')
            .success(function(data) {
              checkIsIndexing();
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('rebuildIndexError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };
      $scope.indexInEs = function() {
        return $http.put('../api/site/index/es')
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('indexInEsDone'),
                timeout: 2,
                type: 'success'});
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('indexInEsDoneError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.optimizeIndex = function() {
        return $http.get('admin.index.optimize')
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('indexOptimizationInProgress'),
                timeout: 2,
                type: 'success'});
              // TODO: Does this is asynch and make the search unavailable?
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('rebuildIndexError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.reloadLuceneConfig = function() {
        return $http.get('admin.index.config.reload')
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('luceneConfigReloaded'),
                timeout: 2,
                type: 'success'});
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('rebuildIndexError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.clearXLinkCache = function() {
        return $http.get('admin.index.rebuildxlinks')
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('xlinkCacheCleared'),
                timeout: 2,
                type: 'success'});
              // TODO: Does this is asynch and make the search unavailable?
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('rebuildIndexError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.clearJsCache = function() {
        return $http.get('../../static/wroAPI/reloadModel')
            .success(function(data) {
              $http.get('../../static/wroAPI/reloadCache')
              .success(function(data) {
                   $rootScope.$broadcast('StatusUpdated', {
                     msg: $translate.instant('jsCacheCleared'),
                     timeout: 2,
                     type: 'success'});
                 });
            });
      };

      $scope.clearFormatterCache = function() {
        return $http.delete('../api/formatters/cache')
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('formatterCacheCleared'),
                timeout: 2,
                type: 'success'});
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('formatCacheClearFailure'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      gnConfigService.loadPromise.then(function(settings) {
        $scope.isBackupArchiveEnabled =
            settings['metadata.backuparchive.enable'];
      });

      $scope.triggerBackupArchive = function() {
        return $http({method: 'PUT', url: '../api/records/backups'}).
            then(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('generatingArchiveBackup'),
                error: data,
                timeout: 2,
                type: 'success'
              });
            });

      };

      $scope.replacer = {};
      $scope.replacer.group = '';
      $scope.replacer.element = '';
      $scope.replacer.elements = [];
      $scope.replacer.replacements = [];
      $scope.data.replacementsConfig = [];

      $scope.addReplacement = function() {
        if (!$scope.replacer.replacements) {
          $scope.replacer.replacements = [];
        }
        $scope.replacer.replacements.push({
          'package': $scope.replacer.group,
          'element': $scope.replacer.element,
          'searchval': $scope.replacer.searchval,
          'replaceval': $scope.replacer.replaceval
        });

        $scope.replacer.group = '';
        $scope.replacer.element = '';
        $scope.replacer.searchval = '';
        $scope.replacer.replaceval = '';
      };

      $scope.removeReplacement = function(replacement) {
        $scope.replacer.replacements.splice(
            $scope.replacer.replacements.indexOf(replacement), 1);
      };

      $scope.loadReplacementConfig = function() {
        try {
          $scope.replacer.replacements =
              angular.fromJson($scope.data.replacementsConfig);
        } catch (e) {
          $rootScope.$broadcast('StatusUpdated', {
            title: $translate.instant('error'),
            error: e,
            timeout: 0,
            type: 'danger'});
        }
      };

      $scope.downloadReplacementConfig = function($event) {
        var content = 'data:text/json;charset=utf-8,' +
                      encodeURIComponent(
            JSON.stringify(
                          $scope.replacer.replacements));
        $($event.target).parent('a')
            .attr('download', 'config.json')
            .attr('href', content);
        $event.stopPropagation();
      };

      $scope.$watch('replacer.group', function(newValue, oldValue) {

        // Ignore empty value: in initial setup and
        // if form already mirrors new value.
        if ((newValue === '') || (newValue === oldValue)) {
          return;
        }

        $scope.replacer.elements =
            $scope.selectedProcess.config[newValue].elements;
      });


      $scope.$watch('data.selectedProcess', function(n, o) {
        if (n !== o) {
          $scope.processReport = null;
          $scope.selectedProcess = $scope.data.selectedProcess;
        }
      });

    }]);
})();
